package com.igweze.ebi.fingerprinter

import android.content.Context
import android.support.design.widget.BottomSheetDialog
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.view.View
import kotlinx.android.synthetic.main.content_dialog_finger_print.*


open class FingerPrintDialog private constructor(builder: Builder) : BottomSheetDialog(builder.context, R.style.BaseBottomSheetDialog), View.OnClickListener {

    private var onCancel: () -> Unit = {}
    private lateinit var callback: FingerprintManagerCompat.AuthenticationCallback

    init {
        setDialogView()

        builder.apply {
            itemTitle.text = title
            itemSubtitle.text = subtitle
            itemDescription.text = description
            btnCancel.text = negativeButtonText
            this@FingerPrintDialog.callback = callback
            this@FingerPrintDialog.onCancel =  onCancel
        }

    }

    private fun setDialogView() {
        val bottomSheetView = layoutInflater.inflate(R.layout.content_dialog_finger_print, null)
        setContentView(bottomSheetView)
        btnCancel.setOnClickListener(this)
        setOnDismissListener { cancel() }
        updateLogo()
    }

    fun setTitle(title: String) {
        itemTitle.text = title
    }

    fun updateStatus(status: String) {
        itemStatus.text = status
    }

    fun setSubtitle(subtitle: String) {
        itemSubtitle.text = subtitle
    }

    fun setDescription(description: String) {
        itemDescription.text = description
    }

    fun setButtonText(negativeButtonText: String) {
        btnCancel.text = negativeButtonText
    }

    private fun updateLogo() {
        try {
            val drawable = context.packageManager.getApplicationIcon(context.packageName)
            imgLogo.setImageDrawable(drawable)

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun authenticate() {
        show() // show the dialog
        val fingerPrintManager = FingerPrintManager()
        fingerPrintManager.authenticateUser(context, callback)
    }


    override fun onClick(view: View) {
        dismiss()
        onCancel()
    }

    class Builder(val context: Context, val callback: FingerprintManagerCompat.AuthenticationCallback) {

        var title: String = ""
        var subtitle: String = ""
        var description: String = ""
        var negativeButtonText: String = ""
        var onCancel = {}

        fun setTitle(title: String): Builder {
            this.title = title
            return this
        }

        fun setSubtitle(subtitle: String): Builder {
            this.subtitle = subtitle
            return this
        }

        fun setDescription(description: String): Builder {
            this.description = description
            return this
        }

        fun onDimiss(onCancel: () -> Unit): Builder {
            this.onCancel = onCancel
            return this
        }

        fun setNegativeButtonText(negativeButtonText: String): Builder {
            this.negativeButtonText = negativeButtonText
            return this
        }

        fun build(): FingerPrintDialog {
            return FingerPrintDialog(this)
        }
    }
}