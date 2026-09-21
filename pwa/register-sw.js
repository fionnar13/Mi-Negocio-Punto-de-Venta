/* register-sw.js — ثبت Service Worker نسخهٔ PWA «سازمان فروشگاه» */
(function () {
  'use strict';

  if (!('serviceWorker' in navigator)) {
    console.warn('[SW] این مرورگر از Service Worker پشتیبانی نمی‌کند.');
    return;
  }

  window.addEventListener('load', function () {
    navigator.serviceWorker
      .register('./service-worker.js', { scope: './' })
      .then(function (reg) {
        console.log('[SW] ثبت شد — scope:', reg.scope);
        // به‌روزرسانی فوری: نسخهٔ جدید await شده و بلافاصله فعال می‌شود
        reg.addEventListener('updatefound', function () {
          var nw = reg.installing;
          if (!nw) return;
          nw.addEventListener('statechange', function () {
            if (nw.state === 'installed' && navigator.serviceWorker.controller) {
              console.log('[SW] نسخهٔ جدید نصب شد (skipWaiting فعال است).');
            }
          });
        });
      })
      .catch(function (err) {
        console.warn('[SW] خطا در ثبت Service Worker:', err);
      });
  });
})();
