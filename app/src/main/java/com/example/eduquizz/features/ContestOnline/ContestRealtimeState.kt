package com.example.eduquizz.features.ContestOnline

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.google.firebase.database.*

object ContestRealtimeState {
    var targetHour by mutableStateOf(21)
    var durationMinutes by mutableStateOf(60)
    var loaded by mutableStateOf(false)

    fun startListening() {
        if (loaded) return // tránh lặp listener

        val ref = FirebaseDatabase.getInstance().getReference("data")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.child("timestart").getValue(Int::class.java)?.let { targetHour = it }
                snapshot.child("timedur").getValue(Int::class.java)?.let { durationMinutes = it }
                loaded = true
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
