package com.pinmyhome.app.ui.components

import java.util.Date

data class BuyerDemand(
    val name: String,
    val phone: String,
    val minBudget: String,
    val maxBudget: String,
    val flatType: String,
    val area: String,
    val isExclusive: Boolean = false,
    val addedDate: Date = Date(),
    val matchingCount: Int = 0,
    val tag: String? = null,
)
