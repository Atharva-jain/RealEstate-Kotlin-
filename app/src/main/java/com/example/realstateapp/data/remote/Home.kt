package com.example.realstateapp.data.remote

import androidx.annotation.Keep
import com.google.firebase.firestore.ServerTimestamp
import java.io.Serializable
import java.util.*

@Keep
data class Home(
    val ownerUid: String = "",
    val uid: String = "",
    val title: String = "",
    val state: String = "",
    val city: String = "",
    val address: String = "",
    val zipCode: String = "",
    val propertyType: String = "",
    val residentialType: String = "",
    val price: Int = 0,
    val priceType: String = "",
    val bhk: String = "",
    val bedroom: String = "",
    val bathroom: String = "",
    val phoneNumber: String = "",
    val description: String = "",
    @ServerTimestamp
    var timeStamp: Date = Date(),
    val frontHouseImage: String = "",
    val houseImages: List<String> = emptyList()
): Serializable
