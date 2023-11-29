/**
 * Copyright (C)  - All Rights Reserved
 *
 * Copyright details are in the LICENSE.md file located in the root of this Android project.
 * Everything written in the LICENSE.md file applies on this file.
 *
 * Any unauthorized copying, editing, or publishing, even partial, of this file is strictly forbidden.
 *
 * Owner of this file, its content, and the copyright related : Paul Musial, paul.musial.dev@gmail.com
 */
package com.seventeen.edtinp

import kotlinx.serialization.Serializable

@Serializable
class DataClass(
    var version: String,
    var ecole: String,
    var currentDayOfWeek: Int,
    var currentWeekId: Int,
    var identifier: String,
    var dimensions: List<Int>,
    var treeId: String,
    var loggingState: Boolean
)