package org.sfvl.application;

public class FizzBuzz {
    public static String get(int i) {
        String result = "";
        if (i % 3 == 0) {
            result += "Fizz";
        }
        if (i % 5 == 0) {
            result += "Buzz";
        }
        return result.isEmpty()
                ? Integer.toString(i)
                : result;
    }
}
