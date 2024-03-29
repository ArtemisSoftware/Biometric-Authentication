package com.artemissoftware.biometricauthentication

import android.app.KeyguardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CancellationSignal
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var cancelationSignal: CancellationSignal? = null

    private val authenticationCallback: BiometricPrompt.AuthenticationCallback
        get() =
            @RequiresApi(Build.VERSION_CODES.P)
            object : BiometricPrompt.AuthenticationCallback(){
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                    super.onAuthenticationError(errorCode, errString)
                    notifyUser("Authentication error: $errString")
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                    super.onAuthenticationSucceeded(result)

                    notifyUser("Authentication sucess")
                    startActivity(Intent(this@MainActivity, SecretActivity::class.java))
                }
            }


    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(checkBiometricSupport() == true) {

            btn_authenticate.setOnClickListener {

                val biometricPrompt: BiometricPrompt = BiometricPrompt.Builder(this)
                    .setTitle("The Biometric Prompt")
                    .setSubtitle("Authentication is required")
                    .setDescription("This app uses fingerprint protection to keep your data secure")
                    .setNegativeButton("Cancel", this.mainExecutor,
                        DialogInterface.OnClickListener { dialog, which ->
                            notifyUser("Authentication cancelled")
                        }).build()

                biometricPrompt.authenticate(getCancellationSignal(), mainExecutor, authenticationCallback)
            }
        }
    }

    private fun notifyUser(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    private fun getCancellationSignal(): CancellationSignal{
        cancelationSignal = CancellationSignal()
        cancelationSignal?.setOnCancelListener {
            notifyUser("Authentication was cancelled by the user")
        }

        return cancelationSignal as CancellationSignal
    }

    private fun checkBiometricSupport(): Boolean{
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        if(!keyguardManager.isKeyguardSecure){
            notifyUser("Fingerprint authentication has not been enabled in settings")
            return false
        }

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.USE_BIOMETRIC) != PackageManager.PERMISSION_GRANTED){
            notifyUser("Fingerprint authentication permission is not enabled")
            return false
        }

        return if(packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)){
            true
        }
        else true

    }
}