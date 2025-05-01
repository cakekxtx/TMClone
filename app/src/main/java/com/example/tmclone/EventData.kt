package com.example.tmclone
import com.google.gson.annotations.SerializedName


data class EventData(

	@SerializedName("_embedded") val eventsArray: EventArray
)

data class EventArray(
	val events: List<Event>
)

data class Event(
	val name: String,
	val id: String,
	val images: List<ImageInfo>,
	val dates: DateAndTime,
	@SerializedName("url") val ticketLink: String? = "",
	@SerializedName("_embedded") val embeddedVenues: EmbeddedVenues,
	val priceRanges: List<Price>
)

data class ImageInfo(
	val url: String,
	val width: Int,
	val height: Int
)

data class Price(
	val min: Double,
	val max: Double
)

data class DateAndTime(
	val start: ActualDateAndTime
)

data class ActualDateAndTime(
	val localDate: String? ="TBA",
	val localTime: String? = "TBA"
)

data class EmbeddedVenues(
	val venues: List<Venues>,
)



data class Venues(
	@SerializedName("name") val venueName: String,
	val city: City,
	val state: State,
	val address: AddressInfo,
	//might use for extra credit/final project
	val location: Location
)

data class City(
	@SerializedName("name") val venueCity: String? = "TBA"
)

data class State(
	@SerializedName("name") val venueState: String? = "TBA"
)

data class AddressInfo(
	@SerializedName("line1") val venueAddress: String? = "Address not found."
)

data class Location(
	val longitude: String,
	val latitude: String
)
