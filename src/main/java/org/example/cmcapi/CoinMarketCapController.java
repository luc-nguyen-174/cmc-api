package org.example.cmcapi;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CoinMarketCapController {

    @Value("${cmc.api.key}")
    private String apiKey;

    @Value("${cmc.api.url}")
    private String apiUrl;

    @GetMapping("/latest")
    public String getLatestListings(
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String slug,
            @RequestParam(required = false) String symbol) {

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("id", id));
        parameters.add(new BasicNameValuePair("slug", slug));
        parameters.add(new BasicNameValuePair("symbol", symbol));

        parameters.removeIf(parameter -> parameter.getValue() == null || parameter.getValue().isEmpty());

        try {
            String result = makeAPICall(apiUrl + "/v1/cryptocurrency/quotes/latest", parameters);
            System.out.println(result);
            return result;
        } catch (IOException | URISyntaxException e) {
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/info")
    public String getMetadata(
            @RequestParam Number id,
            @RequestParam String slug,
            @RequestParam String symbol) {

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("id", id.toString()));
        parameters.add(new BasicNameValuePair("slug", slug));
        parameters.add(new BasicNameValuePair("symbol", symbol));

        try {
            return makeAPICall(apiUrl + "/v2/cryptocurrency/quotes/historical", parameters);
        } catch (IOException | URISyntaxException e) {
            return "Error: " + e.getMessage();
        }
    }

    public String makeAPICall(String uri, List<NameValuePair> parameters)
            throws URISyntaxException, IOException {
        String response_content = "";

        URIBuilder query = new URIBuilder(uri);
        query.addParameters(parameters);

        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(query.build());

        request.setHeader(HttpHeaders.ACCEPT, "application/json");
        request.addHeader("X-CMC_PRO_API_KEY", apiKey);

        try (CloseableHttpResponse response = client.execute(request)) {
            System.out.println(response.getStatusLine());
            HttpEntity entity = response.getEntity();
            response_content = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
        }

        return response_content;
    }
}