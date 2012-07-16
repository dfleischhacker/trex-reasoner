package de.krkm.trex.util;

public class Util {
    private final static boolean NO_SHORTENING = true;
    public static String getFragment(String uri) {
        if (NO_SHORTENING) {
            return uri;
        }
        String[] parts = uri.split("#");

        if (parts.length == 1) {
            parts = uri.split("/");
            return parts[parts.length - 1];
        } else {
            return parts[parts.length - 1];
        }
    }
}
