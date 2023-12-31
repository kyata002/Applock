package com.mtg.applock.ui.activity.main.settings.email

import android.content.Context
import android.text.TextUtils
import com.mtg.applock.R
import com.mtg.applock.ui.base.viewmodel.RxAwareViewModel
import com.mtg.applock.util.CommonUtils
import com.mtg.applock.util.preferences.AppLockerPreferences
import es.MTG.toasty.Toasty
import javax.inject.Inject

class EmailViewModel @Inject constructor(private val appLockerPreferences: AppLockerPreferences) : RxAwareViewModel() {
    fun getEmail(): String {
        return appLockerPreferences.getEmail()
    }

    fun saveEmail(context: Context, email: String): Boolean {
        return if (CommonUtils.emailValidator(email)) {
            if (TextUtils.equals(email, appLockerPreferences.getEmail())) {
                // cần thay đổi string
                Toasty.showToast(context, R.string.msg_email_email_used, Toasty.WARNING)
                false
            } else {
                appLockerPreferences.setEmail(email)
                Toasty.showToast(context, R.string.msg_email_change_successful, Toasty.SUCCESS)
                true
            }
        } else {
            if (TextUtils.isEmpty(email)) {
                Toasty.showToast(context, R.string.msg_please_enter_text, Toasty.ERROR)
            } else {
                Toasty.showToast(context, R.string.text_invalid_email_address, Toasty.ERROR)
            }
            false
        }
    }
}