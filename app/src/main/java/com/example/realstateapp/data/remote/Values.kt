package com.example.realstateapp.data.remote

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class Values (
    val collection: String = "",
    val type: String = ""
): Serializable