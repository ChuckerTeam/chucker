package com.chuckerteam.chucker.sample

interface IGraphQLTask {
    fun run(query: String, variables: String? = null)
}
