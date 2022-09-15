package com.vibeviroma.esign;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.vibeviroma.esign.auth.login;

public class Welcome extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);


        Thread th= new Thread(){
            @Override
            public void run() {
                try {
                    sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    if(FirebaseAuth.getInstance().getCurrentUser()==null){
                        startActivity(new Intent(Welcome.this, login.class));;
                        finish();
                    }else{
                        startActivity(new Intent(Welcome.this, MainActivity.class));;
                        finish();
                    }
                }
            }
        };
        th.start();
    }
}