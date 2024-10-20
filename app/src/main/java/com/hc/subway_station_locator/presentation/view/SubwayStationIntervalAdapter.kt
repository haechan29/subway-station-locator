package com.hc.subway_station_locator.presentation.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hc.subway_station_locator.app.utils.Utils.size
import com.hc.subway_station_locator.databinding.ListitemSubwayStationArrivalBinding
import com.hc.subway_station_locator.databinding.ListitemSubwayStationMiddleBinding
import com.hc.subway_station_locator.databinding.ListitemSubwayStationTransferBinding
import com.hc.subway_station_locator.domain.model.SubwayStationArrivalVO
import com.hc.subway_station_locator.domain.model.SubwayStationInterval
import com.hc.subway_station_locator.domain.model.SubwayStationMiddleVO
import com.hc.subway_station_locator.domain.model.SubwayStationTransferVO
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class SubwayStationIntervalAdapter: ListAdapter<SubwayStationInterval, SubwayStationIntervalAdapter.ViewHolder>(object: DiffUtil.ItemCallback<SubwayStationInterval>() {
    override fun areItemsTheSame(oldItem: SubwayStationInterval, newItem: SubwayStationInterval): Boolean {
        return oldItem.subwayStation == newItem.subwayStation
    }

    override fun areContentsTheSame(oldItem: SubwayStationInterval, newItem: SubwayStationInterval): Boolean {
        return oldItem is SubwayStationTransferVO && newItem is SubwayStationTransferVO && oldItem.isFolded == newItem.isFolded
    }
}) {

    companion object {
        private const val VIEW_TYPE_SUBWAY_STATION_TRANSFER = 0
        private const val VIEW_TYPE_SUBWAY_STATION_MIDDLE = 1
        private const val VIEW_TYPE_SUBWAY_STATION_ARRIVAL = 2
    }

    private val holder = mutableMapOf<String, List<SubwayStationInterval>>()

    sealed class ViewHolder(binding: ViewDataBinding): RecyclerView.ViewHolder(binding.root)

    inner class SubwayStationTransferViewHolder(private val binding: ListitemSubwayStationTransferBinding): ViewHolder(binding) {
        fun bind(subwayStationTransfer: SubwayStationTransferVO) {
            binding.subwayStationTransfer = subwayStationTransfer
            binding.viewHolder = this@SubwayStationTransferViewHolder
        }

        fun toggleFoldState(subwayStationTransfer: SubwayStationTransferVO) {

            subwayStationTransfer.toggleFoldState()

            val mutableList = currentList.toMutableList()

            if (holder[subwayStationTransfer.subwayStation.frCode] == null) {

                holder[subwayStationTransfer.subwayStation.frCode] = (0 until subwayStationTransfer.indicesOfSubwayStationMiddle.size).map {
                    mutableList.removeAt(adapterPosition + 1)
                }
            } else {

                holder[subwayStationTransfer.subwayStation.frCode]!!.reversed().forEach {
                    mutableList.add(adapterPosition + 1, it)
                }

                holder.remove(subwayStationTransfer.subwayStation.frCode)
            }

            submitList(mutableList)

            notifyItemRangeChanged(adapterPosition, adapterPosition + subwayStationTransfer.indicesOfSubwayStationMiddle.size)
        }
    }

    inner class SubwayStationMiddleViewHolder(private val binding: ListitemSubwayStationMiddleBinding): ViewHolder(binding) {
        fun bind(subwayStationMiddle: SubwayStationMiddleVO) {
            binding.subwayStationMiddle = subwayStationMiddle
        }
    }

    inner class SubwayStationArrivalViewHolder(private val binding: ListitemSubwayStationArrivalBinding): ViewHolder(binding) {
        fun bind(subwayStationArrival: SubwayStationArrivalVO) {
            binding.subwayStationArrival = subwayStationArrival
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is SubwayStationTransferVO -> VIEW_TYPE_SUBWAY_STATION_TRANSFER
            is SubwayStationMiddleVO -> VIEW_TYPE_SUBWAY_STATION_MIDDLE
            is SubwayStationArrivalVO -> VIEW_TYPE_SUBWAY_STATION_ARRIVAL
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SUBWAY_STATION_TRANSFER -> SubwayStationTransferViewHolder(
                ListitemSubwayStationTransferBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            VIEW_TYPE_SUBWAY_STATION_MIDDLE -> SubwayStationMiddleViewHolder(
                ListitemSubwayStationMiddleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            else -> SubwayStationArrivalViewHolder(
                ListitemSubwayStationArrivalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is SubwayStationTransferViewHolder -> holder.bind(getItem(position) as SubwayStationTransferVO)
            is SubwayStationMiddleViewHolder -> holder.bind(getItem(position) as SubwayStationMiddleVO)
            is SubwayStationArrivalViewHolder -> holder.bind(getItem(position) as SubwayStationArrivalVO)
        }
    }
}