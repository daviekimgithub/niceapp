package com.example.niceapp.onboarding

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.niceapp.R
import com.example.niceapp.auth.AuthenticationActivity
import com.example.niceapp.auth.StudentDetailsActivity
import com.example.niceapp.general.HomeActivity
import kotlinx.android.synthetic.main.activity_getting_started.*

class GettingStartedActivity : AppCompatActivity() {
    @SuppressLint("ShowToast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_getting_started)

        btnGetStarted.setOnClickListener {
            val intent = Intent(this, AuthenticationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }
}