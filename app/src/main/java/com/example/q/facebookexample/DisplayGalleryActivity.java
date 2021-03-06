package com.example.q.facebookexample;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DisplayGalleryActivity extends AppCompatActivity {
    final String scheme = "http";
    final String host = "13.124.41.33";
    final Integer port = 1234;
    final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private CustomGalleryAdapter adapter = new CustomGalleryAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_gallery);
        getPictures();

        GridView pictureGridView = (GridView) findViewById(R.id.gridView1);

        final Intent intent = new Intent(this, DisplayPictureDetailActivity.class);
        pictureGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                intent.putExtra("photoID", ((Picture) adapter.getItem(position)).getPhotoID());
                intent.putExtra("photoDir", ((Picture) adapter.getItem(position)).getPhotoDir());
                startActivity(intent);
            }
        });
    }

//
//    public void showPictureDetail(View view){
//
//        view.getVerticalScrollbarPosition()
//        String pictureDir = "";
//        Log.i("showPictureDetail", pictureDir);
//        startActivity(intent);
//    }


    public void nothingNoticeGalleryTextViewGone() {
        TextView nothingNoticeGalleryTextView = (TextView) findViewById(R.id.nothingNoticeGalleryTextView);
        nothingNoticeGalleryTextView.setVisibility(View.GONE);
    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//    }

    // display the list by global adapter
    private void displayList() {
        GridView gridView = (GridView) findViewById(R.id.gridView1);
        gridView.setAdapter(adapter);
    }


    public ArrayList<Picture> jsonToPictureList(String body) {
        ArrayList<Picture> serverPictureList = new ArrayList<>();
        try {
            Log.i("jsonToPictureList", "parsing start");
            JSONArray items = new JSONArray(body);
            Log.i("jsonToPictureList", "body to JSONArray");
            for(int i = 0 ; i < items.length() ; i++) {
                JSONObject item = (JSONObject) items.get(i);
                Picture newPicture = new Picture();
                newPicture.setPhotoID(item.getString("_id"));
                newPicture.setPhotoDir(scheme + "://" + host + ":" + String.valueOf(port) + "/" + item.getString("photoDir"));
                newPicture.setPhotoName(item.getString("photoName"));
                if (!item.has("thumbDir"))
                    continue;
                newPicture.setThumbnailDir(scheme + "://" + host + ":" + String.valueOf(port) + "/" + item.getString("thumbDir"));

                serverPictureList.add(newPicture);
            }

            Log.i("jsonToPictureList", "parsing finish");
            return serverPictureList;

        } catch (JSONException e) {
            Log.e("jsonToPictureList", e.getMessage());
        }
        return null;
    }


    public ArrayList<Picture> getServerDB(String userID) {
        Log.i("getServerDB", "start api call");
        OkHttpClient client = new OkHttpClient();
        Log.i("getServerDB", "open client");
        HttpUrl url = new HttpUrl.Builder()
                .scheme(scheme)
                .host(host)
                .port(port)
                .encodedPath("/api/photos/" + userID)
                .build();

        Log.i("getServerDB", url.toString());
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        Log.i("getServerDB", "request build");

        try {
            Log.i("getServerDB", "just before request execute");
            Response response = client.newCall(request).execute();
            Log.i("getServerDB", "request sended");
            String body = response.body().string();
            response.close();
            Log.i("getServerDB", body);
            return jsonToPictureList(body);
        } catch(Exception e) {
            Log.e("getServerDB", "error");
        }
        return null;
    }

    public JSONArray pictureListToJsonArray(ArrayList<Picture> pictureList) {
//        JSONObject jObject = new JSONObject();
        try {
            JSONArray jArray = new JSONArray();
            for (Picture picture : pictureList) {
                JSONObject pictureJson = new JSONObject();


//                contactJson.put("contactID", contact.getID());
//                contactJson.put("name", contact.getName());
//                contactJson.put("phoneNum", contact.getPhoneNumber());
////                contactJson.put("email", contact.getEmail());

                jArray.put(pictureJson);
            }
            return jArray;
        } catch (Exception e) {
            Log.e("contactListToJson", e.getMessage());
        }
        return null;
    }

    public void postServerDB(String userID, ArrayList<Picture> postPictureList) {
        Log.i("postServerDB", "start api call : " + String.valueOf(postPictureList.size()));
        OkHttpClient client = new OkHttpClient();
        Log.i("postServerDB", "open client");
        HttpUrl url = new HttpUrl.Builder()
                .scheme(scheme)
                .host(host)
                .port(port)
                .encodedPath("/api/photos/" + userID)
                .build();

        Log.i("postServerDB", url.toString());

        RequestBody body = RequestBody.create(JSON, pictureListToJsonArray(postPictureList).toString());

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Log.i("postServerDB", "request build");

        try {
            Response response = client.newCall(request).execute();
            Log.i("postServerDB", "request sended");
            String getBody = response.body().string();
            response.close();
            Log.i("postServerDB", getBody);
        } catch(Exception e) {
            Log.e("postServerDB", e.getMessage());
        }
    }

    public void getPictures() {
        adapter = new CustomGalleryAdapter();

        AsyncTask.execute(new Runnable() {

            @Override
            public void run() {
            try {
                String userID = AccessToken.getCurrentAccessToken().getUserId().toString();
                userID = "krista";

                // get server DB
                ArrayList<Picture> serverPictureList = getServerDB(userID);

                // after all toast the alarm that every contact sync
//                    runOnUiThread(new Runnable() {
//                        public void run() {
//                            Toast.makeText(getApplicationContext(), "Every Contacts Synchronized ", Toast.LENGTH_SHORT).show();
//                        }
//                    });

                Log.i("syncServerDB", String.valueOf(serverPictureList.size()));

                // set adapter
                adapter.setPictureViewItemList(serverPictureList);

                displayList();
                if (adapter.getCount() != 0) { // there is any picture
                    nothingNoticeGalleryTextViewGone();  // hide notice take a picture or uploading text view
                    for(int i = 0 ; i < adapter.getCount() ; i++) {
                        Log.i("photoDir", adapter.getPictureViewItemList().get(i).getPhotoDir());

                    }
                }

            } catch (Exception e) {
                Log.e("Error", e.getMessage());
            }


            }
        });
    }

}
