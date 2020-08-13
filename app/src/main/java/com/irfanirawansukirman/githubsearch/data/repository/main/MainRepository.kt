package com.irfanirawansukirman.githubsearch.data.repository.main

import com.irfanirawansukirman.githubsearch.abstraction.util.Const.GITHUB_USER_LIMIT
import com.irfanirawansukirman.githubsearch.data.remote.response.Item

interface MainRepository {
    interface Remote {
        suspend fun getUserList(
            query: String,
            page: Int
        ): List<Item>
    }

    interface Cache
}