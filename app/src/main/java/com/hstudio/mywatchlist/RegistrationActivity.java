package com.hstudio.mywatchlist;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegistrationActivity extends AppCompatActivity {
    EditText regEmail, regPwd, cfmPwd;
    Button regBtn;
    TextView loginQuestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_registration);


        regEmail = findViewById(R.id.registerEmail);
        regPwd = findViewById(R.id.registerPassword);
        cfmPwd = findViewById(R.id.confirmPassword);
        regBtn = findViewById(R.id.registerButton);
        loginQuestion = findViewById(R.id.loginQuestion);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        loginQuestion.setOnClickListener(view -> {
            Intent intent = new Intent(RegistrationActivity.this,LoginActivity.class);
            startActivity(intent);
        });

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = regEmail.getText().toString().trim();
                String pass = regPwd.getText().toString().trim();
                String cfmPass = cfmPwd.getText().toString().trim();

                if(email.isEmpty()){
                    regEmail.setError("Email is required");
                    return;
                }
                if(pass.isEmpty()){
                    regPwd.setError("Password is required");
                }

                if(pass.equals(cfmPass)){
                    mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Intent intent = new Intent(RegistrationActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            }else{
                                String error = task.getException().toString();
                                Toast.makeText(RegistrationActivity.this, "Error: "+error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    Toast.makeText(RegistrationActivity.this, "Password and Confirm Password not matched", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}