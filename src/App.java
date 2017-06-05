import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import utility.Fetch;
import utility.Logger;

class App {
    private static final String TOKEN = "7cf44938f02ba95c1b757b8ff36b7b3287235ee8";

    public static void main(String[] args) {

        Fetch.waitUntilReady(TOKEN);

        try (Logger logger = Logger.getInstance()) {
            logger.log("date,committer,owner,message,additions,deletions,changes");
            Fetch.repos().forEach(App::processRepo);

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

        record.owner = repo.get("owner").getAsString();

        String commitsURL = String.format(URL_TEMPLATE, owner, name, TOKEN);
        while (true) {
            try {
                Fetch.multipage(commitsURL).forEach(App::processCommitDetails);
                break;

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("failed to fetch " + commitsURL);

                Fetch.waitUntilReady(TOKEN);
            }
        }
    }

    private static void processCommitDetails(JsonElement element) {
        JsonObject details = element.getAsJsonObject();
        JsonObject commit = details.getAsJsonObject("commit");
        record.message = escapify(commit.get("message").getAsString());

        JsonObject committer = commit.getAsJsonObject("committer");
        record.committer = committer.get("name").getAsString();
        record.date = committer.get("date").getAsString();

        while (true) {
            String commitURL = String.format("%s?access_token=%s", details.get("url").getAsString(), TOKEN);
            try {
                JsonElement files = Fetch.page(commitURL).get("files");
                files.getAsJsonArray().forEach(App::processFile);
                break;

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("failed to fetch " + commitURL);

                Fetch.waitUntilReady(TOKEN);
            }
        }
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

    private static void processFile(JsonElement element) {
        JsonObject file = element.getAsJsonObject();
        if (file.get("filename").getAsString().toLowerCase().contains("readme.md")) {
            record.additions = file.get("additions").getAsInt();
            record.deletions = file.get("deletions").getAsInt();
            record.changes = file.get("changes").getAsInt();
            try {
                System.out.println(record);
                Logger.getInstance().log(record.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
