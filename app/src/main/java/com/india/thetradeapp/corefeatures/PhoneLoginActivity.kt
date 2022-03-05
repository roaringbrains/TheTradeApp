package com.india.thetradeapp.corefeatures

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.india.thetradeapp.R
import com.india.thetradeapp.databinding.ActivityPhoneLoginBinding
import java.util.concurrent.TimeUnit
import kotlin.coroutines.EmptyCoroutineContext.plus

class PhoneLoginActivity : AppCompatActivity()  {

    private lateinit var binding: ActivityPhoneLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityPhoneLoginBinding.inflate(layoutInflater)
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()


        //binding = DataBindingUtil.setContentView(this,R.layout.activity_phone_login)
        
        super.onCreate(savedInstanceState)

        //setContentView(R.layout.activity_phone_login)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)


        //binding.editTextPhoneEnterOtp.setText("saurav")


        binding.buttonLoginGetOtp.setOnClickListener{

            var mobileNumber = binding.textLoginMobileNumber.text.toString()

            if(mobileNumber.isNullOrEmpty()){
                Toast.makeText(this,"Enter a Mobile Number",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }else if(mobileNumber.count() < 10 || mobileNumber.count() > 10){
                Toast.makeText(this,"Enter a 10 Digit Mobile Number",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }else{
                mobileNumber = "+91"+mobileNumber
            }




            //First Check is the Signup Code is Valid entered or not, If not entered then the users is a Retailer
            if(binding.textLoginSignupCode.text.toString().isNotEmpty()){

                //The user is Supplier or Trader, Now check if the Signup Code is Valid or not.
                val allSignupCodes = db.collection("SignUpCodes")

                allSignupCodes.whereEqualTo("SignupCode",binding.textLoginSignupCode.text.toString())
                    .whereEqualTo("IsActive",true)
                    .whereEqualTo("IsReadWrite",true).get()
                    .addOnSuccessListener {task ->

                            //If no Signup records are found
                            if(task.isEmpty){
                                //Display a message that signup code is not valid
                                Toast.makeText(this,"Signup code is Invalid. Please reach out to Admin",Toast.LENGTH_LONG).show()
                            }else{

                                //If Signup code is valid then Check if it is mapped to a User if not then map it with current user.
//                                db.collection("UserSignUpCodeMapping").whereEqualTo("SignupCode",binding.textLoginSignupCode.text.toString())
//                                    .whereEqualTo("MobileNumber",bindi)
                                Toast.makeText(this,"Logging in as Supplier or Trader",Toast.LENGTH_LONG).show()
                                //If Signup code Exists then proceed with Mobile number Auth and verification.
                                startPhoneNumberVerification(mobileNumber)

                            }
                    }
                    .addOnFailureListener {

                        Toast.makeText(this,"Something broke while retrieving Signup Code",Toast.LENGTH_LONG).show()
                    }


            }else{

                Toast.makeText(this,"Logging in as Retailer",Toast.LENGTH_LONG)
                startPhoneNumberVerification(mobileNumber)
                //startPhoneNumberVerification(binding.textLoginMobileNumber!!.text.toString())
            }

                //binding.editTextPhoneEnterOtp.setText("saurav1")

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


            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
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

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {

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

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {

        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)

        signInWithPhoneAuthCredential(credential)

    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {

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


    private fun updateUI(user: FirebaseUser? = auth.currentUser) {
        // startActivity(Intent(this,HomeActivity::class.java))
    }

    companion object {
        private const val TAG = "PhoneLoginActivity"
    }
}