package com.github.kory33.bigmath.integer

import java.util.*

private fun addLong(x : Long, y : Long, carry : Boolean) : Pair<Long, Boolean> {
    val sum = x + y + (if (carry) 1 else 0)

    return if (x >= 0 && y >= 0) {
        Pair(sum, false)
    } else if (x < 0 && y < 0) {
        Pair(sum, true)
    } else {
        Pair(sum, sum >= 0 || (carry && sum == -1L))
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

    while (thisIterator.hasNext()) {
        val nextValue = thisIterator.next()
        carry = carry and (nextValue == -1L)

        newIntegerDeque.add(nextValue + if (carry) 1L else 0L)
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
            return thisNext.compareTo(anotherNext)
        }
    }

    return 0
}
