package com.seventeen.edtinp

import kotlinx.serialization.Serializable

@Serializable
class DataClass(
    var classe: String,
    var currentDayOfWeek: Int,
    var currentWeekId: Int
)