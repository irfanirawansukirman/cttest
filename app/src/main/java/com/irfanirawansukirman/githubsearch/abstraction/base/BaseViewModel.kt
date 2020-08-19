package com.irfanirawansukirman.githubsearch.abstraction.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.irfanirawansukirman.githubsearch.abstraction.ui.UIState
import com.irfanirawansukirman.githubsearch.abstraction.util.Const.ERROR_403_FORBIDDEN
import com.irfanirawansukirman.githubsearch.abstraction.util.Const.ERROR_404_NOT_FOUND
import com.irfanirawansukirman.githubsearch.abstraction.util.Const.ERROR_408_TIMEOUT
import com.irfanirawansukirman.githubsearch.abstraction.util.Const.ERROR_422_ENPROCESSABLE_ENTITY
import com.irfanirawansukirman.githubsearch.abstraction.util.Const.ERROR_500_SOMETHING_WENT_WRONG
import com.irfanirawansukirman.githubsearch.abstraction.util.Const.ERROR_502_CONNECTION_ERROR
import com.irfanirawansukirman.githubsearch.abstraction.util.CoroutineContextProvider
import com.irfanirawansukirman.githubsearch.abstraction.util.isInternetAvailable
import com.irfanirawansukirman.githubsearch.abstraction.util.isNetworkAvailable
import kotlinx.coroutines.*
import java.io.IOException

abstract class BaseViewModel(
    private val applicationContext: Application,
    private val coroutineContextProvider: CoroutineContextProvider
) : AndroidViewModel(applicationContext) {

    private val _timeoutException = MutableLiveData<UIState<String>>()
    val timeoutException: LiveData<UIState<String>>
        get() = _timeoutException

    private val _errorException = MutableLiveData<UIState<String>>()
    val errorException: LiveData<UIState<String>>
        get() = _errorException

    private val _ioException = MutableLiveData<UIState<String>>()
    val ioException: LiveData<UIState<String>>
        get() = _ioException

    private val _uiException = MutableLiveData<HashMap<String, Int>>()
    val uiException: LiveData<HashMap<String, Int>>
        get() = _uiException

    val errorMap = HashMap<String, Int>()

    fun executeJob(
        execute: suspend () -> Unit
    ) {
        viewModelScope.launch(coroutineContextProvider.io) {
            if (isNetworkAvailable(applicationContext) && isInternetAvailable()) {
                try {
                    withTimeout(10_000) {
                        execute()
                    }
                } catch (e: TimeoutCancellationException) {
                    _timeoutException.postValue(UIState.timeout(e.message ?: "Request Timeout"))

                    errorMap["error_type"] = ERROR_408_TIMEOUT
                    _uiException.postValue(errorMap)
                } catch (e: IOException) {
                    _ioException.postValue(UIState.error(e.message ?: "No Internet Connection"))

                    errorMap["error_type"] = ERROR_502_CONNECTION_ERROR
                    _uiException.postValue(errorMap)
                } catch (e: Exception) { // 403, 422, 404
                    val errorMessage = e.message ?: "Something Went Wrong"
                    _errorException.postValue(
                        UIState.error(
                            errorMessage
                        )
                    )

                    errorMap["error_type"] = when {
                        errorMessage.contains("403") -> {
                            ERROR_403_FORBIDDEN
                        }
                        errorMessage.contains("422") -> {
                            ERROR_422_ENPROCESSABLE_ENTITY
                        }
                        else -> {
                            ERROR_404_NOT_FOUND
                        }
                    }
                    _uiException.postValue(errorMap)
                }
            } else {
                _errorException.postValue(
                    UIState.error("No Internet Connection")
                )

                errorMap["error_type"] = ERROR_502_CONNECTION_ERROR
                _uiException.postValue(errorMap)
            }
        }
    }

    fun executeAsyncJob(
        execute: suspend () -> Unit
    ) {
        viewModelScope.launch(coroutineContextProvider.io) {
            if (isNetworkAvailable(applicationContext) && isInternetAvailable()) {
                try {
                    withTimeout(10_000) {
                        withContext(Dispatchers.IO) { execute() }
                    }
                } catch (e: TimeoutCancellationException) {
                    _timeoutException.postValue(UIState.timeout(e.message ?: "Request Timeout"))

                    errorMap["error_type"] = ERROR_408_TIMEOUT
                    _uiException.postValue(errorMap)
                } catch (e: IOException) {
                    _ioException.postValue(UIState.error(e.message ?: "No Internet Connection"))

                    errorMap["error_type"] = ERROR_502_CONNECTION_ERROR
                    _uiException.postValue(errorMap)
                } catch (e: Exception) {
                    _errorException.postValue(
                        UIState.error(
                            e.message ?: "Something Went Wrong"
                        )
                    )

                    errorMap["error_type"] = ERROR_500_SOMETHING_WENT_WRONG
                    _uiException.postValue(errorMap)
                }
            } else {
                _errorException.postValue(
                    UIState.error("No Internet Connection")
                )

                errorMap["error_type"] = ERROR_502_CONNECTION_ERROR
                _uiException.postValue(errorMap)
            }
        }
    }

    fun setupUIException(errorType: HashMap<String, Int>) {
        _uiException.postValue(errorType)
    }
}