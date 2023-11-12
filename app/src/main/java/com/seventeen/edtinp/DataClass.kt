package com.seventeen.edtinp

import kotlinx.serialization.Serializable

@Serializable
class DataClass(
    var version: String,
    var classe: String,
    var currentDayOfWeek: Int,
    var currentWeekId: Int,
    var identifier: String,
    var dimensions: List<Int>,
    var treeId: String,
    var loggingState: Boolean
)