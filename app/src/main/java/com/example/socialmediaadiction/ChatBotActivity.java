package com.example.socialmediaadiction;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.socialmediaadiction.Gemini.ChatApiService;
import com.example.socialmediaadiction.Gemini.ChatRequest;
import com.example.socialmediaadiction.Gemini.ChatResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatBotActivity extends AppCompatActivity {

    EditText etMessage;
    Button btnSend;
    LinearLayout chatContainer;
    ScrollView chatScroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);

        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        chatContainer = findViewById(R.id.chatContainer);
        chatScroll = findViewById(R.id.chatScroll);

        btnSend.setOnClickListener(v -> {

            String message =
                    etMessage.getText().toString().trim();

            if (!message.isEmpty()) {

                addMessage("You: " + message, true);

                etMessage.setText("");

                sendMessageToAI(message);
            }
        });
    }

    private void sendMessageToAI(String message) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://smartbilling.mycloudspace.in/ecommerce/Android/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ChatApiService api =
                retrofit.create(ChatApiService.class);

        ChatRequest request =
                new ChatRequest(message);

        Call<ChatResponse> call =
                api.sendMessage(request);

        call.enqueue(new Callback<ChatResponse>() {

            @Override
            public void onResponse(Call<ChatResponse> call,
                                   Response<ChatResponse> response) {

                try {

                    if (response.isSuccessful()
                            && response.body() != null) {

                        String reply = response.body().reply;

                        addMessage("AI: " + reply, false);

                    } else {

                        addMessage("AI failed to respond", false);
                    }

                } catch (Exception e) {

                    addMessage("Error: " + e.getMessage(), false);

                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call,
                                  Throwable t) {

                addMessage("AI: " + t.getMessage(), false);
            }
        });
    }

    private void addMessage(String text, boolean isUser) {

        TextView tv = new TextView(this);

        tv.setText(text);

        tv.setTextSize(16);

        tv.setPadding(20, 20, 20, 20);

        tv.setTextColor(getResources()
                .getColor(android.R.color.white));

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(10, 10, 10, 10);

        if (isUser) {

            params.gravity = Gravity.END;

            tv.setBackgroundColor(0xFF5E35B1);

        } else {

            params.gravity = Gravity.START;

            tv.setBackgroundColor(0xFF3949AB);
        }

        tv.setLayoutParams(params);

        chatContainer.addView(tv);

        chatScroll.post(() ->
                chatScroll.fullScroll(ScrollView.FOCUS_DOWN));
    }
}