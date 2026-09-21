/* app.js — سازمان فروشگاه (PWA)
 * منطق اصلی برنامه: IndexedDB (آینهٔ Room)، فروش/خرید، کالاها، گزارش‌ها،
 * اسکن بارکد (BarcodeDetector + ورود دستی)، تاریخ شمسی و ارقام فارسی.
 * بدون هیچ وابستگی خارجی — همه‌چیز محلی.
 */
'use strict';

/* ============================================================
 * ۱) ارقام و قالب‌بندی فارسی (قرینهٔ NumberUtils/PersianFormat اندروید)
 * ============================================================ */
var FA_DIGITS = '۰۱۲۳۴۵۶۷۸۹';

/** تبدیل ارقام لاتین به فارسی. */
function toFa(value) {
  return String(value).replace(/[0-9]/g, function (d) { return FA_DIGITS[+d]; });
}

/** تبدیل ارقام فارسی به لاتین (برای ورودی کاربر). */
function toLatin(value) {
  return String(value)
    .replace(/[۰-۹]/g, function (d) { return String(FA_DIGITS.indexOf(d)); })
    .replace(/٫/g, '.')
    .replace(/،/g, ',');
}

/** جداکنندهٔ هزارگان لاتین. */
function groupLatin(n) {
  return String(Math.trunc(Math.abs(n))).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
}

/** مبلغ با ارقام فارسی و جداکنندهٔ هزارگان: «۱۲٬۵۰۰» (+ تا ۳ رقم اعشار «٫۵»). */
function money(value) {
  var v = Number(value) || 0;
  var sign = v < 0 ? '-' : '';
  var abs = Math.abs(v);
  var whole = Math.trunc(abs);
  var frac = Math.round((abs - whole) * 1000) / 1000;
  var fracText = '';
  if (frac > 0) {
    var f = String(frac).split('.')[1] || '';
    fracText = '٫' + toFa(f);
  }
  return sign + toFa(groupLatin(whole).replace(/,/g, '٬')) + fracText;
}

/** تعداد: عدد صحیح یا وزنی با ارقام فارسی. */
function qtyFmt(value) {
  var v = Number(value) || 0;
  return Number.isInteger(v) ? money(v) : money(v);
}

/** تبدیل ورودی کاربر به عدد (ارقام فارسی/ممیز فارسی پشتیبانی می‌شوند)؛ ناموفق → 0 */
function parseNum(input) {
  var x = parseFloat(toLatin(String(input)).replace(/,/g, '.'));
  return isNaN(x) ? 0 : x;
}

/** برچسب فارسی واحد (قرینهٔ unitLabel در CartTable.kt). */
function unitLabel(unit) {
  switch (unit) {
    case 'u': return 'عدد';
    case 'c': return 'کارتن';
    case 'kg': return 'کیلو';
    case 'g': return 'گرم';
    case 'p': return 'بسته';
    default: return unit || '';
  }
}

var UNITS = [
  { value: 'u', label: 'عدد' },
  { value: 'c', label: 'کارتن' },
  { value: 'kg', label: 'کیلو' },
  { value: 'g', label: 'گرم' },
  { value: 'p', label: 'بسته' }
];

/* ============================================================
 * ۲) تقویم شمسی (جلالی) — الگوریتم استاندارد jalaali
 * ============================================================ */
var J_MONTHS = ['فروردین', 'اردیبهشت', 'خرداد', 'تیر', 'مرداد', 'شهریور',
  'مهر', 'آبان', 'آذر', 'دی', 'بهمن', 'اسفند'];
var J_DAYS = ['شنبه', 'یکشنبه', 'دوشنبه', 'سه‌شنبه', 'چهارشنبه', 'پنجشنبه', 'جمعه'];

function _div(a, b) { return ~~(a / b); }

/** میلادی → شمسی؛ خروجی: [jy, jm, jd] */
function g2j(gy, gm, gd) {
  var gDm = [0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334];
  var jy = (gy <= 1600) ? 0 : 977;
  gy -= (gy <= 1600) ? 621 : 1600;
  var gy2 = (gm > 2) ? (gy + 1) : gy;
  var days = (365 * gy) + _div(gy2 + 3, 4) - _div(gy2 + 99, 100) +
    _div(gy2 + 399, 400) - 80 + gd + gDm[gm - 1];
  jy += 33 * _div(days, 12053);
  days %= 12053;
  jy += 4 * _div(days, 1461);
  days %= 1461;
  if (days > 365) {
    jy += _div(days - 1, 365);
    days = (days - 1) % 365;
  }
  var jm = (days < 186) ? 1 + _div(days, 31) : 7 + _div(days - 186, 30);
  var jd = 1 + ((days < 186) ? (days % 31) : ((days - 186) % 30));
  return [jy, jm, jd];
}

