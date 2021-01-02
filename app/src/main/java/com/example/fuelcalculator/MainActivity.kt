package com.example.fuelcalculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.fuelcalculator.databinding.ActivityMainBinding
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: ViewModel
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProviders.of(this).get(ViewModel::class.java)
        binding.viewModel = viewModel

        viewModel.cities.observe(this, Observer { cities ->
            val cityAdapter = ArrayAdapter(this, android.R.layout.select_dialog_item, cities)
            binding.departureTextView.setAdapter(cityAdapter)
            binding.destinationTextView.setAdapter(cityAdapter)

            binding.departureTextView.setOnItemClickListener { parent, _, position, id ->
                val selectedItem = parent.getItemAtPosition(position).toString()
                viewModel.setDeparture(selectedItem)
            }

            binding.destinationTextView.setOnItemClickListener { parent, _, position, id ->
                val selectedItem = parent.getItemAtPosition(position).toString()
                viewModel.setDestination(selectedItem)
            }
        })

        viewModel.cars.observe(this, Observer { cars ->
            binding.carTextView.setAdapter(ArrayAdapter(this, android.R.layout.select_dialog_item, cars))

            binding.carTextView.setOnItemClickListener { parent, _, position, id ->
                val selectedItem = parent.getItemAtPosition(position).toString()
                viewModel.setCar(selectedItem)
            }
        })

        viewModel.eventResultReceived.observe(this, Observer { received ->
            if (received){
                binding.resultText.text = viewModel.result.value
            }
        })
    }

}