package com.example.ecomer.model

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class Product(
    @get:Exclude
    var id: String = "",

    @get:PropertyName("tenSanPham")
    @set:PropertyName("tenSanPham")
    var tenSanPham: String = "",

    @get:PropertyName("loaiSanPham")
    @set:PropertyName("loaiSanPham")
    var loaiSanPham: String = "",

    @get:PropertyName("gia")
    @set:PropertyName("gia")
    var gia: String = "",

    @get:PropertyName("image")
    @set:PropertyName("image")
    var image: String = ""
) {
    // No-arg constructor required by Firebase Firestore
    constructor() : this("", "", "", "", "")

    fun toMap(): Map<String, Any> {
        return mapOf(
            "tenSanPham" to tenSanPham,
            "loaiSanPham" to loaiSanPham,
            "gia" to gia,
            "image" to image
        )
    }

    companion object {
        fun fromDocument(doc: DocumentSnapshot): Product {
            val name = doc.getString("tenSanPham") 
                ?: doc.getString("tên sản phẩm") 
                ?: doc.getString("name") 
                ?: ""
            val category = doc.getString("loaiSanPham") 
                ?: doc.getString("loại sản phẩm") 
                ?: doc.getString("category") 
                ?: ""
            val price = doc.get("gia")?.toString() 
                ?: doc.get("price")?.toString() 
                ?: ""
            val img = doc.getString("image") 
                ?: doc.getString("Image") 
                ?: doc.getString("imageUrl") 
                ?: ""

            return Product(
                id = doc.id,
                tenSanPham = name,
                loaiSanPham = category,
                gia = price,
                image = img
            )
        }
    }
}
