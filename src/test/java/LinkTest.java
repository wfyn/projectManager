import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkTest {
    public static final Pattern Pattern_Link = Pattern.compile(
            "((?:http://|https://|www\\.)?[-A-Za-z0-9+&@\\#/%?=~_|!:.]*\\.(com|cn|net|org|gov|edu)(?:/[-A-Za-z0-9+&@\\#/%?=~_|!:.]*)?(?:(?![-A-Za-z0-9+&@\\#/%?=~_|!:])|/|$))",
            Pattern.MULTILINE | Pattern.DOTALL
    );
    @Test
    public void test1() {


        Matcher m = Pattern_Link.matcher("https://github.com我");
        while (m.find()) {
            String link = m.group(1).trim();

            System.out.println(link); //https://github.com
        }
    }


}
