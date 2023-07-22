package it.am.gpsmodule.map.navbar.owncar.addcar.brandadapter



import android.view.LayoutInflater
import android.view.View

import android.view.ViewGroup
import android.widget.Adapter

import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView
import it.am.gpsmodule.R
import it.am.gpsmodule.databinding.CardviewBrandAddowncarBinding
import it.am.gpsmodule.map.navbar.owncar.addcar.BrandModel


class BrandAdapter(
    private val brandList: ArrayList<BrandModel>,
    private val listener: (BrandModel, Int) -> Unit
) :
    RecyclerView.Adapter<BrandAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = CardviewBrandAddowncarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(brandList[position])
        holder.itemView.setOnClickListener { listener(brandList[position], position) }
    }

    override fun getItemCount(): Int {
        return brandList.size
    }

    class ViewHolder(var itemBinding: CardviewBrandAddowncarBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bindItem(car: BrandModel) {
            itemBinding.image.setImageResource(car.image)
            itemBinding.name.text = car.name
        }
    }
}