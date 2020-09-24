package com.irfanirawansukirman.githubsearch.util

import android.util.Log
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object CtTest {

    fun Factorial(n: Int) {
        var result = 1
        var process = ""
        for (i in n downTo 1)  {
            result *= i
            process += "$i"
            if (i > 1) process += "*"
        }
        print("$n! ($process) = $result\n\n")
    }

    fun ValueLevel(n: Int) {
        val numberAsString = n.toString()
        for (i in 1..numberAsString.length) {
            for (j in 1..i) {
                if (j > 1) print(" ")
            }

            val selectedNumber = numberAsString[i.minus(1)]
            print(selectedNumber)

            for (k in numberAsString.length.minus(1) downTo i) {
                print("0")
            }
            println()
        }
    }

    fun Duration(d1: Date, d2: Date) {
        try {
            val diffInTime = d2.time - d1.time
            val diffInSeconds: Long = (TimeUnit.MILLISECONDS
                .toSeconds(diffInTime)
                    % 60)
            val diffInMinutes: Long = (TimeUnit.MILLISECONDS
                .toMinutes(diffInTime)
                    % 60)
            val diffInHours: Long = (TimeUnit.MILLISECONDS
                .toHours(diffInTime)
                    % 24)
            val diffInDays: Long = (TimeUnit.MILLISECONDS
                .toDays(diffInTime)
                    % 365)
            val diffInYears: Long = (TimeUnit.MILLISECONDS
                .toDays(diffInTime)
                    / 365L)

//            if (diffInYears > 0) {
                print("${diffInYears}Y")
//            }

//            if (diffInDays > 0) {
                print("${diffInDays}D")
//            }

//            if (diffInHours > 0) {
                print("${diffInHours}H")
//            }

//            if (diffInMinutes > 0) {
                print("${diffInMinutes}M")
//            }

//            if (diffInSeconds > 0) {
                print("${diffInSeconds}S")
//            }

        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }
}