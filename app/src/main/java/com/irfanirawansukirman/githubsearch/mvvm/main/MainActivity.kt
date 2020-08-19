package com.irfanirawansukirman.githubsearch.mvvm.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.ferfalk.simplesearchview.SimpleSearchView
import com.ferfalk.simplesearchview.utils.DimensUtils
import com.irfanirawansukirman.extensions.subscribe
import com.irfanirawansukirman.extensions.widget.*
import com.irfanirawansukirman.githubsearch.R
import com.irfanirawansukirman.githubsearch.abstraction.base.BaseActivity
import com.irfanirawansukirman.githubsearch.abstraction.ui.UIState
import com.irfanirawansukirman.githubsearch.abstraction.ui.UIState.Status.*
import com.irfanirawansukirman.githubsearch.abstraction.ui.widget.InfiniteScrollProvider
import com.irfanirawansukirman.githubsearch.abstraction.util.Const.ERROR_200_EMPTY
import com.irfanirawansukirman.githubsearch.abstraction.util.Const.ERROR_403_FORBIDDEN
import com.irfanirawansukirman.githubsearch.abstraction.util.Const.ERROR_404_NOT_FOUND
import com.irfanirawansukirman.githubsearch.abstraction.util.Const.ERROR_422_ENPROCESSABLE_ENTITY
import com.irfanirawansukirman.githubsearch.abstraction.util.Const.ERROR_502_CONNECTION_ERROR
import com.irfanirawansukirman.githubsearch.abstraction.util.Const.EXTRA_REVEAL_CENTER_PADDING
import com.irfanirawansukirman.githubsearch.abstraction.util.Const.GITHUB_USER_LIMIT
import com.irfanirawansukirman.githubsearch.data.remote.response.Item
import com.irfanirawansukirman.githubsearch.data.remote.response.User
import com.irfanirawansukirman.githubsearch.databinding.MainActivityBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.main_activity.*
import kotlin.math.roundToInt

