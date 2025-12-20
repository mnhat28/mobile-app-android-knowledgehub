package com.myapp.project;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.EditText;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class LibraryContentActivity extends AppCompatActivity {

    private static final String EXTRA_LIBRARY = "extra_library";
    private Library library;
    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private DocumentAdapter documentAdapter;
    private List<Document> docs;

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

        loadDocuments();

        // Floating button to add existing files to this library
        FloatingActionButton fab = findViewById(R.id.fabAddFileToLibrary);
        fab.setOnClickListener(v -> showAddFileToLibraryDialog());
    }

    private void loadDocuments() {
        docs = dbHelper.getDocumentsInLibrary(library.getId());
        documentAdapter = new DocumentAdapter(this, docs);
        // optionally set long click listener to allow remove file from library or edit
        documentAdapter.setOnDocumentLongClickListener((document, position) -> {
            // when long-press a doc inside library, show option to remove from library or edit
            String[] options = {"Gỡ khỏi thư viện", "Chỉnh sửa thông tin", "Xóa file"};
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setTitle(document.getName())
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            dbHelper.removeDocumentFromLibrary(library.getId(), document.getId());
                            loadDocuments();
                        } else if (which == 1) {
                            // reuse existing edit dialog in MainActivity logic or implement quick edit
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
                                        DatabaseHelper dh = new DatabaseHelper(this);
                                        dh.updateDocument(document);
                                        loadDocuments();
                                    })
                                    .setNegativeButton("Hủy", null)
                                    .show();
                        } else if (which == 2) {
                            // delete file permanently
                            DatabaseHelper dh = new DatabaseHelper(this);
                            dh.deleteDocument(document.getId());
                            // also delete file from disk not handled here (MainActivity did it); keep consistent
                            loadDocuments();
                        }
                    }).show();
        });
        recyclerView.setAdapter(documentAdapter);
    }

    private void showAddFileToLibraryDialog() {
        // Reuse a simple dialog listing all documents (from full list), allowing selection
        List<Document> allDocs = dbHelper.getAllDocuments();
        if (allDocs == null || allDocs.isEmpty()) {
            androidx.appcompat.app.AlertDialog.Builder b = new androidx.appcompat.app.AlertDialog.Builder(this);
            b.setMessage("Không có tài liệu nào trong kho để thêm.")
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
