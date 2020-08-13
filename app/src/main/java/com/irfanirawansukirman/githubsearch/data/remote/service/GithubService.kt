package com.irfanirawansukirman.githubsearch.data.remote.service

import com.irfanirawansukirman.githubsearch.abstraction.util.Const.GITHUB_USER_LIMIT
import com.irfanirawansukirman.githubsearch.data.remote.response.User
import retrofit2.http.GET
import retrofit2.http.Query

interface GithubService {

    /**
     * Service for getting github user list
     *
     * @param query String => query search
     * @param page Int => page index
     * @param per_page Int => limit user per page
     * @return User => response as User object
     */
    @GET("search/users")
    suspend fun getUserList(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("per_page") per_page: Int = GITHUB_USER_LIMIT
    ): User
}