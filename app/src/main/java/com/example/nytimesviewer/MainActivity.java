package com.example.nytimesviewer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private Button search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.categories);
        search = findViewById(R.id.search);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_checked, API.CATEGORIES));
        search.setOnClickListener((View view) -> {
            SparseBooleanArray array = listView.getCheckedItemPositions();
            List<String> categories = new ArrayList<>();
            for (int i = 0; i < array.size(); i++) {
                if (array.get(i)) categories.add(API.CATEGORIES.get(i));
            }
            API.findArticles(categories, 0).exceptionally((e) -> {
                Toast.makeText(this, e.getCause().getMessage(), Toast.LENGTH_LONG).show();
                return null;
            }).thenAccept((result) -> {
                if (result != null) {
                    Log.i("stuff", result.toString());
                    Intent intent = new Intent(MainActivity.this, ArticleListActivity.class);
                    intent.putExtra("articles", new ArrayList<>(result));
                    intent.putExtra("categories", new ArrayList<>(categories));
                    startActivity(intent);
                }
            });
        });
    }
}
