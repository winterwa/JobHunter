package com.laioffer.job.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.job.entity.ExtractRequestBody;
import com.laioffer.job.entity.ExtractResponseItem;
import com.laioffer.job.entity.Extraction;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class MonkeyLearnClient {
    private static final String EXTRACT_URL = "https://api.monkeylearn.com/v3/extractors/ex_YCya9nrn/extract/";
    private static final String AUTH_TOKEN = "eb071411e272d3ae903f37250c5a3975c99af449";

    public List<Set<String>> extract(List<String> articles) {
        ObjectMapper mapper = new ObjectMapper();
        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpPost request = new HttpPost(EXTRACT_URL);
        request.setHeader("Content-type", "application/json");
        request.setHeader("Authorization", "Token " + AUTH_TOKEN);
        //Token must have space after it
        ExtractRequestBody body = new ExtractRequestBody(articles, 3);

        String jsonBody;
        try {
            jsonBody = mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }

        try {
            request.setEntity(new StringEntity(jsonBody));
        } catch (UnsupportedEncodingException e) {
            return Collections.emptyList();
        }

        //define how to convert response to needed answer
        ResponseHandler<List<Set<String>>> responseHandler = response -> {
            if (response.getStatusLine().getStatusCode() != 200) {
                return Collections.emptyList();
            }
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return Collections.emptyList();
            }
            ExtractResponseItem[] results = mapper.readValue(entity.getContent(), ExtractResponseItem[].class);
            List<Set<String>> keywordList = new ArrayList<>();
            for (ExtractResponseItem result : results) {
                Set<String> keywords = new HashSet<>();
                for (Extraction extraction : result.extractions) {
                    keywords.add(extraction.parsedValue);
                }
                keywordList.add(keywords);
            }
            return keywordList;
        };

        try {
            return httpClient.execute(request, responseHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

}
