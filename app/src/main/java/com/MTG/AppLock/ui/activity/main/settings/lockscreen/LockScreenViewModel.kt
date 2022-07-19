package com.MTG.AppLock.ui.activity.main.settings.lockscreen

import com.MTG.AppLock.ui.base.viewmodel.RxAwareViewModel
import com.MTG.AppLock.util.preferences.AppLockerPreferences
import javax.inject.Inject

class LockScreenViewModel @Inject constructor(private val appLockerPreferences: AppLockerPreferences) : RxAwareViewModel() {
    fun isVibrate(): Boolean {
        return appLockerPreferences.isVibrate()
    }

    fun setVibrate(isVibrate: Boolean) {
        appLockerPreferences.setVibrate(isVibrate)
    }

    fun isShowPathLine(): Boolean {
        return appLockerPreferences.isShowPathLine()
    }

    fun setShowPathLine(isShowPathLine: Boolean) {
        return appLockerPreferences.setShowPathLine(isShowPathLine)
    }
}