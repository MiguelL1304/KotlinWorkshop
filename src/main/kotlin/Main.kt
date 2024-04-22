import java.io.File
import java.util.Scanner

data class Media(val title: String, val author: String, val description: String)

fun readMediaFromFile(filename: String): List<Media> {
    val mediaList = mutableListOf<Media>()

    File(filename).forEachLine { line ->
        val parts = line.split("\t") // Split the line by tabs

        // Ensure that the line contains all parts (title, author, description)
        if (parts.size >= 3) {
            val title = parts[0].trim()
            val author = parts[1].trim()
            val description = parts[2].trim()

            mediaList.add(Media(title, author, description))
        }
    }

    return mediaList
}

fun transformString (input: String): List<String> {
    var transformedString = input.uppercase()

    transformedString = transformedString.replace(Regex("[^\\x00-\\x7F]")) { char ->
        when (char.value) {
            "Ö" -> "O"
            "Ü" -> "U"
            "Ä" -> "A"
            "É" -> "E"
            "Ñ" -> "N"
            "Ç" -> "C"
            "ß" -> "SS"
            "À" -> "A"
            "Ô" -> "O"
            else -> ""
        }
    }

    val commonChar = arrayOf(',', '.', '@', '%', '!', '?', '&', '(', ')', ':', '\'', '-')
    commonChar.forEach { char ->
        transformedString = transformedString.replace(char.toString(), "")
    }

    val wordsArray = transformedString.split("\\s".toRegex())

    val commonWords = setOf("THE", "OF", "AND", "A", "TO", "IN", "ON", "FOR", "WITH")
    val filteredWordsArray = wordsArray.filter { word -> !commonWords.contains(word)}

    val romanNumerals = mapOf(
        "I" to 1,
        "II" to 2,
        "III" to 3,
        "IV" to 4,
        "V" to 5,
        "VI" to 6,
        "VII" to 7,
        "VIII" to 8,
        "IX" to 9,
        "X" to 10
    )
    val transformedArray = filteredWordsArray.map { word ->
        romanNumerals[word]?.toString() ?: word
    }

    return  transformedArray

}

fun transformMediaTitles(mediaList: List<Media>): List<List<String>> {
    return mediaList.map { media ->
        transformString(media.title)
    }
}

fun transformMediaAuthors(mediaList: List<Media>): List<List<String>> {
    return mediaList.map { media ->
        transformString(media.author)
    }
}

fun transformMediaDescriptions(mediaList: List<Media>): List<List<String>> {
    return mediaList.map { media -> transformString(media.description)
    }
}

fun getUserInput(): String {
    val scanner = Scanner(System.`in`)
    print("Enter search query: ")
    return scanner.nextLine().trim()
}


fun levenshteinDistance(str1: String, str2: String): Int {
    val len1 = str1.length
    val len2 = str2.length
    val matrix = Array(len1 + 1) { IntArray(len2 + 1) }

    // Initialize matrix with 0s
    for (i in 0..len1) {
        matrix[i][0] = i
    }
    for (j in 0..len2) {
        matrix[0][j] = j
    }

    // Fill in the rest of the matrix
    for (i in 1..len1) {
        for (j in 1..len2) {
            val cost = if (str1[i - 1] == str2[j - 1]) 0 else 1
            matrix[i][j] = minOf(
                matrix[i - 1][j] + 1, // Deletion
                matrix[i][j - 1] + 1, // Insertion
                matrix[i - 1][j - 1] + cost // Substitution
            )
        }
    }

    // Return the bottom-right cell of the matrix
    return matrix[len1][len2]
}

fun searchMedia(userInput: String, transformedTitles: List<List<String>>, mediaList: List<Media>): List<Media> {
    val transformedInput = transformString(userInput) // Transform user input

    val searchResults = mutableListOf<Media>()

    // Iterate over each transformed title in listingTitles
    transformedTitles.forEachIndexed { index, transformedTitle ->
        var totalDistance = 0
        var matchCount = 0

        // Iterate over each word in transformedInput
        transformedInput.forEach { inputWord ->
            var minDistance = Int.MAX_VALUE // Initialize minDistance for each input word
            // Find the closest matching word in transformedTitle
            transformedTitle.forEach { titleWord ->
                val distance = levenshteinDistance(inputWord, titleWord)
                if (distance < minDistance) {
                    minDistance = distance
                }
            }

            // Add the closest matching word distance to totalDistance
            totalDistance += minDistance
            matchCount++
        }


        // Calculate average distance for the title based on matching words only
        val averageDistance = if (matchCount == 0) Double.POSITIVE_INFINITY else totalDistance.toDouble() / matchCount

        // Define a threshold value for average distance
        val averageDistanceThreshold = 2.5 // Example threshold value, adjust as needed

        // If average distance is below the threshold, consider it a relevant result
        if (averageDistance <= averageDistanceThreshold) {
            // Add the corresponding media object to the searchResults
            searchResults.add(mediaList[index])
        }
    }

    return searchResults
}

fun main() {
    val filename = "src/main/media_data.txt"
    val mediaList = readMediaFromFile(filename)

    println("All media objects:")
    mediaList.forEachIndexed { index, media ->
        println("${index + 1}. Title: ${media.title}")
        println("   Author: ${media.author}")
        println("   Description: ${media.description}\n")
    }

    var userInput: String
    while (true) {
        userInput = getUserInput()

        if (userInput == "x") {
            break
        }

        val transformedUserInput = transformString(userInput)
        println(transformedUserInput)

        val transformedTitles = transformMediaTitles(mediaList)
        val transformedAuthors = transformMediaAuthors(mediaList)
        val transformedDescriptions = transformMediaDescriptions(mediaList)

        // Search for relevant media objects
        val relevantMedia = searchMedia(userInput, transformedDescriptions, mediaList)
        println("Relevant media objects found:")
        println()
        relevantMedia.forEach { media ->
            println("   Title: ${media.title}")
            println("   Author: ${media.author}")
            println("   Description: ${media.description}\n")
        }
    }



}