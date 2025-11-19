package com.example.firstproject

import android.content.Context
import android.widget.Toast

object Utils {

    fun Context.showToast(message : String, duration : Int = Toast.LENGTH_SHORT){
        Toast.makeText(this, message, duration).show()
    }


}