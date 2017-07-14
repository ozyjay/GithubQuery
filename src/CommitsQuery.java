import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import utility.Fetch;
import utility.Logger;
import utility.Processor;
import utility.Token;

import java.util.StringJoiner;

class CommitsQuery {

    public static void main(String[] args) {

        Fetch.waitUntilReady(Token.value);

        try (Logger logger = Logger.getInstance()) {
            logger.create("results", "resources/results.csv");
            logger.with("results").log("commitDate,committer,repoName,repoOwner,commitMessage,additions,deletions,changes");

            Fetch.repos("resources/repos.csv").forEach(CommitsQuery::processRepo);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static CommitRecord commitRecord = new CommitRecord();
    private static int count = 0;

    private static void processRepo(JsonElement element) {
        JsonObject repo = element.getAsJsonObject();

        count += 1;
        System.out.println("Count: " + count);
        System.out.println("processing " + repo);

        String owner = repo.get("owner").getAsString();
        String name = repo.get("name").getAsString();

        commitRecord.owner = owner;
        commitRecord.name = name;

        final String URL_TEMPLATE = "https://api.github.com/repos/%s/%s/commits?access_token=%s";
        String commitsURL = String.format(URL_TEMPLATE, owner, name, Token.value);
        Processor.multipage(commitsURL, CommitsQuery::processCommitDetails);
    }

    private static void processCommitDetails(JsonElement element) {
        JsonObject details = element.getAsJsonObject();
        JsonObject commit = details.getAsJsonObject("commit");
        commitRecord.message = Processor.escapify(commit.get("message").getAsString());

        JsonObject committer = commit.getAsJsonObject("committer");
        commitRecord.committer = Processor.escapify(committer.get("name").getAsString());
        commitRecord.date = committer.get("date").getAsString();

        String commitURL = String.format("%s?access_token=%s", details.get("url").getAsString(), Token.value);
        Processor.page(commitURL, (singleCommit) -> {
            JsonObject stats = singleCommit.get("stats").getAsJsonObject();
            commitRecord.additions = stats.get("additions").getAsInt();
            commitRecord.deletions = stats.get("deletions").getAsInt();

            JsonArray files = singleCommit.get("files").getAsJsonArray();
            commitRecord.filenames = processFiles(files);

            saveRecord();
        });
    }

    private static String processFiles(JsonArray files) {
        StringJoiner joiner = new StringJoiner(" ");

        files.forEach(element -> joiner.add(element.getAsJsonObject().get("filename").getAsString()));

        return joiner.toString();
    }

    private static void saveRecord() {
        try {
            System.out.println(commitRecord);
            Logger.getInstance().with("results").log(commitRecord.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
