package com.example.moneytracker

import android.app.Activity
import android.content.Intent
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat

class AddTransactionActivity : AppCompatActivity() {

    private var idEdit = 0
    private var tanggalDipilih: Long = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        // ==============================
        // PANAH BACK DI ACTION BAR
        // ==============================
        // Menampilkan tombol panah kembali di sebelah judul ("Tambah
        // Transaksi" / "Edit Transaksi"). Ditangani lewat onSupportNavigateUp().
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // ==============================
        // HUBUNGKAN VIEW DENGAN XML
        // ==============================

        val editKeterangan =
            findViewById<EditText>(R.id.editKeterangan)

        val editNominal =
            findViewById<EditText>(R.id.editNominal)

        val groupTipe =
            findViewById<RadioGroup>(R.id.groupTipe)

        val spinnerKategori =
            findViewById<Spinner>(R.id.spinnerKategori)

        val buttonPilihTanggal =
            findViewById<Button>(R.id.buttonPilihTanggal)

        val buttonSimpan =
            findViewById<Button>(R.id.buttonSimpan)

        val buttonHapus =
            findViewById<Button>(R.id.buttonDelete)

        val textTanggalDipilih =
            findViewById<TextView>(R.id.textTanggalDipilih)

        // ==============================
        // CEK MODE TAMBAH / EDIT
        // ==============================

        idEdit = intent.getIntExtra(EXTRA_ID, 0)

        if (idEdit == 0) {

            // MODE TAMBAH
            title = "Tambah Transaksi"

            // Tombol hapus tidak diperlukan
            buttonHapus.visibility = View.GONE

            tanggalDipilih = System.currentTimeMillis()

        } else {

            // MODE EDIT
            title = "Edit Transaksi"

            // Tampilkan tombol hapus
            buttonHapus.visibility = View.VISIBLE

            // Ambil data transaksi lama
            editKeterangan.setText(
                intent.getStringExtra(EXTRA_JUDUL) ?: ""
            )

            editNominal.setText(
                formatRupiah(
                    intent.getLongExtra(
                        EXTRA_NOMINAL,
                        0
                    )
                )
            )

            val tipe =
                intent.getStringExtra(EXTRA_TIPE)

            if (tipe == "Pemasukan") {

                groupTipe.check(
                    R.id.radioPemasukan
                )

            } else {

                groupTipe.check(
                    R.id.radioPengeluaran
                )
            }

            // Ambil kategori transaksi lama & pilihkan di Spinner
            // (sebelumnya tidak pernah di-set, jadi selalu reset ke item pertama)
            val kategoriLama =
                intent.getStringExtra(EXTRA_KATEGORI)

            if (kategoriLama != null) {

                val daftarKategori =
                    resources.getStringArray(R.array.daftar_kategori)

                val indexKategori =
                    daftarKategori.indexOf(kategoriLama)

                if (indexKategori >= 0) {
                    spinnerKategori.setSelection(indexKategori)
                }
            }

            // Ambil tanggal transaksi lama
            tanggalDipilih =
                intent.getLongExtra(
                    EXTRA_TANGGAL,
                    System.currentTimeMillis()
                )
        }

        // Tampilkan tanggal
        textTanggalDipilih.text =
            formatTanggal(tanggalDipilih)

        // ==============================
        // PILIH TANGGAL
        // ==============================

        buttonPilihTanggal.setOnClickListener {

            pilihTanggalDanJam { millis ->

                tanggalDipilih = millis

                textTanggalDipilih.text =
                    formatTanggal(millis)
            }
        }

        // ==============================
        // SIMPAN / UPDATE
        // ==============================

