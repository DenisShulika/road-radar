package com.denisshulika.road_radar.model

import com.denisshulika.road_radar.R

enum class NavigationItem(
    val title : String,
    val icon : Int
) {
    Incidents(
        icon = R.drawable.home,
        title = "Incidents"
    ),
    Map(
        icon = R.drawable.map,
        title = "Map Radar"
    ),
    Profile(
        icon = R.drawable.person,
        title = "Profile"
    ),
    Settings(
        icon = R.drawable.settings,
        title = "Settings"
    ),
    About(
        icon = R.drawable.info,
        title = "About"
    ),
    Signout(
        icon = R.drawable.logout,
        title = "Sign Out"
    )
}