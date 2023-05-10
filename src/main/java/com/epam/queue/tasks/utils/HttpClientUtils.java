package com.epam.queue.tasks.utils;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

public class HttpClientUtils {

    static {
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials
                = new UsernamePasswordCredentials("guest", "guest");
        provider.setCredentials(AuthScope.ANY, credentials);

        client = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .build();
    }

    private static final HttpClient client;

    public static int getMessageReady(String queueName) {
        HttpResponse response;
        try {
            response = client.execute(
                    new HttpGet("http://localhost:15672/api/queues"));
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != 200) {
                throw new IllegalStateException("Unexpected response.");
            }

            String queues = EntityUtils.toString(response.getEntity());
            JSONArray array = new JSONArray(queues);

            JSONObject json = null;
            for (Object o : array) {
                json = (JSONObject) o;
                String name = json.getString("name");
                if (queueName.equals(name)) {
                    break;
                }
            }

            if (Objects.isNull(json)) {
                throw new NullPointerException("Invalid queue name.");
            }

            return json.getInt("messages_ready");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
