package studentmanagement.utils;

import java.util.Scanner;

public class InputHelper {
    public static int getInt(Scanner sc, String message) {
        System.out.print(message);
        while (!sc.hasNextInt()) {
            System.out.println("Invalid input! Enter a number.");
            sc.next();
        }
        return sc.nextInt();
    }
}