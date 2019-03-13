package com.igweze.ebi.fingerprinter

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import android.support.v4.app.ActivityCompat
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat





/// check if biometric prompt is enabled: only on Android Pie
fun isBiometricPromptEnabled(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

/// check if biometric authentication is supported: only Marshmallow and above
fun isSdkVersionSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

/// check if device has fingerprint sensors: required only if android.hardware.fingerprint is not marked as required in manifest
fun isHardwareSupported(context: Context): Boolean = FingerprintManagerCompat.from(context).isHardwareDetected

/// check if user has previously registered any finger prints to match with
fun isFingerprintAvailable(context: Context): Boolean = FingerprintManagerCompat.from(context).hasEnrolledFingerprints()

/// check if user granted app biometric auth permissions
@TargetApi(Build.VERSION_CODES.M)
fun isPermissionGranted(context: Context): Boolean = ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED

/// show biometric prompt: only on Android Pie
@TargetApi(Build.VERSION_CODES.P)
fun showBiometricDialog(context: Context, callback: BiometricPrompt.AuthenticationCallback, onCancel: () -> Unit) = context.apply {
    BiometricPrompt.Builder(this)
            .setTitle(getString(R.string.title_login))
            .setSubtitle(getString((R.string.title_login_description)))
            .setDescription(getString(R.string.title_biometric_prompt))
            .setNegativeButton(getString(android.R.string.cancel), mainExecutor, DialogInterface.OnClickListener { dialog, _ ->
                onCancel()
                dialog.dismiss()
            })
            .build()
            .authenticate(CancellationSignal(), mainExecutor, callback)
}

/// show custom finger print dialog for Marshmallow and above
@TargetApi(Build.VERSION_CODES.M)
fun showFingerprintDialog(context: Context, callback: FingerprintManagerCompat.AuthenticationCallback, onCancel: () -> Unit) = context.let {
    FingerPrintDialog.Builder(context, callback)
            .setTitle(it.getString(R.string.title_login))
            .setSubtitle(it.getString((R.string.title_login_description)))
            .setDescription(it.getString(R.string.title_biometric_prompt))
            .setNegativeButtonText(it.getString(android.R.string.cancel))
            .onDimiss(onCancel)
            .build().apply {
                authenticate()
            }
}


fun canPerformBiometricAuth(context: Context,  biometricCallback: BiometricCallback): Boolean {

    return when {
        !isSdkVersionSupported() -> {
            biometricCallback.onSdkVersionNotSupported()
            false
        }
        !isPermissionGranted(context) -> {
            biometricCallback.onBiometricAuthenticationPermissionNotGranted()
            false
        }
        !isHardwareSupported(context) -> {
            biometricCallback.onBiometricAuthenticationNotSupported()
            false
        }
        !isFingerprintAvailable(context) -> {
            biometricCallback.onBiometricAuthenticationNotAvailable()
            false
        }
        else -> true
    }
}
