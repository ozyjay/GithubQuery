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
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Fetch {
    private static JsonParser parser = new JsonParser();

    public static JsonObject page(String urlAddress) throws IOException {
        URL url = new URL(urlAddress);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        JsonElement result = parser.parse(new InputStreamReader(connection.getInputStream()));

        pauseIfNecessary(connection);

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

            pauseIfNecessary(connection);

            Matcher matcher = pattern.matcher(linkData);
            if (matcher.matches()) {
                pageLink = matcher.group(1);
            } else {
                break; // all pages retrieved
            }
        }

        return results;
    }

    private static void pauseIfNecessary(HttpsURLConnection connection) {
        String remainingData = connection.getHeaderField("X-RateLimit-Remaining");
        int remainingCount = Integer.parseInt(remainingData);
        System.out.println("fetching... remaining fetches before pause: " + remainingCount);

        if (remainingCount == 0) {
            String resetTimeData = connection.getHeaderField("X-RateLimit-Reset");
            long resetTime = Long.parseLong(resetTimeData) * 1000;
            pause(resetTime);
        }
    }

    public static void waitUntilReady(final String TOKEN) {
        try {
            String urlAddress = String.format("https://api.github.com/rate_limit?access_token=%s", TOKEN);
            URL url = new URL(urlAddress);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            JsonElement result = parser.parse(new InputStreamReader(connection.getInputStream()));

            JsonObject rate = result.getAsJsonObject().getAsJsonObject("rate");
            int remainingCount = rate.get("remaining").getAsInt();

            if (remainingCount == 0) {
                long resetTime = rate.get("reset").getAsLong() * 1000;
                pause(resetTime);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void pause(long resetTime) {
        Date now = new Date();
        long sleepDuration = resetTime - now.getTime() + 1000 * 10;

        System.out.print(String.format("pausing until %s zzzzzz.....", new Date(resetTime)));
        try {
            Thread.sleep(sleepDuration);
        } catch (InterruptedException e) {
            System.out.println("aborted pause!");
        }
        System.out.println("done!");
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
