package cryptography

import java.awt.Color
import kotlin.system.exitProcess
import java.io.File
import javax.imageio.ImageIO

fun main() {
    do {
        println("Task (hide, show, exit):")
        val userInput = readln()
        when (userInput) {
            "hide" -> hide()
            "show" -> show()
            "exit" -> {
                println("Bye!")
                exitProcess(0)
            }

            else -> println("Wrong task: $userInput")
        }
    } while (true)

}

fun hide() {
    println("Input image file:")
    val inputFile = readln()
    println("Output image file:")
    val outputFile = readln()
    println("Message to hide:")

    val messageToHide = readln().trim()
    val messageToBytes = messageToHide.encodeToByteArray()
    val messageBits = messageToBytes.joinToString("") { it.toString(2).padStart(8, '0') }.map { it.digitToInt() }
    println("Password:")
    val password = readln().trim()
    val passwordToBytes = password.encodeToByteArray()
    val passwordBits = passwordToBytes.joinToString("") { it.toString(2).padStart(8, '0') }.map { it.digitToInt() }
    var encryptedBinaryString = ""
    var passwordIndex = 0
    for (char in messageBits) {
        if (passwordIndex == passwordBits.size) passwordIndex = 0
        encryptedBinaryString += char xor passwordBits[passwordIndex]
        passwordIndex++
    }
    encryptedBinaryString += "000000000000000000000011"


    try {
        val image = ImageIO.read(File(inputFile))
        if (messageToHide.length * 8 + 24 > image.width * image.height) {
            println("The input image is not large enough to hold this message.")
        }
        var encryptedBinaryStringIndex = 0
        exit@ for (row in 0 until image.height) {
            for (col in 0 until image.width) {
                val color = Color(image.getRGB(col, row))
                if (encryptedBinaryStringIndex > encryptedBinaryString.lastIndex) break@exit
                val r = color.red
                val g = color.green
                var b = color.blue
                val bit = encryptedBinaryString[encryptedBinaryStringIndex]
                if (b % 2 == bit.digitToInt()) {
                    encryptedBinaryStringIndex++
                    continue
                } else if (b % 2 == 1 && bit.digitToInt() == 0) {
                    b--
                } else {
                    b++
                }
                val newColor = Color(r, g, b)
                image.setRGB(col, row, newColor.rgb)
                encryptedBinaryStringIndex++

            }
        }
        ImageIO.write(image, "png", File(outputFile))
        println("Message saved in $outputFile image.")
    } catch (e: Exception) {
        println(e.message)
    }


}

fun show() {
    println("Input image file:")
    val inputfile = readln()
    println("Password:")
    val password = readln()
    try {
        val image = ImageIO.read(File(inputfile))
        var messageBit = ""
        var count = 0
        exit@ for (row in 0 until image.height) {
            for (col in 0 until image.width) {
                val color = Color(image.getRGB(col, row))
                messageBit += color.blue % 2
                count++
                if (count > 24) {
                    val substring = messageBit.subSequence(messageBit.length - 24, messageBit.length)
                    if (substring == "000000000000000000000011") break@exit
                }
            }
        }
        val passwordToBytes = password.encodeToByteArray()
        val passwordBits = passwordToBytes.joinToString("") { it.toString(2).padStart(8, '0') }.map { it.digitToInt() }
            .joinToString("")

        var decodedMessageBinary = ""
        var passwordIndex = 0
        for (char in messageBit) {
            if (passwordIndex == passwordBits.length) passwordIndex = 0
            decodedMessageBinary += char.digitToInt() xor passwordBits[passwordIndex].digitToInt()
            passwordIndex++
        }
        val decodedMessageList = decodedMessageBinary.chunked(8).toMutableList()
        repeat(3) { decodedMessageList.removeLast() }
        val decodeMessage = decodedMessageList.map { it.toInt(2).toChar() }.joinToString("")
        println("Message:\n$decodeMessage")

    } catch (e: Exception) {
        println(e.message)
    }

}