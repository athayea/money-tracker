package com.example.moneytracker

import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * format Tanggal = mengubah timestamp (Long milidetik) menjadi teks
 * tanggal & jam yang mudah dibaca.
 * Contoh: 1757142600000 -> "66 Sep 2026, 14:30"
*/

fun formatTanggal(millis: Long): String{
    val pola = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))
    return pola.format(Date(millis))
}

/** dhadotid, 06/09/26, 13.11 Hari 2: cegah pilih tanggal/jam di masa depan pada form
 * batasTidakMasaDepan = aturan untuk MaterialDatePicker agar tanggal
 * di MASA DEPAN tidak bisa dipilih (hanyg hari ini & sebelumnya).
 * Dipakai di form input (Hari 2) dan filter (Hari 4).
*/

fun batasTidakMasaDepan(): CalendarConstraints =
    CalendarConstraints.Builder()
        .setValidator(DateValidatorPointBackward.now())
        .build()