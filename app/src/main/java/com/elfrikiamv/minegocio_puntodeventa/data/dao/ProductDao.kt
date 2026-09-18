package com.elfrikiamv.minegocio_puntodeventa.data.dao

// ProductDao.kt

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.elfrikiamv.minegocio_puntodeventa.data.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>): List<Long>

    @Update
    suspend fun update(product: ProductEntity)

    @Delete
    suspend fun delete(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM products")
    suspend fun deleteAll()

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: Long): ProductEntity?

    @Query("SELECT * FROM products ORDER BY name")
    fun getAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun findByBarcode(barcode: String): ProductEntity?

    /** یافتن کالا با کد ۵ رقمی (برای بارکدهای وزنی ترازو). */
    @Query("SELECT * FROM products WHERE code = :code LIMIT 1")
    suspend fun findByCode(code: String): ProductEntity?

    @Query(
        "SELECT * FROM products WHERE name LIKE '%' || :query || '%' " +
            "OR barcode LIKE '%' || :query || '%' OR code LIKE '%' || :query || '%' " +
            "ORDER BY name"
    )
    fun search(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE cat = :cat ORDER BY name")
    fun getByCategory(cat: String): Flow<List<ProductEntity>>

    /** کالاهایی که موجودی‌شان به حداقل رسیده یا کمتر است. */
    @Query("SELECT * FROM products WHERE stock <= min ORDER BY name")
    fun getLowStock(): Flow<List<ProductEntity>>

    /** تغییر موجودی (کاهش فروش/افزایش خرید) بر حسب واحد پایه. */
    @Query("UPDATE products SET stock = stock + :delta WHERE id = :id")
    suspend fun updateStock(id: Long, delta: Double)

    @Query("UPDATE products SET soldCount = soldCount + :qty WHERE id = :id")
    suspend fun increaseSoldCount(id: Long, qty: Double)

    /** مقادیر متمایز دسته‌بندی‌ها (برای فیلترها). */
    @Query("SELECT DISTINCT cat FROM products WHERE cat != '' ORDER BY cat")
    fun distinctCats(): Flow<List<String>>

    /** زیردسته‌های متمایز؛ با انتخاب دسته، محدود به همان دسته. */
    @Query(
        "SELECT DISTINCT subCat FROM products WHERE subCat != '' " +
            "AND (:cat = '' OR cat = :cat) ORDER BY subCat"
    )
    fun distinctSubCats(cat: String): Flow<List<String>>

    /** برندهای متمایز (برای فیلترها). */
    @Query("SELECT DISTINCT brand FROM products WHERE brand != '' ORDER BY brand")
    fun distinctBrands(): Flow<List<String>>

    /** فهرست کامل کالاها (یک‌بار، برای گزارش‌ها). */
    @Query("SELECT * FROM products")
    suspend fun getAllOnce(): List<ProductEntity>
}
