package ch.aptkn.estimatekwd.service;

import ch.aptkn.estimatekwd.model.AmazonCompletionResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

@Service
public class AmazonAutocompleteService {
    /**
     * Make use of Amazon autocomplete API for a given keyword. Given a keyword "iphone",
     * several autocomplete results for each subkeyword will be obtained: for "i", "ip",
     * "iph", "ipho" etc.
     *
     * @param keyword            sanitized keyword
     * @param httpRequestTimeout max time spent on HTTP requests to Amazon APIs (milliseconds)
     * @return map of suggestions for each subkeyword
     */
    public Map<String, List<String>> getSuggestions(String keyword, int httpRequestTimeout) throws InterruptedException {
        ExecutorService threadPool = new ThreadPoolExecutor(
                keyword.length(),
                keyword.length(),
                100,
                TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(),
                x -> {
                    Thread t = Executors.defaultThreadFactory().newThread(x);
                    t.setName("http-request-" + t.getName());
                    return t;
                }
        );

        Map<String, List<String>> result = new ConcurrentHashMap<>();

        for (int i = 0; i < keyword.length(); i++) {
            final String subkeyword = keyword.substring(0, i + 1);
            threadPool.execute(new AmazonAutocompleteRequest(subkeyword, httpRequestTimeout, result::put));
        }
        threadPool.shutdown();
        while (!threadPool.awaitTermination(100, TimeUnit.MILLISECONDS)) ;
        return result;
    }

    private static class AmazonAutocompleteRequest implements Runnable {
        private final String subkeyword;
        private final int httpRequestTimeout;
        private final BiConsumer<String, List<String>> resultConsumer;

        AmazonAutocompleteRequest(String subkeyword, int httpRequestTimeout, BiConsumer<String, List<String>> resultConsumer) {
            this.subkeyword = subkeyword;
            this.httpRequestTimeout = httpRequestTimeout;
            this.resultConsumer = resultConsumer;
        }

        @Override
        public void run() {
            URIBuilder builder = new URIBuilder();
            builder.setScheme("https");
            builder.setHost("completion.amazon.com");
            builder.setPath("/api/2017/suggestions");
            builder.setParameter("mid", "ATVPDKIKX0DER");
            builder.setParameter("alias", "aps");
            builder.setParameter("prefix", subkeyword);
            try {
                builder.build();
            } catch (URISyntaxException e) {
                return;
            }
            HttpGet getMethod = new HttpGet(builder.toString());
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    getMethod.abort();
                }
            };
            new Timer(true).schedule(task, httpRequestTimeout);
            try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
                HttpResponse response = httpClient.execute(getMethod);

                if (response.getStatusLine().getStatusCode() == 200) {
                    ObjectMapper mapper = new ObjectMapper();
                    AmazonCompletionResult completion = mapper.readValue(EntityUtils.toString(response.getEntity()), AmazonCompletionResult.class);
                    resultConsumer.accept(subkeyword, completion.getSuggestions());
                }
            } catch (IOException ignored) {
            }
        }
    }
}
