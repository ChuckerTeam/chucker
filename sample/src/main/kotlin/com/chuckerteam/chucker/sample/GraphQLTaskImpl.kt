package com.chuckerteam.chucker.sample

import okhttp3.OkHttpClient
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

const val GRAPHQL_BASE_URL = "https://rickandmortyapi.com/"
class GraphQLTaskImpl(client: OkHttpClient):IGraphQLTask {
    private val api = Retrofit.Builder()
        .baseUrl(GRAPHQL_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()
        .create<Api>()

    private val noOpCallback = object : Callback<Any?> {
        override fun onResponse(call: Call<Any?>, response: Response<Any?>) = Unit

        override fun onFailure(call: Call<Any?>, t: Throwable) {
            t.printStackTrace()
        }
    }
    override fun run(query: String, variables: String?) = with(api) {
        getCharacterById(query, variables).enqueue(noOpCallback)
        getCharacterByIdPost(
            GraphQLQuery(
            "query GetCharacter( \$id: ID! ){character(id:\$id) {id:id,      name,status     }}",
            GraphQLVariables(6L)
            )
        ).enqueue(noOpCallback)

    }

    private interface Api {
        @GET("graphql")
        fun getCharacterById(@Query("query")query: String, @Query("variables") variables: String? = null ): Call<Any?>

        @POST("graphql")
        fun getCharacterByIdPost(@Body graphQLQuery: GraphQLQuery): Call<Any?>
    }

    data class GraphQLQuery(val query:String, val variables: GraphQLVariables)
    data class GraphQLVariables(val id: Long)
}
