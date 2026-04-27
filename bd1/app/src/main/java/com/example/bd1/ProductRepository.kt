package com.example.bd1

import android.content.ContentValues
import android.content.Context

class ProductRepository(context: Context) {

    private val admin = AdminSQLiteOpenHelperx(
        context,
        AdminSQLiteOpenHelperx.DB_NAME,
        null,
        AdminSQLiteOpenHelperx.DB_VERSION
    )

    fun getNextCode(): Int {
        val db = admin.readableDatabase
        val cursor = db.rawQuery(
            "SELECT IFNULL(MAX(${AdminSQLiteOpenHelperx.COL_CODIGO}), 0) + 1 FROM ${AdminSQLiteOpenHelperx.TABLE_ARTICULOS}",
            null
        )
        return try {
            if (cursor.moveToFirst()) cursor.getInt(0) else 1
        } finally {
            cursor.close()
            db.close()
        }
    }

    fun saveProduct(codigo: Int?, descripcion: String, precio: Double, imagenUri: String?): Boolean {
        val db = admin.writableDatabase
        return try {
            val data = ContentValues().apply {
                if (codigo != null) put(AdminSQLiteOpenHelperx.COL_CODIGO, codigo)
                put(AdminSQLiteOpenHelperx.COL_DESCRIPCION, descripcion)
                put(AdminSQLiteOpenHelperx.COL_PRECIO, precio)
                put(AdminSQLiteOpenHelperx.COL_IMAGEN_URI, imagenUri)
            }
            db.insert(AdminSQLiteOpenHelperx.TABLE_ARTICULOS, null, data) != -1L
        } finally {
            db.close()
        }
    }

    fun updateProduct(codigo: Int, descripcion: String, precio: Double, imagenUri: String?): Boolean {
        val db = admin.writableDatabase
        return try {
            val data = ContentValues().apply {
                put(AdminSQLiteOpenHelperx.COL_DESCRIPCION, descripcion)
                put(AdminSQLiteOpenHelperx.COL_PRECIO, precio)
                put(AdminSQLiteOpenHelperx.COL_IMAGEN_URI, imagenUri)
            }
            db.update(
                AdminSQLiteOpenHelperx.TABLE_ARTICULOS,
                data,
                "${AdminSQLiteOpenHelperx.COL_CODIGO} = ?",
                arrayOf(codigo.toString())
            ) == 1
        } finally {
            db.close()
        }
    }

    fun deleteProduct(codigo: Int): Boolean {
        val db = admin.writableDatabase
        return try {
            db.delete(
                AdminSQLiteOpenHelperx.TABLE_ARTICULOS,
                "${AdminSQLiteOpenHelperx.COL_CODIGO} = ?",
                arrayOf(codigo.toString())
            ) == 1
        } finally {
            db.close()
        }
    }

    fun getByCode(codigo: Int): ProductItem? {
        val db = admin.readableDatabase
        val cursor = db.rawQuery(
            """
                SELECT ${AdminSQLiteOpenHelperx.COL_CODIGO},
                       ${AdminSQLiteOpenHelperx.COL_DESCRIPCION},
                       ${AdminSQLiteOpenHelperx.COL_PRECIO},
                       ${AdminSQLiteOpenHelperx.COL_IMAGEN_URI}
                FROM ${AdminSQLiteOpenHelperx.TABLE_ARTICULOS}
                WHERE ${AdminSQLiteOpenHelperx.COL_CODIGO} = ?
            """.trimIndent(),
            arrayOf(codigo.toString())
        )

        return try {
            if (!cursor.moveToFirst()) return null
            ProductItem(
                codigo = cursor.getInt(0),
                descripcion = cursor.getString(1),
                precio = cursor.getDouble(2),
                imagenUri = cursor.getString(3)
            )
        } finally {
            cursor.close()
            db.close()
        }
    }

    fun getAllProducts(): List<ProductItem> {
        val db = admin.readableDatabase
        val cursor = db.rawQuery(
            """
                SELECT ${AdminSQLiteOpenHelperx.COL_CODIGO},
                       ${AdminSQLiteOpenHelperx.COL_DESCRIPCION},
                       ${AdminSQLiteOpenHelperx.COL_PRECIO},
                       ${AdminSQLiteOpenHelperx.COL_IMAGEN_URI}
                FROM ${AdminSQLiteOpenHelperx.TABLE_ARTICULOS}
                ORDER BY ${AdminSQLiteOpenHelperx.COL_CODIGO} DESC
            """.trimIndent(),
            null
        )

        val items = mutableListOf<ProductItem>()
        try {
            while (cursor.moveToNext()) {
                items.add(
                    ProductItem(
                        codigo = cursor.getInt(0),
                        descripcion = cursor.getString(1),
                        precio = cursor.getDouble(2),
                        imagenUri = cursor.getString(3)
                    )
                )
            }
            return items
        } finally {
            cursor.close()
            db.close()
        }
    }
}
