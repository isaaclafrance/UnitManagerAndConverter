package com.isaacapps.unitconverterapp.utilities;

public class RegExUtility {

    /**
     * Ex. formats. 1.0, -2, 5.3e-3
     */
    public static final String SIGNED_DOUBLE_VALUE_REGEX = "(?:[-+]?(?:\\d*[.])?\\d+(?:[eE][+-]?\\d+)?)";

    /**
     * Finds reserved characters in the symbol group and escapes them
     */
    public static String escapeRegexReservedCharacters(String stringWithReservedCharacters) {
        String reservedSymbols = "[*+^?<>$.|]";
        StringBuilder escapedStringBuilder = new StringBuilder();

        for (char character : stringWithReservedCharacters.toCharArray()) {
            if (reservedSymbols.contains(String.valueOf(character)))
                escapedStringBuilder.append("\\");

            escapedStringBuilder.append(character);
        }

        return escapedStringBuilder.toString();
    }
}
