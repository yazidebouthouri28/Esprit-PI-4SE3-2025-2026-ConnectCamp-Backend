package tn.esprit.projetintegre.services;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Properties;

@Slf4j
@Service
public class SentimentAnalysisService {

    private StanfordCoreNLP pipeline;

    @PostConstruct
    public void init() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,parse,sentiment");
        pipeline = new StanfordCoreNLP(props);
        log.info("Stanford CoreNLP pipeline initialized for sentiment analysis");
    }

    /**
     * Analyzes the sentiment of the given text and returns the result.
     *
     * @param text the text to analyze
     * @return SentimentResult containing score (-1 to 1) and label
     */
    public SentimentResult analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new SentimentResult(0.0, "neutral");
        }

        try {
            edu.stanford.nlp.pipeline.CoreDocument doc = new edu.stanford.nlp.pipeline.CoreDocument(text);
            pipeline.annotate(doc);

            List<CoreMap> sentences = doc.annotation().get(CoreAnnotations.SentencesAnnotation.class);
            if (sentences == null || sentences.isEmpty()) {
                return new SentimentResult(0.0, "neutral");
            }

            int totalSentiment = 0;
            int sentenceCount = 0;

            for (CoreMap sentence : sentences) {
                String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
                totalSentiment += getSentimentValue(sentiment);
                sentenceCount++;
            }

            double averageScore = sentenceCount > 0 ? (double) totalSentiment / sentenceCount : 0.0;
            String label = getLabelFromScore(averageScore);

            // Normalize score to range -1 to 1
            double normalizedScore = averageScore / 2.0;

            return new SentimentResult(normalizedScore, label);

        } catch (Exception e) {
            log.error("Error analyzing sentiment: {}", e.getMessage(), e);
            return new SentimentResult(0.0, "neutral");
        }
    }

    /**
     * Converts Stanford sentiment class to numeric value:
     * Very Negative = 0, Negative = 1, Neutral = 2, Positive = 3, Very Positive = 4
     * We map these to: -2, -1, 0, 1, 2 for easier averaging
     */
    private int getSentimentValue(String sentiment) {
        return switch (sentiment.toLowerCase()) {
            case "very negative" -> -2;
            case "negative" -> -1;
            case "neutral" -> 0;
            case "positive" -> 1;
            case "very positive" -> 2;
            default -> 0;
        };
    }

    /**
     * Converts average score to label.
     * Score range is -2 to 2, we map to labels.
     */
    private String getLabelFromScore(double score) {
        if (score <= -1.5) {
            return "very negative";
        } else if (score < -0.5) {
            return "negative";
        } else if (score <= 0.5) {
            return "neutral";
        } else if (score < 1.5) {
            return "positive";
        } else {
            return "very positive";
        }
    }

    @Getter
    public static class SentimentResult {
        private final double score;  // -1 to 1
        private final String label;  // positive, neutral, negative, etc.

        public SentimentResult(double score, String label) {
            this.score = Math.max(-1.0, Math.min(1.0, score)); // Clamp between -1 and 1
            this.label = label;
        }
    }
}
