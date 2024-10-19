package com.hc.subway_station_locator.app.utils

import android.location.Location
import androidx.annotation.ColorRes
import com.hc.subway_station_locator.R
import com.hc.subway_station_locator.app.Constants
import com.hc.subway_station_locator.app.Application
import com.hc.subway_station_locator.domain.model.SubwayStationVO
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.util.PriorityQueue

object SubwayStationUtils {
    private const val FILE_NAME_SUBWAY_STATIONS = "subway_stations.json"

    val subwayStations: List<SubwayStationVO> by lazy { loadStationLocationsFromAssets() }
    private val subwayStationNameMap by lazy { subwayStations.groupBy { it.name } }
    private val subwayStationFrCodeMap by lazy { subwayStations.associateBy { it.frCode } }

    fun getNearbySubwayStation(currentLocation: Location, subwayStations: List<SubwayStationVO>): List<SubwayStationVO> {
        return subwayStations.map { subwayStation ->
            subwayStation to LocationUtils.getDistanceBetween(currentLocation.latitude, currentLocation.longitude, subwayStation.latitude, subwayStation.longitude)
        }.sortedBy { (_, distance) ->
            distance
        }.map { (subwayStation, _) ->
            subwayStation
        }.let {
            listOf(it[0], it[1])
        }.sortedBy {
            subwayStations.indexOf(it)
        }
    }

    fun getShortestSubwayStationRoute(departureSubwayStation: SubwayStationVO, arrivalSubwayStation: SubwayStationVO): List<SubwayStationVO> {
        val queue = PriorityQueue<Triple<List<SubwayStationVO>, Int, Int>>(compareBy ({ it.second }, { it.third })).apply {
            add(Triple(listOf(departureSubwayStation), 0, 0))
        }

        while (queue.isNotEmpty()) {
            val (currentRoute, currentTransferCount, currentDistance) = queue.remove()
            val currentSubwayStation = currentRoute.last()

            if (currentSubwayStation.name == arrivalSubwayStation.name) {
                return currentRoute
            }

            subwayStationNameMap[currentSubwayStation.name]!!
                .flatMap { subwayStation -> getAdjacentSubwayStations(subwayStation) }
                .filterNot { adjacentSubwayStation -> adjacentSubwayStation in currentRoute }
                .forEach { nextSubwayStation ->
                    val isTransferred = departureSubwayStation != currentSubwayStation && currentSubwayStation.lineNumber != nextSubwayStation.lineNumber

                    queue.add(
                        Triple(
                            currentRoute + nextSubwayStation,
                            currentTransferCount + if (isTransferred) 1 else 0,
                            currentDistance + 1 + if (isTransferred) 1 else 0,
                        )
                    )
                }
        }

        throw Exception("출발역과 도착역 사이의 경로를 찾지 못함. 출발역: ${departureSubwayStation.name}, 도착역: ${arrivalSubwayStation.name}")
    }

