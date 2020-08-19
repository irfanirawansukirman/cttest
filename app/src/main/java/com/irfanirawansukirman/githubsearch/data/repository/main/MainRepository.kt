package com.irfanirawansukirman.githubsearch.data.repository.main

import com.irfanirawansukirman.githubsearch.data.remote.response.User

interface MainRepository {
    interface Remote {
        suspend fun getUserList(
            query: String,
            page: Int
        ): User
    }

    interface Cache
}