package com.chuckerteam.chucker.sample

import okhttp3.OkHttpClient
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

class GqlHttpTask(
    client: OkHttpClient,
) : HttpTask {

    private val api = Retrofit.Builder()
        .baseUrl("https://countries.trevorblades.com")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build().create<GraphQLSWApi>()

    private val noOpCallback = object : Callback<Any?> {
        override fun onResponse(call: Call<Any?>, response: Response<Any?>) = Unit

        override fun onFailure(call: Call<Any?>, t: Throwable) {
            t.printStackTrace()
        }
    }


    val countriesQuery = "query countries {\n" +
        "  countries {\n" +
        "    name\n" +
        "  }\n" +
        "}\n"

    @Suppress("MagicNumber")
    override fun run() = with(api) {

        getCountries(
            GraphQLResponseBody(
                query = countriesQuery,
                operationName = "countries"
            )
        ).enqueue(noOpCallback)
    }

    data class GraphQLResponseBody(
        val query: String,
        val operationName: String
    )

    internal interface GraphQLSWApi {
        @POST("/")
        @Headers("Content-Type: application/json")
        fun getCountries(@Body body: GraphQLResponseBody?): Call<Any?>
    }
}
