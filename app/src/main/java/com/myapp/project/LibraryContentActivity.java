package com.myapp.project;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class LibraryContentActivity extends AppCompatActivity {

    private static final String EXTRA_LIBRARY = "extra_library";
    private Library library;
    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private DocumentAdapter documentAdapter;
    private List<Document> docs;

    private FloatingActionButton fabChatLibrary;

    public static void open(Context ctx, Library library) {
        Intent i = new Intent(ctx, LibraryContentActivity.class);
        i.putExtra(EXTRA_LIBRARY, library);
        ctx.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_content);

        library = (Library) getIntent().getSerializableExtra(EXTRA_LIBRARY);
        if (library == null) { finish(); return; }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(library.getName());
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerViewLibraryContent);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fabChatLibrary = findViewById(R.id.fabChatLibrary);
        fabChatLibrary.setOnClickListener(v -> chatWithEntireLibrary());

        FloatingActionButton fab = findViewById(R.id.fabAddFileToLibrary);
        fab.setOnClickListener(v -> showAddFileToLibraryDialog());

        loadDocuments();
    }

    // --- ĐÂY LÀ HÀM ĐÃ SỬA ---
    private void chatWithEntireLibrary() {
        if (docs == null || docs.isEmpty()) {
            Toast.makeText(this, "Thư viện này trống, không thể hỏi AI!", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder combinedContent = new StringBuilder();
        combinedContent.append("Đây là nội dung tổng hợp từ thư viện " + library.getName() + ":\n\n");

        int validDocsCount = 0;

        for (Document doc : docs) {
            String text = doc.getExtractedText();
            if (text != null && !text.trim().isEmpty()) {
                combinedContent.append("--- Tài liệu: ").append(doc.getName()).append(" ---\n");
                combinedContent.append(text).append("\n\n");
                validDocsCount++;
            }
        }

        if (validDocsCount == 0) {
            Toast.makeText(this, "Các tài liệu trong thư viện này chưa có nội dung (chưa quét OCR hoặc PDF chưa đọc được).", Toast.LENGTH_LONG).show();
            return;
        }

        Document virtualDoc = new Document();

        // --- QUAN TRỌNG: Sửa dòng này ---
        // Sử dụng số âm của ID thư viện để làm ID duy nhất cho đoạn chat này
        // Ví dụ: Lib ID = 5 => Chat ID = -5
        virtualDoc.setId( -1 * library.getId() );

        virtualDoc.setName("Chat: " + library.getName());
        virtualDoc.setExtractedText(combinedContent.toString());

        Intent intent = new Intent(LibraryContentActivity.this, ChatActivity.class);
        intent.putExtra("document", virtualDoc);
        startActivity(intent);
    }
    // ------------------------------------

    private void loadDocuments() {
        docs = dbHelper.getDocumentsInLibrary(library.getId());
        documentAdapter = new DocumentAdapter(this, docs);

        documentAdapter.setOnDocumentLongClickListener((document, position) -> {
            String[] options = {"Gỡ khỏi thư viện", "Chỉnh sửa thông tin", "Xóa file"};
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setTitle(document.getName())
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            dbHelper.removeDocumentFromLibrary(library.getId(), document.getId());
                            loadDocuments();
                        } else if (which == 1) {
                            androidx.appcompat.app.AlertDialog.Builder editB = new androidx.appcompat.app.AlertDialog.Builder(this);
                            View v = getLayoutInflater().inflate(R.layout.dialog_add_document, null);
                            EditText etName = v.findViewById(R.id.etDocName);
                            EditText etDescription = v.findViewById(R.id.etDocDescription);
                            EditText etTags = v.findViewById(R.id.etDocTags);
                            etName.setText(document.getName());
                            etDescription.setText(document.getDescription());
                            etTags.setText(document.getTags());
                            editB.setTitle("Chỉnh sửa")
                                    .setView(v)
                                    .setPositiveButton("Lưu", (d, w) -> {
                                        document.setName(etName.getText().toString().trim());
                                        document.setDescription(etDescription.getText().toString().trim());
                                        document.setTags(etTags.getText().toString().trim());
                                        dbHelper.updateDocument(document);
                                        loadDocuments();
                                    })
                                    .setNegativeButton("Hủy", null)
                                    .show();
                        } else if (which == 2) {
                            dbHelper.deleteDocument(document.getId());
                            loadDocuments();
                        }
                    }).show();
        });
        recyclerView.setAdapter(documentAdapter);
    }

    private void showAddFileToLibraryDialog() {
        List<Document> allDocs = dbHelper.getAllDocuments();
        if (allDocs == null || allDocs.isEmpty()) {
            androidx.appcompat.app.AlertDialog.Builder b = new androidx.appcompat.app.AlertDialog.Builder(this);
            b.setMessage("Kho tài liệu trống. Hãy thêm tài liệu ở màn hình chính trước.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }
        String[] names = new String[allDocs.size()];
        boolean[] checked = new boolean[allDocs.size()];
        for (int i = 0; i < allDocs.size(); i++) names[i] = allDocs.get(i).getName();

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Thêm file vào " + library.getName());
        builder.setMultiChoiceItems(names, checked, (dialog, which, isChecked) -> checked[which] = isChecked);
        builder.setPositiveButton("Thêm", (dialog, which) -> {
            for (int i = 0; i < checked.length; i++) {
                if (checked[i]) {
                    dbHelper.addDocumentToLibrary(library.getId(), allDocs.get(i).getId());
                }
            }
            loadDocuments();
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }
}