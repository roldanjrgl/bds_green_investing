import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/*
 * Sample code to demonstrate the use of the Full archive search endpoint
 * */
public class FullArchiveSearchDemo {
    // To set your enviroment variables in your terminal run the following line:
    // export 'BEARER_TOKEN'='<your_bearer_token>'
    private static String next_token = null;
    private static String bearerToken = System.getenv("BEARER_TOKEN");

    private static String start_time = "2011-01-01T01:00:00.000Z";
    private static String end_time = "2015-03-01T01:00:00.000Z";
    private static String search_query = "($MEL OR $MEZ OR \"Meridian Energy\" OR \"Meridian Energy Limited\" OR \"Neal Barclay\"  \"CEO\" OR @MeridianEnergy OR from:MeridianEnergy)  lang:en -is:retweet";
    private static String file_name = "8_meridian_energy_tweets.csv";
    private static String max_results = "500";


    public static void main(String args[]) throws IOException, URISyntaxException, ParseException {
        if (null != bearerToken) {
            String response = search(search_query, bearerToken);
//            System.out.println(response);

            save_response_to_csv(response);

            // request more pages if next_token is available
            while (next_token != null) {
                System.out.println("next_token= " + next_token);
                response = search(search_query, bearerToken);
                save_response_to_csv(response);
            }
            next_token = null;
            System.out.println("Requests done!");
        } else {
            System.out.println("There was a problem getting your bearer token. Please make sure you set the BEARER_TOKEN environment variable");
        }
    }

    private static void save_response_to_csv(String response) throws ParseException, IOException, URISyntaxException {
        JSONParser parse = new JSONParser();
        JSONObject response_json = (JSONObject)parse.parse(response);

        if (response_json.containsKey("data")){
            JSONArray data = (JSONArray) response_json.get("data");
//            System.out.println(data);
            save_tweets_to_csv(data);
        }

        if (response_json.containsKey("meta")){
            JSONObject meta = (JSONObject) response_json.get("meta");
            System.out.println(meta);
            if (meta.containsKey("next_token")) {
                next_token = meta.get("next_token").toString();
            } else {
                next_token = null;
            }
        }

    }

    private static void save_tweets_to_csv(JSONArray data) throws IOException {
        FileWriter csvWriter = new FileWriter("/Users/Jroldan001/nyu/spring_2021/bds/bds_project_workspace/intellij_tests/collecting_tweets_v5/data_collected/" + file_name,true);
        csvWriter.append("CreatedAt");
        csvWriter.append(",");
        csvWriter.append("TweetId");
        csvWriter.append(",");
        csvWriter.append("AuthorId");
        csvWriter.append(",");
        csvWriter.append("TweetText");
        csvWriter.append("\n");
        for(int i=0;i<data.size();i++) {
            JSONObject tweet = (JSONObject)data.get(i);
            csvWriter.append(tweet.get("created_at").toString());
            csvWriter.append(",");
            csvWriter.append(tweet.get("id").toString());
            csvWriter.append(",");
            csvWriter.append(tweet.get("author_id").toString());
            csvWriter.append(",");
            csvWriter.append(tweet.get("text").toString().replace("\n", "").replace("\r", "").replace(",", " "));
            csvWriter.append("\n");
        }
        csvWriter.flush();
        csvWriter.close();
    }

    /*
     * This method calls the full-archive search endpoint with a the search term passed to it as a query parameter
     * */
    private static String search(String searchString, String bearerToken) throws IOException, URISyntaxException {
        String searchResponse = null;

        HttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();

        URIBuilder uriBuilder = new URIBuilder("https://api.twitter.com/2/tweets/search/all");
        ArrayList<NameValuePair> queryParameters;
        queryParameters = new ArrayList<>();
        queryParameters.add(new BasicNameValuePair("query", searchString));
        queryParameters.add(new BasicNameValuePair("max_results", max_results));
        queryParameters.add(new BasicNameValuePair("tweet.fields", "created_at,author_id"));
        queryParameters.add(new BasicNameValuePair("start_time", start_time));
        queryParameters.add(new BasicNameValuePair("end_time", end_time));
        if (next_token != null){
            queryParameters.add(new BasicNameValuePair("next_token", next_token));
        }

        uriBuilder.addParameters(queryParameters);

        HttpGet httpGet = new HttpGet(uriBuilder.build());
        httpGet.setHeader("Authorization", String.format("Bearer %s", bearerToken));
        httpGet.setHeader("Content-Type", "application/json");

        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        if (null != entity) {
            searchResponse = EntityUtils.toString(entity, "UTF-8");
        }
        return searchResponse;
    }

}