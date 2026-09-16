package com.example.moneytracker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moneytracker.AddTransactionActivity.Companion.EXTRA_ID
import com.example.moneytracker.AddTransactionActivity.Companion.EXTRA_JUDUL
import com.example.moneytracker.AddTransactionActivity.Companion.EXTRA_KATEGORI
import com.example.moneytracker.AddTransactionActivity.Companion.EXTRA_NOMINAL
import com.example.moneytracker.AddTransactionActivity.Companion.EXTRA_TANGGAL
import com.example.moneytracker.AddTransactionActivity.Companion.EXTRA_TIPE
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

class MainActivity : AppCompatActivity() {

    private lateinit var transactionDao: TransactionDao
    private lateinit var textPemasukan: TextView
    private lateinit var textSaldo: TextView
    private lateinit var textPengeluaran: TextView
    private lateinit var adapter: TransactionAdapter
    private lateinit var chipGroupFilter: ChipGroup

    // Menyimpan sumber LiveData yang sedang di-observe supaya bisa dilepas
    // (removeObservers) sebelum ganti ke sumber baru saat filter berubah.
    private var sumberSaatIni: LiveData<List<Transaction>>? = null

    // Launcher untuk membuka AddTransactionActivity & menerima hasilnya
    // (pengganti modern dari startActivityForResult yang lama)
    private val addLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        val data = result.data ?: return@registerForActivityResult

        when (result.resultCode) {

            // Form ditekan Simpan -> tambah / update transaksi
            Activity.RESULT_OK -> {

                val idEdit = data.getIntExtra(EXTRA_ID, 0)
                val judul = data.getStringExtra(EXTRA_JUDUL) ?: ""
                val nominal = data.getLongExtra(EXTRA_NOMINAL, 0)
                val tipe = data.getStringExtra(EXTRA_TIPE) ?: "Pengeluaran"
                val kategori = data.getStringExtra(EXTRA_KATEGORI) ?: "Lainnya"
                val tanggal = data.getLongExtra(
                    EXTRA_TANGGAL, System.currentTimeMillis()
                )

                val transaksi = Transaction(
                    id = idEdit,
                    title = judul,
                    amount = nominal,
                    type = tipe,
                    category = kategori,
                    date = tanggal
                )

                // CATATAN: RecyclerView SUDAH otomatis diperbarui lewat observer
                // LiveData di loadData() setiap kali data di database berubah.
                // Jangan tambahkan lagi manipulasi list manual + notifyItemInserted
                // di sini -- itu yang menyebabkan RecyclerView crash
                // ("Inconsistency detected") karena isi adapter jadi tidak sinkron
                // dengan yang benar-benar ditampilkan LiveData.
                lifecycleScope.launch {
                    if (idEdit == 0) {
                        transactionDao.insert(transaksi)
                    } else {
                        transactionDao.update(transaksi)
                    }
                }
            }

            // Form ditekan Hapus -> hapus transaksi
            AddTransactionActivity.RESULT_DELETE -> {

                val idHapus = data.getIntExtra(EXTRA_ID, 0)

                if (idHapus != 0) {
                    lifecycleScope.launch {
                        transactionDao.delete(
                            Transaction(
                                id = idHapus,
                                title = "",
                                amount = 0,
                                type = "",
                                category = "",
                                date = 0
                            )
                        )
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        transactionDao = AppDatabase.getInstance(this@MainActivity).transactionDao()

        // Siapkan RecyclerView: susun item vertikal + pasang adapter
        val recycler = findViewById<RecyclerView>(R.id.recyclerTransaksi)
        adapter = TransactionAdapter(emptyList(), onClick = { formEdit(it) })
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        textSaldo = findViewById(R.id.textSaldo)
        textPemasukan = findViewById(R.id.textPemasukan)
        textPengeluaran = findViewById(R.id.textPengeluaran)

        // Tombol Tambah -> buka form AddTransactionActivity
        findViewById<Button>(R.id.buttonTambah).setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            addLauncher.launch(intent)
        }

        setupFilter()

        // Tampilan awal: semua transaksi
        loadData(transactionDao.getAll())
    }

    private fun loadData(sumber: LiveData<List<Transaction>>) {
        sumberSaatIni?.removeObservers(this)
        sumberSaatIni = sumber
        sumber.observe(this) { daftar ->
            adapter.refresh(daftar)
            hitungSaldo(daftar)
        }
    }

    private fun hitungSaldo(daftar: List<Transaction>) {
        val pemasukan = daftar.filter { it.type == "Pemasukan" }
            .sumOf { it.amount }
        val pengeluaran = daftar.filter { it.type == "Pengeluaran" }
            .sumOf { it.amount }
        textPemasukan.text = formatRupiah(pemasukan)
        textPengeluaran.text = formatRupiah(pengeluaran)
        textSaldo.text = formatRupiah(pemasukan - pengeluaran)
    }

    private fun formEdit(transaksi: Transaction) {
        val intent = Intent(this, AddTransactionActivity::class.java).apply {
            putExtra(EXTRA_ID, transaksi.id)
            putExtra(EXTRA_JUDUL, transaksi.title)
            putExtra(EXTRA_NOMINAL, transaksi.amount)
            putExtra(EXTRA_TIPE, transaksi.type)
            putExtra(EXTRA_KATEGORI, transaksi.category)
            putExtra(EXTRA_TANGGAL, transaksi.date)
        }
        addLauncher.launch(intent)
    }

    // ==================================
    // FILTER PERIODE (chip di atas daftar transaksi)
    // ==================================

    private fun setupFilter() {
        chipGroupFilter = findViewById(R.id.chipGroupFilter)

        chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            when (checkedIds.firstOrNull()) {
                R.id.chipSemua -> loadData(transactionDao.getAll())
                R.id.bulanIni -> filterRentang(rentangBulanIni())
                R.id.bulanLalu -> filterRentang(rentangBulanLalu())
                R.id.tigaBulanTerakhir -> filterRentang(rentangTigaBulanTerakhir())
                R.id.pilihBulan -> pilihBulanDialog()
                R.id.rentang -> pilihRentangTanggalDialog()
            }
        }
    }

    private fun filterRentang(rentang: Pair<Long, Long>) {
        loadData(transactionDao.getByRange(rentang.first, rentang.second))
    }

    private fun awalHari(cal: Calendar) {
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
    }

    private fun rentangBulanIni(): Pair<Long, Long> {
        val awal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            awalHari(this)
        }
        return awal.timeInMillis to System.currentTimeMillis()
    }

