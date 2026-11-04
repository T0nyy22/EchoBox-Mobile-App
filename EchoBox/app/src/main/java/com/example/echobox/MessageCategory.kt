package com.example.echobox

object MessageCategory {

    const val ALL = "All"
    const val PERSONAL = "Personal"
    const val GOALS = "Goals"
    const val MEMORIES = "Memories"
    const val FAMILY = "Family"
    const val TRAVEL = "Travel"
    const val WORK = "Work"

    val categories = listOf(
        PERSONAL,
        GOALS,
        MEMORIES,
        FAMILY,
        TRAVEL,
        WORK
    )

    val filterCategories = listOf(ALL) + categories
}