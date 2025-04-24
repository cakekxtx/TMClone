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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tmclone.Event
import com.example.tmclone.EventData
import com.example.tmclone.EventTicketService
import com.example.tmclone.MainActivity
import com.example.tmclone.R
import com.example.tmclone.SuggestEventsAdapter
import com.example.tmclone.databinding.FragmentHomeBinding
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
		startSearchingButton = view.findViewById(R.id.startSearching_button)

		var musicEventList = ArrayList<Event>()
		var sportsEventList = ArrayList<Event>()
		val musicRecyclerAdapter = SuggestEventsAdapter(musicEventList)
		val sportsRecyclerAdapter = SuggestEventsAdapter(sportsEventList)

		musicRecyclerView.adapter = musicRecyclerAdapter
		sportsRecyclerView.adapter = sportsRecyclerAdapter

		//change layout to horizontal
		musicRecyclerView.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false)
		sportsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)


		getEvents(musicEventList, musicRecyclerAdapter, "music")
		getEvents(sportsEventList, sportsRecyclerAdapter, "sports")

		startSearchingButton.setOnClickListener{
			//move to search fragment
		}
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




}