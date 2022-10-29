package com.ostech.muse.models

import java.util.*

class PriceUtils {
    companion object {
        fun formatPrice(price: Double): String {
            val nairaSymbol = Currency.getInstance("NGN").symbol

            return "$nairaSymbol${String.format("%d.2f", price)}"
        }
    }
}