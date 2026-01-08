package com.devansh.simulators;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadTester_Phase_2 {

    static AtomicInteger acceptedCount = new AtomicInteger();
    static AtomicInteger rejectedCount = new AtomicInteger(); // 429
    static AtomicInteger errorCount = new AtomicInteger();

    static final Semaphore inFlight = new Semaphore(50);

    public static void main(String[] args) {

        int totalRequests = 100_000;
        int concurrency = 50;

        ExecutorService executor = Executors.newFixedThreadPool(concurrency);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        long start = System.currentTimeMillis();

        for (int i = 0; i < totalRequests; i++) {
            int id = i;

            futures.add(sendRequest(client, id));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        executor.shutdown();

        long end = System.currentTimeMillis();

        System.out.println("========== LOAD TEST RESULT ==========");
        System.out.println("Total Requests: " + totalRequests);
        System.out.println("Accepted (2xx): " + acceptedCount.get());
        System.out.println("Rejected (429): " + rejectedCount.get());
        System.out.println("Errors: " + errorCount.get());
        System.out.println("Time Taken(ms): " + (end - start));
        System.out.println("Throughput (req/sec): " +
                (totalRequests * 1000L / (end - start)));
    }

    private static CompletableFuture<Void> sendRequest(HttpClient client, int id) {

        try {
            inFlight.acquire();
        } catch (InterruptedException e) {
            errorCount.incrementAndGet();
            return CompletableFuture.completedFuture(null);
        }

        String body = """
            {
              "type": "EMAIL",
              "recipient": "user%d@test.com",
              "message": "Hello"
            }
            """.formatted(id);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/notification"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    int status = response.statusCode();
                    if (status == 200 || status == 202) acceptedCount.incrementAndGet();
                    else if (status == 429) rejectedCount.incrementAndGet();
                    else errorCount.incrementAndGet();
                })
                .exceptionally(ex -> {
                    errorCount.incrementAndGet();
                    return null;
                })
                .whenComplete((r, t) -> inFlight.release());
    }
}
