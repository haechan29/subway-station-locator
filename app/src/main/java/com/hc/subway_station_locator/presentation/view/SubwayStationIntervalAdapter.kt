package com.hc.subway_station_locator.presentation.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hc.subway_station_locator.databinding.ListitemSubwayStationMiddleBinding
import com.hc.subway_station_locator.databinding.ListitemSubwayStationTransferBinding
import com.hc.subway_station_locator.domain.model.SubwayStationInterval
import com.hc.subway_station_locator.domain.model.SubwayStationMiddleVO
import com.hc.subway_station_locator.domain.model.SubwayStationTransferVO
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.pow
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
    }

    sealed class ViewHolder(binding: ViewDataBinding): RecyclerView.ViewHolder(binding.root)

    inner class SubwayTransferViewHolder(private val binding: ListitemSubwayStationTransferBinding): ViewHolder(binding) {
        fun bind(subwayStationTransfer: SubwayStationTransferVO) {
            binding.subwayStationTransfer = subwayStationTransfer
            binding.viewHolder = this@SubwayTransferViewHolder
        }

        fun toggleFoldState(subwayStationTransfer: SubwayStationTransferVO) {
            if (job != null) return

            job = scope.launch {
                if (subwayStationTransfer.isFolded) {
                    unfoldInterval(subwayStationTransfer)
                } else {
                    foldInterval(subwayStationTransfer)
                }

                subwayStationTransfer.isFolded = !subwayStationTransfer.isFolded

                job = null
            }
        }

        private suspend fun foldInterval(subwayStationTransfer: SubwayStationTransferVO) {
            (adapterPosition + 1 .. adapterPosition + subwayStationTransfer.subwayStationMiddles.size)
                .reversed()
                .onEach { i ->
                    submitList(currentList.toMutableList().apply { removeAt(i) })
                    notifyItemRemoved(i)

                    delay(0.05.seconds)
                }
        }

        private suspend fun unfoldInterval(subwayStationTransfer: SubwayStationTransferVO) {
            subwayStationTransfer.subwayStationMiddles
                .map { subwayStationMiddle -> SubwayStationMiddleVO(subwayStationMiddle) }
                .forEachIndexed { i, subwayStation ->
                    submitList(currentList.toMutableList().apply { add(adapterPosition + i + 1, subwayStation) })
                    notifyItemInserted(adapterPosition + i + 1)

                    delay(0.05.seconds)
                }
        }
    }

    inner class SubwayMiddleViewHolder(private val binding: ListitemSubwayStationMiddleBinding): ViewHolder(binding) {
        fun bind(subwayStationMiddle: SubwayStationMiddleVO) {
            binding.subwayStationMiddle = subwayStationMiddle
        }
    }

    private val scope = MainScope()
    private var job: Job? = null

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is SubwayStationTransferVO -> VIEW_TYPE_SUBWAY_STATION_TRANSFER
            is SubwayStationMiddleVO -> VIEW_TYPE_SUBWAY_STATION_MIDDLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SUBWAY_STATION_TRANSFER -> SubwayTransferViewHolder(
                ListitemSubwayStationTransferBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            else -> SubwayMiddleViewHolder(
                ListitemSubwayStationMiddleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is SubwayTransferViewHolder -> holder.bind(getItem(position) as SubwayStationTransferVO)
            is SubwayMiddleViewHolder -> holder.bind(getItem(position) as SubwayStationMiddleVO)
        }
    }
}