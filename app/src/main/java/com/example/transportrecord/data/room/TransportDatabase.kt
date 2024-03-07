package com.example.transportrecord.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.transportrecord.data.entity.Armada
import com.example.transportrecord.data.entity.TransportHistory
import com.example.transportrecord.helper.InitialDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Database(entities = [Armada::class, TransportHistory::class], version = 1, exportSchema = false)
abstract class TransportDatabase : RoomDatabase() {
    abstract fun transportDao(): TransportDao

    companion object {
        @Volatile
        private var INSTANCE: TransportDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context, applicationScope: CoroutineScope): TransportDatabase {
            if (INSTANCE == null) {
                synchronized(TransportDatabase::class.java) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext, TransportDatabase::class.java, "transport"
                    ).fallbackToDestructiveMigration().addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE.let { database ->
                                applicationScope.launch {
                                    val transportDao = database?.transportDao()
                                    transportDao?.insertArmada(InitialDataSource.getArmada())
                                    transportDao?.insertHistory(InitialDataSource.getTransportHistory())
                                }
                            }
                        }
                    }).build()
                }
            }
            return INSTANCE as TransportDatabase
        }
    }
}