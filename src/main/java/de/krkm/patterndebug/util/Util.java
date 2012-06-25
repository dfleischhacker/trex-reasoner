package de.krkm.patterndebug.util;

public class Util {
    public static String getFragment(String uri) {
        String[] parts = uri.split("#");

        if (parts.length == 1) {
            parts = uri.split("/");
            return parts[parts.length - 1];
        }
        else {
            return parts[parts.length - 1];
        }
    }
}
