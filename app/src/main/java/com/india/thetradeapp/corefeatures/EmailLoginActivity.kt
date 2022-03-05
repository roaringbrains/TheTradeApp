package com.india.thetradeapp.corefeatures

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.india.thetradeapp.R
//import kotlinx.android.synthetic.main.activity_email_login.*

class EmailLoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_login)

        // Initialize Firebase Auth
//        auth = Firebase.auth
//
//
////        Register.setOnClickListener{
////            var intent  = Intent(this, RegisterActivity::class.java)
////            startActivity(intent)
////            finish()
////        }
//
//        Login.setOnClickListener{
//
//            if(checking()){
//                val email = ContactsContract.CommonDataKinds.Email.text.toString()
//                val password = Password.text.toString()
//                auth.signInWithEmailAndPassword(email,password)
//                    .addOnCompleteListener(this){
//                            task->
//                        if (task.isSuccessful){
//
////                            var intent  = Intent(this, LoggedIn::class.java)
////                            intent.putExtra("Email",email)
////                            startActivity(intent)
////                            finish()
//                            Toast.makeText(this,"Login Successful",Toast.LENGTH_LONG).show()
//                        }else{
//                            Toast.makeText(this,"Wrong Details", Toast.LENGTH_LONG).show()
//                        }
//                    }
//                //.addOnFailureListener() //not necessary now
//
//            }else{
//                Toast.makeText(this,"Enter the Details", Toast.LENGTH_LONG).show()
//            }
//
//
//        }

    }

//    private fun checking():Boolean{
//
//        if(ContactsContract.CommonDataKinds.Email.text.toString().trim{it<= ' '}.isNullOrEmpty()
//            && Password.text.toString().trim{it<= ' '}.isNullOrEmpty())
//        {
//            return false
//        }
//        return true
//    }
}