package com.github.kory33.bigmath.integer

import java.util.*

private fun toUBinStr(n : Long) = (63 downTo 0)
        .map { (n ushr it) and 1L }
        .map { it.toString() }
        .reduce { s1, s2 -> s1 + s2 }

private fun toUHexStr(n : Long) = (1 downTo 0)
        .map { (n ushr (it * 32)) and 0xffffffffL }
        .map { it.toString(16).padStart(8, '0') }
        .reduce { s1, s2 -> s1 + s2 }

private fun removeFirstChars(s : String, c : Char) : String{
    val dropEndIndex = s.indexOfFirst { it != c }
    return when (dropEndIndex) {
        0 -> s
        -1 -> s.last().toString()
        else -> s.drop(dropEndIndex)
    }
}

/**
 * A class which represents a large positive integer or zero.
 * It stores array of integers in little endian.
 *
 * The array must contain at least one element. It must be ensured that
 * either the last element of array is not zero or the array has a length of 1.
 */
class BigUnsignedInteger internal constructor(internal val container: ArrayDeque<Long> = ArrayDeque()) {
    constructor(int : Long) : this() {
        this.container.add(int)
    }

    init {
        if(container.isEmpty()) {
            container.addFirst(0)
        }
    }

    internal fun removeTrailingZeros() {
        while (container.last == 0L && container.size != 0) {
            container.removeLast()
        }
    }

    fun copy() = BigUnsignedInteger(container.clone())

    fun toHexString() = this.container
            .map { toUHexStr(it) }
            .foldRight("", { lowerBlock, upperBlock ->
                if (upperBlock.isEmpty()) "0x" + removeFirstChars(lowerBlock, '0')
                else upperBlock + lowerBlock
            })

    fun toBinString() = this.container
            .map { toUBinStr(it) }
            .foldRight("", { lowerBlock, upperBlock ->
                if (upperBlock.isEmpty()) "0x" + removeFirstChars(lowerBlock, '0')
                else upperBlock + lowerBlock
            })

    override fun toString(): String {
        return container.toString()
    }
}
