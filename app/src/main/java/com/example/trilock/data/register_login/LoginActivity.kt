package com.example.trilock.data.model

import android.app.KeyguardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Message
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import com.example.trilock.R
import com.example.trilock.data.register_login.MainActivity
import com.example.trilock.data.register_login.RegisterActivity
import com.google.firebase.ktx.Firebase
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {

    lateinit var sharedPreferences: SharedPreferences
    var isSwitched = false
    val PREFS_FILENAME = "SHARED_PREF"

    private var cancellationSignal: CancellationSignal? = null

    private val authenticationCallback: BiometricPrompt.AuthenticationCallback
    get() =
        @RequiresApi(Build.VERSION_CODES.P)
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                super.onAuthenticationError(errorCode, errString)
                toast("Authentication error: $errString")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                super.onAuthenticationSucceeded(result)
                toast("Authentication succeeded")
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                toast("Authentication Failed")
            }
        }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val textViewRegister = findViewById<TextView>(R.id.textViewRegister)

        textViewRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        val imageViewFingerprint = findViewById<ImageView>(R.id.image_view_fingerprint)
        imageViewFingerprint.isGone = true

        sharedPreferences = getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        isSwitched = sharedPreferences.getBoolean("SWITCH", false)
        Log.i("LoginActivity: ", isSwitched.toString())
        if (isSwitched) {

            imageViewFingerprint.isGone = false
            checkBiometricSupport()
            val biometricPrompt = BiometricPrompt.Builder(this)
                    .setTitle("Fingerprint Authentication")
                    .setSubtitle("Authorized Fingerprint Required")
                    .setDescription("Scan your fingerprint or press cancel to log in with email and password")
                    .setNegativeButton("Cancel",this.mainExecutor, DialogInterface.OnClickListener { dialog, which ->
                        toast("Authentication cancelled")
                    }).build()

            biometricPrompt.authenticate(getCancellationSignal(), mainExecutor, authenticationCallback)
        } else {
            Toast.makeText(this, "Biometric authentication is turned off", LENGTH_SHORT).show()
            // ALTERNATIVE LOG IN METHOD HERE
        }

        val buttonLogIn = findViewById<Button>(R.id.button_login)
        buttonLogIn?.setOnClickListener()
        {
            //Check if credentials match and user has verified their email

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        imageViewFingerprint.setOnClickListener(){
            checkBiometricSupport()
            val biometricPrompt = BiometricPrompt.Builder(this)
                    .setTitle("Fingerprint Authentication")
                    .setSubtitle("Authorized Fingerprint Required")
                    .setDescription("Scan your fingerprint or press cancel to log in with email and password")
                    .setNegativeButton("Cancel",this.mainExecutor, DialogInterface.OnClickListener { dialog, which ->
                        toast("Authentication cancelled")
                    }).build()

            biometricPrompt.authenticate(getCancellationSignal(), mainExecutor, authenticationCallback)
        }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, LENGTH_SHORT).show()
    }

    private fun getCancellationSignal(): CancellationSignal {
        cancellationSignal = CancellationSignal()
        cancellationSignal?.setOnCancelListener {
            toast("Authentication cancelled")
        }
        return cancellationSignal as CancellationSignal
    }

    private fun checkBiometricSupport(): Boolean {

        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        if (!keyguardManager.isKeyguardSecure) {
            toast("Fingerprint not enabled on this device")
        }

        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.USE_BIOMETRIC) != PackageManager.PERMISSION_GRANTED) {
            toast("Fingerprint permissions disabled")
        }

        return if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            true
        } else true
    }
}
