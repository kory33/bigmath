package com.github.kory33.bigmath.integer

/**
 * Compute x + y + carry, while taking x and y as unsigned integers.
 * @return pair of result and carry to upper digit
 */
private fun addInt(x : Int, y : Int, carry : Boolean) : Pair<Int, Boolean> {
    val longResult = Integer.toUnsignedLong(x) + Integer.toUnsignedLong(y) + (if (carry) 1 else 0)
    return Pair(longResult.toInt(), longResult > 0xffffffffL)
}

/**
 * Compute x - y - borrow, while taking x and y as unsigned integers.
 * @return pair of result and borrow from upper digit
 */
private fun subtractInt(x : Int, y : Int, borrow : Boolean) : Pair<Int, Boolean> {
    val longX = Integer.toUnsignedLong(x)
    val longSubtractAmount = Integer.toUnsignedLong(y) + if(borrow) 1 else 0

    return if (longX >= longSubtractAmount) {
        Pair((longX - longSubtractAmount).toInt(), false)
    } else {
        Pair((0x100000000 + longX - longSubtractAmount).toInt(), true)
    }
}

/**
 * Compute x * y + carry, while taking x and y as unsigned integers.
 * @return pair of result and carry to upper digit
 */
private fun multInt(x : Int, y : Int, carry : Int) : Pair<Int, Int> {
    val longX = Integer.toUnsignedLong(x)
    val longY = Integer.toUnsignedLong(y)
    val longCarry = Integer.toUnsignedLong(carry)
    val product = longX * longY + longCarry

    return Pair(product.toInt(), (product ushr 32).toInt())
}

/**
 * Compare x and y while taking x and y as unsigned integers
 */
private fun compareInt(x: Int, y: Int) : Int {
    return Integer.toUnsignedLong(x).compareTo(Integer.toUnsignedLong(y))
}


operator fun BigUnsignedInteger.plus(another : BigUnsignedInteger) : BigUnsignedInteger{
    // assure block size of another is smaller than this
    if (container.size < another.container.size) {
        return another + this
    }

    val newIntegerList = ArrayList<Int>()

    var carry = false
    val thisIterator = this.container.iterator()
    another.container.forEach { anotherNextValue ->
        val thisNextValue = thisIterator.next()

        val (blockSum, nextCarry) = addInt(anotherNextValue, thisNextValue, carry)

        newIntegerList.add(blockSum)
        carry = nextCarry
    }

    thisIterator.forEachRemaining { nextValue ->
        newIntegerList.add(nextValue + if (carry) 1 else 0)
        carry = carry && nextValue == -1
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
            return compareInt(thisBlockValue, anotherBlockValue)
        }
    }

    return 0
}

operator fun BigUnsignedInteger.minus(another: BigUnsignedInteger) : BigUnsignedInteger {
    if (another > this) {
        throw ArithmeticException("Result out of range!")
    }
    val newIntegerList = ArrayList<Int>()

    var borrow = false
    val thisIterator = this.container.iterator()
    another.container.forEach { anotherNextValue ->
        val thisNextValue = thisIterator.next()

        val (blockDiff, nextBorrow) = subtractInt(thisNextValue, anotherNextValue, borrow)

        newIntegerList.add(blockDiff)
        borrow = nextBorrow
    }

    thisIterator.forEachRemaining { nextValue ->
        newIntegerList.add(nextValue - (if (borrow) 1 else 0))
        borrow = borrow && nextValue == 0
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

    val newIntegerList = ArrayList<Int>()

    val blockShiftAmount = shiftAmount / BIG_UINT_BLOCK_SIZE
    val bitShiftAmount = (shiftAmount % BIG_UINT_BLOCK_SIZE).toInt()
    val carryShiftAmount = BIG_UINT_BLOCK_SIZE - bitShiftAmount

    (1..blockShiftAmount).forEach { newIntegerList.add(0) }

    if (bitShiftAmount == 0) {
        container.forEach { newIntegerList.add(it) }
    } else {
        // carry from lower block
        var carry = 0

        container.forEach { shiftBlock ->
            newIntegerList.add(carry + (shiftBlock shl bitShiftAmount))
            carry = shiftBlock ushr carryShiftAmount
        }

        if (carry != 0) {
            newIntegerList.add(carry)
        }
    }

    return BigUnsignedInteger(newIntegerList)
}

infix fun BigUnsignedInteger.shl(shiftAmount: Int) = this shl shiftAmount.toLong()

operator fun BigUnsignedInteger.times(another: Int) : BigUnsignedInteger {
    val newIntegerList = ArrayList<Int>()
    var carry = 0

    container.forEach { blockValue ->
        val (blockProduct, blockCarry) = multInt(blockValue, another, carry)

        newIntegerList.add(blockProduct)
        carry = blockCarry
    }

    if (carry != 0) {
        newIntegerList.add(carry)
    }

    return BigUnsignedInteger(newIntegerList)
}

private infix fun BigUnsignedInteger.multLong(another: BigUnsignedInteger) : BigUnsignedInteger {
    var result = BigUnsignedInteger(0)

    another.container.forEachIndexed { index, blockValue ->
        result += (this * blockValue) shl (index * BIG_UINT_BLOCK_SIZE)
    }

    return result
}

operator fun BigUnsignedInteger.times(another: BigUnsignedInteger) : BigUnsignedInteger {
    if (another.container.size > this.container.size) {
        return another * this
    }

    return this multLong another
}
