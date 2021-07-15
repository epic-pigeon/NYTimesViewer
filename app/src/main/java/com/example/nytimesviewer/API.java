package com.example.nytimesviewer;

import android.app.admin.DeviceAdminService;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class API {
    public static final List<String> CATEGORIES = Collections.unmodifiableList(
            Arrays.asList(
                    "Adventure Sports", "Arts & Leisure", "Arts", "Automobiles", "Blogs", "Books", "Booming", "Business Day", "Business", "Cars", "Circuits", "Classifieds", "Connecticut", "Crosswords & Games", "Culture", "DealBook", "Dining", "Editorial", "Education", "Energy", "Entrepreneurs", "Environment", "Escapes", "Fashion & Style", "Fashion", "Favorites", "Financial", "Flight", "Food", "Foreign", "Generations", "Giving", "Global Home", "Health & Fitness", "Health", "Home & Garden", "Home", "Jobs", "Key", "Letters", "Long Island", "Magazine", "Market Place", "Media", "Men's Health", "Metro", "Metropolitan", "Movies", "Museums", "National", "Nesting", "Obits", "Obituaries", "Obituary", "OpEd", "Opinion", "Outlook", "Personal Investing", "Personal Tech", "Play", "Politics", "Regionals", "Retail", "Retirement", "Science", "Small Business", "Society", "Sports", "Style", "Sunday Business", "Sunday Review", "Sunday Styles", "T Magazine", "T Style", "Technology", "Teens", "Television", "The Arts", "The Business of Green", "The City Desk", "The City", "The Marathon", "The Millennium", "The Natural World", "The Upshot", "The Weekend", "The Year in Pictures", "Theater", "Then & Now", "Thursday Styles", "Times Topics", "Travel", "U.S.", "Universal", "Upshot", "UrbanEye", "Vacation", "Washington", "Wealth", "Weather", "Week in Review", "Week", "Weekend", "Westchester", "Wireless Living", "Women's Health", "Working", "Workplace", "World", "Your Money"
            )
    );
    private static final String API_KEY = "PT0FJpFGUyBAzfZQB1Ew9odlrmw6488W";


    public static class Article implements Serializable {
        private final String headline, snippet, leadParagraph, documentType, newsDesk, keyWords[];
        private final Date date;

        private Article(String headline, String snippet, String leadParagraph, String documentType, String newsDesk, String[] keyWords, Date date) {
            this.headline = headline;
            this.snippet = snippet;
            this.leadParagraph = leadParagraph;
            this.documentType = documentType;
            this.newsDesk = newsDesk;
            this.keyWords = keyWords;
            this.date = date;
        }
        private static Article of(JSONObject obj) {
            try {
                String headline = obj.getJSONObject("headline").getString("main");
                String snippet = obj.getString("snippet");
                String leadParagraph = obj.optString("lead_paragraph");
                String documentType = obj.optString("document_type");
                String newsDesk = obj.getString("news_desk");
                JSONArray arr = obj.getJSONArray("keywords");
                String[] keyWords = new String[arr.length()];
                for (int i = 0; i < arr.length(); i++) {
                    keyWords[i] = arr.getJSONObject(i).getString("value");
                } //                                    "1998-03-11 T 05:00:00+0000"
                Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(obj.getString("pub_date"));
                return new Article(headline, snippet, leadParagraph, documentType, newsDesk, keyWords, date);
            } catch (JSONException | ParseException e) {
                throw new RuntimeException("Failed to parse an article", e);
            }
        }

        public String getHeadline() {
            return headline;
        }

        public String getSnippet() {
            return snippet;
        }

        public String getLeadParagraph() {
            return leadParagraph;
        }

        public String getDocumentType() {
            return documentType;
        }

        public String getNewsDesk() {
            return newsDesk;
        }

        public String[] getKeyWords() {
            return keyWords;
        }

        public Date getDate() {
            return date;
        }

        @Override
        public String toString() {
            return headline;
        }
    }
    public static CompletableFuture<List<Article>> findArticles(List<String> categories, int page) {
        return CompletableFuture.supplyAsync(() -> {
            String urlString = "https://api.nytimes.com/svc/search/v2/articlesearch.json?api-key=" + API_KEY + "&page=" + page;

            if (categories.size() > 0) {
                StringBuilder categoriesQuery = new StringBuilder("news_desk:(");
                for (int i = 0; i < categories.size(); i++) {
                    if (i != 0) categoriesQuery.append(" ");
                    categoriesQuery.append('"').append(categories.get(i)).append('"');
                }
                categoriesQuery.append(")");
                urlString += "&fq=" + URLEncoder.encode(categoriesQuery.toString());
            }
            URL url;
            try {
                url = new URL(urlString);
            } catch (Exception e) {
                throw new Error(e);
            }
            HttpURLConnection connection;
            InputStream inputStream;
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            try {
                connection = (HttpURLConnection) url.openConnection();
                inputStream = connection.getInputStream();

                byte[] buffer = new byte[1024];
                for (int length; (length = inputStream.read(buffer)) != -1; ) {
                    result.write(buffer, 0, length);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to connect to server, please check your internet connection", e);
            }

            String json = result.toString();
            List<Article> articles = new ArrayList<>();

            try {
                JSONObject root = new JSONObject(json);
                if (!root.getString("status").equals("OK")) {
                    throw new RuntimeException("API error");
                }
                JSONArray elements = root.getJSONObject("response").getJSONArray("docs");
                for (int i = 0; i < elements.length(); i++) {
                    articles.add(Article.of(elements.getJSONObject(i)));
                }
            } catch (JSONException e) {
                throw new RuntimeException("Failed to parse the response", e);
            }

            return Collections.unmodifiableList(articles);
        });
    }
}
