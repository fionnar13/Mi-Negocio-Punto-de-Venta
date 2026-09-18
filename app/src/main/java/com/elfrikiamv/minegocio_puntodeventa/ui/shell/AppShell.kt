package com.elfrikiamv.minegocio_puntodeventa.ui.shell

// AppShell.kt — پوستهٔ اصلی برنامه «سازمان فروشگاه»
//
// موبایل (< 600dp): نوار ناوبری پایین با ۴ آیتم + دکمه «بیشتر» که یک
// بات‌شیت با بقیهٔ آیتم‌ها را باز می‌کند.
//
// دسکتاپ/تبلت (>= 600dp): کشوی سمت راست (به‌خاطر RTL) با همهٔ آیتم‌ها.

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.elfrikiamv.minegocio_puntodeventa.R
import com.elfrikiamv.minegocio_puntodeventa.navigation.AppDestination
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.accounts.AccountsScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.banks.BanksScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.calendar.CalendarScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.customers.CustomersScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.dashboard.DashboardScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.placeholders.PlaceholderScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.products.ProductsScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.purchase.PurchaseScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.reports.ReportsScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.sale.SaleScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.settings.SettingsScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.suppliers.SuppliersScreen
import com.elfrikiamv.minegocio_puntodeventa.ui.screens.visitors.VisitorsScreen
import com.elfrikiamv.minegocio_puntodeventa.utils.JalaliDateUtils
import androidx.compose.ui.res.stringResource

/** نقطهٔ شکست موبایل/دسکتاپ بر حسب dp. */
private const val EXPANDED_BREAKPOINT = 600

/** عرض کشوی کنار در حالت دسکتاپ/تبلت. */
private const val DRAWER_WIDTH = 280

@Composable
fun AppShell() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var moreSheetVisible by remember { mutableStateOf(false) }

    // بارگذاری پوستهٔ ذخیره‌شده در تنظیمات (یک‌بار در شروع)
    val shellContext = androidx.compose.ui.platform.LocalContext.current
    androidx.compose.runtime.LaunchedEffect(Unit) {
        runCatching {
            val settings = com.elfrikiamv.minegocio_puntodeventa.data.repository
                .SettingsRepository(
                    com.elfrikiamv.minegocio_puntodeventa.data.local.SazmanDatabase
                        .getDatabase(shellContext).settingsDao()
                ).getOrDefaults()
            com.elfrikiamv.minegocio_puntodeventa.ui.theme.AppThemeState
                .set(com.elfrikiamv.minegocio_puntodeventa.ui.theme.AppThemeState.fromSettings(settings.theme))
        }
    }

    fun navigateTo(destination: AppDestination) {
        navController.navigate(destination.route) {
            popUpTo(AppDestination.DASHBOARD.route) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints {
        val isExpanded = maxWidth >= EXPANDED_BREAKPOINT.dp

        if (isExpanded) {
            // دسکتاپ/تبلت: در RTL اولین فرزندِ Row در سمت راست قرار می‌گیرد،
            // پس کشو را اول می‌گذاریم تا در سمت راست باشد.
            Row(modifier = Modifier.fillMaxSize()) {
                AppDrawer(
                    currentRoute = currentRoute,
                    onNavigate = { navigateTo(it) }
                )
                Scaffold(
                    modifier = Modifier.weight(1f),
                    topBar = { AppTopBar(currentRoute) }
                ) { padding ->
                    AppNavHost(navController = navController, padding = padding)
                }
            }
        } else {
            // موبایل: نوار پایین + بات‌شیت «بیشتر»
            Scaffold(
                topBar = { AppTopBar(currentRoute) },
                bottomBar = {
                    AppBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = { navigateTo(it) },
                        onMoreClicked = { moreSheetVisible = true }
                    )
                }
            ) { padding ->
                AppNavHost(navController = navController, padding = padding)
            }

            if (moreSheetVisible) {
                ModalBottomSheet(onDismissRequest = { moreSheetVisible = false }) {
                    MoreSheetContent(
                        currentRoute = currentRoute,
                        onItemSelected = { destination ->
                            moreSheetVisible = false
                            navigateTo(destination)
                        }
                    )
                }
            }
        }
    }
}

