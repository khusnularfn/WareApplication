package com.example.aplikasigudang

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.aplikasigudang.databinding.ActivityFormBahanBakarBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.FileOutputStream

class FormBahanBakar : AppCompatActivity() {

    private val storageReference: StorageReference = FirebaseStorage.getInstance().reference
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editTextList: MutableList<EditText>

    private lateinit var binding: ActivityFormBahanBakarBinding
    private val titleList = listOf(
//        TitleWithPosition("Divisi", 50f, 150f),
        TitleWithPosition("PT", 50f, 150f),
        TitleWithPosition("Unit Kerja", 50f, 200f),
        TitleWithPosition("Kepada Yth", 50f, 250f),
        TitleWithPosition( "Gudang", 50f, 300f),
        TitleWithPosition( "BPPBB", 500f, 200f),
        TitleWithPosition( "Tanggal", 500f, 250f),
        TitleWithPosition( "Nama", 50f, 400f, true),
        TitleWithPosition( "Sandi Perkiraan", 50f, 450f, true),
        TitleWithPosition(   "Bagian / Afdeling", 50f, 500f, true),
        TitleWithPosition(   "Jenis Kendaraan", 50f, 550f, true),
        TitleWithPosition(    "KM/HM (Saat Diisi)", 50f, 600f, true),
        TitleWithPosition(     "Jenis Bahan Bakar", 50f, 650f, true),
        TitleWithPosition(     "Nomor Kode Barang", 50f, 700f, true),
        TitleWithPosition(      "Jumlah Liter", 50f, 750f, true),
        TitleWithPosition(      "Keperluan", 50f, 800f, true),

        )

    data class TitleWithPosition(val title: String, val x: Float, val y: Float, val requiresSpacing: Boolean = false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormBahanBakarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("FormData2", Context.MODE_PRIVATE)
        editTextList = mutableListOf()
        for (i in 2..16) {
            val resId = resources.getIdentifier("editText$i", "id", packageName)
            val editText: EditText = findViewById(resId)
            editTextList.add(editText)
        }

        binding.btnHapus.setOnClickListener {
            binding.signaturePadBahanBakar.clear()
        }

        binding.TombolKirimBahanBakar.setOnClickListener {
            val textList = editTextList.map { editText -> editText.text.toString() }
            val signatureBitmap = binding.signaturePadBahanBakar.transparentSignatureBitmap
            val specificData = binding.editText17.text.toString()
            val namaData = binding.editText8.text.toString()

            val imageBitmap = BitmapFactory.decodeResource(resources, R.drawable.manajer)
            val imageBitmap2 = BitmapFactory.decodeResource(resources, R.drawable.ktu)

            if (textList.all { it.isNotEmpty() } && signatureBitmap != null) {
                generatePdf(this, titleList, textList, specificData, namaData, signatureBitmap, imageBitmap, imageBitmap2)
            } else {
                Toast.makeText(this, "Please fill all fields and provide a signature", Toast.LENGTH_SHORT).show()
            }

            sharedPreferences.edit().clear().apply()
            for (editText in editTextList) {
                editText.text.clear()
            }
        }
    }

