package org.createjoy.downloadprogressview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private DownloadProgressView downloadProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        downloadProgressView = (DownloadProgressView) findViewById(R.id.load);
        downloadProgressView.startLoad();
    }
}
