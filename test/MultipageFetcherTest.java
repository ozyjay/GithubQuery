import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class MultipageFetcherTest {
    @Test
    public void testURLReg() throws Exception {
        String data = "<https://api.github.com/repositories/943149/commits?access_token=7cf44938f02ba95c1b757b8ff36b7b3287235ee8&page=2>; rel=\"next\"";
        Pattern pattern = Pattern.compile("<(.+)&page=(\\d+)>;.+");
        Matcher matcher = pattern.matcher(data);
        assertTrue(matcher.matches());
    }
}