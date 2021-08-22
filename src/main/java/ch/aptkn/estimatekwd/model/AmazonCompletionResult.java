package ch.aptkn.estimatekwd.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AmazonCompletionResult implements Serializable {
    private final List<String> suggestions = new ArrayList<>();

    @JsonCreator
    public AmazonCompletionResult(@JsonProperty("suggestions") List<AmazonCompletionSuggestion> suggestions) {
        this.suggestions.addAll(suggestions.stream().map(AmazonCompletionSuggestion::getValue).collect(Collectors.toList()));
    }

    public List<String> getSuggestions() {
        return suggestions;
    }
}
