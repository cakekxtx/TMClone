package com.example.tmclone

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.util.Log.e
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.TextPart
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.jvm.java


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
		val getLocationButton = itemView.findViewById<ImageButton>(R.id.getLocation_imageButton)
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
					val position = adapterPosition
					retrieveAndAddDataToDB(firebaseDB, uid, position)
					Toast.makeText(itemView.context, "Bookmark added successfully!", Toast.LENGTH_SHORT).show()
					bookmarkButton.setImageResource(R.drawable.filled_bookmark)
				}
			}

			askGeminiButton.setOnClickListener {
				val prompt = "Give info about ${events[adapterPosition].name} event or artist, skip introduction, paragraph format, under 150 words, skip venue details and time"

				askGemini(events[adapterPosition].name, prompt, itemView.context)
			}

			getLocationButton.setOnClickListener {
				val locationIntent = Intent(itemView.context,MapsActivity::class.java)

				val venue = events[adapterPosition].embeddedVenuesAndAttraction.venues[0]
				val longitude = venue.location.longitude
				val latitude = venue.location.latitude
				val city = venue.city.venueCity

				locationIntent.putExtra("longitude", longitude)
				locationIntent.putExtra("latitude", latitude)
				locationIntent.putExtra("city", city)

				itemView.context.startActivity(locationIntent)
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
		val venueName = currItem.embeddedVenuesAndAttraction.venues[0].venueName
		val city = currItem.embeddedVenuesAndAttraction.venues[0].city.venueCity
		val state = currItem.embeddedVenuesAndAttraction.venues[0].state.venueState
		val address = currItem.embeddedVenuesAndAttraction.venues[0].address.venueAddress

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
			append("Date: ${changeDateFormat(localDate)}")
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

	fun changeDateFormat(date:String?):String{ // from yyyy-mm-dd to mm/dd/yyyy
		val dateList = date?.split("-")
		val formattedDate = "${dateList?.get(1)}/${dateList?.get(2)}/${dateList?.get(0)}"
		return formattedDate
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

	fun askGemini(eventName: String, prompt: String, context: android.content.Context):String{

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
								showDialog(eventName, geminiAnswer, context)
							}

							else -> {
								Log.w(
									"GeminiAPI", "Received a non-text part:${firstPart::class.java.simpleName} "
								)
								geminiAnswer = "Unable to display Gemini's response"
								showDialog(prompt, geminiAnswer, context)
							}
						}
					} else {
						Log.w("GeminiAPI", "No parts in the content.")
						geminiAnswer = "No response received."
						showDialog(prompt, geminiAnswer, context)
					}
				} ?: run {
					Log.w("GeminiAPI", "No content in the response. ",)
					geminiAnswer = "No response received."
					showDialog(prompt, geminiAnswer, context)

				}

			} catch (e: Exception) {
				Log.e(TAG, "Error generating Gemini response", e)
				/*runOnUiThread{

				}*/
			}
		}
		return geminiAnswer
	}


	fun addBookmarksDataToDB(firebaseDB: FirebaseFirestore, uid: String, eventsArr: ArrayList<String>, position: Int ){
		val bookmarks = firebaseDB.collection("bookmarks")
		eventsArr.add(events[position].id)
		val toUpdate = mapOf(
			"userbookmarks" to eventsArr
		)

		bookmarks.document(uid)
			.get()
			.addOnSuccessListener { document ->
				if(document != null){
					document.reference.update(toUpdate)
					Log.d(TAG, "Data successfully added to database")
				}
			}
			.addOnFailureListener { e ->
				Log.e(TAG, "Failed to get data from database", e )
			}
	}



	fun retrieveAndAddDataToDB(firebaseDB: FirebaseFirestore, uid: String, position: Int){
		val userBookmarks = firebaseDB.collection("bookmarks")

		userBookmarks.document(uid)
			.get()
			.addOnSuccessListener { document ->
				val bookmarksArray = document.get("userbookmarks") as ArrayList<String>
				Log.d(TAG, "Data from database successfully retreived.")
				addBookmarksDataToDB(firebaseDB, uid, bookmarksArray, position)
			}
			.addOnFailureListener { e ->
				e(TAG, "Error retrieving data.", e )
			}
	}

	fun showDialog(title: String, msg: String, context: android.content.Context){
		val builder = AlertDialog.Builder(context)
			.setTitle(title)
			.setMessage(msg)
			.setPositiveButton("Ok") {dialog, which ->
				//dismiss
			}
			.create()
			.show()
	}
}