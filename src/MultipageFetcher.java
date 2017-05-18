import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class MultipageFetcher {
    private List<String> pageLinks;

    MultipageFetcher() {
        pageLinks = new ArrayList<>();
    }

    String fetch(String urlAddress) throws IOException {
        StringBuilder builder = new StringBuilder();

        URL url = new URL(urlAddress);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        String linkData = connection.getHeaderField("Link");
        String[] pageDataList = linkData.split(",");

        Pattern pattern = Pattern.compile("<(\\S+)&page=(d+)>\\S+");
        for (String pageData : pageDataList) {
            Matcher matcher = pattern.matcher(pageData);
            if (matcher.matches()) {
                System.out.println(matcher.group(0));
            }
        }

        Scanner scanner = new Scanner(connection.getInputStream());
        while (scanner.hasNextLine()) {
            builder.append(scanner.nextLine());
        }
        scanner.close();


        return builder.toString();
    }
}
