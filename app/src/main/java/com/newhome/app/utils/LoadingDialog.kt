package com.newhome.app.utils

import android.app.Activity
import android.app.AlertDialog
import android.graphics.drawable.ColorDrawable
import com.newhome.app.R

class LoadingDialog(private val activity: Activity) {
    var dialog: AlertDialog? = null

    fun start(cancelable: Boolean = false) {
        val builder = AlertDialog.Builder(activity)

        builder.setView(
            activity.layoutInflater.inflate(
                R.layout.loading_animation,
                activity.findViewById(R.id.content)
            )
        )
        builder.setCancelable(cancelable)

        dialog = builder.create()
        dialog?.show()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
    }

    fun stop() {
        dialog?.dismiss()
    }
}
