package com.seventeen.edtinp

import kotlinx.serialization.Serializable
import java.time.DayOfWeek

@Serializable
class DataClass (
    var classe: String,
    var currentDayOfWeek: Int,
    var currentWeekId: Int
)