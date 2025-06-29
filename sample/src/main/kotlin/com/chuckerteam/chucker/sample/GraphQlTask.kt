package com.chuckerteam.chucker.sample

import android.util.Log
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.network.okHttpClient
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Query

private const val GRAPHQL_BASE_URL = "https://rickandmortyapi.com/graphql/"
private const val BASE_URL = "https://rickandmortyapi.com/"

class GraphQlTask(
    client: OkHttpClient,
) : HttpTask {
    private val apolloClient =
        ApolloClient
            .Builder()
            .serverUrl(GRAPHQL_BASE_URL)
            .okHttpClient(client)
            .build()

    private val api =
        Retrofit
            .Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .build()
            .create<Api>()

    val scope =
        CoroutineScope(
            SupervisorJob() +
                Dispatchers.IO +
                CoroutineExceptionHandler { _, error ->
                    Log.e("NetworkScope", "Unexpected error", error)
                },
        )

    override fun run() {
        scope.launch {
            supervisorScope {
                val deferredCharacter =
                    async {
                        api
                            .getCharacterById(GRAPHQL_QUERY, GRAPHQL_QUERY_VARIABLE)
                            .execute()
                    }
                val deferredSearch =
                    async {
                        apolloClient
                            .query(SearchCharactersQuery(Optional.presentIfNotNull("Morty")))
                            .execute()
                    }
                runCatching { deferredCharacter.await() }
                runCatching { deferredSearch.await() }
            }
        }
    }

    private interface Api {
        @GET("graphql")
        fun getCharacterById(
            @Query("query") query: String,
            @Query("variables") variables: String? = null,
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
