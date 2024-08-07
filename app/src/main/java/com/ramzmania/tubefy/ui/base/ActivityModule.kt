package com.ramzmania.tubefy.ui.base

import androidx.activity.ComponentActivity
import javax.inject.Inject
/**
 * Represents a module for managing activities within the application.
 * This module is responsible for providing the necessary dependencies related to activities.
 *
 * @property activity The component activity instance injected into the module.
 * @constructor Creates an instance of the ActivityModule with the provided component activity.
 */
class ActivityModule @Inject constructor(val activity: ComponentActivity) {
}