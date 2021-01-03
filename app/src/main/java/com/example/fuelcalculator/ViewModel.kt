package com.example.fuelcalculator

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fuelcalculator.network.city.CityApi
import com.example.fuelcalculator.network.city.CityProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

//37.61556,55.75222
//30.31413,59.93863
//[[30.31413,59.93863],[37.61556,55.75222]]

enum class CityApiStatus{ LOADING, ERROR, DONE}

class ViewModel: ViewModel() {

    // The internal MutableLiveData String that stores the status of the most recent request
    private val _status = MutableLiveData<CityApiStatus>()

    // The external immutable LiveData for the request status String
    val status: LiveData<CityApiStatus>
        get() = _status

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

    private val _result = MutableLiveData<String>()
    val result: LiveData<String>
        get() = _result

    private val _eventResultReceived = MutableLiveData<Boolean>()
    val eventResultReceived: LiveData<Boolean>
        get() = _eventResultReceived

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    init {
        //todo как именно лучше инициализировать
        getCitiesProperties()
        _cars.value = getCars()
        _car.value = ""
        _result.value = ""
        _eventResultReceived.value = false
    }

    /**
     * Sets the value of the status LiveData to the Mars API status.
     */
    private fun getCitiesProperties() {
        coroutineScope.launch {
            val getPropertiesDeferred = CityApi.retrofitService.getCities()
            try {
                _status.value = CityApiStatus.LOADING
                val listResult = getPropertiesDeferred.await()
                _status.value = CityApiStatus.DONE
                _cities.value = listResult.results
                Timber.i("timber result - ${listResult.results}")

            } catch (e: Exception) {
                _status.value = CityApiStatus.ERROR
                _cities.value = ArrayList()
                Timber.e("timber error - ${e.message}")
            }
        }
    }

    private fun getCars(): Array<String>{
        //todo доработать инициализацию списка
        return arrayOf("Mercedes", "Audi", "Jeep", "Volkswagen", "BMW")
    }

    fun loadResult(){
        //todo запрос и формирование результата
        _result.value = "${_departureCity.value} -- ${_destinationCity.value} -- ${_car.value}"
        _eventResultReceived.value = true
    }

    fun setDeparture(value: CityProperty){
        _departureCity.value = value
    }

    fun setDestination(value: CityProperty){
        _destinationCity.value = value
    }

    fun setCar(value: String){
        _car.value = value
    }
}
