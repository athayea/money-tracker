package com.example.moneytracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(
    private var daftar: List<Transaction>,
    private val onClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val textJudul: TextView =
            view.findViewById(R.id.textJudul)

        val textKategori: TextView =
            view.findViewById(R.id.textKategori)

        val textTanggal: TextView =
            view.findViewById(R.id.textTanggal)

        val textNominal: TextView =
            view.findViewById(R.id.textNominal)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_transaction,
                parent,
                false
            )

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val transaksi = daftar[position]

        holder.textJudul.text = transaksi.title
        holder.textKategori.text = transaksi.category
        holder.textTanggal.text = formatTanggal(transaksi.date)

        // Tanda nominal
        val tanda = if (transaksi.type == "Pemasukan") {
            "+"
        } else {
            "-"
        }

        holder.textNominal.text =
            if (transaksi.type == "Pemasukan") {
                "+ ${formatRupiah(transaksi.amount)}"
            } else {
                "- ${formatRupiah(transaksi.amount)}"
            }

        // Warna nominal
        val warna = if (transaksi.type == "Pemasukan") {
            R.color.green
        } else {
            R.color.red
        }

        holder.textNominal.setTextColor(
            holder.itemView.context.getColor(warna)
        )

        // Klik seluruh item
        holder.itemView.setOnClickListener {
            onClick(transaksi)
        }
    }

    override fun getItemCount(): Int {
        return daftar.size
    }

    fun refresh(dataBaru: List<Transaction>) {
        daftar = dataBaru
        notifyDataSetChanged()
    }
}