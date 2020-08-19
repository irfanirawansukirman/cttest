package com.irfanirawansukirman.githubsearch.mvvm.main

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.irfanirawansukirman.githubsearch.abstraction.base.BaseViewModel
import com.irfanirawansukirman.githubsearch.abstraction.ui.UIState
import com.irfanirawansukirman.githubsearch.abstraction.util.Const.ERROR_200_EMPTY
import com.irfanirawansukirman.githubsearch.abstraction.util.CoroutineContextProvider
import com.irfanirawansukirman.githubsearch.data.remote.response.User
import com.irfanirawansukirman.githubsearch.data.repository.main.MainRepositoryImpl
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext

interface MainContract {
    fun getUser(query: String, page: Int)
}

class MainVM @ViewModelInject constructor(
    @ApplicationContext application: Context,
    private val coroutineContextProvider: CoroutineContextProvider,
    private val mainRepositoryImpl: MainRepositoryImpl
) : BaseViewModel(application as Application, coroutineContextProvider), MainContract {

    private val _user = MutableLiveData<UIState<User>>()
    val user: LiveData<UIState<User>>
        get() = _user

    override fun getUser(query: String, page: Int) {
        Log.d(MainVM::class.java.simpleName, "getUser: Masuk")
        _user.value = UIState.loading()
        executeJob {
            withContext(coroutineContextProvider.main) {
                // getting response
                val response = mainRepositoryImpl.getUserList(query, page)

                // getting user list
                val userList = response.items ?: emptyList()

                if (userList.isEmpty()) { // check if response is empty, show error page
                    errorMap["error_type"] = ERROR_200_EMPTY
                    setupUIException(errorMap)
                } else { // if response is not empty, show user
                    _user.value = UIState.success(response)
                }
            }
        }
        _user.value = UIState.finish()
    }
}