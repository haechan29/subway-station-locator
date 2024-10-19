package com.hc.subway_station_locator.presentation.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hc.subway_station_locator.databinding.ListitemSubwayStationBinding
import com.hc.subway_station_locator.domain.model.SubwayStationVO

class SubwayStationAdapter: ListAdapter<SubwayStationVO, SubwayStationAdapter.ViewHolder>(object: DiffUtil.ItemCallback<SubwayStationVO>() {
    override fun areItemsTheSame(oldItem: SubwayStationVO, newItem: SubwayStationVO): Boolean {
        return oldItem.name == newItem.name && oldItem.lineNumber == newItem.lineNumber
    }

    override fun areContentsTheSame(oldItem: SubwayStationVO, newItem: SubwayStationVO): Boolean {
        return oldItem.name == newItem.name && oldItem.lineNumber == newItem.lineNumber
    }
}) {

    var onItemClickListener: OnItemClickListener? = null

    inner class ViewHolder(private val binding: ListitemSubwayStationBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SubwayStationVO) {
            binding.subwayStation = item

            binding.root.setOnClickListener {
                onItemClickListener?.onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListitemSubwayStationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    interface OnItemClickListener {
        fun onItemClick(subwayStation: SubwayStationVO)
    }
}