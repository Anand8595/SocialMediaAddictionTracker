package com.example.socialmediaadiction.Gemini;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GeminiResponse {

    @SerializedName("candidates")
    public List<Candidate> candidates;

    public static class Candidate {

        @SerializedName("content")
        public Content content;
    }

    public static class Content {

        @SerializedName("parts")
        public List<Part> parts;
    }

    public static class Part {

        @SerializedName("text")
        public String text;
    }
}