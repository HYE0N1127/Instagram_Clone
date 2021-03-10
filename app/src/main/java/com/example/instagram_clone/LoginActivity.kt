package com.example.instagram_clone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {
    var auth: FirebaseAuth? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        val loginButton = findViewById<Button>(R.id.email_login_btn)
        loginButton.setOnClickListener {
            signInAndSignUp()
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

    fun moveMainPage(user:FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}
