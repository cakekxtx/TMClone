package com.example.tmclone

import android.app.ProgressDialog.show
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.util.Log.e
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Glide.init
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.TextPart
import com.google.api.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


private const val TAG = "EventsAdapter"
class EventsAdapter (private val events: ArrayList<Event>):
	RecyclerView.Adapter<EventsAdapter.MyViewHolder>() {

	val GEMINI_API_KEY = "AIzaSyAI5j6qJ9zRampB5G9lMG9TXay3LlWjSls"


	inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val eventName = itemView.findViewById<TextView>(R.id.eventName_textView)
		val eventLocation = itemView.findViewById<TextView>(R.id.eventLocation_textView)
		val eventAddress = itemView.findViewById<TextView>(R.id.eventAddress_textView)
		val eventTimeAndDate = itemView.findViewById<TextView>(R.id.eventDateTime_textView)
		val eventImage = itemView.findViewById<ImageView>(R.id.eventImage_imageView)
		val priceRange = itemView.findViewById<TextView>(R.id.priceRange_textView)
		val getTicketsButton = itemView.findViewById<Button>(R.id.getTickets_button)
		val bookmarkButton = itemView.findViewById<ImageButton>(R.id.searchBookmarks_button)
		val askGeminiButton = itemView.findViewById<ImageButton>(R.id.askGemini_button)
		var ticketurl = ""
		val firebaseDB: FirebaseFirestore = FirebaseFirestore.getInstance()
		//val position = adapterPosition

		init {
			getTicketsButton.setOnClickListener {
				val context = itemView.context
				val browserIntent = Intent(Intent.ACTION_VIEW)
				if (ticketurl.isNotEmpty()) {
					browserIntent.data = Uri.parse(ticketurl)
					context.startActivity(browserIntent)
				} else {
					Toast.makeText(context, "No ticket link found", Toast.LENGTH_SHORT).show()
				}

			}

			bookmarkButton.setOnClickListener {
				//get current user
				val currUser = FirebaseAuth.getInstance().currentUser
				if(currUser == null){
					val intent = Intent(itemView.context, LoginActivity::class.java)
					itemView.context.startActivity(intent)
				}
				else{
					val uid = currUser.uid

					//retrieve data from db --- want to move into a function
					/**
					 * issue: replaces the current object in array in db instead of adding
					 */
					var bookmarkedEvents = ArrayList<Event>()
					bookmarkedEvents = retrieveDataFromDB(firebaseDB, uid)
					bookmarkedEvents.add(events[adapterPosition])
					addBookmarksDataToDB(firebaseDB, uid, bookmarkedEvents)
				}
			}

			askGeminiButton.setOnClickListener {
				val prompt = "Info About ${events[adapterPosition].name}"
				val geminiResponse = askGemini(prompt)
				//show dialog to show gemini's reponse
				val builder = AlertDialog.Builder(itemView.context)
					.setTitle(prompt)
					.setMessage(geminiResponse)

				builder.setPositiveButton("OK") {dialog, which ->
					//dismiss the dialog
				}

				builder.create().show()
			}
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
		val view =
			LayoutInflater.from(parent.context).inflate(R.layout.search_event_layout, parent, false)
		return MyViewHolder(view)
	}

	override fun getItemCount(): Int {
		return events.size
	}

	override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
		val currItem = events[position]

		//event name
		holder.eventName.text = currItem.name
		//event location
		val venueName = currItem.embeddedVenues.venues[0].venueName
		val city = currItem.embeddedVenues.venues[0].city.venueCity
		val state = currItem.embeddedVenues.venues[0].state.venueState
		val address = currItem.embeddedVenues.venues[0].address.venueAddress

		holder.eventLocation.text = buildString {
			append(venueName)
			append(", ")
			append(city)
		}
		//event address
		holder.eventAddress.text = buildString {
			append("Address: $address, ")
			append(city)
			append(", ")
			append(state)
		}
		//event time and date
		var localTime = currItem.dates.start.localTime
		if (localTime != "TBA")
			localTime = convertTo12HrFormat(currItem.dates.start.localTime)
		val localDate = currItem.dates.start.localDate
		holder.eventTimeAndDate.text = buildString {
			append("Date: $localDate")
			append(" @ ")
			append(localTime)
		}


		//event image
		val context = holder.itemView.context
		val highestQualityImage = currItem.images.maxByOrNull {
			it.width * it.height
		}
		//load image from the url using the Glide library
		//Log.d(TAG, "highestQualityImage: ${highestQualityImage?.url}")
		Glide.with(context)
			.load(highestQualityImage?.url)
			.placeholder(R.drawable.ic_launcher_background)
			.into(holder.eventImage)


		//event price range
		if (currItem.priceRanges != null) {
			holder.priceRange.text = buildString {
				append("Price Range: $")
				append(currItem.priceRanges[0].min)
				append(" - $")
				append(currItem.priceRanges[0].max)
			}
		}

		holder.ticketurl = currItem.ticketLink.toString()
	}

	fun convertTo12HrFormat(time: String?): String {
		val timeList = time?.split(":")
		val hour = timeList?.get(0)?.toInt()
		val mins = timeList?.get(1)
		var formattedTime = ""

		if (hour != null) {
			if (hour > 12) {
				formattedTime = buildString {
					append(hour - 12)
					append(":")
					append(mins)
					append(" PM")
				}
			} else {
				formattedTime = buildString {
					append(hour)
					append(":")
					append(mins)
					append(" AM")
				}
			}
		}

		return formattedTime
	}

	fun askGemini(prompt: String):String{
		val harassmentSafety = SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.ONLY_HIGH)
		val hateSpeechSafety =
			SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE)

		val model = GenerativeModel(
			modelName = "gemini-2.0-flash",
			apiKey = GEMINI_API_KEY,
			safetySettings = listOf(harassmentSafety, hateSpeechSafety)
		)

		var geminiAnswer = "" //store gemini's answer

		/**
		 * issue: unable to show gemini's response in dialog
		 * how to return string inside coroutine
		 */
		CoroutineScope(Dispatchers.Main).launch {
			try {
				val response = model.generateContent(prompt)
				val content = response.candidates.firstOrNull()?.content

				content?.let { content ->
					if (content.parts.isNotEmpty()) {
						val firstPart = content.parts[0]
						when (firstPart) {
							is TextPart -> {
								Log.d("GeminiAPI", "Generated response: ${firstPart.text}")
								geminiAnswer = firstPart.text
							}

							else -> {
								Log.w(
									"GeminiAPI", "Received a non-text part:${firstPart::class.java.simpleName} "
								)
								geminiAnswer = "Unable to display Gemini's response"
							}
						}
					} else {
						Log.w("GeminiAPI", "No parts in the content.")
						geminiAnswer = "No response received."
					}
				} ?: run {
					Log.w("GeminiAPI", "No content in the response. ",)
					geminiAnswer = "No response received."

				}

			} catch (e: Exception) {
				Log.e(TAG, "Error generating Gemini response", e)
				/*runOnUiThread{

				}*/
			}
		}
		return geminiAnswer
	}


	fun addBookmarksDataToDB(firebaseDB: FirebaseFirestore, uid: String, bookmarkedEvents: ArrayList<Event>){
		val bookmarks = firebaseDB.collection("bookmarks")
		val toAdd = mapOf(
			"bookmarks" to bookmarkedEvents
		)

		bookmarks.document(uid)
			.get()
			.addOnSuccessListener { document ->
				if(document != null){
					document.reference.update(toAdd)
					Log.d(TAG, "Data successfully added to database")
				}
			}
			.addOnFailureListener { e ->
				Log.e(TAG, "Failed to get data from database", e )
			}
	}


	/**
	 * issue: replaces the current object in array in db instead of adding
	 */
	fun retrieveDataFromDB(firebaseDB: FirebaseFirestore, uid: String): ArrayList<Event>{
		var bookmarkedEvents = ArrayList<Event>()
		val bookmarks = firebaseDB.collection("bookmarks")
		bookmarks.get()
			.addOnSuccessListener { documents ->
				for (document in documents){
					if(document.id == uid) {
						bookmarkedEvents = document.get("bookmarks") as ArrayList<Event>

						//should return from here
					}

				}

				Log.d(TAG, "Data retrieved successfully.")
			}
			.addOnFailureListener { e ->
				e(TAG, "Error retrieving data",e )
			}

		return bookmarkedEvents
	}
}