@AndroidEntryPoint
class MainActivity : BaseActivity<MainVM, MainActivityBinding>(MainActivityBinding::inflate),
    MainContract, InfiniteScrollProvider.OnWhenScroll {

    private lateinit var mainAdapter: MainAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager

    private var isCall = false
    private var isErrorPage = false
    private val startPage = 1
    private var totalPage = 0
    private var currentPage = startPage
    private var isLastPage = false
    private var queryDefault = ""

    override fun getVMClass(): Class<MainVM> = MainVM::class.java

    override fun loadObservers() {
        viewModel.apply {
            user.subscribe(this@MainActivity, ::showUser)
        }
    }

    override fun onFirstLaunch(savedInstanceState: Bundle?) {
        setupUserListAdapter()
        setupUserListRecyclerView()
    }

    override fun continuousCall() {}

    override fun setupViewListener() {
        progress.setOnRefreshListener {
            isErrorPage = false
            isLastPage = false
            currentPage = startPage
            getUser(queryDefault, currentPage)
        }
        searchView.setOnQueryTextListener(object : SimpleSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                clearUser()

                queryDefault = query ?: ""
                getUser(queryDefault, currentPage)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

            override fun onQueryTextCleared(): Boolean {
                return false
            }
        })
    }

    override fun enableBackButton(): Boolean = false

    override fun bindToolbar(): Toolbar? = viewBinding.toolbar

    override fun onWhenScroll() {
        if (linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
            enableProgress()
        } else {
            disableProgress()
        }
    }

    override fun getUser(query: String, page: Int) {
        viewModel.getUser(query, page)
    }

    override fun onRetry() {
        getUser(queryDefault, currentPage)
    }

    override fun onShowErrorPage(errorType: Int) {
        val errorImageSource = when (errorType) {
            ERROR_502_CONNECTION_ERROR -> {
                R.drawable.img_error_no_internet_connection
            }
            ERROR_200_EMPTY -> {
                R.drawable.img_error_not_found
            }
            else -> {
                R.drawable.img_error_something_went_wrong
            }
        }
        val errorColorSource = if (errorType == ERROR_502_CONNECTION_ERROR) {
            R.color.grayError
        } else {
            R.color.white
        }

        if (currentPage == startPage) {
            if (errorType == ERROR_422_ENPROCESSABLE_ENTITY
                || errorType == ERROR_404_NOT_FOUND
                || errorType == ERROR_200_EMPTY
                || errorType == ERROR_502_CONNECTION_ERROR
            ) {
                isErrorPage = false
                hideRecyclerMain()
                showImageError(errorImageSource, errorColorSource)
            }
            enableProgress()
        } else {
            if (errorType == ERROR_403_FORBIDDEN) {
                isErrorPage = false
                hideImageError()
            }
            onWhenScroll()
        }

        hideProgressLoadMore()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        setupSearchView(menu)
        return true
    }

    private fun setupSearchView(menu: Menu) {
        val item = menu.findItem(R.id.action_search)
        searchView.setMenuItem(item)
        // searchView.tabLayout = tabLayout

        // Adding padding to the animation because of the hidden menu item
        val revealCenter = searchView.revealAnimationCenter
        revealCenter.x -= DimensUtils.convertDpToPx(EXTRA_REVEAL_CENTER_PADDING, this)
    }

    override fun onBackPressed() {
        if (searchView.onBackPressed()) {
            return
        }
        super.onBackPressed()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (searchView.onActivityResult(requestCode, resultCode, data)) {
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setupUserListAdapter() {
        if (!::linearLayoutManager.isInitialized) {
            linearLayoutManager = LinearLayoutManager(this)
        }

        if (!::mainAdapter.isInitialized) {
            mainAdapter = MainAdapter()
        }
    }

    private fun setupUserListRecyclerView() {
        viewBinding.recyclerMain.apply {
            setLinearList()
            layoutManager = linearLayoutManager
            adapter = mainAdapter

            val infiniteScrollProvider = InfiniteScrollProvider(this@MainActivity)
            infiniteScrollProvider.attach(this,
                object : InfiniteScrollProvider.OnLoadMoreListener {
                    override fun onLoadMore() {
                        if (!isLastPage && !isErrorPage) {
                            showProgressLoadMore()
                            if (isCall) getUser(queryDefault, currentPage)
                            isCall = true
                        }
                    }
                })
        }
    }

    private fun showUser(state: UIState<User>) {
        when (state.status) {
            LOADING -> {
                if (currentPage == startPage) showProgress()
                hideImageError()
            }
            FINISH -> {
                hideProgress()
            }
            SUCCESS -> {
                state.data?.let { user ->
                    isCall = false

                    totalPage = getTotalPage(user.totalCount)
                    if (totalPage > 0) {
                        if (currentPage == startPage) clearUser()

                        if (currentPage < totalPage) {
                            isLastPage = false
                            currentPage++
                        } else {
                            isLastPage = true
                        }

                        user.items?.let {
                            hideProgressLoadMore()

                            if (it.isNotEmpty()) {
                                showRecyclerMain()
                                showUser(it)
                            } else {
                                if (currentPage == startPage) onShowErrorPage(ERROR_200_EMPTY)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun hideProgressLoadMore() {
        mainAdapter.removeProgressFooter()
    }

    private fun showProgressLoadMore() {
        mainAdapter.addProgressFooter()
    }

    private fun clearUser() {
        mainAdapter.clearData()
    }

    private fun showUser(data: List<Item>) {
        mainAdapter.addAllData(data)
    }

    private fun hideImageError() {
        imgMainError.hide()
    }

    private fun showImageError(errorImageSource: Int, errorColorSource: Int) {
        imgMainError.apply {
            show()
            setImageResource(errorImageSource)
            setBackgroundResource(errorColorSource)
        }
    }

    private fun hideRecyclerMain() {
        recyclerMain.hide()
    }

    private fun showRecyclerMain() {
        recyclerMain.show()
    }

    private fun disableProgress() {
        progress.disableView()
    }

    private fun enableProgress() {
        progress.enableView()
    }

    private fun getTotalPage(totalCount: Int?): Int {
        return (totalCount?.div(GITHUB_USER_LIMIT)?.toFloat() ?: 1.0f).roundToInt()
    }
}