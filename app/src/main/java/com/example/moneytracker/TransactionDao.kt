package com.example.moneytracker

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
@Dao
interface TransactionDao {
    // CREATE: menambah satu transaksi ke tabel.
    // suspend = dijalankan di background (tidak membekukan tampilan).
    @Insert
    suspend fun insert(transaction: Transaction)

    // READ: ambil semua transaksi, terbaru di atas (urut tanggal menurun).
    // LiveData -> tampilan otomatis ikut berubah setiap data di DB berubah.
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAll(): LiveData<List<Transaction>>

    // READ (filter): ambil transaksi dalam RENTANG tanggal tertentu.
    // Parameter : mulai dan sampai adalah timestamp (Long).
    // Dipakai untuk fitur filter periode (bulan ini, bulan lalu, rentang, dll)
    // yang disambungkan ke UI pada Hari 4.
    @Query("SELECT * FROM transactions WHERE date BETWEEN :mulai AND :sampai ORDER BY date DESC")
    fun getByRange(mulai: Long, sampai: Long): LiveData<List<Transaction>>

    // UPDATE: mengubah transaksi yang sudah ada (dicocokkan lewat id).
    @Update
    suspend fun update(transaction: Transaction)

    // DELETE: menghapus satu transaksi.
    @Delete
    suspend fun delete(transaction: Transaction)
}
