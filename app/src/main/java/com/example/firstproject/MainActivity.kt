package com.example.firstproject

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstproject.Utils.showToast
import com.example.firstproject.adapters.UsersAdapter
import com.example.firstproject.databinding.ActivityMainBinding
import com.example.firstproject.models.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var auth: FirebaseAuth
    lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference
        val dataList = mutableListOf<User>()


        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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

    }

}