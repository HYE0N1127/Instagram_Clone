package com.example.instagram_clone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {
    var auth: FirebaseAuth? = null;
    var googleSignInClient : GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()

        val loginButton = findViewById<Button>(R.id.email_login_btn)
        val googleSignInBtn = findViewById<Button>(R.id.google_sign_in_button)

        loginButton.setOnClickListener {
            signInAndSignUp()
        }

        googleSignInBtn.setOnClickListener {
            //First Step
            googleLogin()
        }

        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("610353448877-bcnpjhjctt02puv4mmdvu0larc236oj9.apps.googleusercontent.com")
            .requestEmail().
            build()

        googleSignInClient = GoogleSignIn.getClient(this,gso)

    }

    fun googleLogin() {
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent,GOOGLE_LOGIN_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == GOOGLE_LOGIN_CODE) {
            var result : GoogleSignInResult? = Auth.GoogleSignInApi.getSignInResultFromIntent(data)

             if(result!! .isSuccess)
            //if(result?.isSuccess!!)
             {
                var account= result?.signInAccount
                //Second step
                firebaseAuthWithGoogle(account)
            }
        }
    }

    fun firebaseAuthWithGoogle(account : GoogleSignInAccount?) {
        var credential = GoogleAuthProvider.getCredential(account?.idToken,null)
        auth?.signInWithCredential(credential)

            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Login
                    moveMainPage(task.result?.user)
                } else {
                    //Show the error message
                    Toast.makeText(this, "", Toast.LENGTH_LONG).show()

                }
            }
    }

    fun signInAndSignUp() {
        auth?.createUserWithEmailAndPassword(
            findViewById<EditText>(R.id.email_edittext).text.toString(),
            findViewById<EditText>(R.id.password_edittext).text.toString()
        )
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //creating a user account
                    moveMainPage(task.result?.user)
                } else if (!task.exception?.message.isNullOrEmpty()) {
                    // Show Error message
                    Toast.makeText(this, "", Toast.LENGTH_LONG).show()
                } else {
                    //Login if you have account
                }
            }
    }

    fun signInEmail() {
        auth?.createUserWithEmailAndPassword(
            findViewById<EditText>(R.id.email_edittext).text.toString(),
            findViewById<EditText>(R.id.password_edittext).text.toString()
        )
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Login
                    moveMainPage(task.result?.user)
                } else {
                    //Show the error message
                    Toast.makeText(this, "", Toast.LENGTH_LONG).show()

                }
            }
    }

    fun moveMainPage(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}
