package com.mobiuspace.medical.utils

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowManager


fun Activity.transparentStatusBar() {
  window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
  window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
  val option = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
  val vis = window.decorView.systemUiVisibility
  window.decorView.systemUiVisibility = option or vis
  window.statusBarColor = Color.TRANSPARENT
}

fun Activity.transparentNavBar() {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    window.isNavigationBarContrastEnforced = false
  }
  window.navigationBarColor = Color.TRANSPARENT
  val decorView = window.decorView
  val vis = decorView.systemUiVisibility
  val option =
    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
  decorView.systemUiVisibility = vis or option
}