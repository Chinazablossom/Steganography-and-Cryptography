import org.hyperskill.hstest.dynamic.DynamicTest
import org.hyperskill.hstest.stage.StageTest
import org.hyperskill.hstest.testcase.CheckResult
import org.hyperskill.hstest.testing.TestedProgram
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.security.MessageDigest
import javax.imageio.ImageIO

class SteganographyTest: StageTest<Any>() {
    @DynamicTest
    fun imageCreateTest(): CheckResult {
        val main = TestedProgram()
        var outputString = main.start().toLowerCase().trim()
        if (!outputString.contains("task (hide, show, exit):")) {
            return CheckResult(false, "Prompt \"Task (hide, show, exit):\" is missing.")
        }

        outputString = main.execute("hide").toLowerCase().trim()
        if (!outputString.contains("input image file:")) {
            return CheckResult(false, "Wrong output after the \"hide\" command.")
        }

        try {
            val infile = "test/testimage.png"
            val inputFile = File(infile)
            if (!inputFile.exists()) {
                return CheckResult(false, "Input test image file doesn't exist. Try reloading the project.")
            }
            val inputImage: BufferedImage = ImageIO.read(inputFile)
            if (imageHash(inputImage) != "cc5bc38fad61e51b44ec7731f49ae2e290c06f55") {
                return CheckResult(false, "Input test image file is corrupted. Try reloading the project.")
            }

            outputString = main.execute(infile).toLowerCase().trim()
            if (!outputString.contains("output image file:")) {
                return CheckResult(false, "Wrong output after inserting the input filename.")
            }

            val outfile = "test/out.png"
            outputString = main.execute(outfile).toLowerCase().trim()
            if (!outputString.contains("message to hide:")) {
                return CheckResult(false, "Wrong output after inserting the output filename.")
            }

            val message = "Hyperskill steganography program."
            outputString = main.execute(message).toLowerCase().trim()
            if (!outputString.contains("password:")) {
                return CheckResult(false, "Wrong output after inserting the message.")
            }

            val password = "mypassword"
            outputString = main.execute("mypassword").toLowerCase().trim()
            if ( ( !outputString.contains("message saved")
                && outputString.contains("task (hide, show, exit):") ) ) {
                return CheckResult(false, "Wrong output after inserting the password.")
            }

            val outputFile = File(outfile)
            if (!outputFile.exists()) {
                return CheckResult(false, "Image file $outfile hasn't been created.")
            }
            val outputImage: BufferedImage = ImageIO.read(outputFile)
            if (imageHash(outputImage) != "cbe82b64ab5f3641afc22e8c9b688902506b3444") {
                return CheckResult(false, "Wrong output file. Wrong output image hash.")
            }

            outputString = main.execute("show").toLowerCase().trim()
            if (!outputString.contains("input image file:")) {
                return CheckResult(false, "Wrong output after the \"show\" command.")
            }

            outputString = main.execute(outfile).toLowerCase().trim()
            if (!outputString.contains("password:")) {
                return CheckResult(false, "Wrong output after inserting the message.")
            }

            outputString = main.execute(password).toLowerCase().trim()
            if (!( outputString.contains("message:") && outputString.contains(message.toLowerCase()) &&
                        outputString.contains("task (hide, show, exit):") ) ) {
                return CheckResult(false, "Wrong output after inserting the input filename.")
            }
        } catch (e: Exception) {
            return CheckResult(false, e.message)
        }

        outputString = main.execute("exit").toLowerCase().trim()
        if (!outputString.contains("bye!")) {
            return CheckResult(false, "Wrong output after the \"exit\" command.")
        }

        if (!main.isFinished) {
            return CheckResult(false, "Program has not terminated after the \"exit\" command.")
        }

        return CheckResult(true, "")
    }

    @DynamicTest
    fun smallImageTest(): CheckResult {
        val main = TestedProgram()
        var outputString = main.start().toLowerCase().trim()
        if (!outputString.contains("task (hide, show, exit):")) {
            return CheckResult(false, "Prompt \"Task (hide, show, exit):\" is missing.")
        }

        outputString = main.execute("hide").toLowerCase().trim()
        if (!outputString.contains("input image file:")) {
            return CheckResult(false, "Wrong output after the \"hide\" command.")
        }

        try {
            val infile = "test/small.png"
            val inputFile = File(infile)
            if (!inputFile.exists()) {
                return CheckResult(false, "Input test image file doesn't exist. Try reloading the project.")
            }
            val inputImage: BufferedImage = ImageIO.read(inputFile)
            if (imageHash(inputImage) != "c4679ca63e03c377de73ab3aaede91f37a0d50b6") {
                return CheckResult(false, "Input test image file is corrupted. Try reloading the project.")
            }

            outputString = main.execute(infile).toLowerCase().trim()
            if (!outputString.contains("output image file:")) {
                return CheckResult(false, "Wrong output after inserting the input filename.")
            }

            val outfile = "test/out2.png"
            outputString = main.execute(outfile).toLowerCase().trim()
            if (!outputString.contains("message to hide:")) {
                return CheckResult(false, "Wrong output after inserting the output filename.")
            }

            val message = "Hyperskill steganography program."
            outputString = main.execute(message).toLowerCase().trim()
            if (!outputString.contains("password:")) {
                return CheckResult(false, "Wrong output after inserting the message.")
            }

            val password = "mypassword"
            outputString = main.execute(password).toLowerCase().trim()
            if ( !( outputString.contains("the input image is not large enough to hold this message") && outputString.contains("task (hide, show, exit):") ) ) {
                return CheckResult(false, "The program should check that the image size is adequate for holding the Bytes array.")
            }

        } catch (e: Exception) {
            return CheckResult(false, e.message)
        }

        outputString = main.execute("exit").toLowerCase().trim()
        if (!outputString.contains("bye!")) {
            return CheckResult(false, "Wrong output after the \"exit\" command.")
        }

        if (!main.isFinished) {
            return CheckResult(false, "Program has not terminated after the \"exit\" command.")
        }

        return CheckResult(true, "")
    }

    private fun imageHash(inputImage: BufferedImage) : String {
        val imageByteArray = ByteArray(3 * inputImage.width * inputImage.height)
        var index = 0
        for (y in 0 until inputImage.height) {
            for (x in 0 until inputImage.width) {
                val color = Color(inputImage.getRGB(x, y))
                imageByteArray[index] = color.red.toByte()
                index++
                imageByteArray[index] = color.green.toByte()
                index++
                imageByteArray[index] = color.blue.toByte()
                index++
            }
        }
        val md = MessageDigest.getInstance("SHA-1")
        md.update(imageByteArray)
        return md.digest().joinToString("") { "%02x".format(it) }
    }

}

