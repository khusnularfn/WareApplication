package com.example.aplikasigudang

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikasigudang.adapter.FileAdapter
import com.example.aplikasigudang.databinding.ActivityListPeminjamanBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

class ListPeminjaman : AppCompatActivity(), FileAdapter.FileItemClickListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityListPeminjamanBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var sharedPreferences: SharedPreferences
    private val storageReference: StorageReference = FirebaseStorage.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListPeminjamanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        recyclerView = binding.rvItem

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val folderReference = storageReference.child("uploaded_files")

        folderReference.listAll().addOnSuccessListener { listResult ->

            val fileList = listResult.items // Daftar file dari folder
            val adapter = FileAdapter(fileList, this) // Set this as the FileItemClickListener
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(this)
        }.addOnFailureListener { exception ->
            // Handle error
        }

        binding.btnLogout.setOnClickListener {
            logoutUser()
        }

    }

    override fun onButtonMerahClick(position: Int) {
        // Di sini, lakukan tindakan yang Anda inginkan saat tombol merah ditekan,
        // seperti menghapus item dari daftar
        val adapter = recyclerView.adapter as FileAdapter
        val fileList = adapter.fileList
        val fileToDelete = fileList[position]

//        val uploaderUserId = fileToDelete.metadata?.getMetadata?.get("createdBy")

        // Hapus file dari Firebase Storage
        fileToDelete.delete().addOnSuccessListener {
            // File berhasil dihapus, hapus juga dari daftar RecyclerView
            fileList.removeAt(position)
            adapter.notifyItemRemoved(position)

        }.addOnFailureListener { exception ->
            // Handle error
        }
    }

    override fun onButtonBiruClick(position: Int) {
        val adapter = recyclerView.adapter as FileAdapter
        val fileList = adapter.fileList
        val fileToMove = fileList[position]

        val sourcePath = "uploaded_files/${fileToMove.name}" // Path as per your current structure
        val destinationPath = "manajer_files/${fileToMove.name}" // New path where you want to move the file

        val sourceReference = storageReference.child(sourcePath)
        val destinationReference = storageReference.child(destinationPath)

        sourceReference.downloadUrl.addOnSuccessListener { sourceUri ->
            val sourceFile = File.createTempFile("temp", ".pdf")
            val downloadTask = sourceReference.getFile(sourceFile)

            downloadTask.addOnSuccessListener {
                destinationReference.putFile(Uri.fromFile(sourceFile)).addOnSuccessListener {
                    sourceReference.delete().addOnSuccessListener {
                        // File moved successfully, update the RecyclerView
                        fileList.removeAt(position)
                        adapter.notifyItemRemoved(position)
                    }.addOnFailureListener { deleteException ->
                        // Handle delete failure
                    }
                }.addOnFailureListener { uploadException ->
                    // Handle upload failure
                }
            }.addOnFailureListener { downloadException ->
                // Handle download failure
            }
        }.addOnFailureListener { getUrlException ->
            // Handle error getting download URL
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