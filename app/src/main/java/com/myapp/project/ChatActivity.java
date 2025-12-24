package com.myapp.project;

import android.content.ContentValues;
import android.content.Intent; // Import cho Voice
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.speech.RecognizerIntent; // Import cho Voice
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myapp.project.ai_assistant.AppConfig;
import com.myapp.project.ai_assistant.GeminiApiService;
import com.myapp.project.ai_assistant.GeminiModels;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit; // Import cho Timeout

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatActivity extends AppCompatActivity {

    // Mã request cho giọng nói
    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;

    private Document document;
    private GeminiApiService apiService;
    private ImageButton btnSend, btnMic; // Thêm btnMic
    private EditText etMessage;
    private LinearLayout layoutLoading;
    private RecyclerView rvChat;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        document = (Document) getIntent().getSerializableExtra("document");

        initViews();
        setupRetrofit();
        setupRecyclerView();
        loadChatHistory();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbarChat);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (document != null) {
                if (document.getId() == -1) {
                    getSupportActionBar().setTitle(document.getName());
                    getSupportActionBar().setSubtitle("Hỏi đáp toàn bộ thư viện");
                } else {
                    getSupportActionBar().setTitle(document.getName());
                    getSupportActionBar().setSubtitle("Trợ lý AI");
                }
            } else {
                getSupportActionBar().setTitle("Hỏi Trợ Lý AI");
            }
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        btnSend = findViewById(R.id.btnSend);
        etMessage = findViewById(R.id.etMessage);
        layoutLoading = findViewById(R.id.layoutLoading);
        rvChat = findViewById(R.id.rvChat);

        // --- NÚT MICROPHONE (TÍNH NĂNG MỚI) ---
        btnMic = findViewById(R.id.btnMic);
        // Kiểm tra null để tránh crash nếu chưa kịp sửa layout XML
        if (btnMic != null) {
            btnMic.setOnClickListener(v -> speak());
        }

        btnSend.setOnClickListener(v -> {
            String query = etMessage.getText().toString().trim();
            if (!query.isEmpty()) {
                addMessage(query, true);
                etMessage.setText("");
                askGemini(query);
            }
        });
    }

    // --- HÀM XỬ LÝ GIỌNG NÓI ---
    private void speak() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Đang nghe bạn nói...");

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, "Máy bạn không hỗ trợ nhập liệu giọng nói", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (result != null && !result.isEmpty()) {
                    etMessage.setText(result.get(0)); // Điền chữ vào ô nhập liệu
                }
            }
        }
    }
    // ----------------------------

    private void setupRetrofit() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                // --- TĂNG TIMEOUT LÊN 40 GIÂY ---
                .connectTimeout(40, TimeUnit.SECONDS)
                .readTimeout(40, TimeUnit.SECONDS)
                .writeTimeout(40, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(GeminiApiService.class);
    }

    private void setupRecyclerView() {
        chatMessageList = new ArrayList<>();

        chatAdapter = new ChatAdapter(this, chatMessageList, new ChatAdapter.OnMessageAction() {
            @Override
            public void onEdit(String text) {
                etMessage.setText(text);
                etMessage.setSelection(text.length());
                etMessage.requestFocus();
            }
        });

        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(chatAdapter);
    }

    private void addMessage(String text, boolean isUser) {
        chatMessageList.add(new ChatMessage(text, isUser));
        chatAdapter.notifyItemInserted(chatMessageList.size() - 1);
        rvChat.scrollToPosition(chatMessageList.size() - 1);
        saveMessageToDb(text, isUser);
    }

    private void askGemini(String userQuery) {
        String context = document.getExtractedText();

        if (context == null || context.trim().isEmpty()) {
            addMessage("Hệ thống: Tài liệu này chưa có nội dung văn bản (OCR chưa chạy hoặc rỗng).", false);
            return;
        }

        String prompt = "Dựa trên nội dung tài liệu:\n" + context + "\nHãy trả lời câu hỏi: " + userQuery;

        layoutLoading.setVisibility(View.VISIBLE);
        rvChat.scrollToPosition(chatMessageList.size() - 1);

        // --- KHÓA NÚT GỬI ĐỂ CHẶN SPAM ---
        btnSend.setEnabled(false);
        btnSend.setAlpha(0.5f);

        apiService.getCompletion(AppConfig.GEMINI_API_KEY, new GeminiModels.Request(prompt))
                .enqueue(new Callback<GeminiModels.Response>() {
                    @Override
                    public void onResponse(Call<GeminiModels.Response> call, Response<GeminiModels.Response> response) {
                        layoutLoading.setVisibility(View.GONE);
                        // --- MỞ LẠI NÚT GỬI ---
                        btnSend.setEnabled(true);
                        btnSend.setAlpha(1.0f);

                        if (response.isSuccessful() && response.body() != null) {
                            String answer = response.body().candidates.get(0).content.parts.get(0).text;
                            addMessage(answer, false);
                        } else {
                            addMessage("Lỗi kết nối AI: " + response.code(), false);
                        }
                    }

                    @Override
                    public void onFailure(Call<GeminiModels.Response> call, Throwable t) {
                        layoutLoading.setVisibility(View.GONE);
                        Toast.makeText(ChatActivity.this, "Lỗi kết nối mạng (Timeout)!", Toast.LENGTH_SHORT).show();
                        // --- MỞ LẠI NÚT GỬI KHI LỖI ---
                        btnSend.setEnabled(true);
                        btnSend.setAlpha(1.0f);
                    }
                });
    }

    private void saveMessageToDb(String message, boolean isUser) {
        // KHÔNG CẦN DÒNG 'if (document.getId() == -1) return;' NỮA
        // Vì bên LibraryContentActivity, ta sẽ truyền ID là số âm (-ID_thư_viện)

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Nếu là chat thư viện, document.getId() sẽ là số âm (VD: -5)
        // Nếu là chat tài liệu, document.getId() sẽ là số dương (VD: 10)
        // Cả hai đều được lưu bình thường và không đụng hàng nhau!
        values.put("doc_id", document.getId());

        values.put("message", message);
        values.put("is_user", isUser ? 1 : 0);
        db.insert("chat_history", null, values);
        db.close();
    }

    private void loadChatHistory() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Vẫn load theo doc_id như bình thường
        Cursor cursor = db.query("chat_history", null, "doc_id = ?",
                new String[]{String.valueOf(document.getId())}, null, null, "id ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String msg = cursor.getString(cursor.getColumnIndexOrThrow("message"));
                boolean isUser = cursor.getInt(cursor.getColumnIndexOrThrow("is_user")) == 1;
                chatMessageList.add(new ChatMessage(msg, isUser));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        chatAdapter.notifyDataSetChanged();
        if (chatMessageList.size() > 0) {
            rvChat.scrollToPosition(chatMessageList.size() - 1);
        }
    }
}