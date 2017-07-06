import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import utility.Fetch;
import utility.Logger;

import java.util.*;

class App {

    public static void main(String[] args) {

        Fetch.waitUntilReady(Token.value);

        try (Logger logger = Logger.getInstance()) {
            logger.create("results", "resources/results.csv");
            logger.with("results").log("date,committer,owner,message,additions,deletions,changes");

            Fetch.repos("resources/repos.csv").forEach(App::processRepo);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Record record = new Record();
    private static int count = 0;

    private static void processRepo(JsonElement element) {
        JsonObject repo = element.getAsJsonObject();

        count += 1;
        System.out.println("Count: " + count);
        System.out.println("processing " + repo);

        final String URL_TEMPLATE = "https://api.github.com/repos/%s/%s/commits?access_token=%s";

        String owner = repo.get("owner").getAsString();
        String name = repo.get("name").getAsString();

        record.owner = owner;

        String commitsURL = String.format(URL_TEMPLATE, owner, name, Token.value);
        while (true) {
            try {
                Fetch.multipage(commitsURL).forEach(App::processCommitDetails);
                return;

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("failed to fetch " + commitsURL);

                Fetch.waitUntilReady(Token.value);
            }
        }
    }

    private static void processCommitDetails(JsonElement element) {
        JsonObject details = element.getAsJsonObject();
        JsonObject commit = details.getAsJsonObject("commit");
        record.message = escapify(commit.get("message").getAsString());

        JsonObject committer = commit.getAsJsonObject("committer");
        record.committer = escapify(committer.get("name").getAsString());
        record.date = committer.get("date").getAsString();

        while (true) {
            String commitURL = String.format("%s?access_token=%s", details.get("url").getAsString(), Token.value);
            try {
                JsonObject singleCommit = Fetch.page(commitURL).getAsJsonObject();

                JsonObject stats = singleCommit.get("stats").getAsJsonObject();
                record.additions = stats.get("additions").getAsInt();
                record.deletions = stats.get("deletions").getAsInt();

                JsonArray files = singleCommit.get("files").getAsJsonArray();
                record.filenames = processFiles(files);

                saveRecord();
                return;

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("failed to fetch " + commitURL);

                Fetch.waitUntilReady(Token.value);
            }
        }
    }

    private static String processFiles(JsonArray files) {
        StringJoiner joiner = new StringJoiner(" ");

        files.forEach(element -> joiner.add(element.getAsJsonObject().get("filename").getAsString()));

        return joiner.toString();
    }

    // convert newlines, tabs, and comma's into spaces
    // convert double-quote into single-quote
    // ignore carriage return
    private static String escapify(String message) {
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

    private static void saveRecord() {
        try {
            System.out.println(record);
            Logger.getInstance().with("results").log(record.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