    private fun rentangBulanLalu(): Pair<Long, Long> {
        val awal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            awalHari(this)
            add(Calendar.MONTH, -1)
        }
        val akhir = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            awalHari(this)
            add(Calendar.MILLISECOND, -1)
        }
        return awal.timeInMillis to akhir.timeInMillis
    }

    private fun rentangTigaBulanTerakhir(): Pair<Long, Long> {
        val awal = Calendar.getInstance().apply {
            add(Calendar.MONTH, -3)
        }
        return awal.timeInMillis to System.currentTimeMillis()
    }

    private fun pilihBulanDialog() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setCalendarConstraints(batasTidakMasaDepan())
            .build()

        picker.addOnPositiveButtonClickListener { pilihanUtc ->
            val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = pilihanUtc
            }
            val tahun = utc.get(Calendar.YEAR)
            val bulan = utc.get(Calendar.MONTH)

            val awal = Calendar.getInstance().apply {
                set(tahun, bulan, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val akhir = Calendar.getInstance().apply {
                timeInMillis = awal.timeInMillis
                add(Calendar.MONTH, 1)
                add(Calendar.MILLISECOND, -1)
            }

            filterRentang(awal.timeInMillis to akhir.timeInMillis)
        }

        picker.addOnNegativeButtonClickListener { chipGroupFilter.check(R.id.chipSemua) }
        picker.addOnCancelListener { chipGroupFilter.check(R.id.chipSemua) }

        picker.show(supportFragmentManager, "pilih_bulan")
    }

    private fun pilihRentangTanggalDialog() {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setCalendarConstraints(batasTidakMasaDepan())
            .build()

        picker.addOnPositiveButtonClickListener { pilihan ->
            val mulaiUtc = pilihan.first
            val sampaiUtc = pilihan.second

            if (mulaiUtc == null || sampaiUtc == null) {
                chipGroupFilter.check(R.id.chipSemua)
                return@addOnPositiveButtonClickListener
            }

            val awal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = mulaiUtc
            }
            val awalLokal = Calendar.getInstance().apply {
                set(
                    awal.get(Calendar.YEAR),
                    awal.get(Calendar.MONTH),
                    awal.get(Calendar.DAY_OF_MONTH)
                )
                awalHari(this)
            }

            val akhir = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = sampaiUtc
            }
            val akhirLokal = Calendar.getInstance().apply {
                set(
                    akhir.get(Calendar.YEAR),
                    akhir.get(Calendar.MONTH),
                    akhir.get(Calendar.DAY_OF_MONTH),
                    23, 59, 59
                )
                set(Calendar.MILLISECOND, 999)
            }

            filterRentang(awalLokal.timeInMillis to akhirLokal.timeInMillis)
        }

        picker.addOnNegativeButtonClickListener { chipGroupFilter.check(R.id.chipSemua) }
        picker.addOnCancelListener { chipGroupFilter.check(R.id.chipSemua) }

        picker.show(supportFragmentManager, "pilih_rentang")
    }
}
