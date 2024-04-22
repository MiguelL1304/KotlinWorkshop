import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

class Media {
    String title;
    String author;
    String description;

    public Media(String title, String author, String description) {
        this.title = title;
        this.author = author;
        this.description = description;
    }
}

public class Main {

    public static List<Media> readMediaFromFile(String filename) throws IOException {
        List<Media> mediaList = new ArrayList<>();

        Scanner scanner = new Scanner(new File(filename));
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            String[] parts = line.split("\t");

            if (parts.length >= 3) {
                String title = parts[0].trim();
                String author = parts[1].trim();
                String description = parts[2].trim();

                mediaList.add(new Media(title, author, description));
            }
        }
        scanner.close();

        return mediaList;
    }

    public static String getUserInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter search query: ");
        return scanner.nextLine().trim();
    }

    public static List<String> transformString(String input) {
        String transformedString = input.toUpperCase();

        // Replace special characters
        transformedString = transformedString.replaceAll("[^\\x00-\\x7F]", "");
        transformedString = transformedString.replace("Ö", "O");
        transformedString = transformedString.replace("Ü", "U");
        transformedString = transformedString.replace("Ä", "A");
        transformedString = transformedString.replace("É", "E");
        transformedString = transformedString.replace("Ñ", "N");
        transformedString = transformedString.replace("Ç", "C");
        transformedString = transformedString.replace("ß", "SS");
        transformedString = transformedString.replace("À", "A");
        transformedString = transformedString.replace("Ô", "O");

        // Delete common characters
        char[] commonChar = { ',', '.', '@', '%', '!', '?', '&', '(', ')', ':', '\'', '-' };
        for (char c : commonChar) {
            transformedString = transformedString.replace(Character.toString(c), "");
        }

        // Split the transformedString into an array of words
        String[] wordsArray = transformedString.split("\\s+");

        // Remove common words
        Set<String> commonWords = new HashSet<>();
        commonWords.add("THE");
        commonWords.add("OF");
        commonWords.add("AND");
        commonWords.add("A");
        commonWords.add("TO");
        commonWords.add("IN");
        commonWords.add("ON");
        commonWords.add("FOR");
        commonWords.add("WITH");

        List<String> filteredWordsArray = new ArrayList<>();
        for (String word : wordsArray) {
            if (!commonWords.contains(word)) {
                filteredWordsArray.add(word);
            }
        }

        // Change Roman numerals
        String[] romanNumerals = { "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X" };

        List<String> transformedArray = new ArrayList<>();
        for (String word : filteredWordsArray) {
            boolean isRomanNumeral = false;
            for (String numeral : romanNumerals) {
                if (word.equals(numeral)) {
                    transformedArray.add(Integer.toString(romanToInt(word)));
                    isRomanNumeral = true;
                    break;
                }
            }
            if (!isRomanNumeral) {
                transformedArray.add(word);
            }
        }

        return transformedArray;
    }

    private static int romanToInt(String s) {
        if (s.equals("I")) return 1;
        if (s.equals("II")) return 2;
        if (s.equals("III")) return 3;
        if (s.equals("IV")) return 4;
        if (s.equals("V")) return 5;
        if (s.equals("VI")) return 6;
        if (s.equals("VII")) return 7;
        if (s.equals("VIII")) return 8;
        if (s.equals("IX")) return 9;
        if (s.equals("X")) return 10;
        return -1;
    }

    public static List<List<String>> transformMediaTitles(List<Media> mediaList) {
        List<List<String>> transformedTitles = new ArrayList<>();
        for (Media media : mediaList) {
            transformedTitles.add(transformString(media.title));
        }
        return transformedTitles;
    }

    public static List<List<String>> transformMediaAuthors(List<Media> mediaList) {
        List<List<String>> transformedAuthors = new ArrayList<>();
        for (Media media : mediaList) {
            transformedAuthors.add(transformString(media.author));
        }
        return transformedAuthors;
    }

    public static List<List<String>> transformMediaDescriptions(List<Media> mediaList) {
        List<List<String>> transformedDescriptions = new ArrayList<>();
        for (Media media : mediaList) {
            transformedDescriptions.add(transformString(media.description));
        }
        return transformedDescriptions;
    }

    public static int levenshteinDistance(String str1, String str2) {
        int len1 = str1.length();
        int len2 = str2.length();
        int[][] matrix = new int[len1 + 1][len2 + 1];

        // Initialize matrix with 0s
        for (int i = 0; i <= len1; i++) {
            matrix[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            matrix[0][j] = j;
        }

        // Fill in the rest of the matrix
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1;
                matrix[i][j] = Math.min(Math.min(matrix[i - 1][j] + 1, matrix[i][j - 1] + 1), matrix[i - 1][j - 1] + cost);
            }
        }
        return matrix[len1][len2];
    }

    public static List<Media> searchMedia(String userInput, List<List<String>> transformedTitles, List<Media> mediaList) {
        List<String> transformedInput = transformString(userInput); // Transform user input

        List<Media> searchResults = new ArrayList<>();

        // Iterate over each transformed title in transformedTitles
        for (int index = 0; index < transformedTitles.size(); index++) {
            List<String> transformedTitle = transformedTitles.get(index);
            int totalDistance = 0;
            int matchCount = 0;

            // Iterate over each word in transformedInput
            for (String inputWord : transformedInput) {
                int minDistance = Integer.MAX_VALUE; // Initialize minDistance for each input word
                // Find the closest matching word in transformedTitle
                for (String titleWord : transformedTitle) {
                    int distance = levenshteinDistance(inputWord, titleWord);
                    if (distance < minDistance) {
                        minDistance = distance;
                    }
                }

                // Add the closest matching word distance to totalDistance
                totalDistance += minDistance;
                matchCount++;
            }

            // Calculate average distance for the title based on matching words only
            double averageDistance = (matchCount == 0) ? Double.POSITIVE_INFINITY : (double) totalDistance / matchCount;

            // Define a threshold value for average distance
            double averageDistanceThreshold = 2.5; // Example threshold value, adjust as needed

            // If average distance is below the threshold, consider it a relevant result
            if (averageDistance <= averageDistanceThreshold) {
                // Add the corresponding media object to the searchResults
                searchResults.add(mediaList.get(index));
            }
        }

        return searchResults;
    }

    public static void main(String[] args) throws IOException {
        String filename = "src/main/media_data.txt";
        List<Media> mediaList = readMediaFromFile(filename);

        // Print all media objects
        System.out.println("All media objects:");
        for (int index = 0; index < mediaList.size(); index++) {
            Media media = mediaList.get(index);
            System.out.println((index + 1) + ". Title: " + media.title);
            System.out.println("   Author: " + media.author);
            System.out.println("   Description: " + media.description + "\n");
        }

        // Get user input
        String userInput;
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Search media");
            userInput = getUserInput();

            if (userInput.equals("x")) {
                break; // Exit the loop if user input is 'x'
            }

            // Transform user input
            List<String> transformedInput = transformString(userInput);

            // Print transformed input
            System.out.println("Transformed input:");
            System.out.println(transformedInput);

            List<List<String>> transformedTitles = transformMediaTitles(mediaList);
            List<List<String>> transformedAuthors = transformMediaAuthors(mediaList);
            List<List<String>> transformedDescriptions = transformMediaDescriptions(mediaList);

            // Search for relevant media objects
            List<Media> relevantMedia = searchMedia(userInput, transformedTitles, mediaList);
            System.out.println("Relevant media objects found:");
            System.out.println();
            for (Media media : relevantMedia) {
                System.out.println("   Title: " + media.title);
                System.out.println("   Author: " + media.author);
                System.out.println("   Description: " + media.description + "\n");
            }
        }
    }
}