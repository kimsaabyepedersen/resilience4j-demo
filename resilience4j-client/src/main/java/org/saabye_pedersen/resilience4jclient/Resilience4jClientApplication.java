package org.saabye_pedersen.resilience4jclient;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.event.RetryEvent;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;


public class Resilience4jClientApplication {

    public static void main(String[] args) {

        // 0. Introduce client and makeHttpGetRequest method, specifically the timeouts
        CloseableHttpClient httpClient = HttpClients.createDefault();

        // Concept comment out/in the lines:
        // Response time = makeHttpGet....

        // 1. Make request while server is not running. This has no retry and will fail straight away.
        Response time = makeHttpGetRequest(httpClient, "http://localhost:9991/time");

        Retry retry = getRetry(getRetryConfig());

        // 2. Make request while server is not running. This will enter retry. Start server. Call will complete.
        // Note the console log. Each retry request is 1 second apart (wait) - server down fast to detect.
        //Response time = retry.executeSupplier(() ->makeHttpGetRequest(httpClient, "http://localhost:9991/time"));

        // 3. Make request while server is running. 9 of 10 calls take 30 seconds to complete.
        // Note the console log. Each retry request is 6 seconds apart, 5 seconds before client times out and then 1 second wait.
        //Response time = retry.executeSupplier(() ->makeHttpGetRequest(httpClient, "http://localhost:9991/slowTime"));

        // 4. Make request while server is running. 9 of 10 calls return status code.
        // Note the console log. Each retry request is 1 second apart (wait) - different status code fast to detect.
        //Response time = retry.executeSupplier(() -> makeHttpGetRequest(httpClient, "http://localhost:9991/slowFaultyTime"));

        System.out.printf("Time is %s\n", time.getPayload());


    }

    private static Response makeHttpGetRequest(CloseableHttpClient httpClient, String url) {
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(5_000).setConnectTimeout(5_000).build();

        HttpGet get = new HttpGet(url);
        get.setConfig(requestConfig);

        try (CloseableHttpResponse response = httpClient.execute(get)) {

            try {
                HttpEntity entity1 = response.getEntity();
                return new Response(IOUtils.toString(entity1.getContent(), StandardCharsets.UTF_8.name()), response.getStatusLine().getStatusCode());
            } finally {
                response.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static RetryConfig getRetryConfig() {
        return RetryConfig.custom()
                .maxAttempts(200) // very high for demo
                .retryOnResult(o -> {
                    Response response = (Response) o;
                    return response.getStatusCode() != 200;
                })
                .waitDuration(Duration.ofMillis(1000))
                .retryOnException(throwable -> Arrays.asList(SocketTimeoutException.class, ConnectException.class).contains(ExceptionUtils.getRootCause(throwable).getClass()))
                .build();
    }

    private static Retry getRetry(RetryConfig retryConfig) {
        RetryRegistry retryRegistry = RetryRegistry.of(retryConfig);

        Retry getTimeRetry = retryRegistry.retry("getTimeRetry");
        getTimeRetry.getEventPublisher().onRetry(event -> {
            RetryEvent.Type eventType = event.getEventType();
            System.out.printf("Event type %s\n", eventType.name());
            System.out.printf("Event name %s\n", event.getName());
            System.out.printf("Event name %s\n", event.getCreationTime().toString());
            System.out.printf("Event name %s\n", event.getNumberOfRetryAttempts());
            System.out.println();
        });
        return getTimeRetry;
    }
}


