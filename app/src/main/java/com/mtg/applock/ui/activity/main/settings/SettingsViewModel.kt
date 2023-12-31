package com.mtg.applock.ui.activity.main.settings

import android.app.Application
import com.mtg.applock.ui.base.viewmodel.RxAwareAndroidViewModel
import com.mtg.applock.util.preferences.AppLockerPreferences
import javax.inject.Inject

class SettingsViewModel @Inject constructor(val app: Application, val appLockerPreferences: AppLockerPreferences) : RxAwareAndroidViewModel(app) {
    fun isFingerPrintEnabled(): Boolean {
        return appLockerPreferences.isFingerPrintEnabled()
    }

    fun setFingerPrintEnabled(fingerPrintEnabled: Boolean) {
        return appLockerPreferences.setFingerPrintEnable(fingerPrintEnabled)
    }

    fun isAskLockingNewApplication(): Boolean {
        return appLockerPreferences.isAskLockingNewApplication()
    }

    fun setAskLockingNewApplication(askLockingNewApplication: Boolean) {
        return appLockerPreferences.setAskLockingNewApplication(askLockingNewApplication)
    }

    fun setReload(isReload: Boolean) {
        appLockerPreferences.setReload(isReload)
    }

    fun setLockSettingApp(lockSettingApp: Int) {
        appLockerPreferences.setLockSettingApp(lockSettingApp)
    }

    fun setFinish(isFinish: Boolean) {
        appLockerPreferences.setFinish(isFinish)
    }
    fun isShowPathLine(): Boolean {
        return appLockerPreferences.isShowPathLine()
    }

    fun setShowPathLine(isShowPathLine: Boolean) {
        return appLockerPreferences.setShowPathLine(isShowPathLine)
    }
}