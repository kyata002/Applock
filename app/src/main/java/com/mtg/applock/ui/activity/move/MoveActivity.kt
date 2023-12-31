package com.mtg.applock.ui.activity.move

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import com.common.control.interfaces.AdCallback
import com.common.control.manager.AdmobManager
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.mtg.applock.BuildConfig
import com.mtg.applock.R
import com.mtg.applock.model.EncryptorModel
import com.mtg.applock.model.ItemDetail
import com.mtg.applock.ui.adapter.move.MoveAdapter
import com.mtg.applock.ui.base.BaseActivity
import com.mtg.applock.util.Const
import com.mtg.applock.util.extensions.dialogLayout
import com.mtg.applock.util.extensions.removeBlink
import kotlinx.android.synthetic.main.activity_move.*
import kotlinx.android.synthetic.main.dialog_success_move.view.*
import java.util.*

class MoveActivity : BaseActivity<MoveViewModel>() {
    private var mEncryptor: EncryptorModel? = null
    private var mMoveAdapter: MoveAdapter? = null
    private var mType: Int = Const.TYPE_IMAGES
    private var interAds : InterstitialAd? = null
    private var mMoveList = mutableListOf<ItemDetail>()
    private var mSuccessDialog: AlertDialog? = null
    private var mIsRunning = true

    override fun getViewModel(): Class<MoveViewModel> {
        return MoveViewModel::class.java
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_move
    }

    override fun initViews() {
        setSupportActionBar(toolbar);

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)

        }
        toolbar.setNavigationOnClickListener { onBackPressed() }
        mType = intent.getIntExtra(Const.EXTRA_TYPE, Const.TYPE_IMAGES)
        mEncryptor = intent.getSerializableExtra(Const.EXTRA_DATA) as EncryptorModel?
        mEncryptor?.let {
            when (it.type) {
                Const.TYPE_DECODE -> {
                    toolbar.setTitle(R.string.title_move_out)
                }
                Const.TYPE_ENCODE -> {
                    toolbar.setTitle(R.string.title_move_in)
                }
            }
            val itemDetailList = it.itemDetailList
            val pathList = mutableListOf<String>()
            if (!itemDetailList.isNullOrEmpty()) {
                for (itemDetail in itemDetailList) {
                    itemDetail.isSelected = false
                    pathList.add(itemDetail.path)
                }
                mMoveList.clear()
                mMoveList.addAll(itemDetailList)
            }
            // setup data
            mMoveAdapter = MoveAdapter(this, mMoveList)
            recyclerMove.adapter = mMoveAdapter
            recyclerMove.layoutManager = GridLayoutManager(this, getCountWithType())
            recyclerMove.removeBlink()
            viewModel.move(this, it, pathList, mType)
        }
        buildSuccessDialog()
        viewModel.getMoveFinishedLiveData().observe(this, {
            if (it) {
                finishedProgress()
            }
        })
        viewModel.getProgressLiveData().observe(this, {
            updateProgress(it.percentage, it.path)
        })

    }

    private fun buildSuccessDialog() {
        val builder = AlertDialog.Builder(this)
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_success_move, null, false)
        builder.setView(view)
        builder.setCancelable(false)
        mSuccessDialog?.dismiss()
        mSuccessDialog = builder.create()
        mSuccessDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.btnOkSuccessMove.setOnClickListener {
            if(interAds!=null){
                showInter()
            }
        }

    }
    private fun loadInter(){
        AdmobManager.getInstance().loadInterAds(this,BuildConfig.inter_lock_personal,object :
            AdCallback() {
            override fun onResultInterstitialAd(interstitialAd: InterstitialAd?) {
                super.onResultInterstitialAd(interstitialAd)
                interAds = interstitialAd
            }
        })
    }
    private fun showInter(){
        AdmobManager.getInstance().showInterstitial(this,interAds,object :AdCallback(){
            override fun onAdClosed() {
                super.onAdClosed()
                onBackPressed()
            }
        })
    }

    private fun getCountWithType(): Int {
        var count = 4
        when (mType) {
            Const.TYPE_IMAGES, Const.TYPE_VIDEOS -> {
                count = 4
            }
            Const.TYPE_AUDIOS, Const.TYPE_FILES -> {
                count = 1
            }
        }
        return count
    }

    override fun onResume() {
        super.onResume()
        loadInter()
    }
    override fun onBackPressed() {
        if (mIsRunning) {
            return
        }
        mSuccessDialog?.dismiss()
        val intent = Intent()
        intent.putExtra(Const.EXTRA_TYPE, mType)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun updateProgress(value: Int, path: String) {
        mMoveList[value - 1].isSelected = true
        mMoveList[value - 1].path = path
        runOnUiThread {
            mMoveAdapter?.notifyItemChanged(value - 1)
            mEncryptor?.let {
                when (it.type) {
                    Const.TYPE_DECODE -> {
                        toolbar.setTitle(String.format(Locale.getDefault(), "%s (%d/%d)", getString(R.string.title_move_out), value, mMoveList.size))
                    }
                    Const.TYPE_ENCODE -> {
                        toolbar.setTitle(String.format(Locale.getDefault(), "%s (%d/%d)", getString(R.string.title_move_in), value, mMoveList.size))
                    }
                }
            }
        }
    }

    private fun finishedProgress() {
        mIsRunning = false
        if (isFinishing) return
        recyclerMove.post {
            mSuccessDialog?.show()
            dialogLayout(mSuccessDialog)
        }
    }

    override fun onDestroy() {
        mSuccessDialog?.dismiss()
        super.onDestroy()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, MoveActivity::class.java)
        }
    }
}