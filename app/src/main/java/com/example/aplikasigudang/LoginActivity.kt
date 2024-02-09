package com.example.aplikasigudang

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.Toast
import com.example.aplikasigudang.database.DatabaseHelper
import com.example.aplikasigudang.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


    class LoginActivity : AppCompatActivity() {

        private lateinit var binding: ActivityLoginBinding
        private lateinit var sharedPreferences: SharedPreferences
        private lateinit var auth: FirebaseAuth
        private lateinit var firestore: FirebaseFirestore

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityLoginBinding.inflate(layoutInflater)
            setContentView(binding.root)

            auth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()
            sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

            val text = "Belum Punya Akun?, Register Di Sini !!"
            val spannableString = SpannableString(text)
            val startIndex = text.indexOf("Register Di Sini !!")
            val endIndex = startIndex + "Register Di Sini !!".length
            spannableString.setSpan(StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableString.setSpan(ForegroundColorSpan(Color.YELLOW), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            binding.tvatau.text = spannableString

            binding.tvatau.setOnClickListener {
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
            }

            binding.loginUtama.setOnClickListener {
                val email = binding.inputUserEdit.text.toString()
                val password = binding.inputPwEdit.text.toString()

                if (email.isNotEmpty() && password.isNotEmpty()) {
                    loginUser(email, password)
                } else {
                    showToast("Form Tidak Boleh Kosong !!.")
                }
            }

            // Check if user is already logged in
            val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
            if (isLoggedIn) {
                val email = sharedPreferences.getString("email", "")
                val password = sharedPreferences.getString("password", "")
                loginUser(email!!, password!!)
            }
        }

        private fun loginUser(email: String, password: String) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Save user info to Shared Preferences
                        sharedPreferences.edit()
                            .putBoolean("isLoggedIn", true)
                            .putString("email", email)
                            .putString("password", password)
                            .apply()

                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            checkUserRoleAndRedirect(userId)
                        }
                    } else {
                        showToast("Login failed. Please check your credentials.")
                    }
                }
        }

        private fun showToast(message: String) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

    private fun checkUserRoleAndRedirect(userId: String) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val userRole = document.getString("divisi")
                when (userRole) {
                    "ktu" -> {
                        val intent = Intent(this@LoginActivity, ListPeminjaman::class.java)
                        intent.putExtra("userId", userId)
                        startActivity(intent)
                        finish()
                    }
                    "manajer" ->{
                        val intent = Intent(this@LoginActivity, ListPeminjamanManajer::class.java)
                        intent.putExtra("userId", userId)
                        startActivity(intent)
                        finish()
                    }
                    "kepala gudang" ->{
                        val intent = Intent(this@LoginActivity, ListKepalaGudang::class.java)
                        intent.putExtra("userId", userId)
                        startActivity(intent)
                        finish()
                    }
                    else -> {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("userId", userId)
                        startActivity(intent)
                        finish()
                    }
                }

            }
            .addOnFailureListener { exception ->
                showToast("Failed to fetch user data.")
            }
    }

}