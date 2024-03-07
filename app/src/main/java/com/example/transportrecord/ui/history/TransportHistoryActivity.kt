package com.example.transportrecord.ui.history

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import com.example.transportrecord.databinding.ActivityTransportHistoryBinding
import com.example.transportrecord.ui.MainViewModel
import com.example.transportrecord.ui.ViewModelFactory

class TransportHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransportHistoryBinding
    private val viewModel: MainViewModel by viewModels { ViewModelFactory(application) }

    private var selectedId: Int? = null
    private var selectedTgl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransportHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.getArmadaTransportHistory().observe(this) { list ->
            val armadaId = list.toSet().map { it.armada.armadaId }
            val armadaAdapter = ArrayAdapter(
                this, android.R.layout.simple_spinner_item, armadaId
            )
            armadaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            armadaAdapter.notifyDataSetChanged()
            binding.spIdArmada.adapter = armadaAdapter
            binding.spIdArmada.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?, view: View?, position: Int, id: Long
                    ) {
                        val selectedItem = parent?.getItemAtPosition(position).toString().toInt()
                        viewModel.getArmadaById(selectedItem)
                            .observe(this@TransportHistoryActivity) { armada ->
                                binding.tvNoPol.text = armada.map { it.nopol }.first()
                                binding.tvDriver.text = armada.map { it.driver }.first()
                                binding.tvJenisArmada.text = armada.map { it.jenisArmada }.first()
                            }
                        selectedId = selectedItem
                        getTanggalList()
                        Log.d(TAG, "id $selectedId")
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
        }

        binding.btViewRoute.setOnClickListener {
            if (binding.spIdArmada.adapter != null && binding.spIdArmada.adapter != null) {
                val intent = Intent(this, MapsActivity::class.java)
                intent.putExtra(EXTRA_REF_ARMADA, selectedId)
                intent.putExtra(EXTRA_TGL_TRANSPORT, selectedTgl)
                startActivity(intent)
            }

        }

    }

    private fun getTanggalList() {
        if (selectedId != null) {
            viewModel.getTransportByRefArmada(selectedId!!).observe(this) { transportHistoryList ->
                val tanggal = transportHistoryList.toSet().map { it.tgl }.toSet().toList()
                val tanggalAdapter = ArrayAdapter(
                    this, android.R.layout.simple_spinner_item, tanggal
                )
                tanggalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                tanggalAdapter.notifyDataSetChanged()
                binding.spTanggal.adapter = tanggalAdapter
            }
            binding.spTanggal.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    val selectedItem = parent?.getItemAtPosition(position).toString()
                    selectedTgl = selectedItem
                    Log.d(TAG, "tgl $selectedTgl")
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        }
    }

    companion object {
        const val TAG = "TransportHistoryActivity"
        const val EXTRA_REF_ARMADA = "refArmada"
        const val EXTRA_TGL_TRANSPORT = "tglTransport"
    }
}