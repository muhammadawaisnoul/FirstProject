package com.example.firstproject

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstproject.adapters.UsersAdapter
import com.example.firstproject.databinding.ActivityMainBinding
import com.example.firstproject.models.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var auth: FirebaseAuth
    lateinit var database: DatabaseReference

    //
    private var pdfRenderer: PdfRenderer? = null
    private var currentPage: PdfRenderer.Page? = null
    private var pageIndex = 0

    private lateinit var parcelFileDescriptor: ParcelFileDescriptor

    private val pickPdf = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) openPdf(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference
        val dataList = mutableListOf<User>()

        //
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val post: User = snapshot.getValue<User>() as User
                dataList.add(post)
                binding.recyclerview.adapter = UsersAdapter(dataList)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.i("TAG", "onCancelled:${error.toException()} ")
            }
        }
        database.child("Users").child(
            auth.currentUser?.uid.toString()).addValueEventListener(postListener)

        binding.recyclerview.layoutManager = LinearLayoutManager(this)

        binding.logoutBtn.setOnClickListener {
            auth.signOut()
            finish()
        }
        //



        binding.prevPage.setOnClickListener {
            showPage(pageIndex - 1)
        }

        binding.nextPage.setOnClickListener {
            showPage(pageIndex + 1)
        }

        pickPdf.launch(arrayOf("application/pdf"))

    }

    private fun openPdf(uri: Uri) { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")!!
        pdfRenderer = PdfRenderer(parcelFileDescriptor)
        showPage(0)
    }


    private fun showPage(index: Int) {
        pdfRenderer?.let { renderer ->
            if (index < 0 || index >= renderer.pageCount) return
            currentPage?.close()
            currentPage = renderer.openPage(index)
            val page = currentPage!!
            val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            binding.pdfPageImage.setImageBitmap(bitmap)
            pageIndex = index
        }
    }
}