package ch.aptkn.estimatekwd.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SearchVolumeEstimateService {
    /**
     * Estimate given keyword's search volume
     *
     * @param keyword     sanitized keyword
     * @param suggestions map of autocomplete suggestions for each subkeyword
     * @return estimated score in range 0-100
     */
    public int getScore(String keyword, Map<String, List<String>> suggestions) {
        // find the length of the shortest subkeyword, which suggestions contain the keyword
        AtomicInteger minSubkeywordMatchLen = new AtomicInteger(keyword.length() + 1);
        suggestions.forEach((key, value) -> {
            if (value.contains(keyword) && minSubkeywordMatchLen.get() > key.length()) {
                minSubkeywordMatchLen.set(key.length());
            }
        });

        int maxWeight = 0;
        int curWeight = 0;

        // an abstraction - each numeric level has its weight and represents the significance of the match:
        // 1 - the top. This keyword is treated as one of the most 10 searched ones by Amazon. Max weight.
        // 2 - less significant. Weight is less.
        // N - the last letters in a word give significantly less new information
        // compared to the first ones, since natural languages are superfluous.
        // This means each next level must have its weight drastically decreased.
        // For instance, given "s", one can think of "sketchers", "shoe rack", etc.
        // Given "sa", one can think of "sanitizer" or "safety glasses"
        // Given "sam", it's almost certain it's something related to "samsung"
        for (int i = 1; i < keyword.length() + 1; i++) {
            // just sqr
            int levelWeight = (keyword.length() + 1 - i) * (keyword.length() + 1 - i);
            maxWeight += levelWeight;
            // a match on the higher level also means a match on every lower one
            if (i >= minSubkeywordMatchLen.get()) {
                curWeight += levelWeight;
            }
        }

        return (int) Math.round(curWeight * 100.0 / maxWeight);
    }
}
