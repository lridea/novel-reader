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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
public class CiweimaoCrawler implements BaseCrawler {

    private static final String PLATFORM = "ciweimao";
    private static final String BASE_URL = "https://www.ciweimao.com";

    private static final Pattern BOOK_ID_PATTERN = Pattern.compile("/book/(\\d+)");
    private static final Pattern CHAPTER_ID_PATTERN = Pattern.compile("/chapter/(\\d+)");
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
        log.info("开始抓取刺猬猫小说列表，标签: {}, 增量时间: {}", tags, sinceTime);

        List<Novel> novels = new ArrayList<>();
        AtomicBoolean shouldStop = new AtomicBoolean(false);

        try {
            for (int page = 1; page <= MAX_PAGES && !shouldStop.get(); page++) {
                String pageUrl = buildListUrl(null, page);
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

        log.info("刺猬猫爬虫完成，共抓取 {} 本小说", novels.size());
        return CrawlResult.success(novels);
    }

    private String buildListUrl(String tag, int page) {
        StringBuilder url = new StringBuilder(BASE_URL);
        url.append("/get-search-book-list/0-0-uptime-3-0-0/");
        
        if (tag != null && !tag.isEmpty()) {
            url.append(URLEncoder.encode(tag, StandardCharsets.UTF_8));
        } else {
            url.append(URLEncoder.encode("全部", StandardCharsets.UTF_8));
        }
        
        url.append("//").append(page);
        
        return url.toString();
    }

    private List<Novel> crawlListPage(String url, List<String> filterTags, LocalDateTime sinceTime, AtomicBoolean shouldStop) {
        List<Novel> novels = new ArrayList<>();

        try {
            Request request = buildRequest(url);
            try (Response response = executeWithRetry(request)) {
                if (response == null || !response.isSuccessful()) {
                    log.error("请求失败");
                    return novels;
                }

                String html = response.body().string();
                Document doc = Jsoup.parse(html);

                Elements novelElements = doc.select(".rank-book-list li[data-book-id]");
                log.info("找到 {} 个小说元素", novelElements.size());

                if (novelElements.isEmpty()) {
                    novelElements = doc.select(".rank-book-list li");
                    log.info("尝试其他选择器，找到 {} 个元素", novelElements.size());
                }

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
                        if (!fullNovel.getLatestUpdateTime().isAfter(sinceTime)) {
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

            String bookId = element.attr("data-book-id");
            if (bookId == null || bookId.isEmpty()) {
                Element linkElement = element.select("a[href*=/book/]").first();
                if (linkElement != null) {
                    bookId = extractBookId(linkElement.attr("href"));
                }
            }
            novel.setNovelId(bookId);

            Element titleElement = element.select(".tit a").first();
            if (titleElement != null) {
                novel.setTitle(titleElement.text().trim());
            }

            Element authorElement = element.select(".cnt p:contains(作者) a, .cnt p:eq(1) a").first();
            if (authorElement != null) {
                novel.setAuthor(authorElement.text().trim());
            }

            Element coverElement = element.select(".cover img").first();
            if (coverElement != null) {
                String coverUrl = coverElement.attr("src");
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
        log.info("开始抓取刺猬猫小说详情，novelId: {}", novelId);

        try {
            String url = BASE_URL + "/book/" + novelId;
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
        novel.setSourceUrl("https://www.ciweimao.com/book/" + novelId);

        Element bookNameMeta = doc.select("meta[property=og:novel:book_name]").first();
        if (bookNameMeta != null) {
            novel.setTitle(bookNameMeta.attr("content"));
        }

        Element authorMeta = doc.select("meta[property=og:novel:author]").first();
        if (authorMeta != null) {
            novel.setAuthor(authorMeta.attr("content"));
        }

        Elements labelElements = doc.select(".label-box .label");
        if (!labelElements.isEmpty()) {
            List<String> tags = new ArrayList<>();
            for (Element labelElement : labelElements) {
                String tag;
                Element aElement = labelElement.select("a").first();
                if (aElement != null) {
                    tag = aElement.text().trim();
                } else {
                    tag = labelElement.ownText().trim();
                }
                if (!tag.isEmpty()) {
                    tags.add(tag);
                }
            }
            if (!tags.isEmpty()) {
                novel.setTags(String.join(",", tags));
            }
        }

        Element imageMeta = doc.select("meta[property=og:image]").first();
        if (imageMeta != null) {
            String coverUrl = imageMeta.attr("content");
            if (coverUrl != null && !coverUrl.isEmpty()) {
                if (coverUrl.startsWith("//")) {
                    coverUrl = "https:" + coverUrl;
                }
                novel.setCoverUrl(coverUrl);
            }
        }

        Element descMeta = doc.select("meta[property=og:description]").first();
        if (descMeta != null) {
            novel.setDescription(descMeta.attr("content"));
        }

        Element updateTimeElement = doc.select(".update-time").first();
        if (updateTimeElement != null) {
            String updateText = updateTimeElement.text().trim();
            Matcher matcher = TIME_PATTERN.matcher(updateText);
            if (matcher.find()) {
                String timeStr = matcher.group(1);
                novel.setLatestUpdateTime(parseDateTime(timeStr));
            }
        }

        Element statusElement = doc.select(".update-state").first();
        if (statusElement != null) {
            String status = statusElement.text().trim();
            if (status.contains("完结")) {
                novel.setStatus(1);
            } else {
                novel.setStatus(0);
            }
        }

        Element wordCountElement = doc.select(".book-grade b:eq(2)").first();
        if (wordCountElement != null) {
            String wordCount = wordCountElement.text().trim();
            novel.setWordCount(parseWordCount(wordCount));
        }

        return novel;
    }

    @Override
    public List<Chapter> fetchChapters(String novelId, int count) {
        log.info("开始抓取刺猬猫小说前 {} 章，novelId: {}", count, novelId);

        List<Chapter> chapters = new ArrayList<>();

        try {
            randomDelay();
            
            String url = BASE_URL + "/book/" + novelId;
            Request request = buildRequest(url);

            try (Response response = executeWithRetry(request)) {
                if (response == null || !response.isSuccessful()) {
                    log.error("请求失败");
                    return chapters;
                }

                String html = response.body().string();
                Document doc = Jsoup.parse(html);

                Elements chapterElements = doc.select(".book-chapter-list li a");

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
            chapter.setTitle(element.text().trim());

            String chapterHref = element.attr("href");
            String chapterId = extractChapterId(chapterHref);
            chapter.setChapterId(chapterId);

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

    private String extractBookId(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        Matcher matcher = BOOK_ID_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private String extractChapterId(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        Matcher matcher = CHAPTER_ID_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        String cleanUrl = url.replaceAll("^/+", "").replaceAll("/+$", "");
        String[] parts = cleanUrl.split("/");
        return parts[parts.length - 1].replace(".html", "");
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

    private Long parseWordCount(String wordCount) {
        if (wordCount == null || wordCount.isEmpty()) {
            return null;
        }
        
        wordCount = wordCount.trim();
        
        if (wordCount.contains("万")) {
            String numStr = wordCount.replaceAll("[^0-9.]", "");
            try {
                return (long) (Double.parseDouble(numStr) * 10000);
            } catch (Exception e) {
                return null;
            }
        }
        
        try {
            return Long.parseLong(wordCount.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return null;
        }
    }
}
