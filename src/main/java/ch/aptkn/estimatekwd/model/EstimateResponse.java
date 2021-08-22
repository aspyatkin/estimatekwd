package ch.aptkn.estimatekwd.model;

public class EstimateResponse {
    private final String keyword;
    private final int score;

    public EstimateResponse(String keyword, int score) {
        this.keyword = keyword;
        this.score = score;
    }

    public String getKeyword() {
        return keyword;
    }

    public int getScore() {
        return score;
    }
}
