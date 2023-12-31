package com.mtg.applock.ui.activity.main.settings

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.common.control.dialog.RateAppDialog
import com.common.control.interfaces.RateCallback
import com.common.control.utils.RatePrefUtils
import com.mtg.applock.R
import com.mtg.applock.permissions.IntentHelper
import com.mtg.applock.permissions.PermissionChecker
import com.mtg.applock.ui.activity.intruders.IntrudersPhotosActivity
import com.mtg.applock.ui.activity.main.OnMainListener
import com.mtg.applock.ui.activity.main.settings.superpassword.SuperPasswordActivity
import com.mtg.applock.ui.activity.password.changepassword.ChangePasswordActivity
import com.mtg.applock.ui.activity.password.overlay.activity.OverlayValidationActivity
import com.mtg.applock.ui.activity.policy.PolicyActivity
import com.mtg.applock.ui.base.BaseActivity
import com.mtg.applock.ui.base.BusMessage
import com.mtg.applock.util.CommonUtils
import com.mtg.applock.util.Const
import com.mtg.applock.util.EventBusUtils
import com.mtg.applock.util.extensions.dialogLayout
import com.mtg.applock.util.extensions.gone
import com.mtg.applock.util.extensions.visible
import com.mtg.applock.util.preferences.AppLockerPreferences
import com.mtg.fingerprint.FingerprintIdentify
import es.MTG.toasty.Toasty
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.dialog_fingerprint_warning.view.*
import kotlinx.android.synthetic.main.dialog_hacker_warning.view.*
import org.greenrobot.eventbus.EventBus

