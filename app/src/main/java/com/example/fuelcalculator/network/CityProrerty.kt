package com.example.fuelcalculator.network

class CityProperty (val cityId: Long,
                    val name: String,
                    val location: LocationProperty)

class LocationProperty(val latitude: Double,
                       val longitude: Double)