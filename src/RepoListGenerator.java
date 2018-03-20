import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import utility.Fetch;
import utility.Logger;
import utility.Token;

import java.io.IOException;
import java.util.Arrays;

public class RepoListGenerator {

    private final static String[] validTypes = {"forks", "stars"};
    private final static String[] validLanguages = {"java", "python", "c", "c#", "javascript", "ruby"};

    public static void main(String[] args) {

        if (invalidParams(args)) return;

        Fetch.waitUntilReady(Token.value);

        try (Logger logger = Logger.getInstance()) {
            String type = args[0];
            String language = args[1];
            int count = Integer.parseInt(args[2]);

            final String filename = String.format("resources/repos_%s_%s_%d.csv", type, language, count);
            logger.create("repo", filename);

            final String URL_TEMPLATE = "https://api.github.com/search/repositories?q=%s:>1+language:%s" +
                    "&sort=%s&order=desc&type=repositories";
            String searchURL = String.format(URL_TEMPLATE, type, language, type);
            processSearch(searchURL, count);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean invalidParams(String[] args) {
        if (args.length != 3) {
            System.out.println("usage: RepoListGenerator type language count");
            return true;
        }

        if (!Arrays.asList(validTypes).contains(args[0])) {
            System.out.println("the first parameter must be one of: " + Arrays.toString(validTypes));
            return true;
        }

        if (!Arrays.asList(validLanguages).contains(args[1])) {
            System.out.println("the second parameter must be one of: " + Arrays.toString(validLanguages));
            return true;
        }

        if (!args[2].matches("\\d+")) {
            System.out.println("the third parameter must be a whole number (1 - 500)");
            return true;
        }

        int value = Integer.parseInt(args[2]);
        if (value < 1 || value > 500) {
            System.out.println("the third parameter is not in the range (1-500)");
            return true;
        }

        return false;
    }

    private static void processSearch(String urlAddress, int count) {

        try {
            int pageCount = 1;
            int remaining = count;

            while (true) {
                String pagedAddress = String.format("%s&page=%d", urlAddress, pageCount);
                JsonArray items = Fetch.page(pagedAddress).get("items").getAsJsonArray();

                if (items.size() < remaining) {
                    items.forEach(RepoListGenerator::processItem);
                    remaining -= items.size();
                    ++pageCount;
                } else {
                    for (int i = 0; i < remaining; ++i) {
                        processItem(items.get(i));
                    }
                    return;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("failed to fetch: " + urlAddress);
        }
    }

    private static void processItem(JsonElement element) {
        JsonObject item = element.getAsJsonObject();

        String fullname = item.getAsJsonPrimitive("full_name").getAsString();
        System.out.println(fullname);
        String[] data = fullname.split("/");
        String repoOwner = data[0];
        String repoName = data[1];
        try {
            Logger.getInstance().with("repo").log(String.format("%s,%s", repoOwner, repoName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
