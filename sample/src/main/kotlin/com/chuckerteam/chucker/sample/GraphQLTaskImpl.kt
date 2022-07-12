package com.chuckerteam.chucker.sample

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.Callback
import retrofit2.Call
import retrofit2.Response
import retrofit2.create
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
            query,
             variables ?. let {
                 JsonConverter.gsonInstance.fromJson(variables, GraphQLVariables::class.java)
             } ?: GraphQLVariables()
            )
        ).enqueue(noOpCallback)

    }

    private interface Api {
        @GET("graphql")
        fun getCharacterById(@Query("query")query: String, @Query("variables") variables: String? = null ): Call<Any?>

        @POST("graphql")
        fun getCharacterByIdPost(@Body graphQLQuery: GraphQLQuery): Call<Any?>
    }

    data class GraphQLQuery(val query:String, val variables: GraphQLVariables?)
    data class GraphQLVariables(
        @SerializedName("id") val id: Long? = null
    )
}

const val GRAPHQL_QUERY = "query GetCharacter( \$id: ID! ){\n" +
    "  character(id:\$id) {\n" +
    "      id:id,      \n" +
    "     \tname,\n" +
    "      status     \n" +
    "    \n" +
    "  }\n" +
    "}"

object JsonConverter {
    val gsonInstance by lazy { GsonBuilder().create() }
}
