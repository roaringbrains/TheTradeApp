package com.india.thetradeapp.corefeatures

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.india.thetradeapp.databinding.ActivityPhoneLoginBinding
import java.util.concurrent.TimeUnit

class PhoneLoginActivity : AppCompatActivity()  {

    private lateinit var binding: ActivityPhoneLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?)
    {

        binding = ActivityPhoneLoginBinding.inflate(layoutInflater)
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()


        //binding = DataBindingUtil.setContentView(this,R.layout.activity_phone_login)
        
        super.onCreate(savedInstanceState)

        //setContentView(R.layout.activity_phone_login)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)


        binding.buttonLoginGetOtp.setOnClickListener{

            var textMobileNumber = binding.textLoginMobileNumber.text.toString()
            var textSignupCode = binding.textLoginSignupCode.text.toString()

            if(textMobileNumber.isNullOrEmpty()){
                Toast.makeText(this,"Enter a Mobile Number",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }else if(textMobileNumber.count() < 10 || textMobileNumber.count() > 10){
                Toast.makeText(this,"Enter a 10 Digit Mobile Number",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }else{
                textMobileNumber = "+91"+textMobileNumber
            }

            if(textSignupCode.isNotEmpty())
            {
                db.collection("SignUpCodes").document(textSignupCode).get()
                    .addOnSuccessListener{tasks ->

                        //If Signup code is valid then Check if it is mapped to a User if not then map it with current user.
                        Toast.makeText(this, "Logging in as " + tasks.get("ForUserType").toString(), Toast.LENGTH_LONG).show()
                        if(tasks.get("IsActive").toString() == "true")
                        {
                            if(tasks.get("IsReadWrite").toString() == "true")
                                startPhoneNumberVerification(textMobileNumber)
                            else
                                //Do nothing now
                                Toast.makeText(this, "Do nothing now", Toast.LENGTH_LONG).show()

                        }

                    }
                    .addOnFailureListener { it ->

                        Toast.makeText(this, "Signup code is Invalid. Please reach out to Admin", Toast.LENGTH_LONG).show()
                    }
            }
            else
            {

            }




        }

        binding.buttonVerify.setOnClickListener {

            verifyPhoneNumberWithCode(
                storedVerificationId,
                binding.textLoginEnterOtp!!.text.toString()
            )

        }

        binding.textLoginEnterOtp.isVisible = false
        binding.buttonVerify.isVisible = false




        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                Log.d(TAG, "onVerificationCompleted:$credential")
                signInWithPhoneAuthCredential(credential)

                binding.textLoginEnterOtp.isVisible = true
                binding.buttonVerify.isVisible = true

            }

            override fun onVerificationFailed(e: FirebaseException) {

                if (e is FirebaseAuthInvalidCredentialsException) {
                    //invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    //SMS quota has been exceeded
                }
                //Show a message and update UI


            }


            override fun onCodeSent(verificationId: String,token: PhoneAuthProvider.ForceResendingToken)
            {
                Log.d(TAG, "onCodeSent:$verificationId")
                storedVerificationId = verificationId
                resendToken = token
                binding.textLoginEnterOtp.isVisible = true
                binding.buttonVerify.isVisible = true
                binding.textLoginMobileNumber.isEnabled = false
                binding.textLoginSignupCode.isEnabled = false
            }
        }

        //binding.

    }

    override fun onStart()
    {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun startPhoneNumberVerification(phoneNumber: String)
    {

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
//        binding.textLoginEnterOtp.isVisible = true
//        binding.buttonVerify.isVisible = true
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String)
    {

        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)

        signInWithPhoneAuthCredential(credential)

    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential)
    {

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")

                    val user = task.result?.user
                    Toast.makeText(this, "Welcome to the Jungle : " + user, Toast.LENGTH_SHORT)
                        .show()
                } else {

                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        //The verification Code Entered was invalid
                    }
                    //update UI

                }
            }

    }


    private fun updateUI(user: FirebaseUser? = auth.currentUser)
    {
        // startActivity(Intent(this,HomeActivity::class.java))
    }

    companion object
    {
        private const val TAG = "PhoneLoginActivity"
    }
}