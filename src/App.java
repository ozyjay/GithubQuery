import com.github.jsonj.JsonElement;
import com.github.jsonj.tools.JsonParser;
import com.opencsv.CSVReader;

import javax.net.ssl.HttpsURLConnection;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class App {
    public static void main(String[] args) {
        try {

            CSVReader reader = new CSVReader(new FileReader("resources/repos.csv"));
            final String TOKEN = "db95460bcf441e12ffa610950d627655f39c51a9";

            Map<String, String> repos = new Hashtable<>();
            List<String[]> reposList = reader.readAll();
            for (String[] repo : reposList) {
                String owner = repo[0];
                String repoName = repo[1];
                repos.put(repoName, owner);
            }

            repos.forEach((repoName, owner) -> {
                try {
                    URL url = new URL(String.format("https://api.github.com/repos/%s/%s/commits?access_token=%s", owner, repoName, TOKEN));
                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

                    String link = connection.getHeaderField("Link");
                    System.out.println(link);


                    JsonParser parser = new JsonParser();
                    InputStreamReader results = new InputStreamReader(connection.getInputStream());
                    JsonElement element = parser.parse(results);

                    element.asArray().forEach((commit) -> {
                        commit.asObject().forEach((commitKey, commitValue) -> {
                            if (commitKey.equals("commit")) {
                                commitValue.asObject().forEach((n, v) -> {
                                    System.out.print(n);
                                    System.out.print(" ");
                                });
                            }
                        });
                        System.out.println();
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
