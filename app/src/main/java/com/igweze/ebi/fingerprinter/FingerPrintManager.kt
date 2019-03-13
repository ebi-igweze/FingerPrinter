package com.igweze.ebi.fingerprinter

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.support.v4.os.CancellationSignal
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey


@TargetApi(Build.VERSION_CODES.M)
class FingerPrintManager {

    private lateinit var keyStore: KeyStore
    private lateinit var cipher: Cipher

    /// generate cryptography keys: used for authentication on device below Android Pie
    @TargetApi(Build.VERSION_CODES.M)
    private fun generateKey() {
        try {

            keyStore = KeyStore.getInstance(ANDROID_KEYSTORE_PROVIDER)
            // load android key store instance
            keyStore.load(null)
            // key generator parameter spec

            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE_PROVIDER)
            val paramSpec = KeyGenParameterSpec
                    .Builder(KEY_NAME, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    // set encryption mode
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    // only permit the private key to be used if the user is authenticated
                    .setUserAuthenticationRequired(true)
                    // set padding for encryption block
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build()

            keyGenerator.init(paramSpec)

            keyGenerator.generateKey()

        } catch (exc: KeyStoreException) {
            exc.printStackTrace()
        } catch (exc: NoSuchAlgorithmException) {
            exc.printStackTrace()
        } catch (exc: NoSuchProviderException) {
            exc.printStackTrace()
        } catch (exc: InvalidAlgorithmParameterException) {
            exc.printStackTrace()
        } catch (exc: CertificateException) {
            exc.printStackTrace()
        } catch (exc: IOException) {
            exc.printStackTrace()
        }
    }


    /* This initialisation of Cipher object will be used to create CryptoObject instance.
     * While initialising cipher, the generated and the stored key in the keystore container is used.
     * If the cipher is successfully initialised, then we can assume that the previously stored key
     * is not invalidated and it can still be used.
     */
    @TargetApi(Build.VERSION_CODES.M)
    private fun initCipher(): Boolean {
        try {
            val transacformation = "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}"
            cipher = Cipher.getInstance(transacformation)
        } catch (ex: NoSuchPaddingException) {
            throw RuntimeException("Failed to get Cipher", ex)
        }

        return try {
            keyStore.load(null)

            val secretKey = keyStore.getKey(KEY_NAME, null) as SecretKey
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            true
        } catch (e: KeyPermanentlyInvalidatedException) {
            false
        } catch (e: KeyStoreException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: CertificateException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: UnrecoverableKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: IOException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: InvalidKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        }
    }

    /* The fingerprint scanner will use the CryptoObject to help authenticate the
     * results of a fingerprint scan. The CryptoObject is used to ensure that the
     * fingerprint authentication result was not tampered with.
     * Step 1: Instantiate a FingerprintManagerCompat and call the authenticate method.
     *
     * The authenticate method requires the:
     * 1. cryptoObject
     * 2. The second parameter is always zero.
     *    The Android documentation identifies this as set of flags and is most likely
     *    reserved for future use.
     * 3. The third parameter, cancellationSignal is an object used to turn off the
     *    fingerprint scanner and cancel the current request.
     * 4. The fourth parameter is a class that subclasses the AuthenticationCallback abstract class.
     *    This will be the same as the BiometricAuthenticationCallback
     * 5. The fifth parameter is an optional Handler instance.
     *    If a Handler object is provided, the FingerprintManager will use the Looper from that
     *    object when processing the messages from the fingerprint hardware.
     */
    fun authenticateUser(context: Context, callback: FingerprintManagerCompat.AuthenticationCallback) {
        generateKey()

        if (initCipher()) {
            val cryptoObject = FingerprintManagerCompat.CryptoObject(cipher)
            FingerprintManagerCompat.from(context).authenticate(cryptoObject, 0, CancellationSignal(), callback, null)
        }
    }


    companion object {
        private const val ANDROID_KEYSTORE_PROVIDER = "AndroidKeyStore"
        private val KEY_NAME = UUID.randomUUID().toString()
    }
}