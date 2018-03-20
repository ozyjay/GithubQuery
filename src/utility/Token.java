package utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Token {

    static {
        String text;
        File file = new File("resources/token.txt");

        try (Scanner scanner = new Scanner(file)) {
            text = scanner.next().trim();
        } catch (FileNotFoundException fileNotFound) {
            text = "invalid";
        }

        value = text;
    }

    public static final String value;
}