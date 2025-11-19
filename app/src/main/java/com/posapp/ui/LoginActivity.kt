package com.posapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.posapp.data.R


class LoginActivity : AppCompatActivity() {

    private lateinit var btnLogin: MaterialButton

    private lateinit var tilUsername: TextInputLayout
    private lateinit var tilPassword: TextInputLayout

    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initializeViews()

        btnLogin.setOnClickListener {
            handleLogin()
        }
    }

    private fun initializeViews() {
        btnLogin = findViewById(R.id.btnLogin)

        tilUsername = findViewById(R.id.tilUsername)
        tilPassword = findViewById(R.id.tilPassword)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
    }

    private fun handleLogin() {
        val email = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()
        var isValid = true

        if (email.isEmpty()) {
            tilUsername.error = "Email is required"
            isValid = false
        } else {
            tilUsername.error = null
        }

        if (password.isEmpty()) {
            tilPassword.error = "Password is required"
            isValid = false
        } else {
            tilPassword.error = null
        }

        if (isValid) {
            if (email =="admin" && password=="admin") {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                tilUsername.error = "Invalid credentials"
                tilPassword.error = "Invalid credentials"
            }
        }
    }
}