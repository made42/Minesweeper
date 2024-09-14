package minesweeper

import java.util.Scanner

interface Cell {
    var visible: Boolean
    var marked: Boolean
}

class Safe(override var visible: Boolean = true, override var marked: Boolean = false) : Cell {
    override fun toString(): String {
        return (if (marked) '*' else '.').toString()
    }
}

class Mine(override var visible: Boolean = false, override var marked: Boolean = false) : Cell {
    override fun toString(): String {
        return when {
            visible -> 'X'
            marked -> '*'
            else -> '.'
        }.toString()
    }
}

class Count(override var visible: Boolean = false, override var marked: Boolean = false, private val count: Int) : Cell {
    override fun toString(): String {
        return when {
            visible -> count
            marked -> '*'
            else -> '.'
        }.toString()
    }
}

class Explored(override var visible: Boolean = true, override var marked: Boolean = false) : Cell {
    override fun toString(): String {
        return '/'.toString()
    }
}

const val SIZE_FIELD = 9

var field = List(SIZE_FIELD) { MutableList<Cell>(SIZE_FIELD) { Safe() } }

fun main() {
    print("How many mines do you want on the field? ")
    var mines = readln().toInt()
    while (mines > 0) {
        val row = (0 until SIZE_FIELD).random()
        val column = (0 until SIZE_FIELD).random()
        if (field[row][column] is Safe) { field[row][column] = Mine(); mines-- }
    }

    field.forEachIndexed { rowIndex, row ->
        row.forEachIndexed { columnIndex, cell ->
            if (cell !is Mine) {
                val count = when {
                    rowIndex == 0 || rowIndex == row.lastIndex -> {
                        when (columnIndex) {
                            0 -> {
                                when (rowIndex) {
                                    0 -> listOf(right(rowIndex, columnIndex), below(rowIndex, columnIndex), belowRight(rowIndex, columnIndex)).count { it is Mine }
                                    SIZE_FIELD - 1 -> listOf(above(rowIndex, columnIndex), right(rowIndex, columnIndex), aboveRight(rowIndex, columnIndex)).count { it is Mine }
                                    else -> 0
                                }
                            }
                            SIZE_FIELD - 1 -> {
                                when (rowIndex) {
                                    0 -> listOf(left(rowIndex, columnIndex), below(rowIndex, columnIndex), belowLeft(rowIndex, columnIndex)).count { it is Mine }
                                    SIZE_FIELD - 1 -> listOf(left(rowIndex, columnIndex), above(rowIndex, columnIndex), aboveLeft(rowIndex, columnIndex)).count { it is Mine }
                                    else -> 0
                                }
                            }
                            else -> checkSide(rowIndex, columnIndex)
                        }
                    }
                    else -> {
                        if (columnIndex == 0 || columnIndex == row.lastIndex) checkSide(rowIndex, columnIndex)
                        else listOf(above(rowIndex, columnIndex), aboveRight(rowIndex, columnIndex), right(rowIndex, columnIndex), belowRight(rowIndex, columnIndex), below(rowIndex, columnIndex), belowLeft(rowIndex, columnIndex), left(rowIndex, columnIndex), aboveLeft(rowIndex, columnIndex)).count { it is Mine }
                    }
                }
                if (count > 0) row[columnIndex] = Count(count = count)
            }
        }
    }

    while (true) {
        printField()
        val scanner = Scanner(System.`in`)
        print("Set/unset mines marks or claim a cell as free: ")
        val column = scanner.nextInt() - 1
        val row = scanner.nextInt() - 1
        val command = scanner.next()

        if (row !in 0 until SIZE_FIELD || column !in 0 until SIZE_FIELD) {
            println("Wrong coordinates")
            continue
        }

        when (command) {
            "free" -> {
                when (field[row][column]) {
                    is Mine -> {
                        field.map { it.map { if (it is Mine) it.visible = true} }
                        printField()
                        println("You stepped on a mine and failed!")
                        return
                    }
                    is Count -> field[row][column].visible = true
                    is Safe -> explore(row, column)
                }
            }
            "mine" -> field[row][column].marked = !field[row][column].marked
            else -> continue
        }

        if (field.all { it.all { it is Mine && it.marked } }) {
            println("Congratulations! You found all the mines!")
            return
        }
    }
}

