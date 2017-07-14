import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import utility.Fetch;
import utility.Logger;
import utility.Processor;
import utility.Token;

class StatsQuery {

    public static void main(String[] args) {

        Fetch.waitUntilReady(Token.value);

        try (Logger logger = Logger.getInstance()) {
            logger.create("results", "resources/results.csv");
            logger.with("results").log("repoName,repoOwner,creationDate,watchers,forks,stars," +
                    "contributors,releases,openIssues,closedIssues,openPulls,closedPulls");

            Fetch.repos("resources/repos.csv").forEach(StatsQuery::processOwner);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static RepoRecord record = new RepoRecord();
    private static int count = 0;

    private static void processOwner(JsonElement element) {
        JsonObject repo = element.getAsJsonObject();

        count += 1;
        System.out.println("Count: " + count);
        System.out.println("processing " + repo);


        String owner = repo.get("owner").getAsString();
        String name = repo.get("name").getAsString();

        record.owner = owner;
        record.name = name;

        final String URL_TEMPLATE = "https://api.github.com/users/%s/repos?access_token=%s";
        String userReposURL = String.format(URL_TEMPLATE, owner, Token.value);

        while (true) {
            try {
                JsonObject userRepo = Fetch.find(userReposURL, "name", name);
                if (userRepo == null) {
                    System.out.println(String.format("ERROR: failed to find repo %s for owner %s", name, owner));
                    return;
                }
                processUserRepo(userRepo);
                return;

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("failed to fetch " + userReposURL);

                Fetch.waitUntilReady(Token.value);
            }
        }
    }

    private static void processUserRepo(JsonElement element) {
        JsonObject userRepo = element.getAsJsonObject();

        record.creationDate = userRepo.get("created_at").getAsString();
        record.forks = userRepo.get("forks_count").getAsInt();
        record.stars = userRepo.get("stargazers_count").getAsInt();

        String contributorsURL = String.format("%s?access_token=%s", userRepo.get("contributors_url").getAsString(), Token.value);
        record.contributors = Processor.count(contributorsURL);
        String watchersURL = String.format("%s?state=open&access_token=%s", userRepo.get("subscribers_url").getAsString(), Token.value);
        record.watchers = Processor.count(watchersURL);

        final String URL_TEMPLATE = String.format("https://api.github.com/repos/%s/%s", record.owner, record.name);
        String releasesURL = String.format("%s/releases?access_token=%s", URL_TEMPLATE, Token.value);
        record.releases = Processor.count(releasesURL);

        String openPullsURL = String.format("%s/pulls?state=open&access_token=%s", URL_TEMPLATE, Token.value);
        record.openPulls = Processor.count(openPullsURL);
        String closedPullsURL = String.format("%s/pulls?state=closed&access_token=%s", URL_TEMPLATE, Token.value);
        record.closedPulls = Processor.count(closedPullsURL);

        // note: pull requests are treated as issues by the API - we are ignoring them here
        String openIssuesURL = String.format("%s/issues?state=open&access_token=%s", URL_TEMPLATE, Token.value);
        record.openIssues = Processor.count(openIssuesURL) - record.openPulls;
        String closedIssuesURL = String.format("%s/issues?state=closed&access_token=%s", URL_TEMPLATE, Token.value);
        record.closedIssues = Processor.count(closedIssuesURL) - record.closedPulls;

        saveRecord();
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
