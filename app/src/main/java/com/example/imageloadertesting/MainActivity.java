package com.example.imageloadertesting;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private ImageView mainImageView;
    private Button randomizeButton;
    private Button fetchAlbumButton;
    private Button nextButton;
    private Button backButton;
    private List<String> imgUrls;
    private OkHttpClient imgurClient;
    private EditText albumCodeEditText;
    private TextView currentTextView;
    private int currentImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainImageView = (ImageView) findViewById(R.id.mainImageView);
        randomizeButton = (Button) findViewById(R.id.randomizeButton);
        fetchAlbumButton = (Button) findViewById(R.id.fetchAlbumButton);
        nextButton = (Button) findViewById(R.id.nextButton);
        backButton = (Button) findViewById(R.id.backButton);
        setBottomButtons(false);
        albumCodeEditText = (EditText) findViewById(R.id.albumCodeEditText);
        currentTextView = (TextView) findViewById(R.id.currentTextView);
        currentImage = 0;
        imgUrls = new ArrayList<String>();

        albumCodeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                imgUrls = getImgurData();
            }
        });

        fetchAlbumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeImage(0);
            }
        });

        randomizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeImage((int) (Math.random() * imgUrls.size()));
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeImage(currentImage + 1);
            }

        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeImage(currentImage - 1);
            }
        });
    }

    private List<String> getImgurData() {
        setBottomButtons(false);
        final List<String> result = new ArrayList<String>();
        imgurClient = new OkHttpClient.Builder().build();
        String albumURL = "https://api.imgur.com/3/album/" + albumCodeEditText.getText().toString();
        Request request = new Request.Builder()
                .url(albumURL).header("Authorization", getResources().getString(R.string.clientID))
                .header("User-Agent", getResources().getString(R.string.app_name))
                .build();

        imgurClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("TAG", "Failure");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject data = new JSONObject(response.body().string());
                    JSONObject album = data.getJSONObject("data");
                    JSONArray images = album.getJSONArray("images");

                    for (int i = 0; i < images.length(); i++) {
                        JSONObject item = images.getJSONObject(i);
                        result.add(item.getString("link"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        setBottomButtons(true);
        return result;
    }

    private void changeImage(int newIndex) {
        if(imgUrls.size() != 0) {
            currentImage = newIndex;
            if(currentImage >= imgUrls.size()) {
                currentImage = 0;
            } else if(currentImage < 0) {
                currentImage = imgUrls.size() - 1;
            }
            String randomURL = imgUrls.get(currentImage);
            Picasso.get().
                    load(randomURL).
                    fit().
                    centerInside().
                    into(mainImageView);
            currentTextView.setText((currentImage+1) + "/" + imgUrls.size());
        } else {
            currentTextView.setText("");
            Toast toast = Toast.makeText(getApplicationContext(), "Album was not found", Toast.LENGTH_SHORT);
            toast.show();
            setBottomButtons(false);
        }
    }

    private void setBottomButtons(boolean condition) {
        randomizeButton.setEnabled(condition);
        nextButton.setEnabled(condition);
        backButton.setEnabled(condition);
    }
}
