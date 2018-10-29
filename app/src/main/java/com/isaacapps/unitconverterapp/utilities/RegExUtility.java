package com.isaacapps.unitconverterapp.utilities;

import java.util.regex.Pattern;

public class RegExUtility {

    /**
     * Ex. formats. 1.0, -2, 5.3e-3
     */
    public static final Pattern SIGNED_DOUBLE_VALUE_REGEX_PATTERN = Pattern.compile("(?:[-+]?(?:\\d*[.])?\\d+(?:[eE][+-]?\\d+)?)");

    /**
     * Finds reserved characters in the symbol group and escapes them
     */
    public static String escapeRegexReservedCharacters(String stringWithReservedCharacters) {
        String reservedSymbols = "{([*+^?<>$.|])}";
        StringBuilder escapedStringBuilder = new StringBuilder();

        for (char character : stringWithReservedCharacters.toCharArray()) {
            if (reservedSymbols.contains(String.valueOf(character)))
                escapedStringBuilder.append("\\");

            escapedStringBuilder.append(character);
        }

        return escapedStringBuilder.toString();
    }
}
