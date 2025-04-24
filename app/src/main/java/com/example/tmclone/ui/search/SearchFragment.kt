package com.example.tmclone.ui.search

import android.app.AlertDialog
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tmclone.Event
import com.example.tmclone.EventData
import com.example.tmclone.EventTicketService
import com.example.tmclone.EventsAdapter
import com.example.tmclone.R
import com.google.android.material.navigation.NavigationBarView
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.jvm.java
import retrofit2.Callback
import retrofit2.Response


private val BASE_URL = "https://app.ticketmaster.com/discovery/v2/"
private val API_KEY = "EnVQtrsj7Wf4wwAviGLovBBPOWD7aqGF"
private const val TAG = "SearchFragment"
class SearchFragment : Fragment() {
	//private val viewModel: SearchViewModel by viewModels()

	private val BASE_URL = "https://app.ticketmaster.com/discovery/v2/"
	private val API_KEY = "EnVQtrsj7Wf4wwAviGLovBBPOWD7aqGF"

	lateinit var cityEditText: EditText
	lateinit var categorySpinner: Spinner
	lateinit var noResultsTextView: TextView
	lateinit var eventRecyclerView: RecyclerView
	lateinit var searchButton: Button

	private lateinit var currCategory: String
	private val categoryList = listOf(
		"Choose an Event Category",
		"Music",
		"Sports",
		"Theater",
		"Family",
		"Arts & Theater",
		"Concerts",
		"Comedy",
		"Dance"
	)

	private lateinit var enteredCity: String
	var eventList = ArrayList<Event>()
	val adapter = EventsAdapter(eventList)

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		val view = inflater.inflate(R.layout.fragment_search, container, false)

		val context = view.context

		cityEditText = view.findViewById(R.id.location_editText)
		noResultsTextView = view.findViewById(R.id.noResults_textView)


		//category spinner
		currCategory = categoryList[0] //default to the first item

		val categoryAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, categoryList)
		categorySpinner = view.findViewById(R.id.eventSpinner)
		categorySpinner.adapter = categoryAdapter
		categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
			override fun onItemSelected(
				parent: AdapterView<*>?,
				view: View?,
				position: Int,
				id: Long
			) {
				val selectedItem = parent?.getItemAtPosition(position).toString()
				currCategory = selectedItem
			}

			override fun onNothingSelected(parent: AdapterView<*>?) {
			}

		}

		//recycler view
		eventRecyclerView = view.findViewById(R.id.recyclerView)
		eventRecyclerView.adapter = adapter
		eventRecyclerView.layoutManager = LinearLayoutManager(context)


		searchButton = view.findViewById(R.id.searchButton)
		searchButton.setOnClickListener {
			if(selectCategory()) {
				enteredCity = view.findViewById<EditText>(R.id.location_editText).text.toString()
				if (enteredCityMethod()) {
					retrieveDataFromWeb()
				}
				else{
					eventRecyclerView.visibility = View.GONE
				}
			}
			else{
				eventRecyclerView.visibility = View.GONE
			}
			resetUserInput()
		}


		return view
	}

	/*
		helper function
		desc: retrieve data from the web
		> called when search button is clicked
	 */
	private	fun retrieveDataFromWeb() {
		//retrofit
		val retrofit = Retrofit.Builder()
			.baseUrl(BASE_URL)
			.addConverterFactory(GsonConverterFactory.create())
			.build()

		val ticketEventAPI = retrofit.create(EventTicketService::class.java)
		ticketEventAPI.searchTickets(API_KEY,enteredCity, currCategory, "date,asc").enqueue(object : Callback<EventData>{
			override fun onResponse(p0: Call<EventData?>, response: Response<EventData?>) {
				Log.d(TAG, "onResponse: $response ")


				val body = response.body()
				Log.d(TAG, "Response body: $body")
				if (body == null) {
					Log.w(TAG, "Valid response not received")
					return
				}

				//add all items from the API response to the event list
				if(body.eventsArray != null){
					showResults()
					eventList.clear()
					eventList.addAll(body.eventsArray.events)
					adapter.notifyDataSetChanged()

				}
				else{
					showNoResults()
				}
			}

			override fun onFailure(p0: Call<EventData?>, response: Throwable) {
				Log.d(TAG, "onFailure: $response")
			}

		})
	}

	/*onClick function
	desc: display list based on category and location
	> fetch info from API
	> display content in ascending order based on event date
	*/
	fun search(view: View){
		if(selectCategory()) {
			enteredCity = view.findViewById<EditText>(R.id.location_editText).text.toString()
			if (enteredCityMethod()) {
				retrieveDataFromWeb()
			}
			else{
				eventRecyclerView.visibility = View.GONE
			}

		}
		else{
			eventRecyclerView.visibility = View.GONE
		}
		resetUserInput()
	}

	/*  called in: search()
		desc: method to show a dialog if there is no selected category
	 */
	private fun selectCategory():Boolean{
		if(currCategory == categoryList[0]){
			val title = "Event Category Missing"
			val msg = "Please select an event category to continue."
			dialog(title, msg)
			return false
		}
		return true
	}

	/*  called in: search()
		desc: method to show a dialog if there is no entered location
		initialize enteredCity
	 */
	private fun enteredCityMethod():Boolean{
		if(enteredCity.equals("")){
			val title = "Location missing"
			val msg = "Please enter a city to continue."
			dialog(title, msg)
			return false
		}
		return true
	}

	/*  helper function to show dialog
	 */
	fun dialog(title: String, msg: String){
		val builder = AlertDialog.Builder(view?.context)
		builder
			.setTitle(title)
			.setMessage(msg)
			.setCancelable(true)
			.create()
			.show()

		builder.setNeutralButton("Dismiss"){dialog, which ->
			// dismiss dialog
		}
	}

	fun resetUserInput(){
		categorySpinner.setSelection(0)
		cityEditText.text.clear()
	}

	private fun showNoResults(){
		noResultsTextView.visibility = View.VISIBLE
		eventRecyclerView.visibility = View.GONE
	}

	private fun showResults(){
		noResultsTextView.visibility = View.GONE
		eventRecyclerView.visibility = View.VISIBLE
	}
}