        buttonSimpan.setOnClickListener {

            val keterangan =
                editKeterangan.text
                    .toString()
                    .trim()

            val nominalText =
                editNominal.text
                    .toString()
                    .trim()
                    .replace("Rp", "")
                    .replace(".", "")
                    .replace(",", "")
                    .trim()

            // Cek input kosong
            if (
                keterangan.isEmpty() ||
                nominalText.isEmpty()
            ) {

                Toast.makeText(
                    this,
                    "Keterangan & nominal wajib diisi",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            // Ubah nominal menjadi Long
            val nominal =
                nominalText.toLongOrNull()

            if (
                nominal == null ||
                nominal <= 0
            ) {

                Toast.makeText(
                    this,
                    "Nominal tidak valid",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            // Tipe transaksi
            val tipe =
                if (
                    groupTipe.checkedRadioButtonId ==
                    R.id.radioPemasukan
                ) {
                    "Pemasukan"
                } else {
                    "Pengeluaran"
                }

            // Kategori
            val kategori =
                spinnerKategori.selectedItem
                    .toString()

            // ==============================
            // KIRIM HASIL UPDATE / TAMBAH
            // ==============================

            val hasil =
                Intent().apply {

                    putExtra(
                        EXTRA_ID,
                        idEdit
                    )

                    putExtra(
                        EXTRA_JUDUL,
                        keterangan
                    )

                    putExtra(
                        EXTRA_NOMINAL,
                        nominal
                    )

                    putExtra(
                        EXTRA_TIPE,
                        tipe
                    )

                    putExtra(
                        EXTRA_KATEGORI,
                        kategori
                    )

                    putExtra(
                        EXTRA_TANGGAL,
                        tanggalDipilih
                    )
                }

            setResult(
                Activity.RESULT_OK,
                hasil
            )

            finish()
        }

        // ==============================
        // HAPUS TRANSAKSI
        // ==============================

        buttonHapus.setOnClickListener {

            if (idEdit != 0) {

                val hasil =
                    Intent().apply {

                        putExtra(
                            EXTRA_ID,
                            idEdit
                        )
                    }

                setResult(
                    RESULT_DELETE,
                    hasil
                )

                finish()
            }
        }
    }

    // ==================================
    // PILIH TANGGAL DAN JAM
    // ==================================

    private fun pilihTanggalDanJam(
        onSelesai: (Long) -> Unit
    ) {

        val datePicker =
            MaterialDatePicker.Builder
                .datePicker()
                .setSelection(
                    tanggalDipilih
                )
                .setCalendarConstraints(
                    batasTidakMasaDepan()
                )
                .build()

        datePicker.addOnPositiveButtonClickListener {
                pilihanTanggalUtc ->

            val utc =
                Calendar.getInstance(
                    TimeZone.getTimeZone("UTC")
                )

            utc.timeInMillis =
                pilihanTanggalUtc

            val awal =
                Calendar.getInstance().apply {
                    timeInMillis =
                        tanggalDipilih
                }

            val timePicker =
                MaterialTimePicker.Builder()
                    .setTimeFormat(
                        TimeFormat.CLOCK_24H
                    )
                    .setHour(
                        awal.get(
                            Calendar.HOUR_OF_DAY
                        )
                    )
                    .setMinute(
                        awal.get(
                            Calendar.MINUTE
                        )
                    )
                    .build()

            timePicker.addOnPositiveButtonClickListener {

                val cal =
                    Calendar.getInstance()

                cal.set(
                    utc.get(Calendar.YEAR),
                    utc.get(Calendar.MONTH),
                    utc.get(Calendar.DAY_OF_MONTH),
                    timePicker.hour,
                    timePicker.minute,
                    0
                )

                cal.set(
                    Calendar.MILLISECOND,
                    0
                )

                val sekarang =
                    System.currentTimeMillis()

                if (
                    cal.timeInMillis >
                    sekarang
                ) {

                    Toast.makeText(
                        this,
                        "Waktu tidak boleh masa depan",
                        Toast.LENGTH_SHORT
                    ).show()

                    onSelesai(
                        sekarang
                    )

                } else {

                    onSelesai(
                        cal.timeInMillis
                    )
                }
            }

            timePicker.show(
                supportFragmentManager,
                "pemilih_jam"
            )
        }

        datePicker.show(
            supportFragmentManager,
            "pemilih_tanggal"
        )
    }

    // ==================================
    // PANAH BACK DIKLIK
    // ==================================
    // Dipanggil saat tombol panah di action bar ditekan.
    // Perilakunya disamakan dengan tombol back fisik/gesture:
    // activity ditutup tanpa mengirim hasil (RESULT_CANCELED),
    // jadi tidak ada transaksi yang tersimpan/berubah.

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    // ==================================
    // CONSTANT
    // ==================================

    companion object {

        const val EXTRA_ID =
            "extra_id"

        const val EXTRA_JUDUL =
            "extra_judul"

        const val EXTRA_NOMINAL =
            "extra_nominal"

        const val EXTRA_TIPE =
            "extra_tipe"

        const val EXTRA_KATEGORI =
            "extra_kategori"

        const val EXTRA_TANGGAL =
            "extra_tanggal"

        // Hasil khusus untuk delete
        const val RESULT_DELETE = 300
    }
}