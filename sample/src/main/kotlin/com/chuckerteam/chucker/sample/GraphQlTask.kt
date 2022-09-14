package com.chuckerteam.chucker.sample

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.network.okHttpClient
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Callback
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val GRAPHQL_BASE_URL = "https://rickandmortyapi.com/graphql/"
private const val BASE_URL = "https://rickandmortyapi.com/"
class GraphQlTask (
    client: OkHttpClient,
) : HttpTask {

    private val apolloClient = ApolloClient.Builder()
        .serverUrl(GRAPHQL_BASE_URL)
        .okHttpClient(client)
        .build()

    private val api = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()
        .create<Api>()

    private val scope = MainScope()

    private val noopCallback = object: Callback<Any?> {
        override fun onResponse(call: Call<Any?>, response: Response<Any?>) = Unit

        override fun onFailure(call: Call<Any?>, t: Throwable) {
            t.printStackTrace()
        }

    }
    override fun run() {
        scope.launch {
            with(api) {
                this.getCharacterById(GRAPHQL_QUERY, GRAPHQL_QUERY_VARIABLE).enqueue(noopCallback)

                 apolloClient
                .query(SearchCharactersQuery(Optional.presentIfNotNull("Morty")))
                .execute()
            }
        }
    }

    private interface Api {
        @GET("graphql")
        fun getCharacterById(@Query("query")query: String, @Query("variables") variables: String? = null )
        : Call<Any?>
    }
}

const val GRAPHQL_QUERY = "query GetCharacter( \$id: ID! ){\n" +
    "  character(id:\$id) {\n" +
    "      id:id,      \n" +
    "     \tname,\n" +
    "      status     \n" +
    "    \n" +
    "  }\n" +
    "}"
const val GRAPHQL_QUERY_VARIABLE = "{\"id\":1}"
