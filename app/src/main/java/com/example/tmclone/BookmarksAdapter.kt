package com.example.tmclone

import android.content.Intent
import android.net.Uri
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
import com.google.firebase.firestore.FirebaseFirestore


/**
 * Retrieve bookmarks from db
 * Remove existing bookmarks
 */



class BookmarksAdapter (private val events: ArrayList<Event>):
	RecyclerView.Adapter<BookmarksAdapter.MyViewHolder>(){


	inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
		val eventImage = itemView.findViewById<ImageView>(R.id.bookmarkEvent_Image)
		val eventNameTextView = itemView.findViewById<TextView>(R.id.bookmarkEventName_textview)
		val eventTicketButton = itemView.findViewById<Button>(R.id.bookmarkTicketLink_button)
		val bookmarkButton = itemView.findViewById<ImageButton>(R.id.removeBookmark_button)

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
				events.removeAt(position)
				notifyItemRemoved(position)
				Toast.makeText(context, "${events[position].name} event removed from bookmarks.", Toast.LENGTH_SHORT).show()
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

	fun retrieveDataFromDB(firebaseDB: FirebaseFirestore, uid: String){
		val bookmarks = firebaseDB.collection("bookmarks")
		bookmarks.get()
			.addOnSuccessListener { documents ->
				for(document in documents){
					if(document.id.toString().equals(uid)){ //check if current document is the user's doc

					}
				}
			}
	}

}