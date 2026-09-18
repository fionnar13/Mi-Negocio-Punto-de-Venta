package com.elfrikiamv.minegocio_puntodeventa.ui.screens.settings

// SettingsScreen.kt — پوستهٔ تنظیمات با ۹ تب (چیپ‌های افقی)

import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.stringResource
import com.elfrikiamv.minegocio_puntodeventa.R

/** عنوان تب‌ها به ترتیب. */
internal val TAB_TITLES = listOf(
    R.string.tab_general, R.string.tab_profile, R.string.tab_appearance, R.string.tab_barcode,
    R.string.tab_users, R.string.tab_devices, R.string.tab_data, R.string.tab_server, R.string.tab_debugger
)

/**
 * صفحهٔ تنظیمات: نوار چیپ افقی + محتوای تب انتخابی.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.onMessageShown()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // نوار تب‌های اسکرول‌پذیر
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            TAB_TITLES.forEachIndexed { index, title ->
                FilterChip(
                    selected = state.tab == index,
                    onClick = { viewModel.setTab(index) },
                    label = { Text(stringResource(title)) },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }

        // محتوای تب
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {
            item {
                when (state.tab) {
                    0 -> GeneralTab(state, viewModel)
                    1 -> ProfileTab(state, viewModel)
                    2 -> AppearanceTab(state, viewModel)
                    3 -> BarcodeTab(state, viewModel)
                    4 -> UsersTab(state, viewModel)
                    5 -> DevicesTab(state, viewModel)
                    6 -> DataTab(state, viewModel)
                    7 -> ServerTab(state, viewModel)
                    else -> DebuggerTab(state, viewModel)
                }
            }
        }
    }
}
