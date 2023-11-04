package com.seventeen.edtinp

import kotlinx.serialization.Serializable

@Serializable
class DataClass(
    var classe: String,
    var currentDayOfWeek: Int,
    var currentWeekId: Int,
    var identifiers: MutableMap<String, String>,
    var dimensions: List<Int>,
    var loggingState: Boolean
)