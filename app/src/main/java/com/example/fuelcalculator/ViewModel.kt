package com.example.fuelcalculator

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fuelcalculator.network.city.CityApi
import com.example.fuelcalculator.network.city.CityProperty
import com.example.fuelcalculator.network.distance.DistanceApi
import com.example.fuelcalculator.network.distance.createJsonRequestBody
import com.example.fuelcalculator.network.fuel.FuelService
import com.example.fuelcalculator.network.fuel.FuelService.loadPrices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.collections.ArrayList

enum class CityApiStatus { LOADING, ERROR, DONE }
enum class DistanceApiStatus { LOADING, ERROR, DONE }

class ViewModel : ViewModel() {

    // status of the most recent request to the City Api
    private val _cityApiStatus = MutableLiveData<CityApiStatus>()
    val cityApiStatus: LiveData<CityApiStatus>
        get() = _cityApiStatus

    // status of the most recent request to the Distance Api
    private val _distanceApiStatus = MutableLiveData<DistanceApiStatus>()
    val distanceApiStatus: LiveData<DistanceApiStatus>
        get() = _distanceApiStatus

    private val _cities = MutableLiveData<List<CityProperty>>()
    val cities: LiveData<List<CityProperty>>
        get() = _cities

    private val _cars = MutableLiveData<Array<String>>()
    val cars: LiveData<Array<String>>
        get() = _cars

    private val _departureCity = MutableLiveData<CityProperty>()
    val departureCity: LiveData<CityProperty>
        get() = _departureCity

    private val _destinationCity = MutableLiveData<CityProperty>()
    val destinationCity: LiveData<CityProperty>
        get() = _destinationCity

    private val _car = MutableLiveData<String>()
    val car: LiveData<String>
        get() = _car

    private val _distance = MutableLiveData<Double>()
    val distance: LiveData<Double>
        get() = _distance

    private val _result = MutableLiveData<String>()
    val result: LiveData<String>
        get() = _result

    private val _eventDistanceReceived = MutableLiveData<Boolean>()
    val eventDistanceReceived: LiveData<Boolean>
        get() = _eventDistanceReceived

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
                _cityApiStatus.value = CityApiStatus.LOADING
                val listResult = getPropertiesDeferred.await()
                _cityApiStatus.value = CityApiStatus.DONE
                _cities.value = listResult.results
                Timber.i("timber cities result - ${listResult.results?.size}")

            } catch (e: Exception) {
                _cityApiStatus.value = CityApiStatus.ERROR
                _cities.value = ArrayList()
                Timber.e("timber cities error - ${e.message} ")
            }
        }
    }

    private fun getCars() {
        //todo доработать инициализацию списка
        _cars.value = arrayOf("Mercedes", "Audi", "Jeep", "Volkswagen", "BMW")
    }

    fun loadResult() {
//        todo запрос и формирование результата
//         загрузить расстояние (done)
//         загрузить данные о машине
//         получить стоимость топлива (done)
//         рассчитать результат
        getDistance()
    }

    private fun getDistance() {
        coroutineScope.launch {
            val getPropertyDeferred =
                DistanceApi.retrofitService.getDistance(
                    createJsonRequestBody(departureCity, destinationCity)
                )
            try {
                _distanceApiStatus.value = DistanceApiStatus.LOADING
                val listResult = getPropertyDeferred.await()
                _distanceApiStatus.value = DistanceApiStatus.DONE
                _distance.value = listResult.distances[0][0]
                _eventDistanceReceived.value = true
                Timber.i("timber distance result - ${_distance.value}")

            } catch (e: Exception) {
                _distanceApiStatus.value = DistanceApiStatus.ERROR
                _distance.value = null
                _eventDistanceReceived.value = false
                Timber.e("timber distance error - ${e.message} ")
            }
        }
    }

    fun setDeparture(value: CityProperty) {
        _departureCity.value = value
    }

    fun setDestination(value: CityProperty) {
        _destinationCity.value = value
    }

    fun setCar(value: String) {
        _car.value = value
    }
}
