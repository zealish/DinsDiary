package com.zealish.dinsdiary.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface DinsDestination {

    @Serializable
    data object Onboarding : DinsDestination
    @Serializable
    data object Home : DinsDestination

    @Serializable
    data object Period : DinsDestination

    @Serializable
    data object Calendar : DinsDestination


    @Serializable
    data object Fertility : DinsDestination
    @Serializable
    data object Insights : DinsDestination
    @Serializable
    data object Journal : DinsDestination

    @Serializable
    data object Reminders : DinsDestination

    @Serializable
    data object Privacy : DinsDestination

    @Serializable
    data object Profile : DinsDestination
}
