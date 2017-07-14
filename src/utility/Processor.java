package utility;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.Consumer;

public class Processor {

    public static void page(String url, Consumer<? super JsonObject> action) {
        while (true) {
            try {
                action.accept(Fetch.page(url));
                return;

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("failed to page " + url);

                Fetch.waitUntilReady(Token.value);
            }
        }
    }

    public static int count(String url) {
        while (true) {
            try {
                return Fetch.count(url);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("failed to count " + url);

                Fetch.waitUntilReady(Token.value);
            }
        }
    }

    public static void multipage(String url, Consumer<? super JsonElement> action) {
        while (true) {
            try {
                Fetch.multipage(url).forEach(action);
                return;

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("failed to multipage " + url);

                Fetch.waitUntilReady(Token.value);
            }
        }
    }

    // convert newlines, tabs, and comma's into spaces
    // convert double-quote into single-quote
    // ignore carriage return
    public static String escapify(String message) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < message.length(); ++i) {
            char ch = message.charAt(i);
            switch (ch) {
                case '\t':
                case '\n':
                case ',':
                    builder.append(' ');
                    for (int j = i + 1; j < message.length(); ++j) {
                        if (Character.isWhitespace(message.charAt(j))) {
                            i = j;
                        } else {
                            break;
                        }
                    }
                    break;
                case '\r':
                    break;
                case '"':
                    builder.append("'");
                    break;
                default:
                    builder.append(ch);
            }
        }

        return builder.toString();
    }

}
