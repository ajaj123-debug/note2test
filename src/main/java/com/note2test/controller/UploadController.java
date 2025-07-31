package com.note2test.controller;

import com.note2test.model.Question;
import com.note2test.service.GeminiService;
import com.note2test.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Controller
public class UploadController {

    @Autowired private PdfService pdfService;
    @Autowired private GeminiService geminiService;

    private List<Question> currentQuestions;
    
    @GetMapping("/")
    public String landingPage() {
        return "landing";
    }
    

    @GetMapping("/upload")
    public String uploadPage() {
        return "upload";
    }

    @PostMapping("/upload")
    public String handleUpload(@RequestParam("file") MultipartFile file, Model model) {
        try {
            System.out.println("Received file: " + file.getOriginalFilename());

            String text = pdfService.extractText(file);
            System.out.println("Extracted text length: " + text.length());

            if (text.trim().isEmpty()) {
                model.addAttribute("error", "Could not extract text from the PDF file. Please ensure the file contains readable text.");
                return "upload";
            }

            currentQuestions = geminiService.generateQuestions(text);
            System.out.println("Generated questions count: " + currentQuestions.size());

            if (currentQuestions.isEmpty()) {
                model.addAttribute("error", "Could not generate questions from the content. Please try again.");
                return "upload";
            }

            model.addAttribute("questions", currentQuestions);
            return "questions";
        } catch (RuntimeException e) {
            if (e.getMessage().contains("503") || e.getMessage().contains("overloaded")) {
                model.addAttribute("error", "The AI service is currently busy. Please wait a moment and try again.");
            } else {
                model.addAttribute("error", "An error occurred while processing your file: " + e.getMessage());
            }
            return "upload";
        } catch (Exception e) {
            model.addAttribute("error", "An unexpected error occurred. Please try again.");
            return "upload";
        }
    }


    @PostMapping("/evaluate")
    public String evaluate(@RequestParam Map<String, String> answers, Model model) {
        int score = 0;
        for (int i = 0; i < currentQuestions.size(); i++) {
            String correct = currentQuestions.get(i).getCorrectAnswer();
            String given = answers.get("answers[" + i + "]");
            if (correct.equalsIgnoreCase(given)) score++;
        }
        model.addAttribute("score", score);
        return "result";
    }
}
