package com.hc.subway_station_locator.presentation.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.hc.subway_station_locator.databinding.FragmentSearchSubwayStationBinding
import com.hc.subway_station_locator.domain.model.SubwayStationVO
import com.hc.subway_station_locator.presentation.view_model.MainViewModel

class SearchSubwayStationFragment : Fragment() {

    private var _binding: FragmentSearchSubwayStationBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<MainViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchSubwayStationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        SubwayStationAdapter().apply {
            onItemClickListener = object: SubwayStationAdapter.OnItemClickListener {
                override fun onItemClick(subwayStation: SubwayStationVO) {
                    viewModel.finishSetSubwayStation(subwayStation)
                }
            }
        }.let {
            binding.recyclerViewSearch.adapter = it
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}