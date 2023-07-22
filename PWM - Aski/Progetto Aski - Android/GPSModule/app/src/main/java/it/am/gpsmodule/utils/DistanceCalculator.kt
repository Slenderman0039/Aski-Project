package it.am.gpsmodule.utils

import java.lang.Math.round

class DistanceCalculator{
    fun getDistanceBetweenPointsNew(
        latitude1: Double,
        longitude1: Double,
        latitude2: Double,
        longitude2: Double,
        unit: String
    ): Double {
        val theta = longitude1 - longitude2
        val distance = 60 * 1.1515 * (180 / Math.PI) * Math.acos(
            Math.sin(latitude1 * (Math.PI / 180)) * Math.sin(latitude2 * (Math.PI / 180)) +
                    Math.cos(latitude1 * (Math.PI / 180)) * Math.cos(latitude2 * (Math.PI / 180)) * Math.cos(
                theta * (Math.PI / 180)
            )
        )
         if (unit == "miles") {
            return round((distance * 100) / 100).toDouble()
        } else if (unit == "kilometers") {
           return (round((distance * 1.609344)*100).toDouble()/100)
        } else {
            return 0.0
        }
    }
}