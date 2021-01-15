package com.example.fuelcalculator

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.fuelcalculator.databinding.ActivityMainBinding
import com.example.fuelcalculator.network.city.CityProperty
import timber.log.Timber
import java.util.*

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
            val cityAdapter = CitiesAdapter(this, android.R.layout.select_dialog_item, cities)
            binding.departureTextView.setAdapter(cityAdapter)
            binding.destinationTextView.setAdapter(cityAdapter)

            binding.departureTextView.setOnItemClickListener { parent, _, position, _ ->
                val selectedItem = parent.adapter.getItem(position) as CityProperty
                viewModel.setDeparture(selectedItem)
                binding.departureTextView.setText(selectedItem.name)
            }

            binding.destinationTextView.setOnItemClickListener { parent, _, position, _ ->
                val selectedItem = parent.adapter.getItem(position) as CityProperty
                viewModel.setDestination(selectedItem)
                binding.destinationTextView.setText(selectedItem.name)
            }
        })

        viewModel.cars.observe(this, Observer { cars ->
            binding.carTextView.setAdapter(
                ArrayAdapter(this, android.R.layout.select_dialog_item, cars.keys.toList())
            )

            binding.carTextView.setOnItemClickListener { parent, _, position, _ ->
                val selectedItem = parent.getItemAtPosition(position).toString()
                viewModel.setCar(selectedItem)
            }
        })

        viewModel.eventResultReceived.observe(this, Observer { received ->
            if (received) {
                binding.resultText.text = viewModel.result.value?.get(0).toString()
            }
        })
    }

    inner class CitiesAdapter(
        context: Context,
        @LayoutRes private val layoutResource: Int,
        private val allCities: List<CityProperty>
    ) : ArrayAdapter<CityProperty>(context, layoutResource, allCities),
        Filterable {
        private var mCities: List<CityProperty> = allCities

        override fun getCount(): Int {
            return mCities.size
        }

        override fun getItem(position: Int): CityProperty? {
            return mCities[position]
        }

        override fun getItemId(position: Int): Long {
            return mCities[position].cityId
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view: TextView = convertView as TextView? ?: LayoutInflater.from(context)
                .inflate(layoutResource, parent, false) as TextView
            view.text = mCities[position].name
            return view
        }

        override fun getFilter(): Filter {
            return object : Filter() {
                override fun performFiltering(charSequence: CharSequence?): FilterResults {
                    val queryString = charSequence.toString().toLowerCase(Locale.ROOT)

                    val filterResults = FilterResults()
                    filterResults.values = if (queryString.isEmpty())
                        allCities
                    else allCities.filter { it.name.toLowerCase(Locale.ROOT).contains(queryString) }

                    return filterResults
                }

                override fun publishResults(
                    charSequence: CharSequence?,
                    filterResults: FilterResults?
                ) {
                    mCities = filterResults?.values as List<CityProperty>
                    notifyDataSetChanged()
                }
            }
        }
    }

}