package utility;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opencsv.CSVReader;

import javax.net.ssl.HttpsURLConnection;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Fetch {
    private static JsonParser parser = new JsonParser();

    public static JsonObject page(String urlAddress) throws IOException {
        URL url = new URL(urlAddress);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        JsonElement result = parser.parse(new InputStreamReader(connection.getInputStream()));
        return result.getAsJsonObject();
    }

    public static JsonArray multipage(String urlAddress) throws IOException {
        String regex = "<(.+&page=\\d+)>; rel=\"next\".+";
        Pattern pattern = Pattern.compile(regex);

        JsonArray results = new JsonArray();

        String pageLink = urlAddress;
        while (true) {
            URL url = new URL(pageLink);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            String linkData = connection.getHeaderField("Link");
            if (linkData == null) {
                return results;
            }

            JsonElement result = parser.parse(new InputStreamReader(connection.getInputStream()));
            results.addAll(result.getAsJsonArray());

            Matcher matcher = pattern.matcher(linkData);
            if (matcher.matches()) {
                pageLink = matcher.group(1);
            } else {
                break; // all pages retrieved
            }
        }

        return results;
    }

    public static JsonArray repos() throws IOException {
        JsonArray repos = new JsonArray();

        CSVReader csvReader = new CSVReader(new FileReader("resources/repos.csv"));
        List<String[]> csvData = csvReader.readAll();

        csvData.forEach((entry) -> {
            JsonObject repo = new JsonObject();
            repo.addProperty("owner", entry[0]);
            repo.addProperty("name", entry[1]);
            repos.add(repo);
        });

        return repos;
    }
}