class SettingsActivity : BaseActivity<SettingsViewModel>() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private var mConfirmDialog: AlertDialog? = null
    private var mConfirmFingerprintDialog: AlertDialog? = null
    private lateinit var mFingerprintIdentify: FingerprintIdentify
    private var mOnMainListener: OnMainListener? = null
    
    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, SettingsActivity::class.java)
        }
    }

    override fun getViewModel(): Class<SettingsViewModel> {
        return SettingsViewModel::class.java
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_settings
    }

    override fun initViews() {

        setSupportActionBar(toolbarSettings)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)

        }
        toolbarSettings.setNavigationOnClickListener { onBackPressed() }

        devicePolicyManager = this.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        //
        mFingerprintIdentify = FingerprintIdentify(this)
        mFingerprintIdentify.setSupportAndroidL(true)              // support android L
        mFingerprintIdentify.init()                                // init
        //
        buildConfirmDialog()
        buildConfirmFingerPrintDialog()
        initMyListener()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun buildConfirmDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View = LayoutInflater.from(this).inflate(
            R.layout.dialog_hacker_warning,
            null,
            false
        )
        builder.setView(view)
        builder.setCancelable(false)
        mConfirmDialog?.dismiss()
        mConfirmDialog = builder.create()
        mConfirmDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.btnNoHacker.setOnClickListener {
            mConfirmDialog?.dismiss()
            switchPreventApplicationUninstall.isChecked = true
        }
        view.btnOkHacker.setOnClickListener {
            mConfirmDialog?.dismiss()
            if (SystemClock.elapsedRealtime() > 300000) {
                devicePolicyManager.removeActiveAdmin(
                    ComponentName(this, AdminReceiver::class.java)
                )
                mOnMainListener?.onChangeStatusGroupLock()
            } else {
                switchPreventApplicationUninstall.isChecked = true
                Toasty.showToast(
                    this,
                    R.string.msg_warning_remove_admin,
                    Toasty.WARNING
                )
            }
        }
    }

    private fun buildConfirmFingerPrintDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View = LayoutInflater.from(this).inflate(
            R.layout.dialog_fingerprint_warning,
            null,
            false
        )
        builder.setView(view)
        mConfirmFingerprintDialog?.dismiss()
        mConfirmFingerprintDialog = builder.create()
        mConfirmFingerprintDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.btnOkFingerprint.setOnClickListener {
            mConfirmFingerprintDialog?.dismiss()
            viewModel.setReload(false)
            if (isMIUI()) {
                startActivityForResult(
                    Intent(Settings.ACTION_SETTINGS),
                    Const.REQUEST_CODE_SECURITY_SETTINGS
                )
            } else {
                startActivityForResult(
                    Intent(Settings.ACTION_SECURITY_SETTINGS),
                    Const.REQUEST_CODE_SECURITY_SETTINGS
                )
            }
        }
    }

    private fun isMIUI(): Boolean {
        val device = Build.MANUFACTURER
        if (device == "Xiaomi") {
            return true
        }
        return false
    }

    private fun checkFingerPrintDevice(): Boolean {
        var isHasFinger = false
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val fingerprintManager: FingerprintManager? = this.getSystemService(
                    FINGERPRINT_SERVICE
                ) as FingerprintManager?
                try {
                    if (fingerprintManager?.isHardwareDetected == true) {
                        isHasFinger = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return isHasFinger
    }

    private fun initMyListener() {
//        clSettingsTheme.setOnClickListener {
//            startActivity(ThemeActivity.newIntent(this))
//        }
        clSettingsChangeSuperPassword.setOnClickListener {
            startActivity(SuperPasswordActivity.newIntent(this))
        }
        clSettingsChangePassword.setOnClickListener {
            val intent = OverlayValidationActivity.newIntent(this)
            intent.putExtra(Const.EXTRA_CHANGE_PASSWORD, true)
            startActivityForResult(intent, Const.REQUEST_CODE_CHECK_PASSWORD_AND_CHANGE_PASSWORD)
        }
//        clSettingsLockScreen.setOnClickListener {
//            startActivity(LockScreenActivity.newIntent(this))
//        }
        clSettingsIntruder.setOnClickListener {
            startActivity(IntrudersPhotosActivity.newIntent(this))
        }
        if (checkFingerPrintDevice()) {
            clSettingsFingerprint.visible()
        } else {
            clSettingsFingerprint.gone()
        }

        switchShowPathLine.isChecked = viewModel.isShowPathLine()
        switchShowPathLine.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setShowPathLine(isChecked)
        }
        switchSettingsFingerprint.isChecked = isFingerprintChecked()
        switchSettingsFingerprint.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (mFingerprintIdentify.isHardwareEnable) {
                    if (mFingerprintIdentify.isFingerprintEnable) {
                        viewModel.setFingerPrintEnabled(true)
                    } else {
                        viewModel.setLockSettingApp(AppLockerPreferences.LOCK_APP_WAIT_FINGERPRINT)
                        switchSettingsFingerprint.isChecked = false
                        mConfirmFingerprintDialog?.show()
                        dialogLayout(mConfirmFingerprintDialog)
                    }
                } else {
                    viewModel.setFingerPrintEnabled(false)
                    switchSettingsFingerprint.isChecked = false
                    Toasty.showToast(
                        this,
                        R.string.msg_phone_does_not_support_fingerprints,
                        Toasty.WARNING
                    )
                }
            } else {
                viewModel.setFingerPrintEnabled(false)
            }
            EventBus.getDefault().postSticky(BusMessage(EventBusUtils.EVENT_UPDATE_FINGERPRINT))
        }
        switchPreventApplicationUninstall.isChecked = devicePolicyManager.isAdminActive(
            ComponentName(this, AdminReceiver::class.java)
        )
        switchPreventApplicationUninstall.setOnCheckedChangeListener { _, isChecked ->
            if (mOnMainListener?.onAppSelected() == false) {
                switchPreventApplicationUninstall.isChecked = false
                return@setOnCheckedChangeListener
            }
            preventApplicationUninstall(isChecked)
        }
        switchAskLockingNewApplication.isChecked = viewModel.isAskLockingNewApplication()
        switchAskLockingNewApplication.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAskLockingNewApplication(isChecked)
        }

        clSettingsPrivacyPolicy.setOnClickListener {
            startActivity(PolicyActivity.newIntent(this))
        }
        clSettingsRateApplication.setOnClickListener {
//            com.common.control.utils.CommonUtils.getInstance().rateApp( this)
            showRate(true)
        }
    }

    fun Context.showRate(isFinish: Boolean) {
        val dialog = RateAppDialog(this)
        dialog.setCallback(object : RateCallback {
            override fun onMaybeLater() {
                if (isFinish) {
                    RatePrefUtils.increaseCountRate(this@showRate)
                    (this@showRate as Activity).finish()
                }
            }

            override fun onSubmit(review: String) {
                Toast.makeText(
                    this@showRate,
                    "Thank you for reviewing...",
                    Toast.LENGTH_SHORT
                ).show()
                RatePrefUtils.setRated(this@showRate)
                if (isFinish) {
                    (this@showRate as Activity).finish()
                }
            }

            override fun onRate() {
                com.common.control.utils.CommonUtils.getInstance().rateApp(this@showRate)
                RatePrefUtils.setRated(this@showRate)
            }
        })
        dialog.show()
    }

    private fun isFingerprintChecked(): Boolean {
        return viewModel.isFingerPrintEnabled() && mFingerprintIdentify.isHardwareEnable && mFingerprintIdentify.isFingerprintEnable
    }

    private fun preventApplicationUninstall(isChecked: Boolean) {
        if (isChecked) {
            if (devicePolicyManager.isAdminActive(ComponentName(this, AdminReceiver::class.java))) {
                return
            }
            viewModel.setReload(false)
            viewModel.setLockSettingApp(AppLockerPreferences.LOCK_APP_WAIT)
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, ComponentName(this, AdminReceiver::class.java))
            intent.putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                getString(R.string.device_admin_explanation)
            )
            startActivityForResult(intent, Const.REQUEST_CODE_DEVICE_ADMIN)
        } else {
            if (!devicePolicyManager.isAdminActive(ComponentName(this, AdminReceiver::class.java))) {
                return
            }
            mConfirmDialog?.show()
            dialogLayout(mConfirmDialog)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Const.REQUEST_CODE_DEVICE_ADMIN -> {
                val checked = devicePolicyManager.isAdminActive(ComponentName(this, AdminReceiver::class.java))
                mOnMainListener?.onChangeStatusGroupLock()
                switchPreventApplicationUninstall.isChecked = checked
                if (PermissionChecker.checkFullPermission(this)) {
                    if (checked) {
                        viewModel.setReload(false)
                    } else {
                        viewModel.setFinish(true)
                    }
                }
            }
            Const.REQUEST_CODE_SECURITY_SETTINGS -> {
                mFingerprintIdentify.init()
                if (mFingerprintIdentify.isHardwareEnable) {
                    if (mFingerprintIdentify.isFingerprintEnable) {
                        viewModel.setFingerPrintEnabled(true)
                        switchSettingsFingerprint.isChecked = true
                    } else {
                        viewModel.setFingerPrintEnabled(false)
                        switchSettingsFingerprint.isChecked = false
                    }
                } else {
                    viewModel.setFingerPrintEnabled(false)
                    switchSettingsFingerprint.isChecked = false
                }
                viewModel.setLockSettingApp(AppLockerPreferences.LOCK_APP_ALWAYS)
                EventBus.getDefault().postSticky(BusMessage(EventBusUtils.EVENT_UPDATE_FINGERPRINT))
            }
            Const.REQUEST_CODE_CHANGE_PASSWORD -> {
                if (resultCode == Activity.RESULT_OK) {
                    Toasty.showToast(
                        this,
                        R.string.msg_password_has_been_changed,
                        Toasty.SUCCESS
                    )
                }
            }
            Const.REQUEST_CODE_CHECK_PASSWORD_AND_CHANGE_PASSWORD -> {
                if (resultCode == Activity.RESULT_OK) {
                    val intent = ChangePasswordActivity.newIntent(this)
                    startActivityForResult(intent, Const.REQUEST_CODE_CHANGE_PASSWORD)
                }
            }
        }
    }

    override fun onDestroy() {
        mConfirmDialog?.dismiss()
        mConfirmFingerprintDialog?.dismiss()
        mFingerprintIdentify.cancelIdentify()
        super.onDestroy()
    }


}