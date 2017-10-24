package com.github.kory33.bigmath.integer

import java.util.*

/**
 * Compute x + y + carry, while taking x and y as unsigned integers.
 * @return pair of result and carry to upper digit
 */
private fun addLong(x : Long, y : Long, carry : Boolean) : Pair<Long, Boolean> {
    val sum = x + y + (if (carry) 1 else 0)

    return if (x >= 0 && y >= 0) {
        Pair(sum, false)
    } else if (x < 0 && y < 0) {
        Pair(sum, true)
    } else {
        Pair(sum, sum >= 0)
    }
}

/**
 * Compute x - y, while taking x and y as unsigned integers.
 * @return pair of result and borrow from upper digit
 */
private fun subtractLong(x : Long, y : Long) : Pair<Long, Boolean> {
    val diff = x - y

    return if ((x >= 0 && y >= 0) || (x < 0 && y < 0)) {
        Pair(diff, diff < 0)
    } else {
        Pair(diff, y < 0)
    }
}

/**
 * Compute x - y - borrow, while taking x and y as unsigned integers.
 * @return pair of result and borrow from upper digit
 */
private fun subtractLong(x : Long, y : Long, borrow : Boolean) : Pair<Long, Boolean> {
    val (diff, diffBorrow) = subtractLong(x, y)

    return if (borrow) {
        if (diff != 0L) {
            Pair(diff - 1, diffBorrow)
        } else {
            Pair(-1L, true)
        }
    } else {
        Pair(diff, diffBorrow)
    }
}

/**
 * Compare x and y while taking x and y as unsigned integers
 */
private fun compareLong(x: Long, y: Long) : Int {
    return if ((x >= 0 && y >= 0) || (x < 0 && y < 0)) {
        x.compareTo(y)
    } else {
        -x.compareTo(0)
    }
}


operator fun BigUnsignedInteger.plus(another : BigUnsignedInteger) : BigUnsignedInteger{
    // assure block size of another is smaller than this
    if (container.size < another.container.size) {
        return another + this
    }

    val newIntegerList = ArrayList<Long>()

    var carry = false
    val thisIterator = this.container.iterator()
    another.container.forEach { anotherNextValue ->
        val thisNextValue = thisIterator.next()

        val (blockSum, nextCarry) = addLong(anotherNextValue, thisNextValue, carry)

        newIntegerList.add(blockSum)
        carry = nextCarry
    }

    thisIterator.forEachRemaining { nextValue ->
        newIntegerList.add(nextValue + if (carry) 1L else 0L)
        carry = carry && nextValue == -1L
    }

    if (carry) {
        newIntegerList.add(1)
    }

    return BigUnsignedInteger(newIntegerList)
}

operator fun BigUnsignedInteger.compareTo(another: BigUnsignedInteger) : Int {
    if (container.size != another.container.size) {
        return container.size.compareTo(another.container.size)
    }

    (container.lastIndex downTo 0).forEach { compareIndex ->
        val thisBlockValue = container[compareIndex]
        val anotherBlockValue = another.container[compareIndex]
        if (thisBlockValue != anotherBlockValue) {
            return compareLong(thisBlockValue, anotherBlockValue)
        }
    }

    return 0
}

operator fun BigUnsignedInteger.minus(another: BigUnsignedInteger) : BigUnsignedInteger {
    if (another > this) {
        throw ArithmeticException("Result out of range!")
    }
    val newIntegerList = ArrayList<Long>()

    var borrow = false
    val thisIterator = this.container.iterator()
    another.container.forEach { anotherNextValue ->
        val thisNextValue = thisIterator.next()

        val (blockDiff, nextBorrow) = subtractLong(thisNextValue, anotherNextValue, borrow)

        newIntegerList.add(blockDiff)
        borrow = nextBorrow
    }

    thisIterator.forEachRemaining { nextValue ->
        newIntegerList.add(nextValue - (if (borrow) 1L else 0L))
        borrow = borrow && nextValue == 0L
    }

    val newBigUInt = BigUnsignedInteger(newIntegerList)
    newBigUInt.removeTrailingZeros()

    return newBigUInt
}


infix fun BigUnsignedInteger.shl(shiftAmount : Long) : BigUnsignedInteger {
    if (shiftAmount < 0) {
        throw ArithmeticException("Shift amount invalid!")
    }

    if (this == BigUnsignedInteger(0)) {
        return BigUnsignedInteger(0)
    }

    val newIntegerList = ArrayList<Long>()

    val blockShiftAmount = shiftAmount / BIG_UINT_BLOCK_SIZE
    val bitShiftAmount = (shiftAmount % BIG_UINT_BLOCK_SIZE).toInt()
    val carryShiftAmount = BIG_UINT_BLOCK_SIZE - bitShiftAmount

    (1..blockShiftAmount).forEach { newIntegerList.add(0) }

    if (bitShiftAmount == 0) {
        container.forEach { newIntegerList.add(it) }
    } else {
        var carry = 0L
        container.forEach { shiftBlock ->
            newIntegerList.add(carry + (shiftBlock shl bitShiftAmount))
            carry = shiftBlock ushr carryShiftAmount
        }
    }

    return BigUnsignedInteger(newIntegerList)
}
