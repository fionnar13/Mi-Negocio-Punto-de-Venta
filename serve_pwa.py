#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""serve_pwa.py — سرور تست محلی نسخهٔ PWA «سازمان فروشگاه»

فقط از کتابخانهٔ استاندارد پایتون استفاده می‌کند (http.server / socketserver).

کاربرد:
    python3 serve_pwa.py                # http://0.0.0.0:8080
    python3 serve_pwa.py --port 9000    # پورت دلخواه
    python3 serve_pwa.py --https        # HTTPS با گواهی self-signed (برای تست روی گوشی)

ویژگی‌ها:
    • MIME درست برای .html/.js/.css/.json/.webmanifest/.woff2/.png/.svg و…
    • سرآیند Service-Worker-Allowed: /
    • Cache-Control: no-cache برای HTML/JS/CSS (در حین توسعه)
    • لاگ هر درخواست (متد، مسیر، وضعیت)
    • SPA fallback: مسیر بدون پسوندِ موجود → index.html
    • چاپ LAN URL برای باز کردن روی گوشی (همان Wi-Fi)
    • --https: ساخت گواهی self-signed با openssl (بار اول) و سرو HTTPS
"""

import argparse
import http.server
import os
import socket
import socketserver
import ssl
import subprocess
import sys
import urllib.parse

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "pwa")
CERT_DIR = os.path.join(ROOT, ".cert")
CERT_FILE = os.path.join(CERT_DIR, "pwa-dev-cert.pem")
KEY_FILE = os.path.join(CERT_DIR, "pwa-dev-key.pem")

MIME_TYPES = {
    ".html": "text/html; charset=utf-8",
    ".htm": "text/html; charset=utf-8",
    ".js": "text/javascript; charset=utf-8",
    ".mjs": "text/javascript; charset=utf-8",
    ".css": "text/css; charset=utf-8",
    ".json": "application/json; charset=utf-8",
    ".webmanifest": "application/manifest+json; charset=utf-8",
    ".woff2": "font/woff2",
    ".woff": "font/woff",
    ".ttf": "font/ttf",
    ".otf": "font/otf",
    ".png": "image/png",
    ".jpg": "image/jpeg",
    ".jpeg": "image/jpeg",
    ".gif": "image/gif",
    ".svg": "image/svg+xml",
    ".ico": "image/x-icon",
    ".txt": "text/plain; charset=utf-8",
    ".map": "application/json; charset=utf-8",
    ".wasm": "application/wasm",
}

# پسوندهایی که در حین توسعه نباید کش شوند
NO_CACHE_EXT = {".html", ".htm", ".js", ".mjs", ".css", ".webmanifest", ".json", ".txt"}


class PWAServer(socketserver.ThreadingTCPServer):
    """سرور رشته‌ای با استفاده مجدد از پورت."""

    allow_reuse_address = True
    daemon_threads = True


class PWARequestHandler(http.server.SimpleHTTPRequestHandler):
    """سرو کردن پوشهٔ pwa/ با سرآیندهای مناسب PWA."""

    server_version = "PWAServer/1.0"
    protocol_version = "HTTP/1.1"

    # ---------- مسیریابی ----------
    def _path_only(self):
        return urllib.parse.urlsplit(self.path).path

    def _resolve(self, path):
        """نگاشت امن مسیر URL به فایل داخل pwa/ (بدون خروج از ریشه)."""
        path = urllib.parse.unquote(path)
        if path in ("", "/"):
            path = "/index.html"
        rel = os.path.normpath(path.lstrip("/"))
        if rel.startswith("..") or os.path.isabs(rel):
            return None
        full = os.path.join(ROOT, rel)
        if os.path.isdir(full):
            full = os.path.join(full, "index.html")
        if os.path.isfile(full):
            return full
        return None

    # ---------- پاسخ ----------
    def _send_file(self, full):
        ext = os.path.splitext(full)[1].lower()
        mime = MIME_TYPES.get(ext, "application/octet-stream")
        try:
            with open(full, "rb") as f:
                data = f.read()
        except OSError:
            self._status = 500
            self.send_error(500, "Cannot read file")
            return
        self._status = 200
        self.send_response(200)
        self.send_header("Content-Type", mime)
        self.send_header("Content-Length", str(len(data)))
        self.send_header("Service-Worker-Allowed", "/")
        if ext in NO_CACHE_EXT:
            self.send_header("Cache-Control", "no-cache")
        else:
            self.send_header("Cache-Control", "public, max-age=3600")
        self.send_header("X-Content-Type-Options", "nosniff")
        self.end_headers()
        if self.command != "HEAD":
            self.wfile.write(data)

    def _handle(self):
        path = self._path_only()
        full = self._resolve(path)
        if full is None:
            # SPA fallback: مسیر بدون پسوند → index.html
            if "." not in os.path.basename(path.rstrip("/")):
                full = self._resolve("/index.html")
        if full is None:
            self._status = 404
            self.send_error(404, "File not found: %s" % path)
            return
        self._send_file(full)

    def do_GET(self):
        self._handle()

    def do_HEAD(self):
        self._handle()

    # ---------- لاگ ----------
    def log_request(self, code="-", size="-"):
        status = getattr(self, "_status", code)
        try:
            print("%s %s -> %s" % (self.command, self.path, status), flush=True)
        except Exception:
            pass

    def log_message(self, fmt, *args):
        pass  # لاگ سفارشی در log_request


# ---------- ابزارها ----------
def detect_lan_ip():
    """تشخیص IP شبکهٔ محلی (بدون ارسال بستهٔ واقعی)."""
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        s.connect(("8.8.8.8", 80))
        return s.getsockname()[0]
    except Exception:
        return "127.0.0.1"
    finally:
        s.close()


def ensure_certificate():
    """ساخت گواهی self-signed با openssl (فقط بار اول). خروجی: True/False"""
    if os.path.exists(CERT_FILE) and os.path.exists(KEY_FILE):
        return True
    try:
        os.makedirs(CERT_DIR, exist_ok=True)
        cmd = [
            "openssl", "req", "-x509", "-newkey", "rsa:2048",
            "-keyout", KEY_FILE, "-out", CERT_FILE,
            "-days", "365", "-nodes",
            "-subj", "/CN=localhost",
            "-addext", "subjectAltName=DNS:localhost,IP:127.0.0.1",
        ]
        subprocess.run(cmd, check=True, capture_output=True)
        print("[https] گواهی self-signed ساخته شد: pwa/.cert/", flush=True)
        return True
    except FileNotFoundError:
        print("[هشدار] openssl در سیستم موجود نیست — HTTPS فعال نشد.", flush=True)
        return False
    except subprocess.CalledProcessError as e:
        print("[هشدار] ساخت گواهی ناموفق بود: %s" % e.stderr.decode(errors="replace")[:200], flush=True)
        return False


def print_banner(host, port, use_https, https_ok):
    lan = detect_lan_ip()
    scheme = "https" if (use_https and https_ok) else "http"
    print()
    print("=" * 58)
    print("  سازمان فروشگاه — سرور تست PWA")
    print("=" * 58)
    print("  Local URL:  %s://localhost:%s" % (scheme, port))
    if lan != "127.0.0.1":
        print("  LAN URL:    %s://%s:%s   ← روی گوشی (همان Wi-Fi) باز کنید" % (scheme, lan, port))
    print()
    if use_https and https_ok:
        print("  ⚠ گواهی self-signed است؛ مرورگر هشدار می‌دهد —")
        print("    در Chrome روی «پیشرفته ← ادامه به سایت» بزنید.")
    elif use_https and not https_ok:
        print("  ⚠ HTTPS در دسترس نیست. Service Worker روی localhost بدون HTTPS هم")
        print("    کار می‌کند؛ برای گوشی یکی از این راه‌ها:")
        print("      ۱) openssl نصب کنید و دوباره --https بدهید؛")
        print("      ۲) با adb reverse tcp:8080 tcp:8080 پورت گوشی را به سیستم ببرید؛")
        print("      ۳) از ابزاری مثل ngrok / mkcert استفاده کنید.")
        print("  (فعلاً با HTTP سرو می‌شود)")
    print()
    print("  Ctrl+C برای توقف سرور")
    print("=" * 58)
    print()


def main():
    parser = argparse.ArgumentParser(description="سرور تست PWA «سازمان فروشگاه»")
    parser.add_argument("--port", type=int, default=8080, help="پورت (پیش‌فرض: 8080)")
    parser.add_argument("--host", default="0.0.0.0", help="آدرس گوشدادن (پیش‌فرض: 0.0.0.0)")
    parser.add_argument("--https", action="store_true", help="فعال‌سازی HTTPS با گواهی self-signed")
    args = parser.parse_args()

    if not os.path.isdir(ROOT):
        print("[خطا] پوشهٔ pwa/ پیدا نشد: %s" % ROOT, file=sys.stderr)
        sys.exit(1)

    https_ok = False
    if args.https:
        https_ok = ensure_certificate()

    server = PWAServer((args.host, args.port), PWARequestHandler)

    if args.https and https_ok:
        ctx = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
        try:
            ctx.load_cert_chain(CERT_FILE, KEY_FILE)
            server.socket = ctx.wrap_socket(server.socket, server_side=True)
        except ssl.SSLError as e:
            print("[هشدار] راه‌اندازی TLS ناموفق (%s) — ادامه با HTTP" % e, flush=True)
            https_ok = False

    print_banner(args.host, args.port, args.https, https_ok)

    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\n[سرور] متوقف شد. خداحافظ!")
        server.server_close()


if __name__ == "__main__":
    main()
