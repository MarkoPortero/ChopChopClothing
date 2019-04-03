package com.example.markporter.chopchopclothing;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class OutfitView extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor sensor;
    private static float SHAKE_THRESHOLD_GRAVITY = 2;
    private long lastUpdateTime;

    private static final String TAG = "Browse Clothing";
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference myRef;
    private String userID;
    private String displayID = "0";
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase lFirebaseDatabase;
    private FirebaseAuth lAuth;

    ArrayList<ClothingInformation> array = new ArrayList<>();
    int iterator=24;
    ImageView img;
    final private int REQUEST_INTERNET = 123;
    private ListView outfitListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfit_view);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        lastUpdateTime = System.currentTimeMillis();

        lAuth = FirebaseAuth.getInstance();
        lFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = lFirebaseDatabase.getReference();
        FirebaseUser user = lAuth.getCurrentUser();
        userID = user.getUid();

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

        myRef.addValueEventListener(new ValueEventListener() {
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
                    Toast.makeText(this, "Image Failed!!!", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            getAccelerometer(event);
        }
    }
    private void getAccelerometer(SensorEvent event){
        System.out.println("Accel reached");
        float[] values = event.values;

        float x = values[0];
        float y = values[1];
        float z = values[2];

        float gx = x / SensorManager.GRAVITY_EARTH;
        float gy = y / SensorManager.GRAVITY_EARTH;
        float gz = z / SensorManager.GRAVITY_EARTH;

        float gForce = (float)Math.sqrt(gx * gx + gy * gy + gz * gz);

        long currentTime = System.currentTimeMillis();
        if(gForce >= SHAKE_THRESHOLD_GRAVITY)
        {
            if(currentTime - lastUpdateTime < 200){
                return;
            }
            lastUpdateTime = currentTime;
            System.out.println("Device shaken");
            Intent intent = new Intent(OutfitView.this, NavBrowseClothing.class);
            startActivity(intent);
        }
    }
    @Override
    protected void onResume(){
        super.onResume();
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    protected void onPause(){
        super.onPause();
        sensorManager.unregisterListener(this);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        protected Bitmap doInBackground(String... urls){
            return DownloadImage(urls[0]);
        }

        protected void onPostExecute(Bitmap result){
            ImageView img = (ImageView) findViewById(R.id.imgMain3);
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

    public void NextObject(View view){
        ClothingInformation Temp = new ClothingInformation();
        System.out.print(iterator);
        if(iterator<28) {
            iterator++;
            Temp = array.get(iterator);
            System.out.println(Temp.getProductName());
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
        if(iterator>23) {
            iterator--;
            System.out.print(iterator);
            Temp = array.get(iterator);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.INTERNET}, REQUEST_INTERNET);
            } else {
                ClothingInformation CurrentItem = array.get(iterator);
                String location = CurrentItem.getPictureLocation();
                new DownloadImageTask().execute(location);
            }
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
}
