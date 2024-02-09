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
import android.text.Editable
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.aplikasigudang.databinding.ActivityFormBarangBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.FileOutputStream

class FormBarang : AppCompatActivity() {

    private val storageReference: StorageReference = FirebaseStorage.getInstance().reference
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editTextList: MutableList<EditText>

    private lateinit var binding: ActivityFormBarangBinding
    private val titleList = listOf(
//        TitleWithPosition("Divisi", 50f, 150f),
        TitleWithPosition("PT", 50f, 150f),
        TitleWithPosition("Unit Kerja", 50f, 200f),
        TitleWithPosition("Kepada Yth", 50f, 250f),
        TitleWithPosition( "Gudang", 50f, 300f),
        TitleWithPosition( "BPPBB", 750f, 60f),
        TitleWithPosition( "Tanggal", 750f, 120f),
        TitleWithPosition( "Mandor", 750f, 180f),
        TitleWithPosition( "DIV / AFD", 750f, 220f),
        TitleWithPosition( "Nomor Kode Barang", 50f, 550f),
        TitleWithPosition( "Nama & Spesifikasi Barang", 405f, 550f),
        TitleWithPosition( "Satuan", 805f, 550f),
        TitleWithPosition( "Kuantiti", 1005f, 550f),
        TitleWithPosition( "Sandi Perkiraan", 1165f, 550f),
        TitleWithPosition( "Keterangan Penggunaan", 1400f, 550f),
        )