    fun getSubwayStationLineNumberColor(lineNumber: String): Int {
        return SubwayStationLineNumber.entries.first { it.nameInKorean == lineNumber }.color
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun loadStationLocationsFromAssets(): List<SubwayStationVO> {
        return Application.context.assets.open(FILE_NAME_SUBWAY_STATIONS)
            .let { Json.decodeFromStream<List<SubwayStationVO>>(it) }
    }

    private fun getAdjacentSubwayStations(subwayStation: SubwayStationVO): List<SubwayStationVO> {
        return FrCode(subwayStation.frCode).getAdjacentSubwayStations()
    }

    enum class SubwayStationLineNumber(val nameInKorean: String, @ColorRes val color: Int) {
        LINE_NUMBER_1("01호선", R.color.subway_staion_line_number_color_line_1),
        LINE_NUMBER_2("02호선", R.color.subway_staion_line_number_color_line_2),
        LINE_NUMBER_3("03호선", R.color.subway_staion_line_number_color_line_3),
        LINE_NUMBER_4("04호선", R.color.subway_staion_line_number_color_line_4),
        LINE_NUMBER_5("05호선", R.color.subway_staion_line_number_color_line_5),
        LINE_NUMBER_6("06호선", R.color.subway_staion_line_number_color_line_6),
        LINE_NUMBER_7("07호선", R.color.subway_staion_line_number_color_line_7),
        LINE_NUMBER_8("08호선", R.color.subway_staion_line_number_color_line_8),
        LINE_NUMBER_9("09호선", R.color.subway_staion_line_number_color_line_9),
        LINE_GYENGGANG("경강선", R.color.subway_staion_line_number_color_line_gyenggang),
        LINE_GYEONGUI("경의선", R.color.subway_staion_line_number_color_line_gyeongui),
        LINE_GYEONGCHUN("경춘선", R.color.subway_staion_line_number_color_line_gyeongchun),
        LINE_AIRPORT_RAILLOAD_EXPRESS("공항철도", R.color.subway_staion_line_number_color_line_airport_railload_express),
        LINE_GIMPO_GOLD("김포골드라인", R.color.subway_staion_line_number_color_line_gimpo_gold),
        LINE_SEOHAE("서해선", R.color.subway_staion_line_number_color_line_seohae),
        LINE_SUIN_BUNDANG("수인분당선", R.color.subway_staion_line_number_color_line_suin_bundang),
        LINE_SHIN_BUNDANG("신분당선", R.color.subway_staion_line_number_color_line_shin_bundang),
        LINE_LRT_YONGIN("용인경전철", R.color.subway_staion_line_number_color_line_lrt_yongin),
        LINE_LRT_UI_SINSEOL("우이신설경전철", R.color.subway_staion_line_number_color_line_lrt_sinseol),
        LINE_LRT_UIJEONGBU("의정부경전철", R.color.subway_staion_line_number_color_line_lrt_uijeongbu),
        LINE_INCHEON("인천선", R.color.subway_staion_line_number_color_line_incheon),
        LINE_INCHEON_2("인천2호선", R.color.subway_staion_line_number_color_line_incheon_2),
    }

    class FrCode(frCodeString: String) {
        companion object {
            private val regexPattern = Constants.PATTERN_SUBWAY_STATION_FR_CODE.toRegex()
        }

        private val prefix: String?
        private val number: Int
        private val suffix: Int?

        private val pattern: Pattern

        init {
            val destructured = regexPattern.matchEntire(frCodeString)!!.destructured

            prefix = destructured.component1().ifEmpty { null }
            number = destructured.component2().toInt()
            suffix = destructured.component3().let { if (it.isEmpty()) null else it.toInt() }

            pattern = Pattern.entries.first { it.matches(frCodeString) }
        }

        override fun toString(): String {
            return StringBuilder().apply {
                prefix?.let{ append(it) }
                append(number)
                suffix?.let { append("-").append(it) }
            }.toString()
        }

        fun getAdjacentSubwayStations(): List<SubwayStationVO> {
            return getAdjacentSubwayStationFrCodes().mapNotNull { subwayStationFrCodeMap[it] }
        }

        private fun getAdjacentSubwayStationFrCodes(): List<String> {
            if (pattern == Pattern.NORMAL && number == Constants.FR_CODE_NUMBER_OF_LINE_NUMBER_TWO_FIRST) {
                return listOf("${number + 1}", "${Constants.FR_CODE_NUMBER_OF_LINE_NUMBER_TWO_LAST}")
            }

            if (pattern == Pattern.NORMAL && number == Constants.FR_CODE_NUMBER_OF_LINE_NUMBER_TWO_LAST) {
                return listOf("${number - 1}", "${Constants.FR_CODE_NUMBER_OF_LINE_NUMBER_TWO_FIRST}")
            }

            return when (pattern) {
                Pattern.NORMAL -> { listOf("$number-1", "${number - 1}", "${number + 1}") }
                Pattern.ALPHABET -> { listOf("$prefix$number-1", "$prefix${number - 1}", "$prefix${number + 1}") }
                Pattern.DASH -> { if (suffix == 1) listOf("$number", "$number-${suffix + 1}") else listOf("$number-${suffix!! - 1}", "$number-${suffix + 1}") }
                Pattern.ALPHABET_DASH -> { if (suffix == 1) listOf("$prefix$number", "$prefix$number-${suffix + 1}") else listOf("$prefix$number-${suffix!! - 1}", "$prefix$number-${suffix + 1}") }
            }
        }

        enum class Pattern(private val regex: Regex) {
            NORMAL(Constants.PATTERN_NORMAL_SUBWAY_STATION_FR_CODE.toRegex()),
            ALPHABET(Constants.PATTERN_ALPHABET_SUBWAY_STATION_FR_CODE.toRegex()),
            DASH(Constants.PATTERN_DASH_SUBWAY_STATION_FR_CODE.toRegex()),
            ALPHABET_DASH(Constants.PATTERN_ALPHABET_DASH_SUBWAY_STATION_FR_CODE.toRegex());

            fun matches(string: String): Boolean {
                return regex.matches(string)
            }
        }
    }
}