package com.chuckerteam.chucker.sample

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.network.okHttpClient
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Query

private const val GRAPHQL_BASE_URL = "https://rickandmortyapi.com/graphql/"
private const val BASE_URL = "https://rickandmortyapi.com/"
class GraphQlTask(
    client: OkHttpClient
) : HttpTask {

    private val apolloClient = ApolloClient.Builder()
        .serverUrl(GRAPHQL_BASE_URL)
        .okHttpClient(client)
        .build()

    private val api = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .build()
        .create<Api>()

    private val scope = MainScope()

    override fun run() {
        scope.launch {
            api.getCharacterById(GRAPHQL_QUERY, GRAPHQL_QUERY_VARIABLE).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) = Unit

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    t.printStackTrace()
                }
            })
            apolloClient
                .query(SearchCharactersQuery(Optional.presentIfNotNull("Morty")))
                .execute()
        }
    }

    private interface Api {
        @GET("graphql")
        fun getCharacterById(
            @Query("query") query: String,
            @Query("variables") variables: String? = null
        ): Call<ResponseBody>
    }
}

const val GRAPHQL_QUERY = """query GetCharacter( ${'$'}id: ID! ){
    character(id:${'$'}id) {
        id:id,
        name,
        status
    }
}"""
const val GRAPHQL_QUERY_VARIABLE = """{"id":1}"""
