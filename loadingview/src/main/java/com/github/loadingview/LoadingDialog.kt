package com.github.loadingview

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater

class LoadingDialog private constructor(internal var activity: Activity) {

    internal var isShowing = false
    internal var loadingView: LoadingView? = null
    internal var alertDialog: AlertDialog? = null

    fun show(): LoadingDialog {
        if (!isShowing && alertDialog != null) {
            isShowing = true
            try {
                alertDialog!!.window!!.setBackgroundDrawable(ColorDrawable(0))
                alertDialog!!.window!!.attributes.windowAnimations = R.style.DialogAnimations
            } catch (e: Exception) {

            }

            alertDialog!!.show()
            if (loadingView != null)
                loadingView!!.start()
        }
        return this
    }

    fun hide(): LoadingDialog {
        isShowing = false
        if (loadingView != null)
            loadingView!!.stop()
        if (alertDialog != null)
            alertDialog!!.dismiss()
        return this
    }

    companion object {

        operator fun get(activity: Activity): LoadingDialog {
            val dialog = LoadingDialog(activity)
            val builder = AlertDialog.Builder(dialog.activity)
            val v = LayoutInflater.from(dialog.activity).inflate(R.layout.loading_view, null)
            dialog.loadingView = v.findViewById(R.id.loading_view)
            builder.setView(v)
            builder.setCancelable(false)
            dialog.alertDialog = builder.create()
            return dialog
        }
    }
}