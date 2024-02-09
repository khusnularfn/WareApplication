package com.example.aplikasigudang

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.aplikasigudang.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val db = Firebase.firestore

        binding.registerUtama.setOnClickListener {
            val name = binding.inputNamaEdit.text.toString()
            val divisi = binding.inputDivisiEdit.text.toString()
            val email = binding.inputUserEdit.text.toString()
            val password = binding.inputPwEdit.text.toString()

            if (name.isNotEmpty() && divisi.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                registerUser(name, divisi, email, password)

            } else {
                Toast.makeText(this, "Tidak Boleh Kosong", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser(name: String, divisi: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registration successful
                    val userId = auth.currentUser?.uid
                    Log.d(TAG, "Registrasi Berhasil")

                    if (userId != null) {
                        saveUserInfo(userId, name, divisi)
                    }
                } else {
                    // Registration failed
                    Log.d(TAG, "Registrasi Gagal")
                    Toast.makeText(this, "Registrasi Gagal", Toast.LENGTH_SHORT).show()
                    // Handle error and show appropriate message to the user.
                }
            }
    }

    private fun saveUserInfo(userId: String, name: String, divisi: String) {
        val user = hashMapOf(
            "name" to name,
            "divisi" to divisi,

        )

        firestore.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
            Log.d(TAG, "Success added")

                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()

                // User information saved successfully
                // You might want to navigate to the next activity or show a success message.
            }
            .addOnFailureListener {

                Log.d(TAG, "Failed added")
                // User information save failed
                // Handle error and show appropriate message to the user.
            }
    }


}