    override fun onPause() {
        super.onPause()

        val sharedPrefs = getSharedPreferences("FormData2", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()

        for ((index, editText) in editTextList.withIndex()) {
            val text = editText.text.toString()
            editor.putString("editText$index", text)
        }

        editor.apply()
    }

    override fun onResume() {
        super.onResume()

        val sharedPrefs = getSharedPreferences("FormData2", Context.MODE_PRIVATE)

        for ((index, editText) in editTextList.withIndex()) {
            val savedText = sharedPrefs.getString("editText$index", "")
            editText.setText(savedText)

        }
    }


    private fun generatePdf(context: Context, titleList: List<TitleWithPosition>, textList: List<String>, specificData: String, namaData : String, signatureBitmap: Bitmap, imageBitmap: Bitmap, imageBitMap2 : Bitmap) {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(800, 1850, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        canvas.drawColor(Color.WHITE)

        // Create a Paint object to draw text
        val paint = Paint()
        val pain2 = Paint()
        pain2.color = Color.BLUE
        pain2.textSize = 30f
        pain2.typeface = Typeface.DEFAULT_BOLD

        paint.color = Color.BLACK
        paint.textSize  = 29f
        paint.typeface = Typeface.DEFAULT_BOLD

        val teksbiasa = Paint()
        teksbiasa.color = Color.BLACK
        teksbiasa.textSize = 25f

        val underline = Paint()
        underline.color = Color.BLUE
        underline.strokeWidth = 2f

        canvas.drawText("BUKTI PERMINTAAN PEMAKAIAN BAHAN BAKAR", 80f, 80f, pain2)



        // Draw text fields to the PDF
        val lineHeight = 20f
        val contentStartX = pageInfo.pageWidth / 2f
        var yPosition = lineHeight

        for ((index, text) in textList.withIndex()) {
            val titleWithPosition = titleList.getOrNull(index) ?: TitleWithPosition("Field ${index + 1}", 50f, 150f)
            val (title, x, y, requiresSpacing) = titleWithPosition


            canvas.drawText("$title: ", x, yPosition + y, paint)

            //Draw Content Text
            val titleTextWidth = paint.measureText("$title: ")
//            val contentX = x + titleTextWidth + (if (requiresSpacing) contentStartX else 0f)
            val contentX = (if (requiresSpacing) contentStartX else (x + titleTextWidth))
            if (requiresSpacing){
//                val contentTextWidth = paint.measureText(text)
                canvas.drawLine(contentX, yPosition + y + 5f, contentX + 350f , yPosition + y + 5f, underline)
            } else {
                canvas.drawLine(contentX, yPosition + y + 5f, contentX + 150f , yPosition + y + 5f, underline)
            }
            canvas.drawText(text, contentX , yPosition + y, teksbiasa)
//            canvas.drawText("________________________________", contentX, yPosition + y + 3, underline)



            yPosition += lineHeight


            if (index == titleList.lastIndex && specificData.isNotEmpty() && namaData.isNotEmpty()) {
                val namaData = "$namaData"
                canvas.drawText(namaData,150f, 1430f, paint )
                canvas.drawLine(150f, 1435f, 400f , 1435f, underline)
                val specificDataText = "$specificData"
                canvas.drawText(specificDataText, 500f, 1740f, paint)
                canvas.drawLine(500f, 1745f, 700f , 1745f, underline)// Modify the coordinates as per your requirement
            }

        }



        // Draw signature image to the PDF
        canvas.drawText("Diminta Oleh,",500f, 1570f, paint)
        val scaledSignatureBitmap = Bitmap.createScaledBitmap(signatureBitmap, 200, 100, false)
        canvas.drawBitmap(scaledSignatureBitmap, 500f, 1600f, paint)

        canvas.drawText("Pemohon",500f, 1780f, paint)

        //draw Image manajer
        canvas.drawText("Disetujui Oleh,",500f, 1200f, paint)

        val imageWidth = 210
        val imageHeight = 137

        val scaledImageBitmap = Bitmap.createScaledBitmap(imageBitmap, imageWidth, imageHeight, false)
        canvas.drawBitmap(scaledImageBitmap,500f, 1260f, paint)
        canvas.drawText("Hasman Hasby",500f, 1430f, paint)
        canvas.drawLine(500f, 1435f, 700f , 1435f, underline)
        canvas.drawText("Manager",500f, 1470f, paint)

        //Draw Image Diterima Oleh
        canvas.drawText("Diterima Oleh,",150f, 1200f, paint)
        canvas.drawText("Penerimaan BBM",150f, 1470f, paint)

        //Draw Ktu
        val scaledImageBitmap2 = Bitmap.createScaledBitmap(imageBitMap2, 247, 158, false)
        canvas.drawBitmap(scaledImageBitmap2,150f, 1600f, paint)
        canvas.drawText("Diperiksa Oleh,",150f, 1570f, paint)
        canvas.drawText("Fahrur Rozi Nasution",150f, 1740f, paint)
        canvas.drawLine(150f, 1745f, 450f , 1745f, underline)
        canvas.drawText("KTU",150f, 1780f, paint)



            doc.finishPage(page)

        // Save the PDF to the app's private storage directory
        val timestamp = System.currentTimeMillis()
        val fileName = "BBM_${timestamp}.pdf"
        val filePath = context.getExternalFilesDir(null)?.absolutePath + File.separator + fileName
        val file = File(filePath)

        try {
            val fos = FileOutputStream(file)
            doc.writeTo(fos)
            fos.close()
            doc.close()

            val publicFilePath = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath + File.separator + fileName
            val publicFile = File(publicFilePath)

//            val publicFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + File.separator + fileName
//            val publicFile = File(publicFilePath)
//
            file.copyTo(publicFile, overwrite = true)

            val fileUri = Uri.fromFile(file) // Mengubah file menjadi URI
            uploadFileToFirebaseStorage(fileUri) // Mengupload file ke Firebase Storage

            Toast.makeText(context, "PDF generated successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
        }
    }

    fun uploadFileToFirebaseStorage(fileUri: Uri) {
        val timestamp = System.currentTimeMillis()
        val fileName = "BBM_${timestamp}.pdf"
//        val userId = intent.getStringExtra("userId")
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val userId = user?.uid

        val metadata = StorageMetadata.Builder()
            .setCustomMetadata("createdBy", auth.currentUser?.uid)
            .setCustomMetadata("uploadTime", System.currentTimeMillis().toString())
            .build()


        val fileReference = storageReference.child("uploaded_files").child(fileName)

        fileReference.putFile(fileUri, metadata)
            .addOnSuccessListener { taskSnapshot ->



                // File uploaded successfully
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                    if (userId != null) {
                        saveFileInfoToFirestore(fileName, downloadUri.toString(), userId)
                    }

                    val intent = Intent(this, MainActivity::class.java) // Kirim URL unduhan ke aktivitas berikutnya
                    startActivity(intent)
                    finish()
                    // You can save the downloadUrl to Firestore or perform other actions here
                }
            }
            .addOnFailureListener { exception ->
                // File upload failed
                // Handle the error and show appropriate message to the user
            }
    }

    private fun saveFileInfoToFirestore(fileName: String, downloadUrl: String, userId: String) {
        val fileInfo = HashMap<String, Any>()
        fileInfo["fileName"] = fileName
        fileInfo["downloadUrl"] = downloadUrl
        fileInfo["userId"] = userId
        fileInfo["timestamp"] = System.currentTimeMillis()

        firestore.collection("uploaded_files").add(fileInfo)
            .addOnSuccessListener { documentReference ->
                // File info saved to Firestore successfully
            }
            .addOnFailureListener { exception ->
                // Saving file info to Firestore failed
                // Handle the error
            }
    }


}


