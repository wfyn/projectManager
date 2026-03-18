import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SmTest {


    @Test
    public void test3() throws Exception {
        System.out.println("当前工作目录: " + Paths.get("").toAbsolutePath());

        // 打印文件绝对路径
        System.out.println("文件绝对路径: " + Paths.get("article.html").toAbsolutePath());
        String html = new String(Files.readAllBytes(Paths.get("article.html")), "UTF-8");
        // 正则表达式
        String regex = "<div class=\"rich_media_content[\\s\\S]*?id=\"js_content\"[^>]*>([\\s\\S]*?)</div>";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(html);

        // 查找并打印匹配内容
        while (matcher.find()) {
            System.out.println("=== 匹配到的完整内容 ===");
            System.out.println(matcher.group(0));  // 完整匹配

            System.out.println("\n=== 捕获组内容 (id=js_content的div内部) ===");
            System.out.println(matcher.group(1));  // 第一个捕获组
        }

    }

    @Test
    public void test4() throws Exception {
        System.out.println("当前工作目录: " + Paths.get("").toAbsolutePath());

        // 打印文件绝对路径
        System.out.println("文件绝对路径: " + Paths.get("article.html").toAbsolutePath());
        String html = new String(Files.readAllBytes(Paths.get("article.html")), "UTF-8");
        Set<String> list = extractImageUrls(html);
        for (String s : list) {
            System.out.println(s);
        }

    }

    /**
     * 提取HTML中的图片URL
     */
    public static Set<String> extractImageUrls(String html) {
        Set<String> urls = new HashSet<>();

        // 匹配 data-src 属性（微信文章常用）
        String dataSrcPattern = "data-src\\s*=\\s*\"([^\"]+)\"";
        Pattern pattern1 = Pattern.compile(dataSrcPattern);
        Matcher matcher1 = pattern1.matcher(html);
        while (matcher1.find()) {
            String url = matcher1.group(1);
            if (!urls.contains(url)) {
                urls.add(cleanUrl(url));
            }
        }

        // 匹配 src 属性
        String srcPattern = "src\\s*=\\s*\"([^\"]+)\"";
        Pattern pattern2 = Pattern.compile(srcPattern);
        Matcher matcher2 = pattern2.matcher(html);
        while (matcher2.find()) {
            String url = matcher2.group(1);
            // 过滤掉 base64 和 svg 占位图
            if (!url.startsWith("data:image/svg") && !urls.contains(url)) {
                urls.add(cleanUrl(url));
            }
        }

        return urls;
    }


    /**
     * 清理URL中的微信参数，获取干净链接
     */
    public static String cleanUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }

        // 第一步：移除 URL 中的锚点部分（#imgIndex=xxx）
        String urlWithoutAnchor = url.split("#")[0];

        // 第二步：处理查询参数
        int queryIndex = urlWithoutAnchor.indexOf("?");
        if (queryIndex == -1) {
            // 没有查询参数，直接返回
            return urlWithoutAnchor;
        }

        // 获取基础路径（问号之前的部分）
        String baseUrl = urlWithoutAnchor.substring(0, queryIndex);

        // 获取查询字符串（问号之后的部分）
        String queryString = urlWithoutAnchor.substring(queryIndex + 1);

        // 第三步：提取并只保留 wx_fmt 参数
        Pattern wxFmtPattern = Pattern.compile("(^|&)wx_fmt=[^&]+");
        Matcher matcher = wxFmtPattern.matcher(queryString);

        if (matcher.find()) {
            String wxFmtParam = matcher.group();
            // 移除开头的 & 符号（如果存在）
            if (wxFmtParam.startsWith("&")) {
                wxFmtParam = wxFmtParam.substring(1);
            }
            return baseUrl + "?" + wxFmtParam;
        }

        // 如果没有 wx_fmt 参数，返回基础URL（不带任何参数）
        return baseUrl;
    }
}
