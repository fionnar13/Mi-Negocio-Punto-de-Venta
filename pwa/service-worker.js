/* service-worker.js — کش آفلاین نسخهٔ PWA «سازمان فروشگاه»
 *
 * راهبرد‌ها:
 *  - Precache: همهٔ فایل‌های برنامه در زمان نصب (pos-v1)
 *  - درخواست‌های Firebase / API خارجی: network-first (با جازدن کش به‌عنوان جایگزین)
 *  - فایل‌های استاتیک هم‌مبدأ: cache-first (با پرکردن کش در اولین موفقیت)
 *  - ناوبری‌ها: شبکه → کش index.html → offline.html
 *
 * skipWaiting + clients.claim() باعث می‌شود به‌روزرسانی‌ها بلافاصله اعمال شوند.
 */
'use strict';

var CACHE_NAME = 'pos-v1';

var PRECACHE_URLS = [
  './',
  './index.html',
  './offline.html',
  './manifest.json',
  './styles.css',
  './app.js',
  './register-sw.js',
  './icons/icon-64.png',
  './icons/icon-192.png',
  './icons/icon-512.png',
  './icons/icon-maskable-192.png',
  './icons/icon-maskable-512.png',
  './fonts/Vazirmatn-Regular.woff2',
  './fonts/Vazirmatn-Medium.woff2',
  './fonts/Vazirmatn-SemiBold.woff2',
  './fonts/Vazirmatn-Bold.woff2'
];

/* ---------- نصب ---------- */
self.addEventListener('install', function (event) {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(function (cache) { return cache.addAll(PRECACHE_URLS); })
      .then(function () { return self.skipWaiting(); })
  );
});

/* ---------- فعال‌سازی ---------- */
self.addEventListener('activate', function (event) {
  event.waitUntil(
    caches.keys()
      .then(function (keys) {
        return Promise.all(
          keys
            .filter(function (k) { return k !== CACHE_NAME; })
            .map(function (k) { return caches.delete(k); })
        );
      })
      .then(function () { return self.clients.claim(); })
  );
});

/* ---------- بازآوری پیام از صفحه (مثلاً پیام «skipWaiting») ---------- */
self.addEventListener('message', function (event) {
  if (event.data === 'SKIP_WAITING') self.skipWaiting();
});

/* ---------- راهبردهای کش ---------- */

/** استاتیک: cache-first؛ در صورت نبود، از شبکه و ذخیره در کش. */
function cacheFirst(request) {
  return caches.open(CACHE_NAME).then(function (cache) {
    return cache.match(request).then(function (hit) {
      if (hit) return hit;
      return fetch(request).then(function (res) {
        if (res && res.ok) cache.put(request, res.clone());
        return res;
      });
    });
  });
}

/** شبکه-اول: برای درخواست‌های Firebase/API خارجی؛ در شکست، از کش. */
function networkFirst(request) {
  return caches.open(CACHE_NAME).then(function (cache) {
    return fetch(request)
      .then(function (res) {
        if (res && res.ok) cache.put(request, res.clone());
        return res;
      })
      .catch(function () {
        return cache.match(request).then(function (hit) {
          return hit || Response.error();
        });
      });
  });
}

/** ناوبری: شبکه → کش (index.html) → offline.html */
function navigationHandler(request) {
  return caches.open(CACHE_NAME).then(function (cache) {
    return fetch(request)
      .then(function (res) {
        if (res && res.ok) cache.put('./index.html', res.clone());
        return res;
      })
      .catch(function () {
        return cache.match('./index.html').then(function (hit) {
          return hit || cache.match('./offline.html');
        });
      });
  });
}

/* ---------- رهگیری درخواست‌ها ---------- */
self.addEventListener('fetch', function (event) {
  var request = event.request;
  if (request.method !== 'GET') return;

  var url = new URL(request.url);

  /* درخواست‌های خارجی: فقط Firebase/Google APIs با network-first پاس داده می‌شوند */
  if (url.origin !== location.origin) {
    var isFirebaseOrApi = /(^|\.)((firebaseio|firebaseapp|googleapis|gstatic|googleadservices|doubleclick)\.com)$/.test(url.hostname);
    if (isFirebaseOrApi) {
      event.respondWith(networkFirst(request));
    }
    return; // بقیهٔ درخواست‌های خارجی دست‌نخورده می‌مانند
  }

  /* ناوبری صفحه */
  if (request.mode === 'navigate') {
    event.respondWith(navigationHandler(request));
    return;
  }

  /* فایل‌های استاتیک هم‌مبدأ: cache-first */
  event.respondWith(cacheFirst(request));
});
