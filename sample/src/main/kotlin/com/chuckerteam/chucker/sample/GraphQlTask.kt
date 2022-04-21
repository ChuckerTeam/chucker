package com.chuckerteam.chucker.sample

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.network.okHttpClient
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

class GraphQlTask (
    client: OkHttpClient,
) : HttpTask {

    private val apolloClient = ApolloClient.Builder()
        .serverUrl("https://rickandmortyapi.com/graphql")
        .okHttpClient(client)
        .build()

    private val scope = MainScope()

    override fun run() {
        scope.launch {
            apolloClient
                .query(SearchCharactersQuery(Optional.presentIfNotNull("Morty")))
                .execute()
        }
    }
}
