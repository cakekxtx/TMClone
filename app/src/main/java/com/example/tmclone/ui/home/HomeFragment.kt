package com.example.tmclone.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tmclone.Event
import com.example.tmclone.EventData
import com.example.tmclone.EventTicketService
import com.example.tmclone.MainActivity
import com.example.tmclone.R
import com.example.tmclone.SuggestEventsAdapter
import com.example.tmclone.databinding.FragmentHomeBinding
import com.google.api.Context
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "HomeFragment"
class HomeFragment : Fragment() {
	
	private val BASE_URL = "https://app.ticketmaster.com/discovery/v2/"
	private val API_KEY = "EnVQtrsj7Wf4wwAviGLovBBPOWD7aqGF"

	lateinit var musicRecyclerView: RecyclerView
	lateinit var sportsRecyclerView: RecyclerView
	lateinit var theaterRecyclerView: RecyclerView
	lateinit var startSearchingButton: Button

	
	
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		val view = inflater.inflate(R.layout.fragment_home, container, false)
		val context = view.context
		//declare variables
		musicRecyclerView = view.findViewById(R.id.musicEventsRecyclerView)
		sportsRecyclerView = view.findViewById(R.id.sportsEventsRecyclerView)
		theaterRecyclerView = view.findViewById(R.id.theaterEventsRecyclerView)

		var musicEventList = ArrayList<Event>()
		var sportsEventList = ArrayList<Event>()
		var theaterEventList = ArrayList<Event>()
		val musicRecyclerAdapter = SuggestEventsAdapter(musicEventList)
		val sportsRecyclerAdapter = SuggestEventsAdapter(sportsEventList)
		val theaterRecyclerAdapter = SuggestEventsAdapter(theaterEventList)

		musicRecyclerView.adapter = musicRecyclerAdapter
		sportsRecyclerView.adapter = sportsRecyclerAdapter
		theaterRecyclerView.adapter = theaterRecyclerAdapter

		//change layout to horizontal
		/*musicRecyclerView.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false)
		sportsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
		sportsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)*/
		changeLayoutToHorizontal(musicRecyclerView, context)
		changeLayoutToHorizontal(sportsRecyclerView, context)
		changeLayoutToHorizontal(theaterRecyclerView, context)

		//add divider
		/*val dividerItemDecoration = DividerItemDecoration(view.context, DividerItemDecoration.HORIZONTAL)
		musicRecyclerView.addItemDecoration(dividerItemDecoration)
		sportsRecyclerView.addItemDecoration(dividerItemDecoration)*/
		addDivider(musicRecyclerView, context)
		addDivider(sportsRecyclerView, context)
		addDivider(theaterRecyclerView, context)

		getEvents(musicEventList, musicRecyclerAdapter, "music")
		getEvents(sportsEventList, sportsRecyclerAdapter, "sports")
		getEvents(theaterEventList,theaterRecyclerAdapter, "theater")

		return view
	}

	private fun getEvents(eventList: ArrayList<Event>, adapter: SuggestEventsAdapter, category: String){
		//retrieve date from the web
		val retrofit = Retrofit.Builder()
			.baseUrl(BASE_URL)
			.addConverterFactory(GsonConverterFactory.create())
			.build()

		val ticketEventAPI = retrofit.create(EventTicketService:: class.java)
		ticketEventAPI.suggestEvents(API_KEY, category, "us").enqueue(object: Callback<EventData?>{
			override fun onResponse(p0: Call<EventData?>, response: Response<EventData?>) {
				Log.d(TAG, "onResponse: $response")
				val body = response.body()
				Log.d(TAG, "body: $body")
				if(body == null){
					return
				}
				if(body.eventsArray != null){
					eventList.addAll(body.eventsArray.events)
					adapter.notifyDataSetChanged()
				}
			}

			override fun onFailure(
				p0: Call<EventData?>, response: Throwable) {
				Log.d(TAG, "onFailure: $response")
			}

		})
	}

	fun changeLayoutToHorizontal(recyclerView: RecyclerView, context: android.content.Context){
		recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
	}

	fun addDivider(recyclerView: RecyclerView, context: android.content.Context){
		val dividerItemDecoration = DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL)
		recyclerView.addItemDecoration(dividerItemDecoration)
	}

}