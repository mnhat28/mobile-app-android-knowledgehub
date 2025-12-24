package com.myapp.project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AccountChooserActivity extends AppCompatActivity {

    ListView listView;
    List<String> emails = new ArrayList<>();
    List<String> uids = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_chooser);

        listView = findViewById(R.id.listAccounts);
        loadAccounts();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                R.layout.item_account,
                R.id.tvEmail,
                emails
        );

        listView.setAdapter(adapter);

        listView.setOnItemClickListener((a, v, pos, id) -> {
            switchToAccount(emails.get(pos));
        });
    }

    private void loadAccounts() {
        SharedPreferences prefs = getSharedPreferences("accounts", MODE_PRIVATE);
        Set<String> set = prefs.getStringSet("list", new HashSet<>());

        for (String s : set) {
            String[] parts = s.split("\\|");
            emails.add(parts[0]);
            uids.add(parts[1]);
        }
    }

    private void switchToAccount(String email) {
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("email", email);
        startActivity(intent);
        finish();
    }
}
