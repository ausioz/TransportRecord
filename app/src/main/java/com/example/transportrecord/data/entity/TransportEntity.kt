package com.example.transportrecord.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation


@Entity
data class Armada(
    @PrimaryKey(autoGenerate = true)
    val armadaId: Int = 0,
    val nopol: String,
    val driver: String,
    val jenisArmada: String,
    )

@Entity
data class TransportHistory(
    @PrimaryKey(autoGenerate = true)
    val transportId: Int = 0,
    val refArmada: Int,
    val tgl: String,
    val jam: String,
    val latitude: Double?,
    val longitude: Double?,
)


data class ArmadaAndTransportHistory(
    @Embedded
    val armada: Armada,
    @Relation(
        parentColumn = "armadaId",
        entityColumn = "refArmada"
    )
    val transportHistory: List<TransportHistory>
)