package com.cactusbiceps.doggies;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private VideoView videoView;
    private TextView textView;

    private String currentDoggieURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        videoView = findViewById(R.id.videoView);
        textView = findViewById(R.id.textView);

        requestNewDoggie();

        OnSwipeTouchListener swipeListener = new OnSwipeTouchListener(this) {
            public void onSwipeTop() {
                requestNewDoggie();
            }
            public void onSwipeRight() {
//                Toast.makeText(getBaseContext(), "right", Toast.LENGTH_SHORT).show();
            }
            public void onSwipeLeft() {
                requestNewDoggie();
            }
            public void onSwipeBottom() {
//                Toast.makeText(getBaseContext(), "bottom", Toast.LENGTH_SHORT).show();
            }

        };

        imageView.setOnTouchListener(swipeListener);
        videoView.setOnTouchListener(swipeListener);
    }

    private void requestNewDoggie(){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://random.dog/woof.json";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Log the first 500 characters of the response string.
                        Log.i("GET Request", "Response is: "+ response);
                        parseJSON(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("GET Request", "That didn't work!");
                displayErrorMessage();
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void parseJSON(String json){
        try{
            JSONObject jObject = new JSONObject(json);
            String doggieUrl = jObject.getString("url");
            currentDoggieURL = doggieUrl;

            if(isImage(doggieUrl)){
                loadImageFromWeb(doggieUrl);
            } else if (isVideo(doggieUrl)){
                playVideo(doggieUrl);
            } else {
                displayErrorMessage();
            }

        } catch(Exception e){
            Log.e("Parse JSON", e.getMessage());
            displayErrorMessage();
        }
    }

    private boolean isImage(String url){
        boolean result = false;
        String extension = "";

        int i = url.lastIndexOf('.');
        if (i > 0) {
            extension = url.substring(i+1).toLowerCase();
        }

        switch(extension){
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "bmp":
            case "jfif":
                result = true;
        }

        return result;
    }

    private boolean isVideo(String url){
        boolean result = false;
        String extension = "";

        int i = url.lastIndexOf('.');
        if (i > 0) {
            extension = url.substring(i+1).toLowerCase();
        }

        switch(extension){
            case "m4v":
            case "avi":
            case "mpg":
            case "mp4":
            case "webm":
            case "mov":
            case "mpeg":
                result = true;
        }

        return result;
    }

    private void loadImageFromWeb(String url){
        imageView.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.INVISIBLE);
        textView.setVisibility(View.INVISIBLE);

        Glide.with(this)
                .load(url)
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                .into(imageView);
    }

    private void playVideo(String url){
        imageView.setVisibility(View.INVISIBLE);
        videoView.setVisibility(View.VISIBLE);
        textView.setVisibility(View.INVISIBLE);

        try {
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);
            Uri video = Uri.parse(url);
            videoView.setMediaController(mediaController);
            videoView.setVideoURI(video);

            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setLooping(true);
                }
            });

            videoView.start();
        } catch (Exception e) {
            Log.e("Playing Video", e.getMessage());
            Toast.makeText(this, "Error connecting", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayErrorMessage(){
        imageView.setVisibility(View.INVISIBLE);
        videoView.setVisibility(View.INVISIBLE);
        textView.setVisibility(View.VISIBLE);

        textView.setText("Unable to load doggie :(");
    }
}