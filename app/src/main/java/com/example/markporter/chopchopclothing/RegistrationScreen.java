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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;

public class RegistrationScreen extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private ProgressBar progressBar;
    private Button regbtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_screen);

        firebaseAuth = FirebaseAuth.getInstance();

        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.INVISIBLE);

        regbtn = (Button) findViewById(R.id.btnRegister);
    }

    public void registerUser(View view){

        String email = ((EditText) findViewById(R.id.txtEmail)).getText().toString();
        String password = ((EditText) findViewById(R.id.txtPass)).getText().toString();
        String confirmPass = ((EditText) findViewById(R.id.txtConfirmPass)).getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter an Email.", Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter a password.", Toast.LENGTH_LONG).show();
            return;
        }
        if(!TextUtils.equals(password, confirmPass)){
            Toast.makeText(this, "Your passwords do not match!", Toast.LENGTH_LONG).show();
            return;
        }

        regbtn.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegistrationScreen.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(RegistrationScreen.this,
                                    "Registered Successfully!", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(RegistrationScreen.this, LoginScreen.class));
                            finish();
                        }else{
                            FirebaseAuthException e = (FirebaseAuthException) task.getException();
                            Toast.makeText(RegistrationScreen.this,
                                    "Failed to register! " + e.getMessage(), Toast.LENGTH_LONG).show();
                            regbtn.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }
    //public Task<AuthResult> createUserWithEmailAndPassword (String email, String password){

    //}
}
