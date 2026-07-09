package com.example.socialmediaadiction.Gemini;

import java.util.ArrayList;
import java.util.List;

public class GeminiRequest {

    public List<Content> contents;

    public GeminiRequest(String userMessage) {

        contents = new ArrayList<>();

        Part part = new Part();
        part.text = userMessage;

        Content content = new Content();
        content.parts = new ArrayList<>();
        content.parts.add(part);

        contents.add(content);
    }

    public static class Content {

        public List<Part> parts;
    }

    public static class Part {

        public String text;
    }
}