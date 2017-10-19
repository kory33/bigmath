package com.github.kory33.bigmath.integer

import java.util.*

private val subIntegerHexSize = 16
private fun splitStr(str : String, index : Int) = Pair(
        str.subSequence(0, index).toString(),
        str.subSequence(index, str.length).toString()
)

private fun longFromUHex(s : String) = if ((s.length < 16) or (s[0].toLong() < 0x8)) {
    s.toLong(16)
} else {
    // change the uppermost half-byte into negative representation
    -((0x10 - s[0].toString().toLong(16)) shl 60) + s.subSequence(1, s.length).toString().toLong(16)
}

object BigUIntFactory {
    fun fromHexStr(str: String): BigUnsignedInteger {
        var remainingString = str
        val deque = ArrayDeque<Long>()

        while (remainingString.length >= subIntegerHexSize) {
            val (rem, block) = splitStr(remainingString, remainingString.length - subIntegerHexSize)
            remainingString = rem

            deque.addLast(longFromUHex(block))
        }

        if (!remainingString.isEmpty()) {
            deque.addLast(longFromUHex(remainingString))
        }

        return BigUnsignedInteger(deque)
    }
}
