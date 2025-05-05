package com.example.tmclone

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.util.Log.e
import android.util.Log.v
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


/**
 * Retrieve bookmarks from db
 * Remove existing bookmarks
 */


private const val TAG = "BookmarksAdapter"
class BookmarksAdapter (private val events: ArrayList<Event>):
	RecyclerView.Adapter<BookmarksAdapter.MyViewHolder>(){


	inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
		val eventImage = itemView.findViewById<ImageView>(R.id.bookmarkEvent_Image)
		val eventNameTextView = itemView.findViewById<TextView>(R.id.bookmarkEventName_textview)
		val eventTicketButton = itemView.findViewById<Button>(R.id.bookmarkTicketLink_button)
		val bookmarkButton = itemView.findViewById<ImageButton>(R.id.removeBookmark_button)
		val getLocationButton = itemView.findViewById<ImageButton>(R.id.getLocationBM_imageButton)
		val firebaseDB  = FirebaseFirestore.getInstance()
		val currUser = FirebaseAuth.getInstance().currentUser
		var ticketurl = " "

		init{
			val context = itemView.context

			eventTicketButton.setOnClickListener{
				val browserIntent = Intent(Intent.ACTION_VIEW)
				if(ticketurl.isNotEmpty()){
					browserIntent.data = Uri.parse(ticketurl)
					context.startActivity(browserIntent)
				}
				else{
					Toast.makeText(context, "No ticket link found", Toast.LENGTH_SHORT).show()
				}
			}

			bookmarkButton.setOnClickListener {
				val position = adapterPosition

				//update database
				retrieveAndRemoveDataFromDB(firebaseDB, currUser?.uid.toString(), events[position].id)
				Toast.makeText(context, "${events[position].name} event removed from bookmarks.", Toast.LENGTH_SHORT).show()
				events.removeAt(position)
				notifyItemRemoved(position)

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
		val view = LayoutInflater.from(parent.context)
			.inflate(R.layout.bookmarks_layout, parent, false)
		return MyViewHolder(view)
	}

	override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
		val currItem = events[position]
		val context = holder.itemView.context

		holder.ticketurl = currItem.ticketLink.toString()
		holder.eventNameTextView.text = currItem.name

		val highestQualityImage = currItem.images.maxByOrNull { it.width*it.height }

		Glide.with(context)
			.load(highestQualityImage?.url)
			.placeholder(R.drawable.ic_launcher_background)
			.into(holder.eventImage)
	}

	override fun getItemCount(): Int {
		return events.size
	}


	fun retrieveAndRemoveDataFromDB(firebaseDB: FirebaseFirestore, uid: String, eventId: String){
		val bookmarks = firebaseDB.collection("bookmarks")

		bookmarks.document(uid)
			.get()
			.addOnSuccessListener { document ->
				var eventIdArray = ArrayList<String>()
				if (document.get("userbookmarks") != null){
					Log.d(TAG, "in userbookmarks: ${document.get("userbookmarks")}")
					eventIdArray =  document.get("userbookmarks") as ArrayList<String>
					if(eventIdArray.contains(eventId)){
						eventIdArray.remove(eventId)
						val toUpdate = mapOf(
							"userbookmarks" to eventIdArray
						)
						document.reference.update(toUpdate)
					}
				}
			}
			.addOnFailureListener { e ->

			}
	}

}