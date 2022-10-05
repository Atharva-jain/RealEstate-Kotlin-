package com.example.realstateapp.utils

import android.util.Log

class ConvertDigitIntoValues {
    companion object {
        fun setValueAccordingToLacCr(mPriceType: String, price: Int): Int {
            if (mPriceType != "") {
                try {
                    var value = 0
                    if (mPriceType == "lac") {
                        value = price / 100000
                        return value
                    }
                    if (mPriceType == "cr") {
                        value = price / 10000000
                        return value
                    }
                    return price
                } catch (e: Exception) {
                    Log.d("ConvertDigitIntoValues", "error to converting prices $e")
                }
            }
            return 0
        }
    }
}