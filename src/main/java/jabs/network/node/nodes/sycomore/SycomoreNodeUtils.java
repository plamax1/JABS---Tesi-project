package jabs.network.node.nodes.sycomore;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
public class SycomoreNodeUtils {
            // Method to populate the array with random numbers based on specified intervals
        public static int[] populateArray() {
            int array[] = new int[3600];
            Random rand = new Random();
            for (int i = 0; i < array.length; i++) {
                if ((i >= 0 && i <= 500) || (i >= 1000 && i <= 1500) || (i >= 2000 && i <= 2500)) {
                    // Generate random number between 0 and 100 for specified intervals
                    array[i] = rand.nextInt(101); // Generates a random integer between 0 (inclusive) and 101 (exclusive)
                } else {
                    // Generate random number between 600 and 999 for other intervals
                    array[i] = rand.nextInt(400) + 600; // Generates a random integer between 600 (inclusive) and 1000 (exclusive)
                }
            }
            return array;
        }

    public static HashMap<Integer, Integer> arrayToMap(int[] array) {
        HashMap<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < array.length; i++) {
            map.put(i, array[i]);
        }
        return map;
    }
    }


