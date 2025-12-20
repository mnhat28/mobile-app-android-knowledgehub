package com.myapp.project;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.barteksc.pdfviewer.PDFView;
import com.myapp.project.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class DocumentDetailActivity extends AppCompatActivity {

    private PDFView pdfView;
    private TextView tvContent;
    private ImageView ivImage;
    private ScrollView scrollViewText;
    private ScrollView scrollViewImage;
    private Document document;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_detail);

        document = (Document) getIntent().getSerializableExtra("document");

        if (document == null) {
            Toast.makeText(this, "Không tìm thấy tài liệu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        displayDocument();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(document.getName());

        pdfView = findViewById(R.id.pdfView);
        tvContent = findViewById(R.id.tvContent);
        ivImage = findViewById(R.id.ivImage);
        scrollViewText = findViewById(R.id.scrollViewText);
        scrollViewImage = findViewById(R.id.scrollViewImage);

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void displayDocument() {
        File file = new File(document.getFilePath());

        if (!file.exists()) {
            Toast.makeText(this, "File không tồn tại", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (document.getFileType()) {
            case "PDF":
                displayPDF(file);
                break;
            case "TXT":
                displayText(file);
                break;
            case "IMAGE":
                displayImage(file);
                break;
            default:
                Toast.makeText(this, "Định dạng file không được hỗ trợ", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayPDF(File file) {
        pdfView.setVisibility(View.VISIBLE);
        scrollViewText.setVisibility(View.GONE);
        scrollViewImage.setVisibility(View.GONE);

        pdfView.fromFile(file)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .defaultPage(0)
                .spacing(0)
                .load();
    }

    private void displayText(File file) {
        pdfView.setVisibility(View.GONE);
        scrollViewText.setVisibility(View.VISIBLE);
        scrollViewImage.setVisibility(View.GONE);

        try {
            StringBuilder text = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line).append("\n");
            }
            br.close();

            tvContent.setText(text.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi đọc file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void displayImage(File file) {
        pdfView.setVisibility(View.GONE);
        scrollViewText.setVisibility(View.GONE);
        scrollViewImage.setVisibility(View.VISIBLE);

        ivImage.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
    }
}