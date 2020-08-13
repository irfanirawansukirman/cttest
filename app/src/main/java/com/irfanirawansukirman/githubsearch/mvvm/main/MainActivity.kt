package com.irfanirawansukirman.githubsearch.mvvm.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import com.irfanirawansukirman.extensions.logD
import com.irfanirawansukirman.extensions.showToast
import com.irfanirawansukirman.extensions.subscribe
import com.irfanirawansukirman.githubsearch.abstraction.base.BaseActivity
import com.irfanirawansukirman.githubsearch.abstraction.ui.UIState
import com.irfanirawansukirman.githubsearch.abstraction.ui.UIState.Status.*
import com.irfanirawansukirman.githubsearch.data.remote.response.Item
import com.irfanirawansukirman.githubsearch.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate), MainContract {

    private val viewModel by viewModels<MainVM>()

    private val startPage = 1
    private val queryDefault = "q"

    override fun loadObservers() {
        viewModel.apply {
            userList.subscribe(this@MainActivity, ::showUserList)
        }
    }

    override fun onFirstLaunch(savedInstanceState: Bundle?) {
        getUserList(queryDefault, startPage)
    }

    override fun continuousCall() {

    }

    override fun setupViewListener() {
        progress.setOnRefreshListener {
            getUserList(queryDefault, startPage)
        }
    }

    override fun enableBackButton(): Boolean = false

    override fun bindToolbar(): Toolbar? = null

    override fun getUserList(query: String, page: Int) {
        viewModel.getUserList(query, page)
    }

    private fun showUserList(state: UIState<List<Item>>) {
        when (state.status) {
            LOADING -> showProgress()
            FINISH -> hideProgress()
            SUCCESS -> {
                state.data?.let { userList ->
                    userList.forEach { user ->
                        logD(user)
                    }
                }
            }
            ERROR -> {
                showToast(state.error)
            }
            else -> {
                showToast(state.error)
            }
        }
    }
}