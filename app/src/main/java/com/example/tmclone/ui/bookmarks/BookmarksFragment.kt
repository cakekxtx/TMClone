package com.example.tmclone.ui.bookmarks

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tmclone.BookmarksAdapter
import com.example.tmclone.Event
import com.example.tmclone.EventData
import com.example.tmclone.EventTicketService
import com.example.tmclone.R
import com.example.tmclone.databinding.FragmentBookmarksBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.Callback
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "BookmarksFragment"
class BookmarksFragment : Fragment() {

	private val BASE_URL = "https://app.ticketmaster.com/discovery/v2/"
	private val API_KEY = "EnVQtrsj7Wf4wwAviGLovBBPOWD7aqGF"

	lateinit var bookmarkRecyclerView: RecyclerView

	var bookmarkedList = ArrayList<Event>()
	val adapter = BookmarksAdapter(bookmarkedList)

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
	): View {
		val view = inflater.inflate(R.layout.fragment_bookmarks, container, false)

		//recycler view
		bookmarkRecyclerView = view.findViewById(R.id.bookmarks_recyclerView)
		bookmarkRecyclerView.adapter = adapter
		bookmarkRecyclerView.layoutManager = LinearLayoutManager(context)


		val firebaseDB = FirebaseFirestore.getInstance()
		val currUser = FirebaseAuth.getInstance().currentUser

		//retrieve data from Firebase
		val userBookmarks = firebaseDB.collection("bookmarks")
		userBookmarks.document(currUser?.uid.toString())
			.get()
			.addOnSuccessListener { document ->
				val eventIdArray = document.get("userbookmarks") as ArrayList<String>
				Log.d(TAG, "Data from Firebase retrieved successfully.")
				retrieveDataFromWeb(eventIdArray)
			}
			.addOnFailureListener { e ->
				Log.e(TAG, "Error retrieving data from database", e)
			}



		Log.d(TAG, "bookmarkList: ${bookmarkedList.size}")
		return view
	}



	fun retrieveDataFromWeb(eventIdArray: ArrayList<String>){
		//retrofit
		val retrofit = Retrofit.Builder()
			.baseUrl(BASE_URL)
			.addConverterFactory(GsonConverterFactory.create())
			.build()

		val eventAPI = retrofit.create(EventTicketService::class.java)

		val numOfEvents = eventIdArray.size

		for(eventId in eventIdArray) {
			eventAPI.getEventThroughID(API_KEY, eventId).enqueue(
				object : retrofit2.Callback<EventData> {
					override fun onResponse(
						p0: Call<EventData?>,
						response: Response<EventData?>
					) {
						Log.d(TAG, "onResponse: $response")
						val body = response.body()
						Log.d(TAG, "Response body: $body")
						if(body == null){
							Log.w(TAG, "Valid response not received.", )
						}

						val event = body?.eventsArray?.events[0]
						if (event != null){
							bookmarkedList.add(event)
							//Log.d(TAG, "bookmarkedList: $bookmarkedList")

						}

						if(numOfEvents ==  bookmarkedList.size){
							adapter.notifyDataSetChanged()
						}
					}

					override fun onFailure(
						p0: Call<EventData?>,
						response: Throwable
					) {
						Log.d(TAG, "onFailure: $response")
					}

				}
			)

		}

	}


}