/** گراف ناوبری با همهٔ مقصدها (صفحات جای‌نگه‌دار). */
@Composable
private fun AppNavHost(navController: NavHostController, padding: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.DASHBOARD.route,
        modifier = Modifier.padding(padding)
    ) {
        AppDestination.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    // داشبورد
                    AppDestination.DASHBOARD -> DashboardScreen()
                    // فروش (POS) — ثبت فاکتور فروش
                    AppDestination.SALE -> SaleScreen()
                    // خرید — ثبت فاکتور خرید از تامین‌کننده
                    AppDestination.PURCHASE -> PurchaseScreen()
                    // کالاها — صفحهٔ کامل مدیریت کالا
                    AppDestination.PRODUCTS -> ProductsScreen()
                    // مشتریان
                    AppDestination.CUSTOMERS -> CustomersScreen()
                    // تامین‌کنندگان
                    AppDestination.SUPPLIERS -> SuppliersScreen()
                    // ویزیتورها
                    AppDestination.VISITORS -> VisitorsScreen()
                    // بانک‌ها
                    AppDestination.BANKS -> BanksScreen()
                    // حساب‌ها (دفتر کل)
                    AppDestination.ACCOUNTS -> AccountsScreen()
                    // گزارش‌ها
                    AppDestination.REPORTS -> ReportsScreen()
                    // تقویم و رویدادها
                    AppDestination.CALENDAR -> CalendarScreen()
                    // تنظیمات (۹ تب)
                    AppDestination.SETTINGS -> SettingsScreen()
                    else -> PlaceholderScreen(destination = destination)
                }
            }
        }
    }
}

/** نوار بالای برنامه با عنوان بخش جاری. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(currentRoute: String?) {
    val destination = AppDestination.fromRoute(currentRoute)
    TopAppBar(
        title = {
            Text(
                text = if (destination == null || destination == AppDestination.DASHBOARD) {
                    stringResource(R.string.str_267)
                } else {
                    destination.faTitle
                }
            )
        }
    )
}

/** نوار پایین موبایل: ۴ آیتم اول + دکمه «بیشتر». */
@Composable
private fun AppBottomBar(
    currentRoute: String?,
    onNavigate: (AppDestination) -> Unit,
    onMoreClicked: () -> Unit
) {
    NavigationBar {
        AppDestination.bottomBarItems.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = { onNavigate(destination) },
                icon = {
                    Icon(
                        painter = painterResource(destination.iconRes),
                        contentDescription = destination.faTitle
                    )
                },
                label = { Text(destination.faTitle) }
            )
        }

        // دکمه «بیشتر»: وقتی یکی از آیتم‌های بات‌شیت فعال باشد، خودش هم فعال دیده می‌شود
        NavigationBarItem(
            selected = AppDestination.moreItems.any { it.route == currentRoute },
            onClick = onMoreClicked,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_more),
                    contentDescription = AppDestination.MORE_LABEL
                )
            },
            label = { Text(AppDestination.MORE_LABEL) }
        )
    }
}

/** بات‌شیت «بیشتر» با ۸ آیتم باقی‌ماندهٔ منو. */
@Composable
private fun MoreSheetContent(
    currentRoute: String?,
    onItemSelected: (AppDestination) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = AppDestination.MORE_LABEL,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        AppDestination.moreItems.forEach { destination ->
            val selected = currentRoute == destination.route
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemSelected(destination) }
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Icon(
                    painter = painterResource(destination.iconRes),
                    contentDescription = destination.faTitle,
                    tint = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = destination.faTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/** کشوی کنار (سمت راست در RTL) برای دسکتاپ/تبلت با همهٔ آیتم‌ها. */
@Composable
private fun AppDrawer(
    currentRoute: String?,
    onNavigate: (AppDestination) -> Unit
) {
    Column(
        modifier = Modifier
            .width(DRAWER_WIDTH.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .statusBarsPadding()
    ) {
        // سربرگ کشو: نام برنامه + تاریخ شمسی امروز
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(
                text = stringResource(R.string.str_267),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = JalaliDateUtils.todayLong(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp)
        ) {
            AppDestination.entries.forEach { destination ->
                NavigationDrawerItem(
                    label = { Text(destination.faTitle) },
                    icon = {
                        Icon(
                            painter = painterResource(destination.iconRes),
                            contentDescription = destination.faTitle
                        )
                    },
                    selected = currentRoute == destination.route,
                    onClick = { onNavigate(destination) },
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}
