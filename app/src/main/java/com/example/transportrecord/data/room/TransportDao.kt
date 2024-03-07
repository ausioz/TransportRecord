package com.example.transportrecord.data.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.transportrecord.data.entity.Armada
import com.example.transportrecord.data.entity.ArmadaAndTransportHistory
import com.example.transportrecord.data.entity.TransportHistory

@Dao
interface TransportDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArmada(armada: List<Armada>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHistory(history: List<TransportHistory>)

    @Query("SELECT * FROM Armada")
    fun getAllArmada(): LiveData<List<Armada>>

    @Query("SELECT * FROM Armada WHERE armadaId = :armadaId")
    fun getArmadaById(armadaId:Int): LiveData<List<Armada>>

    @Query("SELECT * FROM TransportHistory WHERE refArmada = :refArmada")
    fun getTransportByRefArmada(refArmada:Int): LiveData<List<TransportHistory>>

    @Query("SELECT * FROM TransportHistory INNER JOIN Armada on Armada.armadaId = TransportHistory.refArmada ")
    fun getArmadaTransportHistory(): LiveData<List<ArmadaAndTransportHistory>>

    @Query("SELECT * FROM TransportHistory WHERE refArmada = :refArmada AND tgl = :tgl ORDER BY transportId ASC")
    fun getTransportHistoryByArmadaAndDate(refArmada: Int, tgl: String): LiveData<List<TransportHistory>>

}