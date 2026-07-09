package com.example.socialmediaadiction.Model;

import java.util.List;

public class ReportResponse {
    String status;
    List<UsageModel> data;

    public List<UsageModel> getData() {
        return data;
    }
}