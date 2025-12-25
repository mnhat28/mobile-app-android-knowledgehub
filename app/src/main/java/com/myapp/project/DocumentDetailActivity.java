package com.myapp.project;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.io.File;

public class DocumentDetailActivity extends AppCompatActivity {

    private TextView tvTags, tvContent;
    private ImageView ivIcon, ivContentImage;
    private PDFView pdfView;
    private CardView cardTextContent;
    private NestedScrollView scrollViewContent;
    private ExtendedFloatingActionButton btnChat;
    private Document document;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_detail);

        document = (Document) getIntent().getSerializableExtra("document");
        if (document == null) {
            finish();
            return;
        }

        initViews();
        setupData();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbarDetail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // --- ĐƯA TÊN TÀI LIỆU LÊN TOOLBAR ---
            getSupportActionBar().setTitle(document.getName());
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Ánh xạ View (Lưu ý: Không còn tvName nữa vì đã đưa lên Toolbar)
        tvTags = findViewById(R.id.tvDetailTags);
        tvContent = findViewById(R.id.tvDetailContent);
        ivIcon = findViewById(R.id.ivDetailIcon);

        pdfView = findViewById(R.id.pdfView);
        ivContentImage = findViewById(R.id.ivContentImage);
        cardTextContent = findViewById(R.id.cardTextContent);
        scrollViewContent = findViewById(R.id.scrollViewContent);

        btnChat = findViewById(R.id.btnChatWithAI);

        btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(DocumentDetailActivity.this, ChatActivity.class);
            intent.putExtra("document", document);
            startActivity(intent);
        });
    }

    private void setupData() {
        // 1. Setup Tags
        if (document.getTags() != null && !document.getTags().isEmpty()) {
            tvTags.setText(document.getTags());
            tvTags.setVisibility(View.VISIBLE);
        } else {
            tvTags.setVisibility(View.GONE);
        }

        // 2. Logic Hiển thị theo loại file
        String type = document.getFileType();
        File file = new File(document.getFilePath());

        // Mặc định ẩn tất cả
        pdfView.setVisibility(View.GONE);
        scrollViewContent.setVisibility(View.GONE);
        ivContentImage.setVisibility(View.GONE);
        cardTextContent.setVisibility(View.GONE);

        if ("PDF".equals(type) && file.exists()) {
            // --- TRƯỜNG HỢP PDF ---
            // Chỉ hiện PDFView, ẩn ScrollView (chứa icon và tags) đi cho rộng chỗ
            pdfView.setVisibility(View.VISIBLE);

            pdfView.fromFile(file)
                    .enableSwipe(true)
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .defaultPage(0)
                    .load();

        } else if ("IMAGE".equals(type)) {
            // --- TRƯỜNG HỢP ẢNH ---
            scrollViewContent.setVisibility(View.VISIBLE);
            ivContentImage.setVisibility(View.VISIBLE);

            // Header icon
            ivIcon.setBackground(null);
            ivIcon.clearColorFilter();
            Glide.with(this).load(file).transform(new CircleCrop()).into(ivIcon);

            // Ảnh nội dung
            Glide.with(this).load(file).into(ivContentImage);

        } else {
            // --- TRƯỜNG HỢP TEXT ---
            scrollViewContent.setVisibility(View.VISIBLE);
            cardTextContent.setVisibility(View.VISIBLE);

            setupIcon(Color.parseColor("#2E7D32"), android.R.drawable.ic_menu_edit);

            String content = document.getExtractedText();
            tvContent.setText(content != null && !content.isEmpty() ? content : "Không có nội dung.");
        }
    }

    private void setupIcon(int color, int iconRes) {
        ivIcon.setBackgroundResource(R.drawable.bg_circle_icon);
        if (ivIcon.getBackground() instanceof GradientDrawable) {
            ((GradientDrawable) ivIcon.getBackground()).setColor(color);
        }
        ivIcon.setImageResource(iconRes);
        ivIcon.setColorFilter(Color.WHITE);
    }

}
