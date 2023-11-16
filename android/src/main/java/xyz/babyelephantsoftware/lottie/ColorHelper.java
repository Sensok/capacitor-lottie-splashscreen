package xyz.babyelephantsoftware.lottie;

public class ColorHelper {
    private static final String LOG_TAG = "ColorHelper";

    /**
     * Parses the color string and returns the corresponding color-int.
     *
     * @param colorString The color string to parse.
     * @return The parsed color as an int.
     * @throws IllegalArgumentException If the color string is invalid.
     */
    public static int parseColor(String colorString) {
        if (colorString == null || colorString.isEmpty()) {
            throw new IllegalArgumentException("Color string cannot be null or empty");
        }

        String xColorString = colorString.charAt(0) == '#' ? colorString.substring(1) : colorString;

        // Reverse alpha value if present
        if (xColorString.length() == 8) {
            xColorString = xColorString.substring(6) + xColorString.substring(0, 6);
        }

        long color = Long.parseLong(xColorString, 16);

        if (xColorString.length() == 6) {
            color |= 0xFF000000; // Add opaque alpha
        } else if (xColorString.length() != 8) {
            throw new IllegalArgumentException("Unknown color: " + colorString);
        }

        return (int) color;
    }
}