    data class TitleWithPosition(val title: String, val x: Float, val y: Float, val requiresSpacing: Boolean = false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormBarangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        sharedPreferences = getSharedPreferences("FormData", Context.MODE_PRIVATE)


       editTextList = mutableListOf()
        for (i in 2..17) {
            val resId = resources.getIdentifier("editText$i", "id", packageName)
            val editText: EditText = findViewById(resId)
            editTextList.add(editText)
        }



        binding.btnHapus.setOnClickListener {
            binding.signaturePadBarang.clear()
        }


        binding.TombolKirimBarang.setOnClickListener {
            val textList = editTextList.map { editText -> editText.text.toString() }
            val signatureBitmap = binding.signaturePadBarang.transparentSignatureBitmap
            val specificData = binding.editText16.text.toString()
            val namaPemerima = binding.editText17.text.toString()

            val imageBitmap = BitmapFactory.decodeResource(resources, R.drawable.manajer)
            val imageBitmap2 = BitmapFactory.decodeResource(resources, R.drawable.ktu)

            if (textList.all { it.isNotEmpty() } && signatureBitmap != null) {
                generatePdf(this, titleList, textList, specificData,namaPemerima, signatureBitmap, imageBitmap, imageBitmap2)
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

        val sharedPrefs = getSharedPreferences("FormData", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()

        for ((index, editText) in editTextList.withIndex()) {
            val text = editText.text.toString()
            editor.putString("editText$index", text)
        }

        editor.apply()
    }

    override fun onResume() {
        super.onResume()

        val sharedPrefs = getSharedPreferences("FormData", Context.MODE_PRIVATE)

        for ((index, editText) in editTextList.withIndex()) {
            val savedText = sharedPrefs.getString("editText$index", "")
            editText.setText(savedText)
        }
    }


    private fun generatePdf(context: Context, titleList: List<TitleWithPosition>, textList: List<String>, specificData: String, namaPenerima: String, signatureBitmap: Bitmap, imageBitmap: Bitmap, imageBitMap2 : Bitmap) {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(1800, 1600, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas


        canvas.drawColor(Color.WHITE)

        // Create a Paint object to draw text
        val colorTable = Color.parseColor("#0DA2FF")
        val headerColor = Color.parseColor("#3c92c3")

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

        val contentPaint = Paint()
        contentPaint.color = Color.BLACK
        contentPaint.typeface = Typeface.DEFAULT_BOLD
        contentPaint.textSize = 25f

        val headerPaint = Paint()
        headerPaint.color = colorTable
        headerPaint.textSize = 25f

        val teksbaris = TextPaint()
        teksbaris.color = Color.WHITE
        teksbaris.textSize = 25f

        val contentPaint2 = Paint()
        contentPaint2.color = headerColor
        contentPaint2.textSize = 25f

        val underlinePaint = Paint()
        underlinePaint.color = Color.BLUE
        underlinePaint.strokeWidth = 2f


        val underline = Paint()
        underline.color = Color.BLUE
        underline.strokeWidth = 2f

        val linecolor = Paint()
        linecolor.color = Color.WHITE
        linecolor.strokeWidth = 3f

        canvas.drawText("BUKTI PERMINTAAN PEMAKAIAN BARANG", 500f, 50f, pain2)

        //variabel header
        val tableStartX = 50f
        val tableStartY = 600f
        val columnWidth = 400
        val rowHeight = 60
        val numRows = 10
        val numCols = 6

        val headerWidth = 400 // Lebar header tabel
        val headerHeight = 100 // Tinggi header tabel


        //header tabel
        for (i in 8..13) {
            val titleWithPosition = titleList.getOrNull(i) ?: TitleWithPosition("Title ${i + 1}", tableStartX, tableStartY)
            canvas.drawRect(titleWithPosition.x, titleWithPosition.y, titleWithPosition.x + headerWidth, titleWithPosition.y + headerHeight, headerPaint)
            canvas.drawLine(titleWithPosition.x + headerWidth + 5, titleWithPosition.y, titleWithPosition.x + headerWidth + 5, titleWithPosition.y + headerHeight, linecolor)
            canvas.drawText(titleWithPosition.title, titleWithPosition.x + 10f, titleWithPosition.y + headerHeight / 2, contentPaint)
            }

        // Draw text fields to the PDF
        val lineHeight = 20f
        val contentStartX = 50f
        val contentStartY = 500f
        var yPosition = lineHeight
        val textListRange = textList.subList(0, 9)

        for ((index, text) in textListRange.withIndex()) {
            val titleWithPosition = titleList.getOrNull(index) ?: TitleWithPosition("Field ${index + 1}", 50f, 150f)
            val (title, x, y, requiresSpacing) = titleWithPosition


            canvas.drawText("$title: ", x, yPosition + y, paint)

            //Draw Content Text
            val titleTextWidth = paint.measureText("$title: ")
//            val contentX = x + titleTextWidth + (if (requiresSpacing) contentStartX else 0f)
            val contentX = (if (requiresSpacing) contentStartX else (x + titleTextWidth))
            canvas.drawText(text, contentX, yPosition + y, teksbiasa)
            canvas.drawLine(contentX, yPosition + y + 5f, contentX + 150f , yPosition + y + 5f, underline)

//            val contentTextWidth = paint.measureText(text)
//            canvas.drawLine(contentX, yPosition + y + 5f, contentX + contentTextWidth, yPosition + y + 5f, underline)

            yPosition += lineHeight

            // nama pemohon
            if (index == titleList.lastIndex && specificData.isNotEmpty() && namaPenerima.isNotEmpty()) {
                val namaPenerima = "$namaPenerima"
//                canvas.drawText(namaPenerima, 150f, 1430f, paint)
                val specificDataText = "$specificData"
//                canvas.drawText(specificDataText, 1200f, 1470f, paint) // Modify the coordinates as per your requirement
            }

        }


        //konten
        val colWidthkonten = 400
        val rowsHeightkonten = 60
        val contentTableStartX = 50f
        val contentTableStartY = 650f
        val startXcontent = contentTableStartX
        val startYcontent = contentTableStartY
        val rowHeights = 60f



        //row 1
        val startYcontent1 = startYcontent
        canvas.drawRect(startXcontent, startYcontent, 350f, startYcontent + 500, contentPaint2)
        canvas.drawLine(345f, startYcontent, 345f, startYcontent + 500, linecolor)
        var textLayout1 = StaticLayout.Builder.obtain(textList[8], 0, textList[8].length, teksbaris, 285)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(1f, 1f)
            .setIncludePad(false)
            .build()

       // Ganti dengan ID EditText yang sesuai
        binding.editText10.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Periksa jika tombol "Enter" ditekan
                if (count > 0 && s?.subSequence(start, start + count).toString() == "\n") {
                    // Tambahkan newline ke teks
                    val newText = StringBuilder(s).apply { insert(start, "\n") }.toString()
                    binding.editText10.setText(newText)
                    binding.editText10.setSelection(start + count + 1) // Pindahkan kursor ke baris baru
                }
                // Perbarui textLayout1 dengan teks yang baru
                val updatedText = textList[8].substring(0, start) + s + textList[8].substring(start + count)
                val updatedTextLayout1 = StaticLayout.Builder.obtain(updatedText, 0, updatedText.length, teksbaris, 285)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(1f, 1f)
                    .setIncludePad(false)
                    .build()

                textLayout1 = updatedTextLayout1
                canvas.save()
                canvas.translate(80f, startYcontent + rowHeight / 2) // Apply the starting position
                textLayout1.draw(canvas)
                canvas.restore()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        canvas.save()
        canvas.translate(80f, startYcontent + rowHeight / 2) // Apply the starting position
        textLayout1.draw(canvas)
        canvas.restore()


        //row2
        val startYcontent2 = startYcontent1 + rowHeight
        canvas.drawRect(350f, startYcontent, 750f, startYcontent + 500, contentPaint2)
        canvas.drawLine(745f, startYcontent, 745f, startYcontent + 500, linecolor)
        var textLayout2 = StaticLayout.Builder.obtain(textList[9], 0, textList[9].length, teksbaris, 365)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(1f, 1f)
            .setIncludePad(false)
            .build()

        // Ganti dengan ID EditText yang sesuai
        binding.editText11.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Periksa jika tombol "Enter" ditekan
                if (count > 0 && s?.subSequence(start, start + count).toString() == "\n") {
                    // Tambahkan newline ke teks
                    val newText = StringBuilder(s).apply { insert(start, "\n") }.toString()
                    binding.editText11.setText(newText)
                    binding.editText11.setSelection(start + count + 1) // Pindahkan kursor ke baris baru
                }
                // Perbarui textLayout1 dengan teks yang baru
                val updatedText = textList[9].substring(0, start) + s + textList[9].substring(start + count)
                val updatedTextLayout2 = StaticLayout.Builder.obtain(updatedText, 0, updatedText.length, teksbaris, 365)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(1f, 1f)
                    .setIncludePad(false)
                    .build()

                textLayout2 = updatedTextLayout2
                canvas.save()
                canvas.translate(80f, startYcontent + rowHeight / 2) // Apply the starting position
                textLayout2.draw(canvas)
                canvas.restore()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        canvas.save()
        canvas.translate(startXcontent + 330f, startYcontent + rowHeight / 2) // Apply the starting position
        textLayout2.draw(canvas)
        canvas.restore()
//        canvas.drawLine(startXcontent, startYcontent2 + rowHeight, 1800f, startYcontent2 + rowHeight, linecolor)
//
//        canvas.drawText(textList[10], startXcontent + 350f   , startYcontent + rowHeight / 2, teksbaris)

        //row3
        val startYcontent3 = startYcontent2 + rowHeight
        canvas.drawRect(750f, startYcontent, 950f, startYcontent + 500, contentPaint2)
        canvas.drawLine(945f, startYcontent, 945f, startYcontent + 500, linecolor)
        var textLayout3 = StaticLayout.Builder.obtain(textList[10], 0, textList[10].length, teksbaris, 185)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(1f, 1f)
            .setIncludePad(false)
            .build()

        binding.editText12.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Periksa jika tombol "Enter" ditekan
                if (count > 0 && s?.subSequence(start, start + count).toString() == "\n") {
                    // Tambahkan newline ke teks
                    val newText = StringBuilder(s).apply { insert(start, "\n") }.toString()
                    binding.editText12.setText(newText)
                    binding.editText12.setSelection(start + count + 1) // Pindahkan kursor ke baris baru
                }
                // Perbarui textLayout1 dengan teks yang baru
                val updatedText = textList[10].substring(0, start) + s + textList[10].substring(start + count)
                val updatedTextLayout3 = StaticLayout.Builder.obtain(updatedText, 0, updatedText.length, teksbaris, 185)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(1f, 1f)
                    .setIncludePad(false)
                    .build()

                textLayout3 = updatedTextLayout3
                canvas.save()
                canvas.translate(80f, startYcontent + rowHeight / 2) // Apply the starting position
                textLayout3.draw(canvas)
                canvas.restore()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        canvas.save()
        canvas.translate(startXcontent + 750f, startYcontent + rowHeight / 2) // Apply the starting position
        textLayout3.draw(canvas)
        canvas.restore()
//        canvas.drawLine(startXcontent, startYcontent3 + rowHeight, 1800f, startYcontent3 + rowHeight, linecolor)
//        canvas.drawText(textList[11], startXcontent + 750f   , startYcontent + rowHeight / 2, teksbaris)


        //row4
        val startYcontent4 = startYcontent3 + rowHeight
        canvas.drawRect(950f, startYcontent, 1150f, startYcontent + 500, contentPaint2)
        canvas.drawLine(1150f, startYcontent, 1150f, startYcontent + 500, linecolor)
        var textLayout4 = StaticLayout.Builder.obtain(textList[11], 0, textList[11].length, teksbaris, 185)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(1f, 1f)
            .setIncludePad(false)
            .build()

        binding.editText13.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Periksa jika tombol "Enter" ditekan
                if (count > 0 && s?.subSequence(start, start + count).toString() == "\n") {
                    // Tambahkan newline ke teks
                    val newText = StringBuilder(s).apply { insert(start, "\n") }.toString()
                    binding.editText13.setText(newText)
                    binding.editText13.setSelection(start + count + 1) // Pindahkan kursor ke baris baru
                }
                // Perbarui textLayout1 dengan teks yang baru
                val updatedText = textList[11].substring(0, start) + s + textList[11].substring(start + count)
                val updatedTextLayout4 = StaticLayout.Builder.obtain(updatedText, 0, updatedText.length, teksbaris, 185)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(1f, 1f)
                    .setIncludePad(false)
                    .build()

                textLayout4 = updatedTextLayout4
                canvas.save()
                canvas.translate(80f, startYcontent + rowHeight / 2) // Apply the starting position
                textLayout4.draw(canvas)
                canvas.restore()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        canvas.save()
        canvas.translate(startXcontent + 950f, startYcontent + rowHeight / 2) // Apply the starting position
        textLayout4.draw(canvas)
        canvas.restore()
//        canvas.drawLine(startXcontent, startYcontent4 + rowHeight, 1800f, startYcontent4 + rowHeight, linecolor)
//        canvas.drawText(textList[12], startXcontent + 950f   , startYcontent + rowHeight / 2, teksbaris)

        //row5
        val startYcontent5 = startYcontent4 + rowHeight
        canvas.drawRect(1150f, startYcontent, 1400f, startYcontent + 500, contentPaint2)
        canvas.drawLine(1400f, startYcontent, 1400f, startYcontent + 500, linecolor)
        var textLayout5 = StaticLayout.Builder.obtain(textList[12], 0, textList[12].length, teksbaris, 235)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(1f, 1f)
            .setIncludePad(false)
            .build()

        binding.editText14.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Periksa jika tombol "Enter" ditekan
                if (count > 0 && s?.subSequence(start, start + count).toString() == "\n") {
                    // Tambahkan newline ke teks
                    val newText = StringBuilder(s).apply { insert(start, "\n") }.toString()
                    binding.editText14.setText(newText)
                    binding.editText14.setSelection(start + count + 1) // Pindahkan kursor ke baris baru
                }
                // Perbarui textLayout1 dengan teks yang baru
                val updatedText = textList[12].substring(0, start) + s + textList[12].substring(start + count)
                val updatedTextLayout5 = StaticLayout.Builder.obtain(updatedText, 0, updatedText.length, teksbaris, 235)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(1f, 1f)
                    .setIncludePad(false)
                    .build()

                textLayout5 = updatedTextLayout5
                canvas.save()
                canvas.translate(80f, startYcontent + rowHeight / 2) // Apply the starting position
                textLayout5.draw(canvas)
                canvas.restore()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        canvas.save()
        canvas.translate(startXcontent + 1150f, startYcontent + rowHeight / 2) // Apply the starting position
        textLayout5.draw(canvas)
        canvas.restore()
//        canvas.drawLine(startXcontent, startYcontent5 + rowHeight, 1800f, startYcontent5 + rowHeight, linecolor)
//        canvas.drawText(textList[13], startXcontent + 1150f   , startYcontent + rowHeight / 2, teksbaris)

        //row6
        val startYcontent6 = startYcontent5 + rowHeight
        canvas.drawRect(1400f, startYcontent, 1800f, startYcontent + 500, contentPaint2)
        var textLayout6 = StaticLayout.Builder.obtain(textList[13], 0, textList[13].length, teksbaris, 340)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(1f, 1f)
            .setIncludePad(false)
            .build()

        binding.editText15.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Periksa jika tombol "Enter" ditekan
                if (count > 0 && s?.subSequence(start, start + count).toString() == "\n") {
                    // Tambahkan newline ke teks
                    val newText = StringBuilder(s).apply { insert(start, "\n") }.toString()
                    binding.editText15.setText(newText)
                    binding.editText15.setSelection(start + count + 1) // Pindahkan kursor ke baris baru
                }
                // Perbarui textLayout1 dengan teks yang baru
                val updatedText = textList[13].substring(0, start) + s + textList[13].substring(start + count)
                val updatedTextLayout6 = StaticLayout.Builder.obtain(updatedText, 0, updatedText.length, teksbaris, 340)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(1f, 1f)
                    .setIncludePad(false)
                    .build()

                textLayout6 = updatedTextLayout6
                canvas.save()
                canvas.translate(80f, startYcontent + rowHeight / 2) // Apply the starting position
                textLayout6.draw(canvas)
                canvas.restore()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        canvas.save()
        canvas.translate(startXcontent + 1360f, startYcontent + rowHeight / 2) // Apply the starting position
        textLayout6.draw(canvas)
        canvas.restore()
//        canvas.drawLine(startXcontent, startYcontent6 + rowHeight, 1800f, startYcontent6 + rowHeight, linecolor)
//        canvas.drawText(textList[14], startXcontent + 1400f   , startYcontent + rowHeight / 2, teksbaris)

        //baris kedua

        // Draw signature image to the PDF
        val textpemohon = textList[14]
        canvas.drawText("Diminta Oleh,",1200f, 1200f, paint)
        val scaledSignatureBitmap = Bitmap.createScaledBitmap(signatureBitmap, 200, 100, false)
        canvas.drawBitmap(scaledSignatureBitmap, 1200f, 1270f, paint)
        canvas.drawText(textpemohon, 1200f, 1430f, paint )
        canvas.drawLine(1200f, 1435f, 1400f , 1435f, underline)
        canvas.drawText("Pemohon",1200f, 1470f, paint)

        //draw Image manajer


        val imageWidth = 210
        val imageHeight = 137
        val namaPenerima = textList[15]

        val scaledImageBitmap = Bitmap.createScaledBitmap(imageBitmap, imageWidth, imageHeight, false)
        canvas.drawBitmap(scaledImageBitmap,500f, 1270f, paint)
        canvas.drawText("Disetujui Oleh,",500f, 1200f, paint)
        canvas.drawText("Hasman Hasby",500f, 1430f, paint)
        canvas.drawLine(500f, 1435f, 780f , 1435f, underline)
        canvas.drawText("Manager",500f, 1470f, paint)

        //Draw Image Diterima Oleh
        canvas.drawText("Diterima Oleh,",150f, 1200f, paint)
        canvas.drawText(namaPenerima,150f, 1430f, paint)
        canvas.drawLine(150f, 1435f, 400f , 1435f, underline)
        canvas.drawText("Pemakai Barang",150f, 1470f, paint)

        //Draw Ktu
        val scaledImageBitmap2 = Bitmap.createScaledBitmap(imageBitMap2, 247, 158, false)
        canvas.drawBitmap(scaledImageBitmap2,850f, 1230f, paint)
        canvas.drawText("Diperiksa Oleh,",850f, 1200f, paint)
        canvas.drawText("Fahrur Rozi Nasution",850f, 1430f, paint)
        canvas.drawLine(850f, 1435f, 1100f , 1435f, underline)
        canvas.drawText("KTU",850f, 1470f, paint)


        doc.finishPage(page)

        // Save the PDF to the app's private storage directory
        val timestamp = System.currentTimeMillis()
        val fileName = "Barang_${timestamp}.pdf"
        val filePath = context.getExternalFilesDir(null)?.absolutePath + File.separator + fileName
        val file = File(filePath)

        try {
            val fos = FileOutputStream(file)
            doc.writeTo(fos)
            fos.close()
            doc.close()



//            val publicFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + File.separator + fileName
            val publicFilePath = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath + File.separator + fileName
            val publicFile = File(publicFilePath)
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
        val fileName = "Barang_${timestamp}.pdf"
//        val userId = intent.getStringExtra("userId")
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val userId = user?.uid

        val fileReference = storageReference.child("uploaded_files").child(fileName)

        fileReference.putFile(fileUri)
            .addOnSuccessListener { taskSnapshot ->

                // File uploaded successfully
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                    if (userId != null) {
                        saveFileInfoToFirestore(fileName, downloadUri.toString(), userId, timestamp)
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

    private fun saveFileInfoToFirestore(fileName: String, downloadUrl: String, userId: String, timestamp: Long) {
        val fileInfo = hashMapOf(
            "fileName" to fileName,
            "downloadUrl" to downloadUrl,
            "userId" to userId,
            "timestamp" to timestamp
        )

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


