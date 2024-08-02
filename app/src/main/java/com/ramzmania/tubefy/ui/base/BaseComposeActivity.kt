package com.ramzmania.tubefy.ui.base
import android.app.Dialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable

import androidx.lifecycle.ViewModelProvider

/**
 * A base activity class for activities that use Jetpack Compose and view models.
 *
 * @param ViewModel The type of view model associated with the activity.
 */
abstract class BaseComposeActivity<ViewModel : BaseViewModel> : ComponentActivity() {

    protected lateinit var viewModel: ViewModel
    protected abstract fun getViewModelClass(): Class<ViewModel>

    var dialog: Dialog? = null

    /**
     * Observes the view model's live data.
     */
    abstract fun observeViewModel()

    /**
     * Observes activity-specific events or states.
     */
    abstract fun observeActivity()

    /**
     * Performs additional setup before setting the content view.
     */
    abstract fun beforeOnContent()

//    abstract fun observeBeforeOnCreate()
    /**
     * Additional setup for observing activity with instance state.
     */
    open fun observeActivityWithInstance(savedInstanceState: Bundle?) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        beforeOnContent()
        setContent {
            setContentView()
        }
        initViewModel()

        observeViewModel()
        observeActivityWithInstance(savedInstanceState)
        observeActivity()
    }
    /**
     * Sets the content of the activity using Jetpack Compose.
     */
    @Composable
    abstract fun setContentView()

    private fun initViewModel() {
        viewModel = ViewModelProvider(this)[getViewModelClass()]
    }
    /**
     * Hides the progress dialog if shown.
     */
    open fun hideProgressDialog() {
        try {
            dialog?.dismiss()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}
