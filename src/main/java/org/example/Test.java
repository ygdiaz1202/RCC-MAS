package org.example;

import java.util.ArrayList;
import java.util.List;

public class Test {
    public static List<int[]> generateAllSubstitutions(int[] originalArray, int currentIndex, int[] currentArray, List<Integer>[] substitutionValues) {
        List<int[]> result = new ArrayList<>();
        if (currentIndex == originalArray.length) {
            result.add(currentArray.clone());
            return result;
        }

        // Continue with the next index if no substitutions are available
        if (substitutionValues[originalArray[currentIndex]] == null || substitutionValues[originalArray[currentIndex]].isEmpty()) {
            currentArray[currentIndex] = originalArray[currentIndex];
            result.addAll(generateAllSubstitutions(originalArray, currentIndex + 1, currentArray, substitutionValues));
        } else {
            // Include the original value in the result
            currentArray[currentIndex] = originalArray[currentIndex];
            result.addAll(generateAllSubstitutions(originalArray, currentIndex + 1, currentArray.clone(), substitutionValues));

            // Substitute with alternative values
            for (int newValue : substitutionValues[originalArray[currentIndex]]) {
                currentArray[currentIndex] = newValue;
                result.addAll(generateAllSubstitutions(originalArray, currentIndex + 1, currentArray.clone(), substitutionValues));
            }
        }
        return result;
    }

    public static void main(String[] args) {
        int[] originalArray = {1, 2, 3, 4};
        int[] currentArray = new int[originalArray.length];

        // Example substitution values for each element in the original array
        List<Integer>[] substitutionValues = new ArrayList[5];
        substitutionValues[1] = new ArrayList<>(List.of(10, 20));
        substitutionValues[2] = new ArrayList<>(List.of(30, 40));
        substitutionValues[3] = new ArrayList<>(List.of(50, 60));

        List<int[]> result = generateAllSubstitutions(originalArray, 0, currentArray.clone(), substitutionValues);

        // Print the result for testing
        for (int[] array : result) {
            for (int value : array) {
                System.out.print(value + " ");
            }
            System.out.println();
        }
    }
    }
