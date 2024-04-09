package jabs.ledgerdata.sycomore;

public class SycomoreBlockUtils {
    public static int binaryToDecimal(String binary) {
        int decimal = 0;
        int power = 0;

        // Iterate over the binary string from right to left
        for (int i = binary.length() - 1; i >= 0; i--) {
            // Convert character to integer
            int digit = Character.getNumericValue(binary.charAt(i));

            // Add the contribution of the current digit to the decimal value
            decimal += digit * Math.pow(2, power);

            // Increment the power (2^0, 2^1, 2^2, ...)
            power++;
        }

        return decimal;
    }

    public static String xorBinaryStrings(String binary1, String binary2) {
        // Make sure the input strings have the same length
        if (binary1.length() != binary2.length()) {
            return String.valueOf(Integer.MAX_VALUE);
            //throw new IllegalArgumentException("Binary strings must have the same length");

        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < binary1.length(); i++) {
            // Convert characters to integers
            int digit1 = Character.getNumericValue(binary1.charAt(i));
            int digit2 = Character.getNumericValue(binary2.charAt(i));

            // XOR the digits and append the result to the StringBuilder
            result.append(digit1 ^ digit2);
        }

        return result.toString();
    }

    public static int binaryDistance(String str1, String str2) {
        return binaryToDecimal(xorBinaryStrings(str1, str2));

    }
}
