package com.myapp.project.ai_assistant;

import java.util.ArrayList;
import java.util.List;

/**
 * Lớp chứa toàn bộ cấu trúc dữ liệu gửi đi và nhận về từ Gemini API
 */
public class GeminiModels {

    // --- PHẦN REQUEST (Gửi đi) ---
    public static class Request {
        public List<Content> contents;

        public Request(String text) {
            this.contents = new ArrayList<>();
            this.contents.add(new Content(text));
        }
    }

    public static class Content {
        public List<Part> parts;
        public Content(String text) {
            this.parts = new ArrayList<>();
            this.parts.add(new Part(text));
        }
    }

    public static class Part {
        public String text;
        public Part(String text) { this.text = text; }
    }

    // --- PHẦN RESPONSE (Nhận về) ---
    public static class Response {
        public List<Candidate> candidates;

        public static class Candidate {
            public ContentResponse content;
        }

        public static class ContentResponse {
            public List<Part> parts;
        }
    }
}