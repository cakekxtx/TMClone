package com.example.tmclone
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface EventTicketService {

	@GET("events.json?")
	fun searchTickets(
		@Query("apikey") apiKey: String,
		@Query("city") city: String,
		@Query("keyword") category: String,
		@Query("sort") sort: String
	): Call<EventData>

	@GET("suggest?")
	fun suggestEvents(
		@Query("apikey") apiKey: String,
		@Query("keyword") category: String,
		@Query("countryCode") country: String
	): Call<EventData>
}