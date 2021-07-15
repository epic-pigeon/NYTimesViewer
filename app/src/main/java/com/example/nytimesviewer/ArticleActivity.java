package com.example.nytimesviewer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class ArticleActivity extends AppCompatActivity {
    private API.Article article;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        article = (API.Article) getIntent().getSerializableExtra("article");

        ((TextView) findViewById(R.id.headline)).setText(article.getHeadline());
        TextView textView = findViewById(R.id.article);
        textView.setText(article.getSnippet());
        textView.setMovementMethod(new ScrollingMovementMethod());
    }
}
