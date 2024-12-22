package com.elfrikiamv.minegocio_puntodeventa.converter

// ProductListConverter.kt

import androidx.room.TypeConverter
import com.elfrikiamv.minegocio_puntodeventa.model.ProductEntity
import com.google.gson.Gson

class ProductListConverter {
    @TypeConverter
    fun fromProductList(products: List<ProductEntity>): String {
        return Gson().toJson(products)
    }

    @TypeConverter
    fun toProductList(data: String): List<ProductEntity> {
        val listType = object : com.google.gson.reflect.TypeToken<List<ProductEntity>>() {}.type
        return Gson().fromJson(data, listType)
    }
}
