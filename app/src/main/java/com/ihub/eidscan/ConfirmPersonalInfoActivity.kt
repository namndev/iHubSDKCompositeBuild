package com.ihub.eidscan

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ihub.eidscan.databinding.ActivityConfirmPersonalInfoBinding

class ConfirmPersonalInfoActivity : AppCompatActivity() {
    private var binding: ActivityConfirmPersonalInfoBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmPersonalInfoBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        loadData()
    }

    fun loadData() {

    }
}