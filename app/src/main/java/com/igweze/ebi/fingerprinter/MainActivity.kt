package com.igweze.ebi.fingerprinter

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BiometricCallback {

    private var fingerPrintDialog: FingerPrintDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loginButton.setOnClickListener { displayBiometricDialog() }
    }

    @SuppressLint("NewApi")
    private fun displayBiometricDialog() {
        if(canPerformBiometricAuth(this, this)) {
            if (isBiometricPromptEnabled()) showBiometricDialog(this, BiometricPromptCallback(), ::onAuthenticationCancelled)
            else showFingerprintDialog(this, FingerPrintCallback(), ::onAuthenticationCancelled).also { dialog -> fingerPrintDialog = dialog }
        }
    }

    override fun onAuthenticationCancelled() = toast(getString(R.string.biometric_cancelled))

    override fun onSdkVersionNotSupported() = toast(getString(R.string.sdk_not_supported))

    override fun onBiometricAuthenticationNotSupported() = toast(getString(R.string.hardware_not_supported))

    override fun onBiometricAuthenticationNotAvailable() = toast(getString(R.string.no_fingerprint_available))

    override fun onBiometricAuthenticationPermissionNotGranted() = toast("Please grant the app permission to for finger print reading")

    override fun onBiometricAuthenticationInternalError(error: String) = toast(error)

    fun onAuthFailed() = toast(getString(R.string.biometric_failed))

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    inner class BiometricPromptCallback: BiometricPrompt.AuthenticationCallback() {

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult ) {
            super.onAuthenticationSucceeded(result)
            toast("Login Successful")
        }

        override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence) {
            super.onAuthenticationHelp(helpCode, helpString)
            toast(helpString.toString())
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence ) {
            super.onAuthenticationError(errorCode, errString)
            toast(errString.toString())
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            onAuthFailed()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    inner class FingerPrintCallback : FingerprintManagerCompat.AuthenticationCallback() {
        override fun onAuthenticationError(errMsgId: Int, errString: CharSequence?) {
            super.onAuthenticationError(errMsgId, errString)
            fingerPrintDialog?.updateStatus(errString.toString())
            toast(errString.toString())
        }

        override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence?) {
            super.onAuthenticationHelp(helpMsgId, helpString)
            fingerPrintDialog?.updateStatus(helpString.toString())
            toast(helpString.toString())
        }

        override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult?) {
            super.onAuthenticationSucceeded(result)
            fingerPrintDialog?.dismiss()
            toast("Login Successful")
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            fingerPrintDialog?.updateStatus(getString(R.string.biometric_failed))
            onAuthFailed()
        }
    }

}
