package com.example.todolist.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(
    tableName = "tbl_tasks"
)
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id:Int=0,
    val name: String,
    val subject: String,
    val deadLine: Long,
    val remainTime: String? = null,
    val priority: Priority,
    val isDone: Boolean = false
) : Parcelable

