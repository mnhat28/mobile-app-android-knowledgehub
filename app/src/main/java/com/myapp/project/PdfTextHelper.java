package com.myapp.project;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PdfTextHelper {

    // Interface để trả kết quả về MainActivity
    public interface OnPdfExtractListener {
        void onSuccess(String text);
        void onFailure(Exception e);
    }

    public static void extractTextFromPdf(Context context, File pdfFile, OnPdfExtractListener listener) {
        // Khởi tạo thư viện (Bắt buộc)
        PDFBoxResourceLoader.init(context);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                // Tải file PDF
                PDDocument document = PDDocument.load(pdfFile);

                // Bóc tách văn bản
                PDFTextStripper stripper = new PDFTextStripper();
                String extractedText = stripper.getText(document);

                document.close();

                // Trả kết quả về luồng chính
                handler.post(() -> {
                    if (extractedText.trim().isEmpty()) {
                        listener.onSuccess("File PDF này không chứa lớp văn bản (có thể là file scan ảnh).");
                    } else {
                        listener.onSuccess(extractedText);
                    }
                });

            } catch (Exception e) {
                handler.post(() -> listener.onFailure(e));
            }
        });
    }
}