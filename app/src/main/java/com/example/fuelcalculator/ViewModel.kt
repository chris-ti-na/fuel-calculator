package com.example.fuelcalculator

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fuelcalculator.network.car.CarService.loadCars
import com.example.fuelcalculator.network.city.CityApi
import com.example.fuelcalculator.network.city.CityProperty
import com.example.fuelcalculator.network.distance.DistanceApi
import com.example.fuelcalculator.network.distance.createJsonRequestBody
import com.example.fuelcalculator.network.fuel.FuelService
import com.example.fuelcalculator.network.fuel.FuelService.loadPrices
import kotlinx.coroutines.*
import timber.log.Timber
import java.math.RoundingMode
import kotlin.collections.ArrayList

class ViewModel : ViewModel() {

    private val _cities = MutableLiveData<List<CityProperty>>()
    val cities: LiveData<List<CityProperty>>
        get() = _cities

    private val _cars = MutableLiveData<Map<String, Double>>()
    val cars: LiveData<Map<String, Double>>
        get() = _cars

    private val _departureCity = MutableLiveData<CityProperty>()
    val departureCity: LiveData<CityProperty>
        get() = _departureCity

    private val _destinationCity = MutableLiveData<CityProperty>()
    val destinationCity: LiveData<CityProperty>
        get() = _destinationCity

    private val _car = MutableLiveData<Pair<String, Double>>()
    val car: LiveData<Pair<String, Double>>
        get() = _car

    private val _distance = MutableLiveData<Double>()
    val distance: LiveData<Double>
        get() = _distance

    private val _costA92 = MutableLiveData<Double>()
    val costA92: LiveData<Double>
        get() = _costA92

    private val _costA95 = MutableLiveData<Double>()
    val costA95: LiveData<Double>
        get() = _costA95

    private val _costA98 = MutableLiveData<Double>()
    val costA98: LiveData<Double>
        get() = _costA98

    private val _costDT = MutableLiveData<Double>()
    val costDT: LiveData<Double>
        get() = _costDT


    private val _eventResultReceived = MutableLiveData<Boolean>()
    val eventResultReceived: LiveData<Boolean>
        get() = _eventResultReceived

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    init {
        getCitiesProperties()
        getCars()
        loadPrices()
    }

    private fun getCitiesProperties() {
        coroutineScope.launch {
            val getPropertiesDeferred = CityApi.retrofitService.getCities()
            try {
                val listResult = getPropertiesDeferred.await()
                _cities.value = listResult.results
                Timber.i("timber cities result - ${listResult.results?.size}")

            } catch (e: Exception) {
                _cities.value = ArrayList()
                Timber.e("timber cities error - ${e.message} ")
            }
        }
    }

    private fun getCars() {
        _cars.value = loadCars()
    }

    fun loadResult() {
        runBlocking { getDistance() }

        if (_car.value != null && _car.value?.second != null && _distance.value != null) {
            // consumption = average consumption [litres per 100 km] * (distance [km] / 100)
            val consumption = _car.value?.second!! * _distance.value?.div(100)!!
            // price = result consumption [litres] * fuel price [rubles per liter]
            _costA92.value = roundDouble2Decimal(consumption * FuelService.A92)
            _costA95.value = roundDouble2Decimal(consumption * FuelService.A95)
            _costA98.value = roundDouble2Decimal(consumption * FuelService.A98)
            _costDT.value = roundDouble2Decimal(consumption * FuelService.DT)
            _eventResultReceived.value = true
            Timber.i("timber 1 result costs A92: ${_costA92.value}, A95: ${_costA95.value}, A98: ${_costA98.value}, DT: ${_costDT.value}")
        }
        else{
            throw Exception("Couldn't calculate result costs")
        }
    }

    private suspend fun getDistance() = coroutineScope {
        async {
            try {
                val getPropertyDeferred = DistanceApi.retrofitService
                    .getDistance(createJsonRequestBody(_departureCity, _destinationCity))
                val listResult = getPropertyDeferred.await()
                _distance.value = listResult.distances[0][0]
                Timber.i("timber distance result - ${_distance.value}")

            } catch (e: Exception) {
                _distance.value = null
                Timber.e("timber distance error - ${e.message} ")
            }
        }
    }.await()

    private fun roundDouble2Decimal(d: Double?): Double?{
        return d?.toBigDecimal()?.setScale(2, RoundingMode.UP)?.toDouble()
    }

    fun setDeparture(value: CityProperty) {
        _departureCity.value = value
    }

    fun setDestination(value: CityProperty) {
        _destinationCity.value = value
    }

    fun setCar(carKey: String) {
        _car.value =
            Pair(carKey, _cars.value?.get(carKey) ?: error("There is no value for car $carKey"))
    }
}
