package com.example.aplikasigudang

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import com.example.aplikasigudang.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        buttonMinyak()
        buttonBarang()
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        val username = intent.getStringExtra("username")

        binding.textView.text = "Selamat Datang di Sistem Peminjaman Gudang"

        binding.btnLogout.setOnClickListener{
            logoutUser()

        }

    }

    fun buttonMinyak () {
        val buttonminyakpress = binding.btnMinyak
        buttonminyakpress.setOnClickListener{
            val intentminyak = Intent(this@MainActivity, FormBahanBakar::class.java)
            startActivity(intentminyak)
        }
    }

    fun buttonBarang () {
        val buttonbarangpress = binding.btnBarang
        buttonbarangpress.setOnClickListener{
            val intentbarang = Intent(this@MainActivity, FormBarang::class.java)
            startActivity(intentbarang)
        }
    }

    private fun logoutUser() {
        auth.signOut()

        sharedPreferences.edit()
            .putBoolean("isLoggedIn", false)
            .remove("email")
            .remove("password")
            .apply()

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

}