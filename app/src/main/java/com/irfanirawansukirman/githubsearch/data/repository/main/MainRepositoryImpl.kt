package com.irfanirawansukirman.githubsearch.data.repository.main

import com.irfanirawansukirman.githubsearch.data.remote.response.User
import com.irfanirawansukirman.githubsearch.data.remote.service.GithubService
import javax.inject.Inject

class MainRepositoryImpl @Inject constructor(
    private val githubService: GithubService
) : MainRepository.Remote {

    override suspend fun getUserList(query: String, page: Int): User =
        githubService.getUserList(query, page)
}