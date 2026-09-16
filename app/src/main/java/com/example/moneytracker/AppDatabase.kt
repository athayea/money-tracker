package com.example.moneytracker

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * AppDatabase = pintu utama ke database lokal (SQLite via Room).
 *
 * @Database mendaftarkan:
 * - entities = tabel apa saja (di sini hanya Transaction)
 * - version = versi skema database (naikkan jika struktur tabel berubah)
 *
 * Pola SINGLETON dipakai (getInstance) supaya seluruh aplikasi memakai
 * SATU koneksi database yang sama — membuka banyak koneksi berulang & rawan error.
 */
@Database(entities = [Transaction::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // Room menyediakan implementasi DAO ini secara otomatis.
    abstract fun transactionDao(): TransactionDao

    companion object {

        // @Volatile: perubahan nilai langsung terlihat oleh semua thread.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Mengambil instance database. Jika belum ada, dibuat sekali saja.
         * synchronized -> aman meski dipanggil dari beberapa thread bersamaan.
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "money_tracker.db" // nama file database di perangkat
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}