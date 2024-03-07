package com.example.transportrecord.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.transportrecord.databinding.ActivityMainBinding
import com.example.transportrecord.ui.history.TransportHistoryActivity
import com.example.transportrecord.ui.record.transport.RecordTransportActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btRecordTransport.setOnClickListener {
            startActivity(Intent(this,RecordTransportActivity::class.java))
        }

        binding.btTransportHistory.setOnClickListener {
            startActivity(Intent(this,TransportHistoryActivity::class.java))
        }

    }
}