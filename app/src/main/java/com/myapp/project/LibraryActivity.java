package com.myapp.project;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class LibraryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LibraryAdapter adapter;
    private DatabaseHelper dbHelper;
    private List<Library> libraries;
    private FloatingActionButton fabAddLibrary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý thư viện");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerViewLibraries);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fabAddLibrary = findViewById(R.id.fabAddLibrary);
        fabAddLibrary.setOnClickListener(v -> showAddLibraryDialog());

        loadLibraries();

        // Search functionality inside this Activity (if you want to add search box in layout)
        // You can implement similar to MainActivity's search (not mandatory here)
    }

    private void loadLibraries() {
        libraries = dbHelper.getAllLibraries();
        adapter = new LibraryAdapter(this, libraries);
        adapter.setOnLibraryLongClickListener((library, position) -> showLibraryOptions(library, position));
        recyclerView.setAdapter(adapter);
    }

    private void showAddLibraryDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_library, null);
        EditText etName = dialogView.findViewById(R.id.etLibraryName);
        EditText etTags = dialogView.findViewById(R.id.etLibraryTags);
        EditText etDesc = dialogView.findViewById(R.id.etLibraryDescription);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tạo thư viện mới")
                .setView(dialogView)
                .setPositiveButton("Tạo", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Tên thư viện là bắt buộc", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Library lib = new Library();
                    lib.setName(name);
                    lib.setTags(etTags.getText().toString().trim());
                    lib.setDescription(etDesc.getText().toString().trim());
                    long id = dbHelper.insertLibrary(lib);
                    if (id > 0) {
                        Toast.makeText(this, "Đã tạo thư viện", Toast.LENGTH_SHORT).show();
                        loadLibraries();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showLibraryOptions(Library library, int position) {
        String[] options = {"Chỉnh sửa", "Xem nội dung", "Xóa"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(library.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showEditLibraryDialog(library);
                    } else if (which == 1) {
                        LibraryContentActivity.open(this, library);
                    } else {
                        showDeleteLibraryConfirm(library, position);
                    }
                }).show();
    }

    private void showEditLibraryDialog(Library library) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_library, null);
        EditText etName = dialogView.findViewById(R.id.etLibraryName);
        EditText etTags = dialogView.findViewById(R.id.etLibraryTags);
        EditText etDesc = dialogView.findViewById(R.id.etLibraryDescription);

        etName.setText(library.getName());
        etTags.setText(library.getTags());
        etDesc.setText(library.getDescription());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chỉnh sửa thư viện")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    library.setName(etName.getText().toString().trim());
                    library.setTags(etTags.getText().toString().trim());
                    library.setDescription(etDesc.getText().toString().trim());
                    dbHelper.updateLibrary(library);
                    loadLibraries();
                    Toast.makeText(this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDeleteLibraryConfirm(Library library, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận xóa")
                .setMessage("Bạn có muốn xóa thư viện \"" + library.getName() + "\" không? Các liên kết tới file sẽ bị xóa nhưng file vẫn nằm trong kho tài liệu.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    dbHelper.deleteLibrary(library.getId());
                    libraries.remove(position);
                    adapter.notifyItemRemoved(position);
                    Toast.makeText(this, "Đã xóa thư viện", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
