package com.vibeviroma.esign.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vibeviroma.esign.MainActivity;
import com.vibeviroma.esign.R;
import com.vibeviroma.esign.objects.User;
import com.vibeviroma.esign.tools.Cte;
import com.vibeviroma.esign.tools.PrefManager;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class signin extends AppCompatActivity {


    private TextView tv_login, tv_signin;
    private TextInputEditText name, prenom, phone, email, password;
    private SweetAlertDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        initViews();
        initClick();
    }
    //Error inflating class com.google.android.material.textfield.TextInputLayout
    private void initViews(){
        tv_login=(TextView)findViewById(R.id.login);
        tv_signin=(TextView)findViewById(R.id.signin);

        name=(TextInputEditText)findViewById(R.id.regist_name);
        prenom=(TextInputEditText)findViewById(R.id.regist_prenom);
        phone=(TextInputEditText)findViewById(R.id.regist_phone);
        email=(TextInputEditText)findViewById(R.id.regist_email);
        password=(TextInputEditText)findViewById(R.id.regis_code);

        loading= new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
    }

    private void initClick(){
        tv_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(signin.this, login.class));
                finish();
            }
        });

        tv_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String _name= name.getText().toString().trim();
                String _prenom= prenom.getText().toString().trim();
                String _phone= phone.getText().toString().trim();
                String _email= email.getText().toString().trim();
                String _pass= password.getText().toString().trim();

                if(_name.isEmpty() || _prenom.isEmpty() || _phone.isEmpty() || _email.isEmpty() || _pass.isEmpty()){
                    new SweetAlertDialog(signin.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Erreur")
                            .showContentText(true)
                            .setContentText("Tous les champs sont requis")
                            .showCancelButton(false)
                            .show();
                }else{
                    createAccount(_email, _pass, _name, _prenom, _phone);
                }
            }
        });
    }

    private void createAccount(String email, String pass, String name, final String prenom, String phone) {
        loading.setContentText("Veuillez patienter...");
        loading.setCanceledOnTouchOutside(false);
        loading.show();
        final User user= new User(name, prenom, email, phone, pass);
        final FirebaseAuth mAuth= FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final CollectionReference user_ref= Cte.getUserRef();

        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    final String user_id = mAuth.getCurrentUser().getUid();
                    user_ref.document(user_id).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            loading.dismissWithAnimation();
                            new SweetAlertDialog(signin.this, SweetAlertDialog.SUCCESS_TYPE)
                                    .setContentText("Félicitation "+prenom+ " pour ton inscription ! \n\nBienvenue sur eSign")
                                    .showContentText(true)
                                    .setTitleText("Succès")
                                    .setConfirmText("Merci")
                                    .showCancelButton(false)
                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sweetAlertDialog) {

                                            PrefManager.setUserId(user_id, signin.this);
                                            PrefManager.setUserInfo(Cte.object2json(user), signin.this);
                                            startActivity(new Intent(signin.this, MainActivity.class));
                                            finish();
                                        }
                                    })
                                    .show();
                        }
                    });
                }else{
                    loading.dismissWithAnimation();
                    Toast.makeText(signin.this, "Erreur: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}