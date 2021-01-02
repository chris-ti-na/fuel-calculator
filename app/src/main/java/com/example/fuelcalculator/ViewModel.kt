package com.example.fuelcalculator

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fuelcalculator.network.CityApi
import com.example.fuelcalculator.network.CityProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.await
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

    private val _departureCity = MutableLiveData<String>()

    val departureCity: LiveData<String>
        get() = _departureCity

    private val _destinationCity = MutableLiveData<String>()
    val destinationCity: LiveData<String>
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
        _departureCity.value = ""
        _destinationCity.value = ""
        _car.value = ""
        _result.value = ""
        _eventResultReceived.value = false
    }

//    private fun getCities(): Array<String>{
//        //todo доработать инициализацию списка
//        return arrayOf("Moscow", "Tula", "Kaluga", "Tver", "Samara", "Saratov", "St. P", "Sochi")
//    }

    /**
     * Sets the value of the status LiveData to the Mars API status.
     */
    private fun getCitiesProperties() {
        coroutineScope.launch {
//            val getPropertiesDeferred = CityApi.retrofitService.getCities()
            val getPropertiesDeferred = CityApi.retrofitService.getGif()
            Timber.i("TIMBER olala")
            try {
                _status.value = CityApiStatus.LOADING
                Timber.i("TIMBER uuulala")
                val listResult = getPropertiesDeferred.await()
                Timber.i("!!!TIMBER VM getPropertiesDeferred: ${listResult.description}")

//                listResult.data?.let {
//                    _status.value = CityApiStatus.DONE
//                    _cities.value = it
//                } ?: let {
//                    _status.value = CityApiStatus.ERROR
//                    _cities.value = ArrayList()
//                }

            } catch (e: Exception) {
                _status.value = CityApiStatus.ERROR
                _cities.value = ArrayList()
            }
            Timber.i("---TIMBER VM cities: ${_cities.value?.size}")

        }

        Timber.i("///TIMBER 2 VM cities: ${_cities.value?.size}")
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

    fun setDeparture(value: String){
        _departureCity.value = value
    }

    fun setDestination(value: String){
        _destinationCity.value = value
    }

    fun setCar(value: String){
        _car.value = value
    }
}
