package perfectstrong.sonako.sonakoreader.helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Simple client to retrieve information from wiki
 */
public class WikiClient {

    private final String USER_AGENT;
    private OkHttpClient okHttpClient;
    private HttpUrl apiEndpoint;
    private static final int MAX_TITLES_PER_CALL = 50;
    private static final int CATEGORIES_LIST_MAX = 500;

    public WikiClient(String apiEndpoint, String userAgent) {
        okHttpClient = new OkHttpClient();
        this.apiEndpoint = HttpUrl.parse(apiEndpoint);
        this.USER_AGENT = userAgent;
    }

    private HttpUrl buildURLFromQueryParams(List<String> params) {
        if (params.size() % 2 != 0) return apiEndpoint;
        HttpUrl.Builder builder = apiEndpoint.newBuilder();
        for (int i = 0; i < params.size(); i += 2) {
            builder.addQueryParameter(params.get(i), params.get(i + 1));
        }
        return builder.build();
    }

    private String concatNonEncodedParams(String... params) {
        StringBuilder stringBuilder;
        stringBuilder = new StringBuilder(params[0]);
        for (int i = 1; i < params.length; i++) {
            stringBuilder.append("|").append(params[i]);
        }
        return stringBuilder.toString();
    }

    /**
     * @param title non URL-encoded
     * @return media wiki code text
     */
    public String getPageText(String title) {
        try (Response response = GET(
                "query",
                "titles", title,
                "prop", "revisions",
                "rvprop", "content",
                "indexpageids", "true"
        )) {
            assert response.body() != null;
            JSONObject query = new JSONObject(response.body().string()).getJSONObject("query");
            String pageId = query.getJSONArray("pageids").getString(0);
            if ("-1".equals(pageId))
                return null;
            return query.getJSONObject("pages")
                    .getJSONObject(pageId)
                    .getJSONArray("revisions")
                    .getJSONObject(0)
                    .getString("*");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * @param category  no "Category:" prefix
     * @param namespace 0 for main page
     * @param limit     "max" to get maximum, or a number
     * @return list of titles. Empty if found nothing
     */
    public List<String> getCategoryMembers(String category,
                                           String namespace,
                                           String limit) {
        List<String> titles = new ArrayList<>();
        try (Response response = GET(
                "query",
                "list", "categorymembers",
                "cmtitle", "Category:" + category,
                "cmnamespace", namespace,
                "cmprop", "title",
                "cmlimit", limit
        )) {
            assert response.body() != null;
            JSONObject query = new JSONObject(response.body().string()).getJSONObject("query");
            JSONArray arrays = query.getJSONArray("categorymembers");
            for (int i = 0; i < arrays.length(); i++) {
                titles.add(arrays.getJSONObject(i).getString("title"));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return titles;
    }


    /**
     * @param titles non URL-encoded
     * @return map of lists of categories for each title. Empty if no category found
     */
    public Map<String, Set<String>> getCategoriesOnPages(List<String> titles) {
        Map<String, Set<String>> map = new HashMap<>();
        // Divide titles into portion
        int currentIndex = 0;
        List<String> portion = new ArrayList<>();
        while (currentIndex < titles.size()) {
            portion.addAll(titles.subList(currentIndex,
                    Math.min(currentIndex + MAX_TITLES_PER_CALL, titles.size())));
            String clcontinue = null;
            String titlesConcat = concatNonEncodedParams(portion.toArray(new String[0]));
            do {
                Response response = null;
                try {
                    if (clcontinue == null)
                        response = GET(
                                "query",
                                "titles", titlesConcat,
                                "prop", "categories",
                                "indexpageids", "true",
                                "cllimit", String.valueOf(CATEGORIES_LIST_MAX)
                        );
                    else
                        response = GET(
                                "query",
                                "titles", titlesConcat,
                                "prop", "categories",
                                "indexpageids", "true",
                                "cllimit", String.valueOf(CATEGORIES_LIST_MAX),
                                "clcontinue", clcontinue
                        );
                    assert response.body() != null;
                    String str = response.body().string();
                    JSONObject res = new JSONObject(str);
                    JSONObject query = res.getJSONObject("query");
                    JSONArray pageids = query.getJSONArray("pageids");
                    JSONObject pages = query.getJSONObject("pages");
                    for (int i = 0; i < pageids.length(); i++) {
                        String id = pageids.getString(i);
                        if (Integer.valueOf(id) < 0) continue;
                        JSONObject page = pages.getJSONObject(id);

                        // Gather
                        Set<String> categories = new HashSet<>();
                        JSONArray cat = page.optJSONArray("categories");
                        if (cat == null) continue;
                        for (int i1 = 0; i1 < cat.length(); i1++) {
                            categories.add(cat.getJSONObject(i1).getString("title"));
                        }

                        // Concat
                        String title = page.getString("title");
                        if (map.containsKey(title))
                            Objects.requireNonNull(map.get(title)).addAll(categories);
                        else
                            map.put(title, categories);
                    }

                    // Check continuation
                    JSONObject queryContinue = res.optJSONObject("query-continue");
                    if (queryContinue != null)
                        clcontinue = queryContinue.getJSONObject("categories")
                                .getString("clcontinue");
                    else
                        clcontinue = null;
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    clcontinue = null; // Stop
                } finally {
                    if (response != null)
                        response.close();
                }
            } while (clcontinue != null);

            currentIndex += portion.size();
            portion.clear();
        }
        return map;
    }

    /**
     * @param title non URL-encoded
     * @return <tt>false</tt> if server returns missing or failed request
     */
    public boolean exists(String title) {
        try (Response response = GET(
                "query",
                "format", "json",
                "titles", title,
                "indexpageids", "true")
        ) {
            assert response.body() != null;
            JSONObject query = new JSONObject(response.body().string()).getJSONObject("query");
            return !"-1".equals(query.getJSONArray("pageids").getString(0));
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Response GET(String action,
                        String... params) throws IOException {
        if (params.length % 2 != 0)
            throw new IllegalArgumentException("Odd number of params");
        List<String> p = new ArrayList<>();
        p.addAll(Arrays.asList(
                "action", action,
                "format", "json"
        ));
        p.addAll(Arrays.asList(params));
        return okHttpClient.newCall(
                new Request.Builder()
                        .url(buildURLFromQueryParams(p))
                        .header("User-Agent", USER_AGENT)
                        .build()
        ).execute();
    }
}
