package com.irfanirawansukirman.githubsearch.abstraction.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.irfanirawansukirman.extensions.showSnackBar
import com.irfanirawansukirman.extensions.subscribe
import com.irfanirawansukirman.githubsearch.abstraction.ui.UIState
import com.irfanirawansukirman.githubsearch.abstraction.ui.UIState.Status.ERROR
import com.irfanirawansukirman.githubsearch.abstraction.ui.UIState.Status.TIMEOUT
import kotlinx.android.synthetic.main.main_activity.view.*

@Suppress("UNCHECKED_CAST")
abstract class BaseActivity<VM : BaseViewModel, VB : ViewBinding>(viewBinder: (LayoutInflater) -> ViewBinding) :
    AppCompatActivity() {

    lateinit var viewModel: VM

    val viewBinding by lazy(LazyThreadSafetyMode.NONE) { viewBinder.invoke(layoutInflater) as VB }

    private var mToolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        setupToolbar()
        setupViewModel()
        onFirstLaunch(savedInstanceState)
        setupViewListener()
        loadObservers()

        // load base message
        viewModel.apply {
            errorException.subscribe(this@BaseActivity, ::showErrorMessage)
            timeoutException.subscribe(this@BaseActivity, ::showErrorMessage)
            ioException.subscribe(this@BaseActivity, ::showErrorMessage)
            uiException.subscribe(this@BaseActivity) { onShowErrorPage(it["error_type"] ?: 0) }
        }
    }

    /**
     * @return viewModel class for base to setup.
     */
    protected abstract fun getVMClass(): Class<VM>

    /**
     * Function for load livedata observer from viewmodel
     */
    abstract fun loadObservers()

    /**
     * Function for first launching like as onCreate
     */
    abstract fun onFirstLaunch(savedInstanceState: Bundle?)

    /**
     * Function for continously call after onCreate and onResume
     */
    abstract fun continuousCall()

    /**
     * Function for setup view listener
     */
    abstract fun setupViewListener()

    /**
     * Function for enable back button toolbar.
     * @return Boolean
     */
    abstract fun enableBackButton(): Boolean

    /**
     * Function for get toolbarId
     * @param void
     * @return Toolbar
     */
    abstract fun bindToolbar(): Toolbar?

    abstract fun onRetry()

    abstract fun onShowErrorPage(errorType: Int)

    override fun onStart() {
        super.onStart()
        continuousCall()
    }

    /**
     * Method listener for menu toolbar
     * @return Boolean
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean = when (item?.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        else -> false
    }

    /**
     * Function for setup toolbar [inactive|active]
     * @param void
     * @return void
     */
    private fun setupToolbar() {
        bindToolbar()?.let {
            mToolbar = it
            setSupportActionBar(mToolbar)
            supportActionBar?.apply {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(enableBackButton())
            }
        }
    }

    private fun setupViewModel() {
        if (!::viewModel.isInitialized) {
            viewModel = ViewModelProvider(this).get(getVMClass())
        }
    }

    fun showProgress() {
        viewBinding.root.progress.isRefreshing = true
    }

    fun hideProgress() {
        viewBinding.root.progress.isRefreshing = false
    }

    private fun showErrorMessage(state: UIState<String>) {
        when (state.status) {
            ERROR -> validateError(state.error)
            TIMEOUT -> validateError(state.error)
        }
    }

    private fun validateError(errorMessage: String) {
        showSnackBar(viewBinding.root, getFilterErrorMessage(errorMessage), "Retry") { onRetry() }
    }

    fun getParentToolbar(): Toolbar? = mToolbar

    private fun getFilterErrorMessage(errorMessage: String): String {
        return when {
            errorMessage.toLowerCase().contains("403") -> {
                "Request is over. Please try again later."
            }
            errorMessage.toLowerCase().contains("422") -> {
                "Search paramater can't be blank."
            }
            else -> {
                errorMessage
            }
        }
    }

}