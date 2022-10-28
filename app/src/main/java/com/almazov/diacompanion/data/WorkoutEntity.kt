package com.almazov.diacompanion.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "workout_table")
data class WorkoutEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Int?,
    val duration: Int?,
    val type: String?,
    val kkalMinus: Double?
    ): Parcelable