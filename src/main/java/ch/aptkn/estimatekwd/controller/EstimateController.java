package ch.aptkn.estimatekwd.controller;

import ch.aptkn.estimatekwd.model.EstimateResponse;
import ch.aptkn.estimatekwd.service.AmazonAutocompleteService;
import ch.aptkn.estimatekwd.service.SearchVolumeEstimateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
public class EstimateController {
    private final static int HTTP_REQUEST_TIMEOUT = 8000;

    private final SearchVolumeEstimateService keywordSearchVolumeEstimator;
    private final AmazonAutocompleteService amazonAutocompleteService;

    public EstimateController(SearchVolumeEstimateService keywordSearchVolumeEstimator, AmazonAutocompleteService amazonAutocompleteService) {
        this.keywordSearchVolumeEstimator = keywordSearchVolumeEstimator;
        this.amazonAutocompleteService = amazonAutocompleteService;
    }

    @GetMapping("/estimate")
    public ResponseEntity<Object> getEstimate(@RequestParam(value = "keyword") String keyword) throws InterruptedException {
        keyword = sanitizeKeyword(keyword);
        if (keyword.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Map<String, List<String>> suggestions = amazonAutocompleteService.getSuggestions(keyword, HTTP_REQUEST_TIMEOUT);
        int score = keywordSearchVolumeEstimator.getScore(keyword, suggestions);
        return ResponseEntity.ok(new EstimateResponse(keyword, score));
    }

    /**
     * Convert to lowercase and get rid of trailing whitespaces
     *
     * @param keyword keyword as submitted by user
     * @return sanitized keyword
     */
    private String sanitizeKeyword(String keyword) {
        return keyword.trim().toLowerCase(Locale.ROOT);
    }
}
