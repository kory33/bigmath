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

    return if ((x >= 0 && y >= 0) || (x <= 0 && y <= 0)) {
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
            Pair(diff - 1, borrow)
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

    val newIntegerDeque = ArrayDeque<Long>()

    var carry = false
    val thisIterator = this.container.iterator()
    another.container.forEach { anotherNextValue ->
        val thisNextValue = thisIterator.next()

        val (blockSum, nextCarry) = addLong(anotherNextValue, thisNextValue, carry)

        newIntegerDeque.addLast(blockSum)
        carry = nextCarry
    }

    thisIterator.forEachRemaining { nextValue ->
        newIntegerDeque.addLast(nextValue + if (carry) 1L else 0L)
        carry = carry && nextValue == -1L
    }

    if (carry) {
        newIntegerDeque.addLast(1)
    }

    return BigUnsignedInteger(newIntegerDeque)
}

operator fun BigUnsignedInteger.compareTo(another: BigUnsignedInteger) : Int {
    if (container.size != another.container.size) {
        return container.size.compareTo(another.container.size)
    }

    val thisIterator = this.container.descendingIterator()
    val anotherIterator  = another.container.descendingIterator()

    while (thisIterator.hasNext()) {
        val thisNext = thisIterator.next()
        val anotherNext = anotherIterator.next()
        if (thisNext != anotherNext) {
            return compareLong(thisNext, anotherNext)
        }
    }

    return 0
}

operator fun BigUnsignedInteger.minus(another: BigUnsignedInteger) : BigUnsignedInteger {
    if (another > this) {
        throw ArithmeticException("Result out of range!")
    }
    val newIntegerDeque = ArrayDeque<Long>()

    var borrow = false
    val thisIterator = this.container.iterator()
    another.container.forEach { anotherNextValue ->
        val thisNextValue = thisIterator.next()

        val (blockDiff, nextBorrow) = subtractLong(thisNextValue, anotherNextValue, borrow)

        newIntegerDeque.addLast(blockDiff)
        borrow = nextBorrow
    }

    thisIterator.forEachRemaining { nextValue ->
        newIntegerDeque.addLast(nextValue - (if (borrow) 1L else 0L))
        borrow = borrow && nextValue == 0L
    }

    val newBigUInt = BigUnsignedInteger(newIntegerDeque)
    newBigUInt.removeTrailingZeros()

    return newBigUInt
}
