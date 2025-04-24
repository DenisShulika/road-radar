package com.denisshulika.road_radar.model

import com.denisshulika.road_radar.R

enum class NavigationItem(
    val icon: Int
) {
    Incidents(icon = R.drawable.home),
    MapRadar(icon = R.drawable.map),
    Profile(icon = R.drawable.person),
    Settings(icon = R.drawable.settings),
    About(icon = R.drawable.info),
    Signout(icon = R.drawable.logout);

    fun getTitle(localization: Map<String, String>): String {
        return when (this) {
            Incidents -> localization["drawer_incidents"]!!
            MapRadar -> localization["drawer_map_radar"]!!
            Profile -> localization["drawer_profile"]!!
            Settings -> localization["drawer_settings"]!!
            About -> localization["drawer_about"]!!
            Signout -> localization["drawer_signout"]!!
        }
    }
}