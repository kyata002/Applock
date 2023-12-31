package com.mtg.applock.service.stateprovider

import android.content.Context
import com.mtg.applock.permissions.PermissionChecker
import io.reactivex.Flowable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PermissionCheckerObservable @Inject constructor(val context: Context) {
    fun get(): Flowable<Boolean> {
        return Flowable.interval(30, TimeUnit.MINUTES).map { PermissionChecker.checkUsageAccessPermission(context).not() }
    }
}