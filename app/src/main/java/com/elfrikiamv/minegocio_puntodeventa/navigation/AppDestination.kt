package com.elfrikiamv.minegocio_puntodeventa.navigation

// AppDestination.kt — مقصدهای ناوبری «سازمان فروشگاه»

import com.elfrikiamv.minegocio_puntodeventa.R

/**
 * مقصدهای اصلی برنامه، به ترتیب منو.
 *
 * هر مقصد شامل مسیر ناوبری، عنوان فارسی و آیکون منو است.
 */
enum class AppDestination(val route: String, val faTitle: String, val iconRes: Int) {

    /** داشبورد */
    DASHBOARD("dashboard", "داشبورد", R.drawable.ic_home),

    /** فروش */
    SALE("sale", "فروش", R.drawable.ic_receipt),

    /** خرید */
    PURCHASE("purchase", "خرید", R.drawable.ic_cart),

    /** کالاها */
    PRODUCTS("products", "کالاها", R.drawable.ic_package),

    /** مشتریان */
    CUSTOMERS("customers", "مشتریان", R.drawable.ic_users),

    /** تامین‌کنندگان */
    SUPPLIERS("suppliers", "تامین‌کنندگان", R.drawable.ic_factory),

    /** ویزیتورها */
    VISITORS("visitors", "ویزیتورها", R.drawable.ic_user_check),

    /** بانک‌ها */
    BANKS("banks", "بانک‌ها", R.drawable.ic_landmark),

    /** حساب‌ها */
    ACCOUNTS("accounts", "حساب‌ها", R.drawable.ic_wallet),

    /** گزارش‌ها */
    REPORTS("reports", "گزارش‌ها", R.drawable.ic_chart_line),

    /** تقویم */
    CALENDAR("calendar", "تقویم", R.drawable.ic_calendar),

    /** تنظیمات */
    SETTINGS("settings", "تنظیمات", R.drawable.ic_settings);

    companion object {

        /** برچسب دکمه «بیشتر» در نوار پایین موبایل. */
        const val MORE_LABEL: String = "بیشتر"

        /** آیتم‌های ثابت نوار پایین در موبایل (۴ مورد اول منو). */
        val bottomBarItems: List<AppDestination> = listOf(
            DASHBOARD, SALE, PURCHASE, PRODUCTS
        )

        /** بقیه آیتم‌های منو که در بات‌شیت «بیشتر» نمایش داده می‌شوند. */
        val moreItems: List<AppDestination> = entries.filter { it !in bottomBarItems }

        /** مقصد متناظر با یک مسیر ناوبری (یا null). */
        fun fromRoute(route: String?): AppDestination? =
            entries.find { it.route == route }
    }
}
