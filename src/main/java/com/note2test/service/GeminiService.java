package com.note2test.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.note2test.model.Question;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.*;
import java.util.*;

@Service
public class GeminiService {

    private static final String API_KEY = "AIzaSyB2xh0evZ_-s_nBoZF_qjD-ChDl4PPnlVg";
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;

    public List<Question> generateQuestions(String content) throws Exception {
        String prompt = """
            You are an educational AI assistant.

            Based on the following academic content, generate 50 multiple choice questions (MCQs). Each question must have:
            - 1 correct answer
            - 4 options
            - Output should be strict JSON without markdown or code blocks.
            - Format:
            {
              "mcqs": [
                {
                  "question": "...",
                  "options": ["...", "...", "...", "..."],
                  "answer": "..."
                }
              ]
            }

            Use only the following content to generate questions:

            """ + content;

        String payload = """
            {
              "contents": [
                {
                  "parts": [
                    {
                      "text": "%s"
                    }
                  ]
                }
              ]
            }
            """.formatted(prompt.replace("\"", "\\\""));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        System.out.println("🟡 Raw Gemini response:\n" + response.body());

        JsonNode responseNode = mapper.readTree(response.body());
        
        // Check for error response
        if (responseNode.has("error")) {
            String errorMessage = responseNode.path("error").path("message").asText("Unknown error");
            throw new RuntimeException("Gemini API error: " + errorMessage);
        }

        // If no error, proceed with parsing the response
        JsonNode textNode = responseNode
                .path("candidates").get(0)
                .path("content").path("parts").get(0)
                .path("text");

        String cleaned = "";
        try {
            String rawText = textNode.asText();

            cleaned = rawText
                    .replaceAll("(?s)^```json\\s*", "")
                    .replaceAll("(?s)```$", "")
                    .replaceAll("`", "")
                    .replaceAll("“|”", "\"")
                    .replaceAll("\\\\n", "")
                    .trim();

            System.out.println("🧹 Cleaned JSON:\n" + cleaned);

            JsonNode root = mapper.readTree(cleaned);
            JsonNode questionsArray = root.has("mcqs") ? root.get("mcqs") : root.get("quiz");

            if (questionsArray == null || !questionsArray.isArray()) {
                System.out.println("⚠️ No questions found in response.");
                return Collections.emptyList();
            }

            List<Question> questionList = new ArrayList<>();
            for (JsonNode node : questionsArray) {
                Question q = new Question();
                q.setQuestionText(node.get("question").asText());

                List<String> opts = new ArrayList<>();
                for (JsonNode opt : node.get("options")) {
                    opts.add(opt.asText());
                }

                q.setOptions(opts);
                q.setCorrectAnswer(node.get("answer").asText());
                questionList.add(q);
            }

            return questionList;

        } catch (Exception e) {
            System.err.println("❌ Failed to parse cleaned JSON:\n" + cleaned);
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
