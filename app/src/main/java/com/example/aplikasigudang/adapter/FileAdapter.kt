package com.example.aplikasigudang.adapter
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikasigudang.R
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class FileAdapter(val fileList: MutableList<StorageReference>, private val fileItemClickListener: FileItemClickListener) : RecyclerView.Adapter<FileAdapter.ViewHolder>() {

    private val storageReference: StorageReference = FirebaseStorage.getInstance().reference

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileNameTextView: TextView = itemView.findViewById(R.id.tv_item_name)
        val buttonHijau : ImageButton = itemView.findViewById(R.id.btndownload)
        val buttonBiru : ImageButton = itemView.findViewById(R.id.btn_acc)
        val buttonMerah : ImageButton = itemView.findViewById(R.id.btn_no)


        var pdfUrl: String = ""

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.itemrow, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fileReference = fileList[position]
        val fileName = fileReference.name
        holder.fileNameTextView.text = fileName

        fileReference.downloadUrl.addOnSuccessListener { uri ->
            holder.pdfUrl = uri.toString()
        }

        holder.buttonHijau.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(android.net.Uri.parse(holder.pdfUrl), "application/pdf")
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            holder.itemView.context.startActivity(intent)
        }

        holder.buttonBiru.setOnClickListener {

            fileItemClickListener.onButtonBiruClick(position)
        }

        holder.buttonMerah.setOnClickListener {
            fileItemClickListener.onButtonMerahClick(position)
        }

    }

    override fun getItemCount(): Int {
        return fileList.size
    }

    interface FileItemClickListener {
        fun onButtonMerahClick(position: Int)
        fun onButtonBiruClick(position: Int)
    }




}
