package com.hc.subway_station_locator.app

object Constants {
    const val EXTRA_FOREGROUND_SERVICE_TITLE = "EXTRA_FOREGROUND_SERVICE_TITLE"
    const val EXTRA_FOREGROUND_SERVICE_CONTENT = "EXTRA_FOREGROUND_SERVICE_CONTENT"

    const val PATTERN_SUBWAY_STATION_FR_CODE = "([A-Za-z])?(\\d{1,3})(?:-(\\d))?"

    const val PATTERN_NORMAL_SUBWAY_STATION_FR_CODE = "\\d{1,3}"
    const val PATTERN_ALPHABET_SUBWAY_STATION_FR_CODE = "[A-Za-z]\\d{1,3}"
    const val PATTERN_DASH_SUBWAY_STATION_FR_CODE = "\\d{1,3}-\\d"
    const val PATTERN_ALPHABET_DASH_SUBWAY_STATION_FR_CODE = "[A-Za-z]\\d{1,3}-\\d"

    const val FR_CODE_NUMBER_OF_LINE_NUMBER_TWO_FIRST = 201
    const val FR_CODE_NUMBER_OF_LINE_NUMBER_TWO_LAST = 243

    const val KEY_MAX_DISTANCE = "MAX_DISTANCE"
}