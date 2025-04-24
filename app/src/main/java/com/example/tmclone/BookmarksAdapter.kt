package com.example.tmclone

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide

class BookmarksAdapter (private val events: ArrayList<Event>):
	RecyclerView.Adapter<BookmarksAdapter.MyViewHolder>(){


	inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
		val eventImage = itemView.findViewById<ImageButton>(R.id.bookmarkEvent_Image)
		val eventNameTextView = itemView.findViewById<TextView>(R.id.bookmarkEventName_textview)
		val eventTicketButton = itemView.findViewById<Button>(R.id.bookmarkTicketLink_button)

		var ticketurl = " "

		init{
			eventTicketButton.setOnClickListener{
				val context = itemView.context
				val browserIntent = Intent(Intent.ACTION_VIEW)
				if(ticketurl.isNotEmpty()){
					browserIntent.data = Uri.parse(ticketurl)
					context.startActivity(browserIntent)
				}
				else{
					Toast.makeText(context, "No ticket link found", Toast.LENGTH_SHORT).show()
				}
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

}