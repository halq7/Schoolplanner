package com.future.schoolplanner.data

data class SchoolYear(
    val id: String,
    val name: String,
    val description: String = "",
    val startDate: String, // ISO date string
    val endDate: String    // ISO date string
)