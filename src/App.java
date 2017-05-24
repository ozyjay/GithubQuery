import com.github.jsonj.JsonElement;
import com.github.jsonj.tools.JsonParser;
import com.opencsv.CSVReader;

import javax.net.ssl.HttpsURLConnection;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class App {

    private static String fetch(String urlAddress) throws IOException {
        String regex = "<(.+&page=\\d+)>; rel=\"next\".+";
        Pattern pattern = Pattern.compile(regex);
        String pageLink = urlAddress;
        StringBuilder builder = new StringBuilder();

        while (true) {
            URL url = new URL(pageLink);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            String linkData = connection.getHeaderField("Link");

            Scanner scanner = new Scanner(connection.getInputStream());
            while (scanner.hasNextLine()) {
                builder.append(scanner.nextLine());
            }
            scanner.close();

            System.out.println("builder: " + builder);

            if (linkData == null) {
                break;
            }

//            System.out.println("linkData:" + linkData);

            Matcher matcher = pattern.matcher(linkData);
            if (matcher.matches()) {
                pageLink = matcher.group(1);
            } else {
                break;
            }

        }
        System.out.println("done");
        return builder.toString().replaceAll("]\\[", ",");
    }

    public static void main(String[] args) {

        Map<String, String> repos = new Hashtable<>();
        try {
            CSVReader reader = new CSVReader(new FileReader("resources/repos.csv"));

            List<String[]> reposList = reader.readAll();
            for (String[] repo : reposList) {
                String owner = repo[0];
                String repoName = repo[1];
                repos.put(repoName, owner);
            }

        } catch (Exception e) {
            System.out.println(e.toString());
        }

        final String URL_TEMPLATE = "https://api.github.com/repos/%s/%s/commits?access_token=%s";
        final String TOKEN = "7cf44938f02ba95c1b757b8ff36b7b3287235ee8";
        repos.forEach((repoName, owner) -> {
            try {
                System.out.println(String.format("processing repo %s owner %s...", repoName, owner));
                String repoURL = String.format(URL_TEMPLATE, owner, repoName, TOKEN);
                String repoResults = fetch(repoURL);
                JsonParser parser = new JsonParser();
                JsonElement repoJSON = parser.parse(repoResults);

                repoJSON.asArray().forEach((repo) -> {
                    try {
                        String commitURL = repo.asObject().get("url").toString();
                        String commitResults = fetch(String.format("%s?access_token=%s", commitURL, TOKEN));
                        JsonElement commitJSON = parser.parse(commitResults);
                        if (commitJSON.isArray()) {
                            commitJSON.asArray().forEach((commit) -> System.out.println(commit.asObject().get("files").toString()));
                        } else {
                            JsonElement filesJSON = commitJSON.asObject().get("files");
                            if (filesJSON.isArray()) {
                                filesJSON.asArray().forEach((file) -> System.out.println(file.asObject().get("filename")));
                            } else {
                                System.out.println(filesJSON.asObject().get("filename"));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
