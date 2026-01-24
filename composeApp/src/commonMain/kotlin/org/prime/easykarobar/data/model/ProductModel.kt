package org.prime.easykarobar.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ProductCategoryList(
    val categories: List<Category>,
    val products: List<Product>

)

@Serializable
data class Category(
    val category_id: Int,
    val category_name: String,
    val profile_picture:String?=null
    //val image: String? = null
)

@Serializable
data class Product(
    val product_id: Int,
    val hospital_id: Int,
    val product_name: String,
    val category_id: Int,
    val unit_id: Int,
    val sales_price: String,
    val mrp: String,
    val purchase_price: String,
    val discount: String,
    val gst_tax_percentage: String,
    val product_description: String,
    val created_at: String,
    val updated_at: String,
    val profile_picture: String,
    val discountted_Price: String
)
