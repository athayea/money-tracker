package com.example.moneytracker

import androidx.room.Entity
import androidx.room.PrimaryKey

//Transaction = satu catatan transaksi keuangan

//@param id nomor unik transaksi
//@param title keterangan, nis, "Makan Siang"
//@param amount nominal dalam rupiah (angka bulat, nis, .25000)
//@param type tipe : "pemasukan" atau "pengeluaran"
//@param kategori kategori, nis. "Makan", "Transport", "Gaji"
//@param date Tanggal Transaksi (timestamp milidetik)
@Entity ("transactions")
data class Transaction (
    @PrimaryKey(true)
    var id: Int,
    val title : String,
    val amount: Long,
    val type: String,
    val category: String,
    val date: Long
    )