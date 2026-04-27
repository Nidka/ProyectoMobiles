package com.example.bd1

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper

class AdminSQLiteOpenHelperx(
    context: Context?,
    name: String?,
    factory: CursorFactory?,
    version: Int
) :
    SQLiteOpenHelper(context, name, factory, version) {

    companion object {
        const val DB_NAME = "administracion"
        const val DB_VERSION = 2

        const val TABLE_ARTICULOS = "articulos"
        const val COL_CODIGO = "codigo"
        const val COL_DESCRIPCION = "descripcion"
        const val COL_PRECIO = "precio"
        const val COL_IMAGEN_URI = "imagen_uri"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_ARTICULOS(
                $COL_CODIGO INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_DESCRIPCION TEXT NOT NULL,
                $COL_PRECIO REAL NOT NULL,
                $COL_IMAGEN_URI TEXT
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_ARTICULOS ADD COLUMN $COL_IMAGEN_URI TEXT")
        }
    }
}