/** شمسی → میلادی؛ خروجی: [gy, gm, gd] */
function j2g(jy, jm, jd) {
  var gy = (jy <= 979) ? 621 : 1600;
  jy -= (jy <= 979) ? 62 : 979;
  var days = (365 * jy) + (_div(jy, 33) * 8) + _div((jy % 33) + 3, 4) +
    78 + jd + ((jm < 7) ? (jm - 1) * 31 : ((jm - 7) * 30) + 186);
  gy += 400 * _div(days, 146097);
  days %= 146097;
  if (days > 36524) {
    gy += 100 * _div(--days, 36524);
    days %= 36524;
    if (days >= 365) days++;
  }
  gy += 4 * _div(days, 1461);
  days %= 1461;
  if (days > 365) {
    gy += _div(days - 1, 365);
    days = (days - 1) % 365;
  }
  var gd = days + 1;
  var leap = ((gy % 4 === 0) && (gy % 100 !== 0)) || (gy % 400 === 0);
  var salA = [0, 31, (leap ? 29 : 28), 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
  var gm;
  for (gm = 0; gm < 13 && gd > salA[gm]; gm++) gd -= salA[gm];
  return [gy, gm, gd];
}

/** تاریخ شمسی امروز: [jy, jm, jd] */
function jalaliToday() {
  var d = new Date();
  return g2j(d.getFullYear(), d.getMonth() + 1, d.getDate());
}

/** رشتهٔ ذخیره‌ای شمسی (لاتین، صفرپرشده) مثل اندروید: «1405/06/27» */
function jalaliString(jy, jm, jd) {
  return jy + '/' + String(jm).padStart(2, '0') + '/' + String(jd).padStart(2, '0');
}

/** تاریخ ذخیره‌ای امروز. */
function todayStorage() {
  var t = jalaliToday();
  return jalaliString(t[0], t[1], t[2]);
}

/** کلید ماه ذخیره‌ای: «1405/06» */
function monthKey(dateStr) { return String(dateStr).slice(0, 7); }

/** ساعت فعلی ذخیره‌ای: «14:30» */
function nowTime() {
  var d = new Date();
  return String(d.getHours()).padStart(2, '0') + ':' + String(d.getMinutes()).padStart(2, '0');
}

/** نمایش فارسی تاریخ ذخیره‌ای: «۱۴۰۵/۰۶/۲۷» */
function displayDate(dateStr) { return toFa(dateStr); }

/** نمایش فارسی ساعت ذخیره‌ای: «۱۴:۳۰» */
function displayTime(timeStr) { return toFa(timeStr); }

/** تاریخ بلند امروز: «پنجشنبه ۲۷ شهریور ۱۴۰۵» */
function todayLong() {
  var t = jalaliToday();
  var jsDay = new Date().getDay();            // 0=یکشنبه…6=شنبه
  var pIdx = (jsDay + 1) % 7;                 // 0=شنبه
  return J_DAYS[pIdx] + ' ' + toFa(t[2]) + ' ' + J_MONTHS[t[1] - 1] + ' ' + toFa(t[0]);
}

/* ============================================================
 * ۳) لایهٔ داده — IndexedDB (آینهٔ طرح Room در اندروید)
 *    stores: products / sales / purchases / counters / settings
 * ============================================================ */
var DB_NAME = 'sazman-pos';
var DB_VERSION = 1;
var _db = null;

function db() {
  if (_db) return Promise.resolve(_db);
  return new Promise(function (resolve, reject) {
    var req = indexedDB.open(DB_NAME, DB_VERSION);
    req.onupgradeneeded = function (e) {
      var d = e.target.result;
      if (!d.objectStoreNames.contains('products')) {
        var s = d.createObjectStore('products', { keyPath: 'id', autoIncrement: true });
        s.createIndex('barcode', 'barcode', { unique: false });
        s.createIndex('name', 'name', { unique: false });
      }
      if (!d.objectStoreNames.contains('sales')) {
        d.createObjectStore('sales', { keyPath: 'id', autoIncrement: true });
      }
      if (!d.objectStoreNames.contains('purchases')) {
        d.createObjectStore('purchases', { keyPath: 'id', autoIncrement: true });
      }
      if (!d.objectStoreNames.contains('counters')) {
        d.createObjectStore('counters', { keyPath: 'id' });
      }
      if (!d.objectStoreNames.contains('settings')) {
        d.createObjectStore('settings', { keyPath: 'key' });
      }
    };
    req.onsuccess = function () { _db = req.result; resolve(_db); };
    req.onerror = function () { reject(req.error); };
  });
}

function _store(name, mode) {
  return db().then(function (d) { return d.transaction(name, mode).objectStore(name); });
}
function _req(request) {
  return new Promise(function (resolve, reject) {
    request.onsuccess = function () { resolve(request.result); };
    request.onerror = function () { reject(request.error); };
  });
}

function dbAll(store) { return _store(store, 'readonly').then(function (s) { return _req(s.getAll()); }); }
function dbAdd(store, obj) { return _store(store, 'readwrite').then(function (s) { return _req(s.add(obj)); }); }
function dbPut(store, obj) { return _store(store, 'readwrite').then(function (s) { return _req(s.put(obj)); }); }
function dbDel(store, id) { return _store(store, 'readwrite').then(function (s) { return _req(s.delete(id)); }); }
function dbClear(store) { return _store(store, 'readwrite').then(function (s) { return _req(s.clear()); }); }

function getSetting(key, def) {
  return _store('settings', 'readonly').then(function (s) { return _req(s.get(key)); })
    .then(function (row) { return row === undefined ? def : row.value; });
}
function setSetting(key, value) {
  return dbPut('settings', { key: key, value: value });
}

/** شمارهٔ بعدی فاکتور (افزایش اتمیک درون یک تراکنش — قرینهٔ CounterEntity). */
function nextNo(field) {
  return db().then(function (d) {
    return new Promise(function (resolve, reject) {
      var t = d.transaction('counters', 'readwrite');
      var s = t.objectStore('counters');
      var g = s.get(1);
      g.onsuccess = function () {
        var c = g.result || { id: 1, saleNo: 0, purchNo: 0 };
        c[field] = (c[field] || 0) + 1;
        s.put(c);
        t.oncomplete = function () { resolve(String(c[field])); };
      };
      g.onerror = function () { reject(g.error); };
    });
  });
}

/* ============================================================
 * ۴) روتر و پوستهٔ برنامه
 * ============================================================ */
var view = document.getElementById('view');
var fab = document.getElementById('fab');
var currentRoute = 'dashboard';

var ROUTES = {
  dashboard: renderDashboard,
  sale: renderSale,
  purchase: renderPurchase,
  products: renderProducts,
  reports: renderReports,
  settings: renderSettings
};

function route() {
  var raw = (location.hash || '#/dashboard').slice(2);
  var name = raw.split('?')[0] || 'dashboard';
  var params = {};
  raw.split('?')[1] && raw.split('?')[1].split('&').forEach(function (kv) {
    var p = kv.split('=');
    params[p[0]] = decodeURIComponent(p[1] || '');
  });
  var fn = ROUTES[name] || ROUTES.dashboard;
  currentRoute = (ROUTES[name] ? name : 'dashboard');
  setActiveNav(currentRoute);
  window.scrollTo(0, 0);
  closeScanner();
  Promise.resolve(fn(params)).then(function () {
    if (params.scan === '1') openScanner({ mode: 'cart', cartType: 'sale' });
  });
}

function setActiveNav(name) {
  document.querySelectorAll('.nav-item').forEach(function (a) {
    a.classList.toggle('active', a.dataset.route === name);
  });
  fab.hidden = (name !== 'products');
}

function rerender() { route(); }

/* ============================================================
 * ۵) ابزارهای UI — توست، دیالوگ، فرارقم
 * ============================================================ */
var toastRoot = document.getElementById('toast-root');

function toast(msg, type) {
  var t = document.createElement('div');
  t.className = 'toast ' + (type || '');
  t.textContent = msg;
  toastRoot.appendChild(t);
  setTimeout(function () {
    t.classList.add('hide');
    setTimeout(function () { t.remove(); }, 300);
  }, 2600);
}

/** دیالوگ عمومی؛ actions: [{label, className, onClick(close), submit}] */
function modal(opts) {
  var root = document.getElementById('dialog-root');
  root.innerHTML =
    '<div class="dialog-backdrop" data-close="1">' +
    '<div class="dialog" role="dialog" aria-modal="true">' +
    '<h3 class="dialog-title">' + (opts.title || '') + '</h3>' +
    '<div class="dialog-body">' + (opts.body || '') + '</div>' +
    '<div class="dialog-actions"></div>' +
    '</div></div>';

  var backdrop = root.firstElementChild;
  var dlg = backdrop.firstElementChild;
  var closed = false;
  function close() {
    if (closed) return;
    closed = true;
    root.innerHTML = '';
  }
  backdrop.addEventListener('click', function (e) {
    if (e.target === backdrop && opts.dismissable !== false) close();
  });
  var actionsBar = dlg.querySelector('.dialog-actions');
  (opts.actions || []).forEach(function (a, i) {
    var b = document.createElement('button');
    b.type = 'button';
    b.className = 'btn ' + (a.className || 'btn-text');
    b.textContent = a.label;
    b.addEventListener('click', function () { a.onClick ? a.onClick(close) : close(); });
    actionsBar.appendChild(b);
  });
  dlg.querySelector('.dialog-body').addEventListener('submit', function (e) {
    e.preventDefault();
    var primary = (opts.actions || []).filter(function (a) { return a.submit; })[0];
    if (primary) primary.onClick(close);
  });
  var firstInput = dlg.querySelector('input, select, textarea');
  if (firstInput) setTimeout(function () { firstInput.focus(); }, 60);
  return { close: close, el: dlg };
}

function confirmDialog(title, message, onYes) {
  modal({
    title: title,
    body: '<p class="small" style="margin:0;line-height:2">' + message + '</p>',
    actions: [
      { label: 'انصراف', className: 'btn-text' },
      { label: 'تأیید', className: 'btn-error', onClick: function (close) { close(); onYes(); } }
    ]
  });
}

function beep() {
  try {
    var ctx = new (window.AudioContext || window.webkitAudioContext)();
    var osc = ctx.createOscillator();
    var gain = ctx.createGain();
    osc.type = 'sine';
    osc.frequency.value = 880;
    gain.gain.value = 0.08;
    osc.connect(gain).connect(ctx.destination);
    osc.start();
    osc.stop(ctx.currentTime + 0.12);
    osc.onended = function () { ctx.close(); };
  } catch (e) { /* بی‌صدا */ }
  if (navigator.vibrate) { try { navigator.vibrate(60); } catch (e) {} }
}

function esc(s) {
  return String(s === undefined || s === null ? '' : s)
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
}

/* ============================================================
 * ۶) داشبورد
 * ============================================================ */
function renderDashboard() {
  return Promise.all([dbAll('sales'), dbAll('purchases'), dbAll('products')])
    .then(function (r) {
      var sales = r[0], purchases = r[1], products = r[2];
      var today = todayStorage();
      var mKey = monthKey(today);

      var todaySales = sales.filter(function (s) { return s.date === today; });
      var monthSales = sales.filter(function (s) { return monthKey(s.date) === mKey; });
      var sum = function (list) { return list.reduce(function (a, s) { return a + (s.grand || 0); }, 0); };
      var lowStock = products.filter(function (p) { return (p.min || 0) > 0 && (p.stock || 0) <= (p.min || 0); });

      view.innerHTML =
        '<div class="hint-box" style="margin-bottom:14px">' + todayLong() + '</div>' +
        '<div class="stat-grid">' +
        statCard('فروش امروز', money(sum(todaySales)) + ' تومان', toFa(todaySales.length) + ' فاکتور') +
        statCard('فروش این ماه', money(sum(monthSales)) + ' تومان', toFa(monthSales.length) + ' فاکتور') +
        statCard('جمع خرید این ماه', money(purchases.filter(function (p) { return monthKey(p.date) === mKey; }).reduce(function (a, p) { return a + (p.grand || 0); }, 0)) + ' تومان', '') +
        statCard('کالای کم‌موجود', toFa(lowStock.length) + ' مورد', lowStock.length ? 'نیاز به سفارش خرید' : 'موجودی سالم') +
        '</div>' +
        '<div class="card">' +
        '<div class="section-title">میان‌برها</div>' +
        '<div class="quick-actions">' +
        '<a class="btn btn-primary" href="#/sale"><svg viewBox="0 0 24 24"><path d="M18 17H6v2h12v-2zm0-4H6v2h12v-2zm0-4H6v2h12V9zm1-6H5c-1.11 0-2 .9-2 2v16l3-2 2 2 2-2 2 2 2-2 2 2 2-2 3 2V5c0-1.1-.9-2-2-2z"/></svg>فروش جدید</a>' +
        '<button class="btn btn-tonal" data-action="new-product"><svg viewBox="0 0 24 24"><path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg>کالای جدید</button>' +
        '<button class="btn btn-secondary-tonal" data-action="open-scanner" data-cart="sale"><svg viewBox="0 0 24 24"><path d="M4 6h2V4H4v2zm0 8h2v-2H4v2zm0 4h2v-2H4v2zM4 10h2V8H4v2zm12 0h2V8h-2v2zm0-6h2V4h-2v2zm4 14h2v-2h-2v2zm0-4h2v-2h-2v2zm0-8h2V8h-2v2zm0 4h2v-2h-2v2zm0 4h2v-2h-2v2zM16 6h2V4h-2v2zM8 20h2v-2H8v2zm0-4h2v-2H8v2zm0-4h2v-2H8v2zm0-4h2V8H8v2zm12 4h2v-2h-2v2zM8 4h2v2H8V4z"/></svg>اسکن بارکد</button>' +
        '</div></div>' +
        '<div class="card">' +
        '<div class="section-title">فاکتورهای اخیر <a class="muted" href="#/reports">گزارش‌ها ←</a></div>' +
        recentSalesHtml(sales.slice(-6).reverse()) +
        '</div>' +
        (lowStock.length ?
          '<div class="card"><div class="section-title">کالاهای کم‌موجود</div><div class="list">' +
          lowStock.slice(0, 6).map(function (p) {
            return '<div class="list-item" data-action="edit-product" data-id="' + p.id + '">' +
              '<div class="avatar">' + esc((p.name || '؟').trim().charAt(0)) + '</div>' +
              '<div class="li-body"><div class="li-title">' + esc(p.name) + '</div>' +
              '<div class="li-sub">موجودی: ' + money(p.stock) + ' ' + unitLabel(p.baseUnit) + ' · حداقل: ' + money(p.min) + '</div></div>' +
              '<span class="badge error">سفارش</span></div>';
          }).join('') + '</div></div>' : '');
    });
}

function statCard(label, value, sub) {
  return '<div class="stat-card"><div class="stat-label">' + label + '</div>' +
    '<div class="stat-value">' + value + '</div>' +
    (sub ? '<div class="stat-sub">' + sub + '</div>' : '') + '</div>';
}

function recentSalesHtml(sales) {
  if (!sales.length) return emptyState('هنوز فاکتوری ثبت نشده است', 'از «فروش جدید» شروع کنید', 'M18 17H6v2h12v-2zm0-4H6v2h12v-2zm0-4H6v2h12V9zm1-6H5c-1.11 0-2 .9-2 2v16l3-2 2 2 2-2 2 2 2-2 2 2 2-2 3 2V5c0-1.1-.9-2-2-2z');
  return '<div class="list">' + sales.map(function (s) {
    return '<div class="list-item">' +
      '<div class="li-body"><div class="li-title">فاکتور ' + toFa(s.no) + ' · ' + esc(s.partyName || 'فروش حضوری') + '</div>' +
      '<div class="li-sub">' + displayDate(s.date) + ' · ' + displayTime(s.time || '') + ' · ' + toFa((s.items || []).length) + ' قلم</div></div>' +
      '<div class="li-end"><span class="price">' + money(s.grand) + '</span><span class="small muted">تومان</span></div>' +
      '</div>';
  }).join('') + '</div>';
}

function emptyState(title, sub, iconPath) {
  return '<div class="empty"><svg viewBox="0 0 24 24"><path d="' + (iconPath || 'M12 2 2 7v10l10 5 10-5V7L12 2zm0 2.3L18.5 8 12 11.2 5.5 8 12 4.3z') + '"/></svg>' +
    '<div class="e-title">' + title + '</div><div class="e-sub">' + sub + '</div></div>';
}

/* ============================================================
 * ۷) فروش و خرید (سبد مشترک — قرینهٔ SaleScreen/PurchaseScreen)
 * ============================================================ */
var cart = { items: [], discount: 0, paid: 0, method: 'نقدی', partyName: '' };
var saleQ = '';
var purchQ = '';

function resetCart() {
  cart = { items: [], discount: 0, paid: 0, method: 'نقدی', partyName: '' };
}

function renderSale() {
  resetCart();
  view.innerHTML =
    '<div class="card"><div class="section-title">فروش جدید</div>' +
    '<div class="search-row">' +
    '<input class="input" id="sale-q" type="text" placeholder="جستجوی کالا (نام / بارکد / کد)" value="' + esc(saleQ) + '">' +
    '<button class="btn btn-tonal" data-action="open-scanner" data-cart="sale" title="اسکن بارکد"><svg viewBox="0 0 24 24"><path d="M4 6h2V4H4v2zm0 8h2v-2H4v2zm0 4h2v-2H4v2zM4 10h2V8H4v2zm12 0h2V8h-2v2zm0-6h2V4h-2v2zm4 14h2v-2h-2v2zm0-4h2v-2h-2v2zm0-8h2V8h-2v2zm0 4h2v-2h-2v2zm0 4h2v-2h-2v2zM16 6h2V4h-2v2zM8 20h2v-2H8v2zm0-4h2v-2H8v2zm0-4h2v-2H8v2zm0-4h2V8H8v2zm12 4h2v-2h-2v2zM8 4h2v2H8V4z"/></svg></button>' +
    '</div>' +
    '<div class="list" id="sale-results" style="margin-top:8px"></div>' +
    '</div>' +
    '<div class="card" id="cart-card"></div>' +
    '<div class="card"><div class="section-title">فاکتورهای اخیر</div><div id="recent-sales"></div></div>';
  renderCartUI('sale');
  refreshSaleResults();
  return dbAll('sales').then(function (sales) {
    var el = document.getElementById('recent-sales');
    if (el) el.innerHTML = recentSalesHtml(sales.slice(-6).reverse());
  });
}

function renderPurchase() {
  resetCart();
  view.innerHTML =
    '<div class="card"><div class="section-title">خرید جدید (رسید تامین‌کننده)</div>' +
    '<div class="search-row">' +
    '<input class="input" id="purch-q" type="text" placeholder="جستجوی کالا (نام / بارکد / کد)" value="' + esc(purchQ) + '">' +
    '<button class="btn btn-tonal" data-action="open-scanner" data-cart="purchase" title="اسکن بارکد"><svg viewBox="0 0 24 24"><path d="M4 6h2V4H4v2zm0 8h2v-2H4v2zm0 4h2v-2H4v2zM4 10h2V8H4v2zm12 0h2V8h-2v2zm0-6h2V4h-2v2zm4 14h2v-2h-2v2zm0-4h2v-2h-2v2zm0-8h2V8h-2v2zm0 4h2v-2h-2v2zm0 4h2v-2h-2v2zM16 6h2V4h-2v2zM8 20h2v-2H8v2zm0-4h2v-2H8v2zm0-4h2v-2H8v2zm0-4h2V8H8v2zm12 4h2v-2h-2v2zM8 4h2v2H8V4z"/></svg></button>' +
    '</div>' +
    '<div class="field" style="margin-top:12px"><label>تامین‌کننده (اختیاری)</label>' +
    '<input class="input" id="purch-supplier" type="text" placeholder="نام تامین‌کننده…"></div>' +
    '<div class="list" id="purch-results"></div>' +
    '</div>' +
    '<div class="card" id="cart-card"></div>' +
    '<div class="card"><div class="section-title">خریدهای اخیر</div><div id="recent-purchases"></div></div>';
  renderCartUI('purchase');
  refreshPurchaseResults();
  return dbAll('purchases').then(function (ps) {
    var el = document.getElementById('recent-purchases');
    if (!el) return;
    if (!ps.length) { el.innerHTML = emptyState('هنوز خریدی ثبت نشده است', 'رسید خرید باعث افزایش موجودی می‌شود'); return; }
    el.innerHTML = '<div class="list">' + ps.slice(-6).reverse().map(function (p) {
      return '<div class="list-item"><div class="li-body"><div class="li-title">خرید ' + toFa(p.no) + ' · ' + esc(p.partyName || 'تامین‌کننده') + '</div>' +
        '<div class="li-sub">' + displayDate(p.date) + ' · ' + toFa((p.items || []).length) + ' قلم</div></div>' +
        '<div class="li-end"><span class="price">' + money(p.grand) + '</span><span class="small muted">تومان</span></div></div>';
    }).join('') + '</div>';
  });
}

function searchProducts(q) {
  var needle = toLatin(String(q)).trim().toLowerCase();
  return dbAll('products').then(function (list) {
    if (!needle) return [];
    return list.filter(function (p) {
      return (p.name || '').toLowerCase().indexOf(needle) >= 0 ||
        (p.barcode || '').indexOf(needle) >= 0 ||
        (p.code || '').toLowerCase().indexOf(needle) >= 0;
    }).slice(0, 10);
  });
}

function refreshSaleResults() {
  searchProducts(saleQ).then(function (list) {
    var el = document.getElementById('sale-results');
    if (!el) return;
    if (!saleQ.trim()) { el.innerHTML = ''; return; }
    if (!list.length) { el.innerHTML = '<div class="small muted" style="padding:10px 2px">کالایی یافت نشد — با «کالای جدید» اضافه کنید یا بارکد را اسکن کنید.</div>'; return; }
    el.innerHTML = list.map(function (p) {
      return '<div class="list-item" data-action="add-to-cart" data-id="' + p.id + '" data-type="sale">' +
        '<div class="avatar">' + esc((p.name || '؟').trim().charAt(0)) + '</div>' +
        '<div class="li-body"><div class="li-title">' + esc(p.name) + '</div>' +
        '<div class="li-sub">' + (p.barcode ? 'بارکد: ' + toFa(p.barcode) : '') + '</div></div>' +
        '<div class="li-end"><span class="price">' + money(p.sellC) + '</span><span class="small muted">تومان · ' + money(p.stock) + ' ' + unitLabel(p.baseUnit) + '</span></div>' +
        '</div>';
    }).join('');
  });
}

function refreshPurchaseResults() {
  searchProducts(purchQ).then(function (list) {
    var el = document.getElementById('purch-results');
    if (!el) return;
    if (!purchQ.trim()) { el.innerHTML = ''; return; }
    if (!list.length) { el.innerHTML = '<div class="small muted" style="padding:10px 2px">کالایی یافت نشد — ابتدا کالا را در «کالاها» ثبت کنید.</div>'; return; }
    el.innerHTML = list.map(function (p) {
      return '<div class="list-item" data-action="add-to-cart" data-id="' + p.id + '" data-type="purchase">' +
        '<div class="avatar">' + esc((p.name || '؟').trim().charAt(0)) + '</div>' +
        '<div class="li-body"><div class="li-title">' + esc(p.name) + '</div>' +
        '<div class="li-sub">' + (p.barcode ? 'بارکد: ' + toFa(p.barcode) : '') + ' · موجودی: ' + money(p.stock) + '</div></div>' +
        '<div class="li-end"><span class="price">' + money(p.buyC) + '</span><span class="small muted">تومان خرید</span></div>' +
        '</div>';
    }).join('');
  });
}

/** افزودن کالا به سبد (فروش: قیمت فروش / خرید: قیمت خرید). */
function addToCart(product, type) {
  var unit = product.baseUnit || 'u';
  var price = (type === 'purchase') ? (product.buyC || 0) : (product.sellC || 0);
  var line = cart.items.filter(function (it) { return it.productId === product.id && it.unit === unit; })[0];
  if (line) {
    line.qty += 1;
    line.total = line.qty * line.price;
  } else {
    cart.items.push({
      productId: product.id, name: product.name, unit: unit,
      qty: 1, price: price, discount: 0, total: price
    });
  }
  renderCartUI(type);
}

function cartTotals() {
  var total = cart.items.reduce(function (a, it) { return a + it.total; }, 0);
  var grand = Math.max(0, total - (cart.discount || 0));
  return { total: total, grand: grand };
}

function renderCartUI(type) {
  var card = document.getElementById('cart-card');
  if (!card) return;
  var t = cartTotals();
  var isSale = type === 'sale';

  var rows = cart.items.map(function (it, idx) {
    return '<tr>' +
      '<td class="wrap">' + esc(it.name) + '</td>' +
      '<td>' + unitLabel(it.unit) + '</td>' +
      '<td><span class="qty-cell">' +
      '<button class="icon-btn" data-action="cart-dec" data-idx="' + idx + '" data-type="' + type + '" title="کاهش"><svg viewBox="0 0 24 24"><path d="M19 13H5v-2h14v2z"/></svg></button>' +
      '<span class="qty-num">' + qtyFmt(it.qty) + '</span>' +
      '<button class="icon-btn" data-action="cart-inc" data-idx="' + idx + '" data-type="' + type + '" title="افزایش"><svg viewBox="0 0 24 24"><path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg></button>' +
      '</span></td>' +
      '<td>' + money(it.price) + '</td>' +
      '<td class="price">' + money(it.total) + '</td>' +
      '<td><button class="icon-btn" data-action="cart-remove" data-idx="' + idx + '" data-type="' + type + '" title="حذف" style="color:var(--error)"><svg viewBox="0 0 24 24"><path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></svg></button></td>' +
      '</tr>';
  }).join('');

  var methodChips = isSale ?
    '<div class="total-row"><span>روش پرداخت</span><span class="chips">' +
    ['نقدی', 'کارت', 'انتقال'].map(function (m) {
      return '<button class="chip' + (cart.method === m ? ' active' : '') + '" data-action="pay-method" data-method="' + m + '" data-type="sale">' + m + '</button>';
    }).join('') + '</span></div>' +
    '<div class="total-input"><span class="small">پرداخت‌شده (تومان)</span>' +
    '<input class="input" id="paid-input" type="text" inputmode="decimal" value="' + (cart.paidInput || '') + '" placeholder="' + Math.round(t.grand) + '"></div>' +
    '<div class="total-row"><span>مانده</span><span class="val" id="t-rem">' + money(Math.max(0, t.grand - (cart.paid || 0))) + '</span></div>'
    : '';

  card.innerHTML =
    '<div class="section-title">' + (isSale ? 'سبد فروش' : 'سبد خرید') +
    '<span class="muted small">' + toFa(cart.items.length) + ' قلم</span></div>' +
    (cart.items.length ?
      '<div class="table-wrap"><table class="table"><thead><tr>' +
      '<th>کالا</th><th>واحد</th><th>تعداد</th><th>فی</th><th>جمع</th><th>حذف</th>' +
      '</tr></thead><tbody>' + rows + '</tbody></table></div>'
      : emptyState('سبد خالی است', 'کالا را جستجو یا اسکن کنید', 'M7 18c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zM1 2v2h2l3.6 7.59-1.35 2.45C4.52 15.37 5.48 17 7 17h12v-2H7l1.1-2h7.45c.75 0 1.41-.41 1.75-1.03L21.7 4H5.21l-.94-2H1zm16 16c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z')) +
    '<div class="totals">' +
    '<div class="total-row"><span>جمع کل</span><span class="val" id="t-total">' + money(t.total) + ' تومان</span></div>' +
    '<div class="total-input"><span class="small">تخفیف (تومان)</span>' +
    '<input class="input" id="discount-input" type="text" inputmode="decimal" value="' + (cart.discountInput || '') + '" placeholder="0"></div>' +
    methodChips +
    '<div class="total-row grand"><span>' + (isSale ? 'قابل پرداخت' : 'مبلغ خرید') + '</span><span class="val" id="t-grand">' + money(t.grand) + ' تومان</span></div>' +
    '</div>' +
    '<button class="btn btn-primary btn-block" data-action="save-invoice" data-type="' + type + '"' + (cart.items.length ? '' : ' disabled') + '>' +
    (isSale ? 'ثبت فاکتور 💾' : 'ثبت خرید 💾') + '</button>';
}

/** ثبت فاکتور فروش/خرید — قرینهٔ منطق اندروید (شماره از Counter + موجودی). */
function saveInvoice(type) {
  if (!cart.items.length) { toast('سبد خالی است', 'error'); return; }
  var t = cartTotals();
  /* پیش‌فرض: اگر کاربر مبلغ پرداخت را خالی گذاشته، کامل پرداخت‌شده فرض می‌شود (نقدی) */
  var paidInputEl = document.getElementById('paid-input');
  var paid = (type === 'sale')
    ? ((paidInputEl && paidInputEl.value.trim()) ? Math.min(cart.paid || 0, t.grand) : t.grand)
    : t.grand;
  var supplierEl = document.getElementById('purch-supplier');
  var record = {
    no: '', date: todayStorage(), time: nowTime(),
    partyName: (type === 'purchase' && supplierEl) ? supplierEl.value.trim() : (cart.partyName || ''),
    items: cart.items.map(function (it) {
      return { productId: it.productId, name: it.name, unit: it.unit, qty: it.qty, price: it.price, discount: it.discount, total: it.total };
    }),
    total: t.total, discount: cart.discount || 0, grand: t.grand, paid: paid,
    pays: (type === 'sale') ? [{ method: cart.method, amount: paid, ref: null }] : []
  };

  nextNo(type === 'sale' ? 'saleNo' : 'purchNo').then(function (no) {
    record.no = no;
    return dbAdd(type === 'sale' ? 'sales' : 'purchases', record);
  }).then(function () {
    /* به‌روزرسانی موجودی/آمار کالاها */
    return db().then(function (d) {
      return new Promise(function (resolve, reject) {
        var tx = d.transaction('products', 'readwrite');
        var st = tx.objectStore('products');
        cart.items.forEach(function (it) {
          st.get(it.productId).onsuccess = function (e) {
            var p = e.target.result;
            if (!p) return;
            if (type === 'sale') {
              p.stock = Math.max(0, (p.stock || 0) - it.qty);
              p.soldCount = (p.soldCount || 0) + it.qty;
            } else {
              p.stock = (p.stock || 0) + it.qty;
              p.buyC = it.price; /* بروزرسانی قیمت خرید با آخرین قیمت رسید */
            }
            st.put(p);
          };
        });
        tx.oncomplete = resolve;
        tx.onerror = function () { reject(tx.error); };
      });
    });
  }).then(function () {
    toast((type === 'sale' ? 'فاکتور ' : 'خرید ') + toFa(record.no) + ' ثبت شد ✓', 'success');
    rerender();
  }).catch(function (err) {
    console.error(err);
    toast('خطا در ذخیره‌سازی: ' + (err && err.message ? err.message : err), 'error');
  });
}

/* ============================================================
 * ۸) کالاها — فهرست/افزودن/ویرایش/حذف (قرینهٔ ProductsScreen + ProductDialog)
 * ============================================================ */
var prodQ = '';

function renderProducts() {
  return dbAll('products').then(function (products) {
    view.innerHTML =
      '<div class="card">' +
      '<div class="search-row">' +
      '<input class="input" id="prod-q" type="text" placeholder="جستجو: نام، بارکد یا کد کالا" value="' + esc(prodQ) + '">' +
      '<button class="btn btn-tonal" data-action="new-product" title="کالای جدید"><svg viewBox="0 0 24 24"><path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg></button>' +
      '</div></div>' +
      '<div class="card">' +
      '<div class="section-title">کالاها <span class="muted" id="prod-count"></span></div>' +
      '<div id="prod-list"></div>' +
      '</div>';
    updateProductsList(products);
  });
}

/** به‌روزرسانی فقط فهرست کالاها (بدون رندر مجدد ورودی — حفظ فوکوس). */
function updateProductsList(allProducts) {
  var listEl = document.getElementById('prod-list');
  if (!listEl) return;
  var products = allProducts;
  if (!products) { dbAll('products').then(function (p) { updateProductsList(p); }); return; }
  var needle = toLatin(prodQ).trim().toLowerCase();
  var filtered = products.filter(function (p) {
    if (!needle) return true;
    return (p.name || '').toLowerCase().indexOf(needle) >= 0 ||
      (p.barcode || '').indexOf(needle) >= 0 ||
      (p.code || '').toLowerCase().indexOf(needle) >= 0;
  });
  var countEl = document.getElementById('prod-count');
  if (countEl) countEl.textContent = toFa(filtered.length) + ' از ' + toFa(products.length);
  listEl.innerHTML = (filtered.length ?
    '<div class="list">' + filtered.map(function (p) {
      var low = (p.min || 0) > 0 && (p.stock || 0) <= (p.min || 0);
      return '<div class="list-item">' +
        '<div class="avatar">' + esc((p.name || '؟').trim().charAt(0)) + '</div>' +
        '<div class="li-body" data-action="edit-product" data-id="' + p.id + '">' +
        '<div class="li-title">' + esc(p.name) + (low ? ' <span class="badge error">کم‌موجود</span>' : '') + '</div>' +
        '<div class="li-sub">' +
        (p.barcode ? toFa(p.barcode) + ' · ' : '') +
        (p.cat ? esc(p.cat) + ' · ' : '') +
        'موجودی: ' + money(p.stock) + ' ' + unitLabel(p.baseUnit) +
        '</div></div>' +
        '<div class="li-end"><span class="price">' + money(p.sellC) + '</span><span class="small muted">تومان</span></div>' +
        '<div class="li-actions">' +
        '<button class="icon-btn" data-action="edit-product" data-id="' + p.id + '" title="ویرایش"><svg viewBox="0 0 24 24"><path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/></svg></button>' +
        '<button class="icon-btn" data-action="delete-product" data-id="' + p.id + '" title="حذف" style="color:var(--error)"><svg viewBox="0 0 24 24"><path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></svg></button>' +
        '</div></div>';
    }).join('') + '</div>'
    : emptyState('هنوز کالایی ثبت نشده است',
      prodQ ? 'عبارت جستجو را تغییر دهید' : 'با دکمهٔ «کالای جدید» اولین کالا را اضافه کنید'));
}

/** دیالوگ کالای جدید/ویرایش — قرینهٔ ProductDialog اندروید. */
function openProductDialog(existing, prefill) {
  var p = existing || { name: '', barcode: '', code: '', cat: '', brand: '', baseUnit: 'u', buyC: 0, sellC: 0, stock: 0, min: 0 };
  var pre = prefill || {};
  var unitOptions = UNITS.map(function (u) {
    return '<option value="' + u.value + '"' + (p.baseUnit === u.value ? ' selected' : '') + '>' + u.label + '</option>';
  }).join('');

  var m = modal({
    title: existing ? 'ویرایش کالا' : 'کالای جدید',
    body:
      '<form id="product-form">' +
      '<div class="form-grid">' +
      '<div class="field span-2"><label>نام کالا *</label>' +
      '<input class="input" id="pf-name" required value="' + esc(p.name) + '" placeholder="مثلاً شیر پرچرب ۱ لیتری"></div>' +
      '<div class="field"><label>بارکد</label>' +
      '<div class="search-row">' +
      '<input class="input" id="pf-barcode" inputmode="numeric" value="' + esc(pre.barcode || p.barcode) + '" placeholder="EAN-13…">' +
      '<button type="button" class="btn btn-tonal" data-action="open-scanner" data-mode="fill" title="اسکن"><svg viewBox="0 0 24 24"><path d="M4 6h2V4H4v2zm0 8h2v-2H4v2zm0 4h2v-2H4v2zM4 10h2V8H4v2zm12 0h2V8h-2v2zm0-6h2V4h-2v2zm4 14h2v-2h-2v2zm0-4h2v-2h-2v2zm0-8h2V8h-2v2zm0 4h2v-2h-2v2zm0 4h2v-2h-2v2zM16 6h2V4h-2v2zM8 20h2v-2H8v2zm0-4h2v-2H8v2zm0-4h2v-2H8v2zm0-4h2V8H8v2zm12 4h2v-2h-2v2zM8 4h2v2H8V4z"/></svg></button>' +
      '</div></div>' +
      '<div class="field"><label>کد کالا</label><input class="input" id="pf-code" value="' + esc(p.code) + '"></div>' +
      '<div class="field"><label>دسته‌بندی</label><input class="input" id="pf-cat" value="' + esc(p.cat) + '" placeholder="لبنیات، نوشیدنی…"></div>' +
      '<div class="field"><label>برند</label><input class="input" id="pf-brand" value="' + esc(p.brand) + '"></div>' +
      '<div class="field"><label>واحد پایه</label><select class="select" id="pf-unit">' + unitOptions + '</select></div>' +
      '<div class="field"><label>قیمت خرید (تومان)</label><input class="input" id="pf-buyc" inputmode="decimal" value="' + (p.buyC ? String(Math.round(p.buyC)) : '') + '"></div>' +
      '<div class="field"><label>قیمت فروش (تومان) *</label><input class="input" id="pf-sellc" inputmode="decimal" required value="' + (p.sellC ? String(Math.round(p.sellC)) : '') + '"></div>' +
      '<div class="field"><label>موجودی</label><input class="input" id="pf-stock" inputmode="decimal" value="' + (p.stock ? String(p.stock) : '') + '"></div>' +
      '<div class="field"><label>حداقل موجودی (هشدار)</label><input class="input" id="pf-min" inputmode="decimal" value="' + (p.min ? String(Math.round(p.min)) : '') + '"></div>' +
      '</div></form>',
    actions: [
      { label: 'انصراف', className: 'btn-text' },
      {
        label: existing ? 'ذخیرهٔ تغییرات' : 'ثبت کالا',
        className: 'btn-primary',
        submit: true,
        onClick: function (close) {
          var name = m.el.querySelector('#pf-name').value.trim();
          var sellC = parseNum(m.el.querySelector('#pf-sellc').value);
          if (!name) { toast('نام کالا الزامی است', 'error'); return; }
          if (sellC <= 0) { toast('قیمت فروش باید بزرگ‌تر از صفر باشد', 'error'); return; }
          var barcode = m.el.querySelector('#pf-barcode').value.trim();

          dbAll('products').then(function (all) {
            var dup = all.filter(function (x) {
              return barcode && x.barcode === barcode && (!existing || x.id !== existing.id);
            })[0];
            if (dup) {
              toast('بارکد تکراری — «' + dup.name + '» این بارکد را دارد', 'error');
              return null;
            }
            var rec = {
              name: name,
              barcode: barcode,
              code: m.el.querySelector('#pf-code').value.trim(),
              cat: m.el.querySelector('#pf-cat').value.trim(),
              brand: m.el.querySelector('#pf-brand').value.trim(),
              baseUnit: m.el.querySelector('#pf-unit').value,
              buyC: parseNum(m.el.querySelector('#pf-buyc').value),
              sellC: sellC,
              stock: parseNum(m.el.querySelector('#pf-stock').value),
              min: parseNum(m.el.querySelector('#pf-min').value),
              soldCount: existing ? (existing.soldCount || 0) : 0,
              createdAt: existing ? (existing.createdAt || Date.now()) : Date.now()
            };
            if (existing) rec.id = existing.id;
            return dbPut('products', rec);
          }).then(function (ok) {
            if (ok === null) return;
            close();
            toast(existing ? 'کالا ویرایش شد ✓' : 'کالا ثبت شد ✓', 'success');
            rerender();
          }).catch(function (err) {
            console.error(err);
            toast('خطا در ذخیره‌سازی', 'error');
          });
        }
      }
    ]
  });
}

function deleteProduct(p) {
  confirmDialog('حذف کالا', 'آیا از حذف «' + esc(p.name) + '» مطمئن هستید؟ این عمل قابل بازگشت نیست.', function () {
    dbDel('products', p.id).then(function () {
      toast('کالا حذف شد', 'success');
      rerender();
    });
  });
}

/* ============================================================
 * ۹) گزارش‌ها (قرینهٔ ReportsScreen — خلاصهٔ فروش)
 * ============================================================ */
var reportPeriod = 'today';

function renderReports() {
  return Promise.all([dbAll('sales'), dbAll('purchases'), dbAll('products')])
    .then(function (r) {
      var sales = r[0], purchases = r[1], products = r[2];
      var today = todayStorage();
      var mKey = monthKey(today);

      var inPeriod = reportPeriod === 'today'
        ? function (s) { return s.date === today; }
        : (reportPeriod === 'month'
          ? function (s) { return monthKey(s.date) === mKey; }
          : function () { return true; });

      var pSales = sales.filter(inPeriod);
      var pPurchases = purchases.filter(inPeriod);

      var count = pSales.length;
      var totalSales = pSales.reduce(function (a, s) { return a + (s.grand || 0); }, 0);
      var cash = pSales.reduce(function (a, s) {
        return a + (s.pays || []).filter(function (x) { return x.method === 'نقدی'; }).reduce(function (b, x) { return b + x.amount; }, 0);
      }, 0);
      var nonCash = totalSales - cash;
      var totalPurch = pPurchases.reduce(function (a, p) { return a + (p.grand || 0); }, 0);

      var buyMap = {};
      var nameMap = {};
      products.forEach(function (p) { buyMap[p.id] = p.buyC || 0; nameMap[p.id] = p.name; });
      var grossProfit = pSales.reduce(function (a, s) {
        return a + (s.items || []).reduce(function (b, it) {
          return b + ((it.price - (buyMap[it.productId] || 0)) * it.qty);
        }, 0);
      }, 0);

      var soldAgg = {};
      pSales.forEach(function (s) {
        (s.items || []).forEach(function (it) {
          soldAgg[it.productId] = (soldAgg[it.productId] || 0) + it.qty;
        });
      });
      var best = Object.keys(soldAgg).map(function (pid) {
        return { name: nameMap[pid] || '—', qty: soldAgg[pid] };
      }).sort(function (a, b) { return b.qty - a.qty; }).slice(0, 5);

      view.innerHTML =
        '<div class="card"><div class="section-title">نوع گزارش</div>' +
        '<div class="chips">' +
        [['today', 'امروز'], ['month', 'این ماه'], ['all', 'همه']].map(function (x) {
          return '<button class="chip' + (reportPeriod === x[0] ? ' active' : '') + '" data-action="report-period" data-period="' + x[0] + '">' + x[1] + '</button>';
        }).join('') +
        '</div></div>' +
        '<div class="stat-grid">' +
        statCard('تعداد فاکتور', toFa(count), '') +
        statCard('مجموع فروش', money(totalSales) + ' تومان', '') +
        statCard('نقدی', money(cash) + ' تومان', '') +
        statCard('بانکی/کارت', money(nonCash) + ' تومان', '') +
        statCard('جمع خرید', money(totalPurch) + ' تومان', '') +
        statCard('سود ناخالص', money(grossProfit) + ' تومان', 'بر پایهٔ قیمت خرید فعلی') +
        '</div>' +
        '<div class="card"><div class="section-title">پرفروش‌ترین کالاها</div>' +
        (best.length ? '<div class="list">' + best.map(function (b) {
          return '<div class="list-item"><div class="avatar">' + esc((b.name || '؟').charAt(0)) + '</div>' +
            '<div class="li-body"><div class="li-title">' + esc(b.name) + '</div></div>' +
            '<div class="li-end"><span class="price">' + qtyFmt(b.qty) + '</span><span class="small muted">فروخته‌شده</span></div></div>';
        }).join('') + '</div>' : emptyState('داده‌ای نیست', 'در این بازه فروشی ثبت نشده است')) +
        '</div>' +
        '<div class="card"><div class="section-title">همهٔ فاکتورها (' + toFa(count) + ')</div>' +
        (pSales.length ? '<div class="table-wrap"><table class="table"><thead><tr>' +
          '<th>شماره</th><th>تاریخ</th><th>ساعت</th><th>اقلام</th><th>مبلغ</th>' +
          '</tr></thead><tbody>' + pSales.slice(-15).reverse().map(function (s) {
            return '<tr><td>' + toFa(s.no) + '</td><td>' + displayDate(s.date) + '</td><td>' + displayTime(s.time || '') + '</td>' +
              '<td>' + toFa((s.items || []).length) + '</td><td class="price">' + money(s.grand) + '</td></tr>';
          }).join('') + '</tbody></table></div>'
          : emptyState('فاکتوری نیست', 'از تب «فروش» فاکتور ثبت کنید')) +
        '</div>';
    });
}

/* ============================================================
 * ۱۰) تنظیمات
 * ============================================================ */
function renderSettings() {
  return getSetting('shopName', 'سازمان فروشگاه').then(function (shopName) {
    view.innerHTML =
      '<div class="card">' +
      '<div class="section-title">هویت فروشگاه</div>' +
      '<div class="field"><label>نام فروشگاه</label>' +
      '<input class="input" id="shop-name" value="' + esc(shopName) + '"></div>' +
      '<button class="btn btn-tonal" data-action="save-settings">ذخیره</button>' +
      '</div>' +
      '<div class="card">' +
      '<div class="section-title">داده‌ها (پشتیبان‌گیری)</div>' +
      '<div class="settings-row"><div class="sr-body"><div class="sr-title">خروجی پشتیبان</div>' +
      '<div class="sr-sub">کل کالاها، فاکتورها و تنظیمات در یک فایل JSON</div></div>' +
      '<button class="btn btn-outline btn-small" data-action="export-data">دریافت فایل</button></div>' +
      '<div class="settings-row"><div class="sr-body"><div class="sr-title">بازیابی از فایل</div>' +
      '<div class="sr-sub">داده‌های فعلی جایگزین می‌شوند</div></div>' +
      '<button class="btn btn-outline btn-small" data-action="import-data">انتخاب فایل</button>' +
      '<input type="file" id="import-file" accept="application/json,.json" hidden>' +
      '</div>' +
      '<div class="settings-row"><div class="sr-body"><div class="sr-title">داده‌های نمونه</div>' +
      '<div class="sr-sub">چند کالا و فاکتور نمونه برای تست سریع</div></div>' +
      '<button class="btn btn-outline btn-small" data-action="seed-data">درج نمونه</button></div>' +
      '</div>' +
      '<div class="card danger-zone">' +
      '<div class="section-title" style="color:var(--error)">ناحیهٔ خطر</div>' +
      '<div class="settings-row"><div class="sr-body"><div class="sr-title">پاک‌کردن همهٔ داده‌ها</div>' +
      '<div class="sr-sub">همهٔ کالاها و فاکتورها برای همیشه حذف می‌شوند</div></div>' +
      '<button class="btn btn-error btn-small" data-action="wipe-data">پاک‌کردن</button></div>' +
      '</div>' +
      '<div class="card">' +
      '<div class="section-title">درباره</div>' +
      '<div class="hint-box">نسخهٔ وب (PWA) ۱٫۰ — قرینهٔ اپ اندروید «سازمان فروشگاه».<br>' +
      'همهٔ داده‌ها فقط روی همین دستگاه (IndexedDB مرورگر) ذخیره می‌شوند؛ بدون سرور و بدون اینترنت.<br>' +
      'امکانات ابری اپ اندروید (Firebase، AdMob، چاپ حرارتی) در نسخهٔ وب موجود نیست.</div>' +
      '</div>';
  });
}

function exportData() {
  Promise.all([dbAll('products'), dbAll('sales'), dbAll('purchases'), dbAll('settings')])
    .then(function (r) {
      var payload = {
        app: 'sazman-pos-pwa', version: 1, exportedAt: new Date().toISOString(),
        products: r[0], sales: r[1], purchases: r[2], settings: r[3]
      };
      var blob = new Blob([JSON.stringify(payload, null, 2)], { type: 'application/json' });
      var a = document.createElement('a');
      a.href = URL.createObjectURL(blob);
      a.download = 'sazman-backup-' + todayStorage().replace(/\//g, '-') + '.json';
      document.body.appendChild(a);
      a.click();
      a.remove();
      setTimeout(function () { URL.revokeObjectURL(a.href); }, 4000);
      toast('فایل پشتیبان ساخته شد ✓', 'success');
    });
}

function importData(file) {
  var reader = new FileReader();
  reader.onload = function () {
    var data;
    try { data = JSON.parse(reader.result); }
    catch (e) { toast('فایل نامعتبر است', 'error'); return; }
    if (!data || data.app !== 'sazman-pos-pwa' || !Array.isArray(data.products)) {
      toast('این فایل پشتیبان نسخهٔ وب نیست', 'error');
      return;
    }
    confirmDialog('بازیابی داده‌ها',
      'فایل شامل ' + toFa(data.products.length) + ' کالا و ' +
      toFa((data.sales || []).length) + ' فاکتور فروش است. داده‌های فعلی حذف و جایگزین می‌شوند. ادامه می‌دهید؟',
      function () {
        var stores = [['products', data.products], ['sales', data.sales || []], ['purchases', data.purchases || []], ['settings', data.settings || []]];
        var chain = Promise.resolve();
        stores.forEach(function (pair) {
          chain = chain.then(function () { return dbClear(pair[0]); }).then(function () {
            return db().then(function (d) {
              return new Promise(function (resolve, reject) {
                var t = d.transaction(pair[0], 'readwrite');
                var st = t.objectStore(pair[0]);
                pair[1].forEach(function (row) { st.put(row); });
                t.oncomplete = resolve;
                t.onerror = function () { reject(t.error); };
              });
            });
          });
        });
        chain.then(function () {
          toast('داده‌ها بازیابی شد ✓', 'success');
          rerender();
        }).catch(function () { toast('خطا در بازیابی', 'error'); });
      });
  };
  reader.readAsText(file);
}

function seedData() {
  var today = todayStorage();
  var products = [
    { name: 'شیر پرچرب ۱ لیتری پگاه', barcode: '6260100100014', code: 'P-101', cat: 'لبنیات', brand: 'پگاه', baseUnit: 'u', buyC: 22000, sellC: 27000, stock: 48, min: 12 },
    { name: 'نان تست جو ۵۰۰ گرمی', barcode: '6260100100021', code: 'P-102', cat: 'نان و غلات', brand: 'تابلید', baseUnit: 'u', buyC: 35000, sellC: 42000, stock: 20, min: 8 },
    { name: 'برنج هاشمی درجه‌یک ۱۰ کیلویی', barcode: '6260100100038', code: 'P-103', cat: 'نان و غلات', brand: 'هاشمی', baseUnit: 'c', buyC: 1250000, sellC: 1450000, stock: 9, min: 4 },
    { name: 'روغن سرخ‌کردنی ۱.۸ لیتری', barcode: '6260100100045', code: 'P-104', cat: 'چاشنی و روغن', brand: 'لادن', baseUnit: 'u', buyC: 185000, sellC: 215000, stock: 15, min: 6 },
    { name: 'چای کیسه‌ای ۱۰۰ عددی', barcode: '6260100100052', code: 'P-105', cat: 'نوشیدنی', brand: 'احمد', baseUnit: 'p', buyC: 145000, sellC: 178000, stock: 11, min: 5 },
    { name: 'ماست پرچرب ۹۰۰ گرمی', barcode: '6260100100069', code: 'P-106', cat: 'لبنیات', brand: 'کاله', baseUnit: 'u', buyC: 98000, sellC: 118000, stock: 4, min: 6 },
    { name: 'پنیر لیقوان ۴۰۰ گرمی', barcode: '6260100100076', code: 'P-107', cat: 'لبنیات', brand: 'لیقوان', baseUnit: 'u', buyC: 165000, sellC: 198000, stock: 7, min: 3 },
    { name: 'سیب زمینی (کیلو)', barcode: '', code: 'P-108', cat: 'میوه و سبزی', brand: '', baseUnit: 'kg', buyC: 25000, sellC: 35000, stock: 120, min: 20 }
  ].map(function (p) { p.soldCount = 0; p.createdAt = Date.now(); return p; });

  db().then(function (d) {
    return new Promise(function (resolve, reject) {
      var t = d.transaction(['products', 'sales', 'purchases', 'counters'], 'readwrite');
      var prodStore = t.objectStore('products');
      var ids = [];
      products.forEach(function (p) {
        var req = prodStore.add(p);
        req.onsuccess = function () { ids.push(req.result); };
      });
      var saleStore = t.objectStore('sales');
      function mkSale(no, offsH, items) {
        var total = items.reduce(function (a, it) { return a + it.total; }, 0);
        return {
          no: String(no), date: today,
          time: String(offsH).padStart(2, '0') + ':' + (no % 2 ? '15' : '45'),
          partyName: '', items: items, total: total, discount: 0, grand: total, paid: total,
          pays: [{ method: 'نقدی', amount: total, ref: null }]
        };
      }
      saleStore.add(mkSale(1, 10, [
        { productId: ids[0], name: products[0].name, unit: 'u', qty: 2, price: 27000, discount: 0, total: 54000 },
        { productId: ids[4], name: products[4].name, unit: 'p', qty: 1, price: 178000, discount: 0, total: 178000 }
      ]));
      saleStore.add(mkSale(2, 13, [
        { productId: ids[3], name: products[3].name, unit: 'u', qty: 1, price: 215000, discount: 0, total: 215000 },
        { productId: ids[6], name: products[6].name, unit: 'u', qty: 2, price: 198000, discount: 0, total: 396000 }
      ]));
      t.objectStore('purchases').add({
        no: '1', date: today, time: '09:00', partyName: 'پخش سراسری کاله',
        items: [{ productId: ids[0], name: products[0].name, unit: 'u', qty: 24, price: 22000, discount: 0, total: 528000 }],
        total: 528000, discount: 0, grand: 528000, paid: 528000
      });
      t.objectStore('counters').put({ id: 1, saleNo: 2, purchNo: 1 });
      t.oncomplete = resolve;
      t.onerror = function () { reject(t.error); };
    });
  }).then(function () {
    toast('داده‌های نمونه درج شد ✓', 'success');
    rerender();
  }).catch(function (e) { console.error(e); toast('خطا در درج داده‌ها', 'error'); });
}

function wipeData() {
  confirmDialog('پاک‌کردن همهٔ داده‌ها',
    'همهٔ کالاها، فاکتورها و تنظیمات برای همیشه حذف می‌شوند. مطمئن هستید؟',
    function () {
      Promise.all([dbClear('products'), dbClear('sales'), dbClear('purchases'), dbClear('counters')])
        .then(function () {
          toast('همهٔ داده‌ها پاک شد', 'success');
          rerender();
        });
    });
}

/* ============================================================
 * ۱۱) اسکنر بارکد — BarcodeDetector + ورود دستی
 * ============================================================ */
var scanner = { ctx: null, stream: null, detector: null, timer: null, torchOn: false, productsCache: [] };

function openScanner(ctx) {
  var modalEl = document.getElementById('scanner-modal');
  var video = document.getElementById('scanner-video');
  var status = document.getElementById('scanner-status');
  var torchBtn = document.getElementById('torch-btn');
  scanner.ctx = ctx;
  modalEl.hidden = false;
  status.textContent = 'در حال راه‌اندازی دوربین…';
  document.documentElement.style.overflow = 'hidden';

  dbAll('products').then(function (list) { scanner.productsCache = list; });

  var constraints = { video: { facingMode: { ideal: 'environment' }, width: { ideal: 1280 }, height: { ideal: 720 } }, audio: false };
  navigator.mediaDevices.getUserMedia(constraints).then(function (stream) {
    scanner.stream = stream;
    video.srcObject = stream;
    return video.play();
  }).then(function () {
    /* چراغ‌قوه (در صورت پشتیبانی) */
    var track = scanner.stream && scanner.stream.getVideoTracks()[0];
    var caps = track && track.getCapabilities ? track.getCapabilities() : {};
    if (caps && caps.torch) {
      torchBtn.hidden = false;
    }
    if (!('BarcodeDetector' in window)) {
      status.textContent = 'این مرورگر BarcodeDetector ندارد — بارکد را دستی وارد کنید (ورود دستی پایین)';
      document.getElementById('scan-manual-input').focus();
      return;
    }
    var wanted = ['ean_13', 'ean_8', 'code_128', 'code_39', 'upc_a', 'upc_e', 'itf', 'qr_code'];
    return BarcodeDetector.getSupportedFormats().then(function (fmts) {
      var use = wanted.filter(function (f) { return fmts.indexOf(f) >= 0; });
      scanner.detector = new BarcodeDetector(use.length ? { formats: use } : {});
      status.textContent = 'در حال اسکن… بارکد را داخل کادر بگیرید';
      scanner.timer = setInterval(detectFrame, 350);
    });
  }).catch(function (err) {
    console.warn('[Scanner]', err);
    status.textContent = 'دسترسی به دوربین ممکن نشد (' + (err.name || err.message || 'خطا') + ') — بارکد را دستی وارد کنید';
    document.getElementById('scan-manual-input').focus();
  });
}

function detectFrame() {
  var video = document.getElementById('scanner-video');
  if (!scanner.detector || video.readyState < 2) return;
  scanner.detector.detect(video).then(function (codes) {
    if (codes && codes.length) handleBarcode(codes[0].rawValue);
  }).catch(function () { /* فریم ناخوانا */ });
}

function handleBarcode(code) {
  if (!code) return;
  beep();
  var frame = document.querySelector('.scan-frame');
  if (frame) {
    frame.classList.add('flash');
    setTimeout(function () { frame.classList.remove('flash'); }, 350);
  }
  if (scanner.ctx && scanner.ctx.mode === 'fill') {
    var input = document.getElementById('pf-barcode');
    if (input) input.value = code;
    toast('بارکد وارد شد: ' + toFa(code), 'success');
    closeScanner();
    return;
  }
  var product = scanner.productsCache.filter(function (p) { return p.barcode === code; })[0];
  if (product) {
    var type = (scanner.ctx && scanner.ctx.cartType) || 'sale';
    addToCart(product, type);
    toast('«' + product.name + '» به سبد اضافه شد ✓', 'success');
  } else {
    toast('بارکد نامعتبر — کالایی با این بارکد ثبت نشده است', 'error');
    closeScanner();
    openProductDialog(null, { barcode: code });
  }
}

function toggleTorch() {
  var track = scanner.stream && scanner.stream.getVideoTracks()[0];
  if (!track) return;
  scanner.torchOn = !scanner.torchOn;
  track.applyConstraints({ advanced: [{ torch: scanner.torchOn }] }).catch(function () {
    toast('چراغ‌قوه پشتیبانی نمی‌شود', 'error');
  });
}

function closeScanner() {
  var modalEl = document.getElementById('scanner-modal');
  var video = document.getElementById('scanner-video');
  if (modalEl.hidden) return;
  if (scanner.timer) { clearInterval(scanner.timer); scanner.timer = null; }
  if (scanner.stream) {
    scanner.stream.getTracks().forEach(function (t) { t.stop(); });
    scanner.stream = null;
  }
  scanner.detector = null;
  scanner.torchOn = false;
  document.getElementById('torch-btn').hidden = true;
  video.srcObject = null;
  modalEl.hidden = true;
  document.documentElement.style.overflow = '';
}

/* ============================================================
 * ۱۲) رویدادها (delegation سراسری)
 * ============================================================ */
document.addEventListener('click', function (e) {
  var el = e.target.closest('[data-action]');
  if (!el) return;
  var action = el.dataset.action;

  switch (action) {
    case 'new-product':
      openProductDialog(null);
      break;
    case 'edit-product': {
      var id = +el.dataset.id;
      dbAll('products').then(function (list) {
        var p = list.filter(function (x) { return x.id === id; })[0];
        if (p) openProductDialog(p);
      });
      break;
    }
    case 'delete-product': {
      var pid = +el.dataset.id;
      dbAll('products').then(function (list) {
        var p = list.filter(function (x) { return x.id === pid; })[0];
        if (p) deleteProduct(p);
      });
      break;
    }
    case 'add-to-cart': {
      var aid = +el.dataset.id;
      var atype = el.dataset.type;
      dbAll('products').then(function (list) {
        var p = list.filter(function (x) { return x.id === aid; })[0];
        if (p) addToCart(p, atype);
      });
      break;
    }
    case 'cart-inc': case 'cart-dec': {
      var idx = +el.dataset.idx;
      var it = cart.items[idx];
      if (!it) break;
      it.qty += (action === 'cart-inc') ? 1 : -1;
      if (it.qty <= 0) cart.items.splice(idx, 1);
      else it.total = it.qty * it.price;
      renderCartUI(el.dataset.type);
      break;
    }
    case 'cart-remove':
      cart.items.splice(+el.dataset.idx, 1);
      renderCartUI(el.dataset.type);
      break;
    case 'pay-method':
      cart.method = el.dataset.method;
      renderCartUI('sale');
      break;
    case 'save-invoice':
      saveInvoice(el.dataset.type);
      break;
    case 'open-scanner':
      openScanner({ mode: el.dataset.mode || 'cart', cartType: el.dataset.cart || currentRoute });
      break;
    case 'close-scanner':
      closeScanner();
      break;
    case 'torch':
      toggleTorch();
      break;
    case 'report-period':
      reportPeriod = el.dataset.period;
      renderReports();
      break;
    case 'save-settings':
      setSetting('shopName', document.getElementById('shop-name').value.trim() || 'سازمان فروشگاه')
        .then(function () {
          document.getElementById('app-title').textContent =
            document.getElementById('shop-name').value.trim() || 'سازمان فروشگاه';
          toast('تنظیمات ذخیره شد ✓', 'success');
        });
      break;
    case 'export-data':
      exportData();
      break;
    case 'import-data':
      document.getElementById('import-file').click();
      break;
    case 'seed-data':
      confirmDialog('درج داده‌های نمونه', 'چند کالا و فاکتور نمونه اضافه می‌شود. ادامه می‌دهید؟', seedData);
      break;
    case 'wipe-data':
      wipeData();
      break;
  }
});

/* ورودی‌های متنی (جستجو / تخفیف / پرداخت) */
document.addEventListener('input', function (e) {
  var t = e.target;
  if (t.id === 'sale-q') { saleQ = t.value; refreshSaleResults(); }
  else if (t.id === 'purch-q') { purchQ = t.value; refreshPurchaseResults(); }
  else if (t.id === 'prod-q') { prodQ = t.value; updateProductsList(); }
  else if (t.id === 'discount-input') {
    cart.discountInput = t.value;
    cart.discount = parseNum(t.value);
    updateTotalsOnly();
  } else if (t.id === 'paid-input') {
    cart.paidInput = t.value;
    cart.paid = parseNum(t.value);
    updateTotalsOnly();
  }
});

/* انتخاب فایل بازیابی */
document.addEventListener('change', function (e) {
  if (e.target.id === 'import-file' && e.target.files && e.target.files[0]) {
    importData(e.target.files[0]);
    e.target.value = '';
  }
});

/* فرم ورود دستی بارکد */
document.addEventListener('submit', function (e) {
  if (e.target.id === 'scan-manual-form') {
    e.preventDefault();
    var val = document.getElementById('scan-manual-input').value.trim();
    if (val) {
      handleBarcode(toLatin(val));
      document.getElementById('scan-manual-input').value = '';
    }
  }
});

/** به‌روزرسانی فقط جمع‌ها بدون رندر مجدد ورودی‌ها (حفظ فوکوس کاربر). */
function updateTotalsOnly() {
  var t = cartTotals();
  var el;
  el = document.getElementById('t-total');
  if (el) el.textContent = money(t.total) + ' تومان';
  el = document.getElementById('t-grand');
  if (el) el.textContent = money(t.grand) + ' تومان';
  el = document.getElementById('t-rem');
  if (el) el.textContent = money(Math.max(0, t.grand - (cart.paid || 0)));
}

/* کلید Escape: بستن اسکنر */
document.addEventListener('keydown', function (e) {
  if (e.key === 'Escape' && !document.getElementById('scanner-modal').hidden) closeScanner();
});

/* ============================================================
 * ۱۳) نصب PWA (beforeinstallprompt)
 * ============================================================ */
var deferredInstall = null;
window.addEventListener('beforeinstallprompt', function (e) {
  e.preventDefault();
  deferredInstall = e;
  var btn = document.getElementById('install-btn');
  if (btn) btn.hidden = false;
});
document.getElementById('install-btn').addEventListener('click', function () {
  if (!deferredInstall) return;
  deferredInstall.prompt();
  deferredInstall.userChoice.then(function (choice) {
    if (choice.outcome === 'accepted') toast('برنامه نصب شد ✓', 'success');
    deferredInstall = null;
    document.getElementById('install-btn').hidden = true;
  });
});
window.addEventListener('appinstalled', function () {
  document.getElementById('install-btn').hidden = true;
  toast('سازمان فروشگاه نصب شد 🎉', 'success');
});

/* ============================================================
 * ۱۴) راه‌اندازی
 * ============================================================ */
(function init() {
  getSetting('shopName', 'سازمان فروشگاه').then(function (name) {
    document.getElementById('app-title').textContent = name;
  });
  document.getElementById('app-date').textContent = todayLong();
  window.addEventListener('hashchange', route);
  route();
})();


