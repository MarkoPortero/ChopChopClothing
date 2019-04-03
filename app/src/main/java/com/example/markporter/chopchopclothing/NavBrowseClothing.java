package com.example.markporter.chopchopclothing;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.net.MalformedURLException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class NavBrowseClothing extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

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
    private ListView outfitListView;
    private static final String OPEN_WEATHER_MAP_API_KEY = "a85d663495b5d1ddd712a4eac55a573a";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_browse_clothing);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        double lat = 55.006763, lon = -7.318268;
        String units = "metric";
        String url = String.format("http://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&units=%s&appid=%s",
                lat, lon, units, OPEN_WEATHER_MAP_API_KEY);
        new GetWeatherTask().execute(url);
    }

    private class GetWeatherTask extends AsyncTask<String, Void, String>{
        protected String doInBackground(String... strings) {
            String weather = "undefined";
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                InputStream stream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder builder = new StringBuilder();

                String inputString;
                while ((inputString = bufferedReader.readLine()) != null) {
                    builder.append(inputString);
                }

                JSONObject topLevel = new JSONObject(builder.toString());
                JSONObject main = topLevel.getJSONObject("main");
                weather = String.valueOf(main.getDouble("temp"));

                urlConnection.disconnect();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return weather;
        }

        protected void onPostExecute(String temp){
            System.out.println("Current Temperature : " + temp);
            Double currentTemp = Double.parseDouble(temp);
            if(currentTemp <= 7){
                Toast.makeText(getApplicationContext(), "It's cold out! Showing cold weather items.", Toast.LENGTH_LONG).show();
                iterator = 14;
            }

        }
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
            ((TextView) findViewById(R.id.txtPrice)).setText("£ " + Temp.getPrice());
            heartOff(view);
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
            ((TextView) findViewById(R.id.txtPrice)).setText("£ " + Temp.getPrice());
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
        ((TextView) findViewById(R.id.txtPrice)).setText("£ " + Temp.getPrice());

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
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nav_browse_clothing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // .
        int id = item.getItemId();
        //handle the liked clothing area
        if (id == R.id.nav_gallery) {
            Intent intent = new Intent(NavBrowseClothing.this, LikedItems.class);
            startActivity(intent);
        } else if (id == R.id.nav_send) {
            // Handle the logout action
            mAuth.signOut();
            startActivity(new Intent(this,LoginScreen.class));
        } else if (id == R.id.outfit_view) {
            Intent intent = new Intent(NavBrowseClothing.this, OutfitView.class);
            startActivity(intent);
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void heartChecker(View view){

    }

    public void heartOn(View view){
        ImageView img = (ImageView) findViewById(R.id.btnLike);
        img.setImageResource(R.drawable.heart_on);
        img.setTag(1);

    }

    public void heartOff(View view){
        ImageView img = (ImageView) findViewById(R.id.btnLike);
        img.setImageResource(R.drawable.heart_off);
        img.setTag(0);
    }

    public void purchaseItem(View view){
        ClothingInformation tempItem = array.get(iterator);
        String url = tempItem.getPurchaseLink();
        Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
        startActivity(viewIntent);
    }

}