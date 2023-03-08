package com.example.locationcheckapp.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = LOCATION_TABLE)
data class Location(
    @PrimaryKey(autoGenerate = true) val id : Int?,
    @ColumnInfo(name="latitude")val latitude :Double?,
    @ColumnInfo(name = "longitude")val longitude :Double?,
    @ColumnInfo(name = "timestamp")val timestamp: Long = System.currentTimeMillis()
)
