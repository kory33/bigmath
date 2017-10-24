package com.github.kory33.bigmath.integer

import java.util.*

private const val SUB_INTEGER_HEX_SIZE = BIG_UINT_BLOCK_SIZE / 4

private fun splitStr(str : String, index : Int) = Pair(
        str.subSequence(0, index).toString(),
        str.subSequence(index, str.length).toString()
)

private fun intFromUHex(s : String) = s.toLong(16).toInt()

object BigUIntFactory {
    fun fromHexStr(str: String): BigUnsignedInteger {
        var remainingString = str
        val newIntegerList = ArrayList<Int>()

        while (remainingString.length >= SUB_INTEGER_HEX_SIZE) {
            val (rem, block) = splitStr(remainingString, remainingString.length - SUB_INTEGER_HEX_SIZE)
            remainingString = rem

            newIntegerList.add(intFromUHex(block))
        }

        if (!remainingString.isEmpty()) {
            newIntegerList.add(intFromUHex(remainingString))
        }

        return BigUnsignedInteger(newIntegerList)
    }
}
