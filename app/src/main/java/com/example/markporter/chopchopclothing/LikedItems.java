package com.example.markporter.chopchopclothing;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class LikedItems extends AppCompatActivity {

    private FirebaseDatabase lFirebaseDatabase;
    private FirebaseAuth lAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private ListView likedList;
    private String userID;
    private int iterator;
    ArrayList<String> array = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liked_items);

        likedList = (ListView) findViewById(R.id.listview);

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
    private void showData(DataSnapshot dataSnapshot){
        //Iterate through data

        for(DataSnapshot ds : dataSnapshot.getChildren()){
            ClothingInformation cInfo = new ClothingInformation();
            cInfo.setPictureLocation(ds.getValue(ClothingInformation.class).getPictureLocation());
            cInfo.setPrice(ds.getValue(ClothingInformation.class).getPrice());
            cInfo.setProduct_ID(ds.getValue(ClothingInformation.class).getProductID());
            cInfo.setProduct_Name(ds.getValue(ClothingInformation.class).getProductName());
            cInfo.setPurchase_Link(ds.getValue(ClothingInformation.class).getPurchaseLink());


            array.add(cInfo.getProductName());
            array.add(cInfo.getPurchaseLink());

            ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,array);
            likedList.setAdapter(adapter);

        }
        likedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String url = array.get(position);
                Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
                startActivity(viewIntent);
            }
        });
    }

}
