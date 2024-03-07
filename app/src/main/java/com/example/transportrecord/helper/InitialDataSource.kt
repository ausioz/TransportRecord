package com.example.transportrecord.helper

import com.example.transportrecord.data.entity.Armada
import com.example.transportrecord.data.entity.TransportHistory

object InitialDataSource {
    fun getArmada():List<Armada>{
        return listOf(
            Armada(1,"AD1000AA","Andi","Truck"),
            Armada(2,"AD1001AA","Budi","Pickup"),
            Armada(3,"AD1002AA","Chandra","Truck"),
            Armada(4,"AD1003AA","Doni","Pickup"),
        )
    }

    fun getTransportHistory():List<TransportHistory>{
        return listOf(
            TransportHistory(1,1,"2024/03/03","08:30",-7.560183, 110.789639),
            TransportHistory(2,1,"2024/03/03","09:00",-7.550061, 110.816065),
            TransportHistory(3,1,"2024/03/03","09:30",-7.558511, 110.828865),
            TransportHistory(4,1,"2024/03/03","10:00",-7.560703, 110.842997),
            TransportHistory(5,1,"2024/03/03","10:30",-7.568492, 110.856314),
            TransportHistory(6,1,"2024/03/03","11:00",-7.566104, 110.867363),
            TransportHistory(7,1,"2024/03/03","11:30",-7.557049, 110.869204),
            TransportHistory(8,1,"2024/03/03","12:00",-7.549508, 110.871664),
            TransportHistory(9,1,"2024/03/03","12:30",-7.549395, 110.877479),
            TransportHistory(10,1,"2024/03/05","13:00",-7.556925, 110.884152),
            TransportHistory(11,2,"2024/03/04","10:00",-7.560703, 110.842997),
            TransportHistory(12,2,"2024/03/04","10:30",-7.568492, 110.856314),
            TransportHistory(13,2,"2024/03/04","11:00",-7.566104, 110.867363),
        )
    }
}