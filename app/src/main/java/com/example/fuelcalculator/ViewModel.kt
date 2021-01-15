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

    private val _result = MutableLiveData<DoubleArray>()
    val result: LiveData<DoubleArray>
        get() = _result

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
//        todo запрос и формирование результата
//         загрузить расстояние (done)
//         загрузить данные о машине (done)
//         получить стоимость топлива (done)
//         рассчитать результат

        runBlocking { getDistance() }

        if (_car.value != null && _car.value?.second != null && _distance.value != null) {
            // consumption = average consumption [litres per 100 km] * (distance [km] / 100)
            val consumption = _car.value?.second!! * _distance.value?.div(100)!!
            // price = result consumption [litres] * fuel price [rubles per liter]
            val priceA92 = consumption * FuelService.A92
            val priceA95 = consumption * FuelService.A95
            val priceA98 = consumption * FuelService.A98
            val priceDT = consumption * FuelService.DT
            _result.value = doubleArrayOf(priceA92, priceA95, priceA98, priceDT)
            _eventResultReceived.value = true
        }
        else{
            throw Exception("Couldn't calculate result value")
        }
        Timber.i("timber result ${result.value?.get(0)}")
    }

    private suspend fun getDistance() = coroutineScope {
        async {
            try {
                val getPropertyDeferred = DistanceApi.retrofitService
                    .getDistance(createJsonRequestBody(departureCity, destinationCity))
                val listResult = getPropertyDeferred.await()
                _distance.value = listResult.distances[0][0]
                Timber.i("timber distance result - ${_distance.value}")

            } catch (e: Exception) {
                _distance.value = null
                Timber.e("timber distance error - ${e.message} ")
            }
        }
    }.await()

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
