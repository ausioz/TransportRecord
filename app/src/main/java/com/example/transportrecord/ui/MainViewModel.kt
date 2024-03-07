package com.example.transportrecord.ui

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.transportrecord.data.entity.Armada
import com.example.transportrecord.data.entity.ArmadaAndTransportHistory
import com.example.transportrecord.data.entity.TransportHistory
import com.example.transportrecord.data.room.TransportDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : ViewModel() {
    private val applicationScope = CoroutineScope(SupervisorJob())
    private val transportDatabase = TransportDatabase.getDatabase(application, applicationScope)

    fun recordArmada(armada: List<Armada>) {
        viewModelScope.launch {
            transportDatabase.transportDao().insertArmada(armada)
        }
    }

    fun recordTransport(transportRecord: List<TransportHistory>) {
        viewModelScope.launch {
            transportDatabase.transportDao().insertHistory(transportRecord)
        }
    }

    fun getAllArmada(): LiveData<List<Armada>>{
        return transportDatabase.transportDao().getAllArmada()
    }

    fun getArmadaById(armadaId:Int):LiveData<List<Armada>>{
        return transportDatabase.transportDao().getArmadaById(armadaId)
    }

    fun getTransportByRefArmada(refArmada: Int):LiveData<List<TransportHistory>>{
        return transportDatabase.transportDao().getTransportByRefArmada(refArmada)
    }

    fun getArmadaTransportHistory(): LiveData<List<ArmadaAndTransportHistory>> {
        return transportDatabase.transportDao().getArmadaTransportHistory()
    }

    fun getTransportHistoryByArmadaAndDate(refArmada: Int, tgl: String): LiveData<List<TransportHistory>>{
        return transportDatabase.transportDao().getTransportHistoryByArmadaAndDate(refArmada,tgl)
    }


}