fun printField() {
    println("\n |${(1..SIZE_FIELD).joinToString("")}|\n-|${"-".repeat(SIZE_FIELD)}|")
    field.forEachIndexed { index, row ->
        println("${index + 1}|${row.joinToString("") { it.toString() }}|")
    }
    println("-|${"-".repeat(SIZE_FIELD)}|")
}

fun explore(row: Int, column: Int) {
    when {
        field[row][column] is Explored -> return
        field[row][column] is Count -> field[row][column].visible = true
        else -> {
            field[row][column] = Explored()
            when (column) {
                0 -> {
                    when (row) {
                        0 -> {
                            explore(row, 1)
                            explore(1, 1)
                            explore(1, column)
                        }
                        SIZE_FIELD - 1 -> {
                            explore(row - 1, column)
                            explore(row - 1, 1)
                            explore(row, 1)
                        }
                        else -> {
                            explore(row - 1, column)
                            explore(row - 1, 1)
                            explore(row, 1)
                            explore(row + 1, 1)
                            explore(row + 1, column)
                        }
                    }
                }
                SIZE_FIELD - 1 -> {
                    when (row) {
                        0 -> {
                            explore(row,column - 1)
                            explore(1, column - 1)
                            explore(1, column)
                        }
                        SIZE_FIELD - 1 -> {
                            explore(row - 1, column)
                            explore(row - 1, column - 1)
                            explore(row, column - 1)
                        }
                        else -> {
                            explore(row - 1, column)
                            explore(row - 1, column - 1)
                            explore(row, column - 1)
                            explore(row + 1, column - 1)
                            explore(row + 1, column)
                        }
                    }
                }
                else -> {
                    when (row) {
                        0 -> {
                            explore(row, column - 1)
                            explore(1, column - 1)
                            explore(1, column)
                            explore(1, column + 1)
                            explore(row, column + 1)
                        }
                        SIZE_FIELD - 1 -> {
                            explore(row, column - 1)
                            explore(row - 1, column - 1)
                            explore(row - 1, column)
                            explore(row - 1, column + 1)
                            explore(row, column + 1)
                        }
                        else -> {
                            explore(row - 1, column)
                            explore(row - 1, column + 1)
                            explore(row, column + 1)
                            explore(row + 1, column + 1)
                            explore(row + 1, column)
                            explore(row + 1, column - 1)
                            explore(row, column - 1)
                            explore(row - 1, column - 1)
                        }
                    }
                }
            }
        }
    }
}

fun checkSide(row: Int, column: Int): Int {
    return when (row) {
        0 -> listOf(left(row, column), belowLeft(row, column), below(row, column), belowRight(row, column), right(row, column)).count { it is Mine }
        SIZE_FIELD - 1 -> listOf(left(row, column), aboveLeft(row, column), above(row, column), aboveRight(row, column), right(row, column)).count { it is Mine }
        else -> 0
    } + when (column) {
        0 -> listOf(above(row, column), aboveRight(row, column), right(row, column), belowRight(row, column), below(row, column)).count { it is Mine }
        SIZE_FIELD - 1 -> listOf(above(row, column), aboveLeft(row, column), left(row, column), belowLeft(row, column), below(row, column)).count { it is Mine }
        else -> 0
    }
}

fun left(row: Int, column: Int) = field[row][column - 1]
fun right(row: Int, column: Int) = field[row][column + 1]
fun above(row: Int, column: Int) = field[row - 1][column]
fun below(row: Int, column: Int) = field[row + 1][column]
fun aboveLeft(row: Int, column: Int) = field[row - 1][column - 1]
fun aboveRight(row: Int, column: Int) = field[row - 1][column + 1]
fun belowLeft(row: Int, column: Int) = field[row + 1][column - 1]
fun belowRight(row: Int, column: Int) = field[row + 1][column + 1]