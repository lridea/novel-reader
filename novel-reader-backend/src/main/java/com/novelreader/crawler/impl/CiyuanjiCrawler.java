package com.novelreader.crawler.impl;

import com.novelreader.crawler.BaseCrawler;
import com.novelreader.crawler.model.CrawlResult;
import com.novelreader.crawler.model.Chapter;
import com.novelreader.entity.Novel;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class CiyuanjiCrawler implements BaseCrawler {

    private static final String PLATFORM = "ciyuanji";
    private static final String BASE_URL = "https://www.ciyuanji.com";

    private static final Pattern BOOK_ID_PATTERN = Pattern.compile("/b_d_(\\d+)\\.html");
    private static final Pattern CHAPTER_ID_PATTERN = Pattern.compile("/chapter/(\\d+)_(\\d+)\\.html");
    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2})");

    private static final int MIN_DELAY_MS = 500;
    private static final int MAX_DELAY_MS = 1000;
    private static final int MAX_PAGES = 100;
    private static final int MAX_RETRIES = 3;

    @Value("${crawler.user-agent}")
    private String userAgent;

    private final Random random = new Random();

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .followRedirects(true)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    @Override
    public String getPlatformName() {
        return PLATFORM;
    }

    @Override
    public CrawlResult<List<Novel>> crawlNovelList(List<String> tags) {
        return crawlNovelList(tags, null);
    }

    @Override
    public CrawlResult<List<Novel>> crawlNovelList(List<String> tags, LocalDateTime sinceTime) {
        log.info("开始抓取次元姬小说列表，标签: {}, 增量时间: {}", tags, sinceTime);

        List<Novel> novels = new ArrayList<>();
        AtomicBoolean shouldStop = new AtomicBoolean(false);

        try {
            for (int page = 1; page <= MAX_PAGES && !shouldStop.get(); page++) {
                String pageUrl = buildListUrl(page);
                log.info("抓取第 {} 页: {}", page, pageUrl);

                List<Novel> pageNovels = crawlListPage(pageUrl, tags, sinceTime, shouldStop);
                
                for (Novel novel : pageNovels) {
                    boolean exists = novels.stream()
                        .anyMatch(n -> n.getNovelId() != null && n.getNovelId().equals(novel.getNovelId()));
                    if (!exists) {
                        novels.add(novel);
                    }
                }

                if (shouldStop.get()) {
                    log.info("检测到小说更新时间早于增量时间，停止继续爬取");
                    break;
                }

                if (page < MAX_PAGES) {
                    randomDelay();
                }
            }
        } catch (Exception e) {
            log.error("抓取列表页失败: {}", e.getMessage(), e);
        }

        log.info("次元姬爬虫完成，共抓取 {} 本小说", novels.size());
        return CrawlResult.success(novels);
    }

    private String buildListUrl(int page) {
        return BASE_URL + "/l_c_0_0_0_0_3_" + page + "_10.html";
    }

    private List<Novel> crawlListPage(String url, List<String> filterTags, LocalDateTime sinceTime, AtomicBoolean shouldStop) {
        List<Novel> novels = new ArrayList<>();
        int tryCrawlCount = 0;
        try {
            Request request = buildRequest(url);
            try (Response response = executeWithRetry(request)) {
                if (response == null || !response.isSuccessful()) {
                    log.error("请求失败");
                    return novels;
                }

                String html = response.body().string();
                Document doc = Jsoup.parse(html);

                Elements novelElements = doc.select(".card_item__BZXh0");
                log.info("找到 {} 个小说元素", novelElements.size());

                for (Element element : novelElements) {
                    Novel basicNovel = parseListNovelElement(element);
                    if (basicNovel == null || basicNovel.getTitle() == null || basicNovel.getTitle().isEmpty()) {
                        continue;
                    }

                    randomDelay();
                    
                    Novel fullNovel = crawlNovelDetail(basicNovel.getNovelId());
                    if (fullNovel == null) {
                        fullNovel = basicNovel;
                    } else {
                        if (basicNovel.getCoverUrl() != null && !basicNovel.getCoverUrl().isEmpty()) {
                            fullNovel.setCoverUrl(basicNovel.getCoverUrl());
                        }
                    }

                    if (sinceTime != null && fullNovel.getLatestUpdateTime() != null) {
                        // 至少存在三本书时间都早于爬取时间再停止爬取，防止网站出现错误数据
                        tryCrawlCount++;
                        if (!fullNovel.getLatestUpdateTime().isAfter(sinceTime) && tryCrawlCount >= 3) {
                            log.info("小说 {} 更新时间 {} 早于或等于增量时间 {}，停止继续爬取", 
                                fullNovel.getTitle(), fullNovel.getLatestUpdateTime(), sinceTime);
                            shouldStop.set(true);
                            break;
                        }
                    }

                    if (filterTags != null && !filterTags.isEmpty()) {
                        if (!matchTags(fullNovel.getTags(), filterTags)) {
                            log.debug("小说 {} 标签不匹配，跳过", fullNovel.getTitle());
                            continue;
                        }
                    }

                    novels.add(fullNovel);
                }
            }
        } catch (Exception e) {
            log.error("抓取列表页失败: {}", e.getMessage());
        }

        return novels;
    }

    private boolean matchTags(String novelTags, List<String> filterTags) {
        if (novelTags == null || novelTags.isEmpty()) {
            return false;
        }
        
        String[] tagArray = novelTags.split(",");
        for (String tag : tagArray) {
            String trimmedTag = tag.trim();
            for (String filterTag : filterTags) {
                if (trimmedTag.equals(filterTag.trim())) {
                    return true;
                }
            }
        }
        return false;
    }

    private Novel parseListNovelElement(Element element) {
        try {
            Novel novel = new Novel();
            novel.setPlatform(PLATFORM);

            Element linkElement = element.select("a[href*=/b_d_]").first();
            if (linkElement != null) {
                String href = linkElement.attr("href");
                Matcher matcher = BOOK_ID_PATTERN.matcher(href);
                if (matcher.find()) {
                    novel.setNovelId(matcher.group(1));
                }
            }

            Element titleElement = element.select(".BookCard_title__nQGag").first();
            if (titleElement != null) {
                novel.setTitle(titleElement.text().trim());
            }

            Element authorElement = element.select(".BookCard_name1__Po00_").first();
            if (authorElement != null) {
                novel.setAuthor(authorElement.text().trim());
            }

            Element coverElement = element.select("img[data-src], img[src]").first();
            if (coverElement != null) {
                String coverUrl = coverElement.attr("data-src");
                if (coverUrl == null || coverUrl.isEmpty()) {
                    coverUrl = coverElement.attr("src");
                }
                if (coverUrl != null && !coverUrl.isEmpty() && !coverUrl.contains("data:image")) {
                    if (coverUrl.startsWith("//")) {
                        coverUrl = "https:" + coverUrl;
                    }
                    novel.setCoverUrl(coverUrl);
                }
            }

            if (novel.getTitle() == null || novel.getTitle().isEmpty()) {
                return null;
            }

            return novel;
        } catch (Exception e) {
            log.error("解析小说元素失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public Novel crawlNovelDetail(String novelId) {
        log.info("开始抓取次元姬小说详情，novelId: {}", novelId);

        try {
            String url = BASE_URL + "/b_d_" + novelId + ".html";
            Request request = buildRequest(url);

            try (Response response = executeWithRetry(request)) {
                if (response == null || !response.isSuccessful()) {
                    log.error("请求失败");
                    return null;
                }

                String html = response.body().string();
                Document doc = Jsoup.parse(html);

                return parseNovelDetail(doc, novelId);
            }
        } catch (Exception e) {
            log.error("抓取小说详情失败: {}", e.getMessage());
            return null;
        }
    }

    private Novel parseNovelDetail(Document doc, String novelId) {
        Novel novel = new Novel();
        novel.setPlatform(PLATFORM);
        novel.setNovelId(novelId);
        novel.setSourceUrl(BASE_URL + "/b_d_" + novelId + ".html");

        Element titleElement = doc.select(".book_detail_title__d9MAd span").first();
        if (titleElement != null) {
            novel.setTitle(titleElement.text().trim());
        }

        if (novel.getTitle() == null || novel.getTitle().isEmpty()) {
            String title = doc.title();
            if (title != null && !title.isEmpty()) {
                int bracketIndex = title.indexOf("(");
                if (bracketIndex > 0) {
                    novel.setTitle(title.substring(0, bracketIndex).trim());
                } else {
                    int endIndex = title.indexOf("-次元姬");
                    if (endIndex > 0) {
                        novel.setTitle(title.substring(0, endIndex).trim());
                    }
                }
            }
        }

        Elements textElements = doc.select(".book_detail_tags__pkrm2 .book_detail_text__HlCNP");
        if (textElements.size() >= 2) {
            novel.setAuthor(textElements.get(0).text().trim());
        }

        // 获取连载状态 - 在标签列表中查找"连载"或"完结"
        for (Element textElement : textElements) {
            String text = textElement.text().trim();
            if ("完结".equals(text)) {
                novel.setStatus(2);  // 2-完结
                break;
            } else if ("连载".equals(text)) {
                novel.setStatus(1);  // 1-连载
                break;
            }
        }

        Element descMeta = doc.select("meta[name=description]").first();
        if (descMeta != null) {
            novel.setDescription(descMeta.attr("content"));
        }

        Element articleElement = doc.select(".book_detail_article__tNriO").first();
        if (articleElement != null && (novel.getDescription() == null || novel.getDescription().isEmpty())) {
            novel.setDescription(articleElement.text().trim());
        }

        Element timeElement = doc.select(".book_detail_time__E3K_Y").first();
        if (timeElement != null) {
            String timeText = timeElement.text().trim();
            novel.setLatestUpdateTime(parseDateTime(timeText));
        }

        Elements tagElements = doc.select(".book_detail_tag__dIVn_");
        if (!tagElements.isEmpty()) {
            List<String> tags = new ArrayList<>();
            for (Element tagElement : tagElements) {
                String tag = tagElement.text().trim();
                if (!tag.isEmpty()) {
                    tags.add(tag);
                }
            }
            if (!tags.isEmpty()) {
                novel.setTags(String.join(",", tags));
            }
        }

        Element wordCountElement = doc.select(".book_detail_statistical__8_BoB .book_detail_num__6z_p_").first();
        if (wordCountElement != null) {
            String wordCountText = wordCountElement.text().trim();
            try {
                novel.setWordCount(parseWordCount(wordCountText));
            } catch (Exception e) {
                log.debug("解析字数失败: {}", wordCountText);
            }
        }

        return novel;
    }

    private Long parseWordCount(String wordCountText) {
        if (wordCountText == null || wordCountText.isEmpty()) {
            return null;
        }
        wordCountText = wordCountText.trim().toLowerCase();
        double value;
        if (wordCountText.endsWith("w")) {
            String numStr = wordCountText.substring(0, wordCountText.length() - 1);
            value = Double.parseDouble(numStr) * 10000;
        } else if (wordCountText.endsWith("万")) {
            String numStr = wordCountText.substring(0, wordCountText.length() - 1);
            value = Double.parseDouble(numStr) * 10000;
        } else {
            value = Double.parseDouble(wordCountText);
        }
        return (long) value;
    }

    @Override
    public List<Chapter> fetchChapters(String novelId, int count) {
        log.info("开始抓取次元姬小说前 {} 章，novelId: {}", count, novelId);

        List<Chapter> chapters = new ArrayList<>();

        try {
            randomDelay();
            
            String url = BASE_URL + "/b_d_" + novelId + ".html";
            Request request = buildRequest(url);

            try (Response response = executeWithRetry(request)) {
                if (response == null || !response.isSuccessful()) {
                    log.error("请求失败");
                    return chapters;
                }

                String html = response.body().string();
                Document doc = Jsoup.parse(html);

                Elements chapterElements = doc.select(".book_detail_item__EMrK7");

                int fetchCount = Math.min(count, chapterElements.size());
                for (int i = 0; i < fetchCount; i++) {
                    Element chapterElement = chapterElements.get(i);
                    Chapter chapter = parseChapterElement(chapterElement, i + 1);
                    if (chapter != null) {
                        chapters.add(chapter);
                    }
                }

                log.info("成功抓取 {} 章", chapters.size());
            }
        } catch (Exception e) {
            log.error("抓取章节失败: {}", e.getMessage());
        }

        return chapters;
    }

    private Chapter parseChapterElement(Element element, int chapterNum) {
        try {
            Chapter chapter = new Chapter();
            chapter.setChapter(chapterNum);
            
            Element titleElement = element.select(".book_detail_text__HlCNP").first();
            if (titleElement != null) {
                chapter.setTitle(titleElement.text().trim());
            } else {
                chapter.setTitle(element.text().trim());
            }

            String href = element.attr("href");
            if (href == null || href.isEmpty()) {
                Element linkElement = element.select("a[href*=/chapter/]").first();
                if (linkElement != null) {
                    href = linkElement.attr("href");
                }
            }
            
            if (href != null) {
                Matcher matcher = CHAPTER_ID_PATTERN.matcher(href);
                if (matcher.find()) {
                    chapter.setChapterId(matcher.group(2));
                }
            }

            return chapter;
        } catch (Exception e) {
            log.error("解析章节元素失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean needsUpdate(String novelId, String updateTime) {
        return false;
    }

    @Override
    public LocalDateTime parseUpdateTime(String updateTimeStr) {
        return parseDateTime(updateTimeStr);
    }

    private Request buildRequest(String url) {
        return new Request.Builder()
                .url(url)
                .header("User-Agent", userAgent)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .header("Connection", "keep-alive")
                .header("Referer", BASE_URL)
                .header("Cache-Control", "max-age=0")
                .build();
    }

    private Response executeWithRetry(Request request) throws Exception {
        Exception lastException = null;
        
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                Response response = httpClient.newCall(request).execute();
                
                if (response.code() == 429) {
                    log.warn("请求过于频繁，等待后重试...");
                    response.close();
                    Thread.sleep(10000 + random.nextInt(5000));
                    continue;
                }
                
                if (response.code() == 403) {
                    log.warn("访问被拒绝，可能被屏蔽，等待后重试...");
                    response.close();
                    Thread.sleep(30000 + random.nextInt(10000));
                    continue;
                }
                
                return response;
            } catch (Exception e) {
                lastException = e;
                log.warn("请求失败，第 {} 次重试: {}", i + 1, e.getMessage());
                if (i < MAX_RETRIES - 1) {
                    Thread.sleep(5000 + random.nextInt(3000));
                }
            }
        }
        
        throw lastException != null ? lastException : new Exception("请求失败");
    }

    private void randomDelay() {
        try {
            int delay = MIN_DELAY_MS + random.nextInt(MAX_DELAY_MS - MIN_DELAY_MS);
            log.debug("等待 {} 毫秒", delay);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private LocalDateTime parseDateTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return null;
        }

        timeStr = timeStr.trim();

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(timeStr, formatter);
        } catch (Exception ignored) {
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            return LocalDateTime.parse(timeStr, formatter);
        } catch (Exception ignored) {
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/M/d H:mm:ss");
            return LocalDateTime.parse(timeStr, formatter);
        } catch (Exception ignored) {
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/M/d");
            return LocalDateTime.parse(timeStr + " 00:00:00", 
                DateTimeFormatter.ofPattern("yyyy/M/d HH:mm:ss"));
        } catch (Exception ignored) {
        }

        if (timeStr.contains("分钟前")) {
            int minutes = extractNumber(timeStr);
            return LocalDateTime.now().minusMinutes(minutes);
        }
        if (timeStr.contains("小时前")) {
            int hours = extractNumber(timeStr);
            return LocalDateTime.now().minusHours(hours);
        }
        if (timeStr.contains("天前")) {
            int days = extractNumber(timeStr);
            return LocalDateTime.now().minusDays(days);
        }

        return null;
    }

    private int extractNumber(String str) {
        try {
            return Integer.parseInt(str.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }
}
