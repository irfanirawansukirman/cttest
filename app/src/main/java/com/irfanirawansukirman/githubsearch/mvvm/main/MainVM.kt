package com.irfanirawansukirman.githubsearch.mvvm.main

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.irfanirawansukirman.githubsearch.abstraction.base.BaseViewModel
import com.irfanirawansukirman.githubsearch.abstraction.ui.UIState
import com.irfanirawansukirman.githubsearch.abstraction.util.CoroutineContextProvider
import com.irfanirawansukirman.githubsearch.data.remote.response.Item
import com.irfanirawansukirman.githubsearch.data.repository.main.MainRepositoryImpl

interface MainContract {
    fun getUserList(query: String, page: Int)
}

class MainVM @ViewModelInject constructor(
    private val coroutineContextProvider: CoroutineContextProvider,
    private val mainRepositoryImpl: MainRepositoryImpl
) : BaseViewModel(coroutineContextProvider), MainContract {

    private val _userList = MutableLiveData<UIState<List<Item>>>()
    val userList: LiveData<UIState<List<Item>>>
        get() = _userList

    override fun getUserList(query: String, page: Int) {
        _userList.value = UIState.loading()
        executeJob {
            _userList.value = UIState.success(mainRepositoryImpl.getUserList(query, page))
        }
        _userList.value = UIState.finish()
    }
}