package com.elfrikiamv.minegocio_puntodeventa.ui.screens.products

// ProductsViewModel.kt — منطق صفحهٔ کالاها (ViewModel + StateFlow)

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.elfrikiamv.minegocio_puntodeventa.data.entity.ProductEntity
import com.elfrikiamv.minegocio_puntodeventa.data.local.SazmanDatabase
import com.elfrikiamv.minegocio_puntodeventa.data.repository.ProductRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * وضعیت رابط کاربری صفحهٔ کالاها.
 *
 * @param products نتیجهٔ جستجو/فیلتر شده.
 * @param query عبارت جستجو (نام، بارکد یا کد).
 * @param cat / subCat / brand فیلترهای انتخابی (null = همه).
 * @param cats / subCats / brands گزینه‌های موجود برای فیلترها.
 */
data class ProductsUiState(
    val products: List<ProductEntity> = emptyList(),
    val query: String = "",
    val cat: String? = null,
    val subCat: String? = null,
    val brand: String? = null,
    val cats: List<String> = emptyList(),
    val subCats: List<String> = emptyList(),
    val brands: List<String> = emptyList()
)

/**
 * ViewModel صفحهٔ کالاها.
 *
 * الگو: ViewModel + StateFlow — جستجو و فیلترها به‌صورت واکنشی از Room
 * خوانده می‌شوند (flatMapLatest روی تغییر فیلترها).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProductsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo =
        ProductRepository(SazmanDatabase.getDatabase(application).productDao())

    // ---- فیلترها ----

    private val _query = MutableStateFlow("")
    private val _cat = MutableStateFlow<String?>(null)
    private val _subCat = MutableStateFlow<String?>(null)
    private val _brand = MutableStateFlow<String?>(null)

    private data class Filters(
        val query: String,
        val cat: String?,
        val subCat: String?,
        val brand: String?
    )

    private val filters = combine(_query, _cat, _subCat, _brand) { q, c, s, b ->
        Filters(q, c, s, b)
    }

    // ---- نتیجهٔ جستجو (DAO) + فیلتر دسته/زیردسته/برند در حافظه ----

    private val results = filters.flatMapLatest { f ->
        repo.search(f.query).map { list ->
            list.filter { p ->
                (f.cat == null || p.cat == f.cat) &&
                    (f.subCat == null || p.subCat == f.subCat) &&
                    (f.brand == null || p.brand == f.brand)
            }
        }
    }

    /** گزینه‌های زیردسته با تغییر دستهٔ انتخابی به‌روز می‌شوند. */
    private val subCatOptions = _cat.flatMapLatest { cat ->
        repo.distinctSubCats(cat ?: "")
    }

    val uiState: StateFlow<ProductsUiState> = combine(
        filters,
        results,
        repo.distinctCats(),
        subCatOptions,
        repo.distinctBrands()
    ) { f, list, cats, subCats, brands ->
        ProductsUiState(
            products = list,
            query = f.query,
            cat = f.cat,
            subCat = f.subCat,
            brand = f.brand,
            cats = cats,
            subCats = subCats,
            brands = brands
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProductsUiState()
    )

    // ---- رویدادهای رابط کاربری ----

    fun onQueryChange(query: String) {
        _query.value = query
    }

    fun onCatChange(cat: String?) {
        _cat.value = cat
        _subCat.value = null // با تغییر دسته، زیردسته بازنشانی می‌شود
    }

    fun onSubCatChange(subCat: String?) {
        _subCat.value = subCat
    }

    fun onBrandChange(brand: String?) {
        _brand.value = brand
    }

    /** ذخیرهٔ کالا (id == 0 یعنی کالای جدید). */
    fun saveProduct(product: ProductEntity) {
        viewModelScope.launch {
            if (product.id == 0L) {
                repo.insert(product)
            } else {
                repo.update(product)
            }
        }
    }

    /** حذف کالا. */
    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch { repo.delete(product) }
    }

    companion object {
        /** کارخانهٔ پیش‌فرض برای ساخت با viewModel(). */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                ProductsViewModel(app)
            }
        }
    }
}
