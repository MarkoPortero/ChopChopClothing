package com.example.markporter.chopchopclothing;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.renderscript.ScriptGroup;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.net.MalformedURLException;

public class BrowseClothing extends AppCompatActivity {

    private static final String TAG = "Browse Clothing";
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference myRef;
    private String userID;
    private String displayID = "0";
    private FirebaseAuth.AuthStateListener mAuthListener;
    ArrayList<ClothingInformation> array = new ArrayList<>();
    int iterator=0;
    ImageView img;
    final private int REQUEST_INTERNET = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_clothing);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        userID = currentUser.getUid();
        displayID = "1";


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user!=null){
                    Toast.makeText(getApplicationContext(),
                            "Signed in correctly.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Sign in failed.", Toast.LENGTH_LONG).show();
                }
            }
        };

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                showData(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch(requestCode){
            case REQUEST_INTERNET:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    ClothingInformation CurrentItem = array.get(iterator);
                    String location = CurrentItem.getPictureLocation();
                    new DownloadImageTask().execute(location);
                }
                else{
                    Toast.makeText(BrowseClothing.this, "Image Failed!!!", Toast.LENGTH_LONG).show();
                }
                break;
                default:
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap>{
        protected Bitmap doInBackground(String... urls){
            return DownloadImage(urls[0]);
        }

        protected void onPostExecute(Bitmap result){
            ImageView img = (ImageView) findViewById(R.id.imgMain);
            img.setImageBitmap(result);
        }
    }

    private Bitmap DownloadImage(String URL){
        Bitmap bitmap = null;
        InputStream in = null;
        try{
            in = OpenHttpConnection(URL);
            bitmap = BitmapFactory.decodeStream(in);
            in.close();
        }
        catch(IOException e1){
            Log.d("Networking...Send help.", e1.getLocalizedMessage());
        }
        return bitmap;
    }

    private InputStream OpenHttpConnection(String urlString) throws IOException{
        InputStream in = null;
        int response = -1;

        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();

        if(! (conn instanceof HttpURLConnection))
            throw new IOException("Not a HTTP Connection");
        try{
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            response = httpConn.getResponseCode();
            if(response == HttpURLConnection.HTTP_OK){
                in = httpConn.getInputStream();
            }
        }
        catch(Exception ex){
            Log.d("Networking", ex.getLocalizedMessage());
            throw new IOException("Error connecting.");
        }
        return in;
    }

    @Override
    public void onStart(){
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop(){
        super.onStop();
        if(mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void NextObject(View view){
        ClothingInformation Temp = new ClothingInformation();
        System.out.print(iterator);
        if(iterator<21) {
            iterator++;
            Temp = array.get(iterator);
            System.out.println(Temp.getProductName());

            ((TextView) findViewById(R.id.txtProductName)).setText(Temp.getProductName());
            ((TextView) findViewById(R.id.txtPrice)).setText(Temp.getPrice());
        }else{
            Toast.makeText(getApplicationContext(),
                    "No more products currently..", Toast.LENGTH_LONG).show();
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.INTERNET}, REQUEST_INTERNET);
        }
        else
        {
            ClothingInformation CurrentItem = array.get(iterator);
            String location = CurrentItem.getPictureLocation();
            new DownloadImageTask().execute(location);
        }


    }
    public void PreviousObject(View view){
        ClothingInformation Temp = new ClothingInformation();
        if(iterator>0) {
            iterator--;
            System.out.print(iterator);
            Temp = array.get(iterator);
            System.out.println(Temp.getProductName());

            ((TextView) findViewById(R.id.txtProductName)).setText(Temp.getProductName());
            ((TextView) findViewById(R.id.txtPrice)).setText(Temp.getPrice());
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.INTERNET}, REQUEST_INTERNET);
            } else {
                ClothingInformation CurrentItem = array.get(iterator);
                String location = CurrentItem.getPictureLocation();
                new DownloadImageTask().execute(location);
            }
            heartOff(view);
        }else{
            Toast.makeText(getApplicationContext(), "No more products this way..", Toast.LENGTH_LONG).show();
        }

    }

    private void showData(DataSnapshot dataSnapshot){
        for(DataSnapshot ds : dataSnapshot.getChildren()){
            ClothingInformation cInfo = new ClothingInformation();
            cInfo.setPictureLocation(ds.getValue(ClothingInformation.class).getPictureLocation());
            cInfo.setPrice(ds.getValue(ClothingInformation.class).getPrice());
            cInfo.setProduct_ID(ds.getValue(ClothingInformation.class).getProductID());
            cInfo.setProduct_Name(ds.getValue(ClothingInformation.class).getProductName());
            cInfo.setPurchase_Link(ds.getValue(ClothingInformation.class).getPurchaseLink());
            array.add(cInfo);

            Log.d(TAG, "Show Data: Product Name: " + cInfo.getProductName());
        }
        ClothingInformation Temp = new ClothingInformation();
        System.out.print(iterator);
        Temp = array.get(iterator);
        System.out.println(Temp.getProductName());

        ((TextView) findViewById(R.id.txtProductName)).setText(Temp.getProductName());
        ((TextView) findViewById(R.id.txtPrice)).setText(Temp.getPrice());

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.INTERNET}, REQUEST_INTERNET);
        }
        else
        {
            ClothingInformation CurrentItem = array.get(iterator);
            String location = CurrentItem.getPictureLocation();
            new DownloadImageTask().execute(location);
        }

    }

    public void heartChecker(View view){
        ImageView img = (ImageView) findViewById(R.id.btnLike);
    }

    public void heartOn(View view){
        ImageView img = (ImageView) findViewById(R.id.btnLike);
        img.setImageResource(R.drawable.heart_on);
    }

    public void heartOff(View view){
        ImageView img = (ImageView) findViewById(R.id.btnLike);
        img.setImageResource(R.drawable.heart_off);
    }


}
