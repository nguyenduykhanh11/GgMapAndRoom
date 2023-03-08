package com.example.locationcheckapp.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LocationDao {
    @Query("SELECT * FROM $LOCATION_TABLE ORDER BY timestamp DESC LIMIT 10")
    fun readData(): LiveData<List<Location>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(location: Location)

    @Query("DELETE FROM $LOCATION_TABLE WHERE timestamp NOT IN (SELECT timestamp FROM $LOCATION_TABLE ORDER BY timestamp DESC LIMIT 10)")
    suspend fun deleteAfterLast10Records()

}