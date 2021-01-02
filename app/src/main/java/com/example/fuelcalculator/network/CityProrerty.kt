package com.example.fuelcalculator.network

import com.squareup.moshi.Json

class CityProperty (val cityId: Long,
                    val name: String)

class LocationProperty(latitude: Long,
                       longitude: Long)

data class GifDetails(
    val id: Long?,
    val description: String?,
    val gifUrl: String?,
    val width: String?,
    val height: String?
)