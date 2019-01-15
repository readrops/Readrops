package com.readrops.readropslibrary.Utils;

import java.io.InputStream;
import java.util.Scanner;

public final class Utils {

    public static String inputStreamToString(InputStream input) {
        Scanner scanner = new Scanner(input).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

}
