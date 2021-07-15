package com.example.nytimesviewer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ArticleListActivity extends AppCompatActivity {
    private ArrayList<API.Article> articles;
    private ArrayAdapter<API.Article> adapter;
    private ListView listView;
    private List<String> categories;
    private int page = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);

        listView = findViewById(R.id.articles);
        articles = (ArrayList<API.Article>) getIntent().getSerializableExtra("articles");
        categories = (ArrayList<String>) getIntent().getSerializableExtra("categories");

        listView.setAdapter(adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, articles));
        findViewById(R.id.load_more).setOnClickListener((view) -> {
            API.findArticles(categories, ++page).exceptionally((e) -> {
                Toast.makeText(this, e.getCause().getMessage(), Toast.LENGTH_LONG).show();
                return null;
            }).thenAccept((result) -> {
                if (result != null) {
                    articles.addAll(result);
                    adapter.notifyDataSetChanged();
                }
            });
        });
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(ArticleListActivity.this, ArticleActivity.class);
            intent.putExtra("article", articles.get(position));
            startActivity(intent);
        });
    }
}
