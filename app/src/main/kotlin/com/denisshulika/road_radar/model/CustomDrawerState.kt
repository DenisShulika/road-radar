package com.denisshulika.road_radar.model

enum class CustomDrawerState {
    Opened,
    Closed
}

fun CustomDrawerState.isOpened() : Boolean {
    return this.name == "Opened"
}

fun CustomDrawerState.opposite() : CustomDrawerState {
    return if (isOpened()) CustomDrawerState.Closed
    else CustomDrawerState.Opened
}