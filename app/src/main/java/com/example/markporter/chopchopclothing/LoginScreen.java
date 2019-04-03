package com.example.markporter.chopchopclothing;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import android.net.*;
import android.content.Context;
public class LoginScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ProgressBar loginProgress;
    private Button loginBtn;
    private TextView txtReg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        mAuth = FirebaseAuth.getInstance();
        loginProgress = (ProgressBar) findViewById(R.id.progressBar);
        loginBtn = (Button) findViewById(R.id.btnLogin);
        loginProgress.setVisibility(View.INVISIBLE);
        txtReg = (TextView) findViewById(R.id.txtRegister);

    }
    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    public void goRegister(View view){
        Intent myIntent = new Intent(this, RegistrationScreen.class);
        startActivity(myIntent);
    }
    public void loginUser(View view){

        String email = ((EditText) findViewById(R.id.txtEmail)).getText().toString();
        String password = ((EditText) findViewById(R.id.txtPass)).getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter an Email.", Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter a password.", Toast.LENGTH_LONG).show();
            return;
        }

        Boolean network = haveNetworkConnection();
        if(network == false){
            Toast.makeText(this, "Please connect to the internet.", Toast.LENGTH_LONG).show();
            return;
        }
        loginProgress.setVisibility(View.VISIBLE);
        loginBtn.setVisibility(View.INVISIBLE);
        txtReg.setVisibility(View.INVISIBLE);

        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(LoginScreen.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), NavBrowseClothing.class));
                            finish();
                        }else{
                            Toast.makeText(LoginScreen.this, "Error logging in!", Toast.LENGTH_LONG).show();
                            loginBtn.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }
    private void updateUI(FirebaseUser user){
        //Set up this if the person is logged in!
    }
}
