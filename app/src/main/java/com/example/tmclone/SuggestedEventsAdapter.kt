package com.example.tmclone

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class SuggestEventsAdapter (private val events: ArrayList<Event>):
	RecyclerView.Adapter<SuggestEventsAdapter.MyViewHolder>(){

	inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
		val imageButton = itemView.findViewById<ImageButton>(R.id.eventImageButton)
		val eventName = itemView.findViewById<TextView>(R.id.suggestedEventName_textview)
		var ticketurl = ""

		init{
			imageButton.setOnClickListener{
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
		val view = LayoutInflater.from(parent.context).inflate(R.layout.suggest_event_layout, parent, false)
		return MyViewHolder(view)
	}

	override fun getItemCount(): Int {
		return events.size
	}

	override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
		val currItem = events[position]
		val context = holder.itemView.context
		holder.ticketurl = currItem.ticketLink.toString()
		holder.eventName.text = currItem.name
		val highestQualityImage = currItem.images.maxByOrNull {
			it.width*it.height
		}

		Glide.with(context)
			.load(highestQualityImage?.url)
			.placeholder(R.drawable.ic_launcher_background)
			.into(holder.imageButton)
	}
}