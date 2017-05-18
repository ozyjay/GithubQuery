import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class MultipageFetcherTest {
    @Test
    public void fetch() throws Exception {

        MultipageFetcher fetcher = new MultipageFetcher();

        final String TOKEN = "db95460bcf441e12ffa610950d627655f39c51a9";
        final String owner = "d3";
        final String repoName = "d3";

        final String urlStr = "https://api.github.com/repos/%s/%s/commits?access_token=%s";
        fetcher.fetch(String.format(urlStr, owner, repoName, TOKEN));
    }

    @Test
    public void testURLReg() throws Exception {
        String data = "<https://api.github.com/repositories/943149/commits?access_token=db95460bcf441e12ffa610950d627655f39c51a9&page=2>; rel=\"next\"";
        Pattern pattern = Pattern.compile("<(.+)&page=(\\d+)>;.+");
        Matcher matcher = pattern.matcher(data);
        assertTrue(matcher.matches());
    }
}