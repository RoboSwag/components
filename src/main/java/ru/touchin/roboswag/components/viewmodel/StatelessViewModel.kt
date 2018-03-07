package ru.touchin.roboswag.components.viewmodel

import android.app.Application
import ru.touchin.roboswag.components.navigation.fragments.ViewControllerFragment

class StatelessViewModel(application: Application) : StateViewModel<ViewControllerFragment.DefaultState>(application)