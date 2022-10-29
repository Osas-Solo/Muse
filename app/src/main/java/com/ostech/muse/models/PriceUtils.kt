package com.ostech.muse.models

class PriceUtils {
    companion object {
        fun formatPrice(price: Double): String {
            val nairaSymbol = '\u20A6'

            return "$nairaSymbol${String.format("%,.2f", price)}"
        }
    }
}