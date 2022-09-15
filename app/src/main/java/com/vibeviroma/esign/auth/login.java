package com.vibeviroma.esign.auth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.vibeviroma.esign.MainActivity;
import com.vibeviroma.esign.R;
import com.vibeviroma.esign.objects.User;
import com.vibeviroma.esign.tools.Cte;
import com.vibeviroma.esign.tools.PrefManager;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class login extends AppCompatActivity {


    private TextInputEditText email, password;
    private TextView tv_login, tv_signin;
    private SweetAlertDialog loading;

    private static final int RC_SIGN_IN = 1;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();
        initClick();
    }

    private void initViews() {
        email = (TextInputEditText) findViewById(R.id.regist_email);
        password = (TextInputEditText) findViewById(R.id.regis_code);
        tv_login = (TextView) findViewById(R.id.login);
        tv_signin = (TextView) findViewById(R.id.signin);
        loading = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id_))
                .requestEmail()
                .build();


        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    private void initClick() {
        findViewById(R.id.sign_in_button).setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
        setGooglePlusButtonText(findViewById(R.id.sign_in_button), "Connectez-vous avec Google");
        tv_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String _email = email.getText().toString().trim();
                String _password = password.getText().toString().trim();
                if (_email.isEmpty() || _password.isEmpty()) {
                    new SweetAlertDialog(login.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Erreur")
                            .showContentText(true)
                            .setContentText("Tous les champs sont requis")
                            .showCancelButton(false)
                            .show();
                } else {
                    make_login(_email, _password);
                }
            }
        });

        tv_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(login.this, signin.class));
                finish();
            }
        });
    }

    private void make_login(String email, String password) {
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        loading.setContentText("Veuillez patienter...");
        loading.show();
        loading.setCanceledOnTouchOutside(false);
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    final String user_id = mAuth.getCurrentUser().getUid();
                    Cte.getUserRef().document(user_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            loading.dismissWithAnimation();
                            User user = documentSnapshot.toObject(User.class);

                            PrefManager.setUserId(user_id, login.this);
                            PrefManager.setUserInfo(Cte.object2json(user), login.this);
                            startActivity(new Intent(login.this, MainActivity.class));
                            finish();
                        }
                    });
                } else {
                    loading.dismissWithAnimation();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                User user = new User(account.getFamilyName(), account.getGivenName(), account.getEmail(), "", "");
                Log.d(Cte.TAG_, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken(), user);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(Cte.TAG_, "Google sign in failed "+e.getMessage());
            }
        }
    }

    protected void setGooglePlusButtonText(SignInButton signInButton, String buttonText) {
        // Find the TextView that is inside of the SignInButton and set its text
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(buttonText);
                return;
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken, User user_) {

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        loading.setContentText("Veuillez patienter...");
        try {
            loading.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        loading.setCanceledOnTouchOutside(false);

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(Cte.TAG_, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        assert user != null;
                        Cte.getUserRef().document(user.getUid()).set(user_).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                PrefManager.setUserId(user.getUid(), login.this);
                                PrefManager.setUserInfo(Cte.object2json(user), login.this);
                                startActivity(new Intent(login.this, MainActivity.class));
                                finish();
                            }
                        });
                    } else {
                        loading.dismiss();
                        // If sign in fails, display a message to the user.
                        Log.w(Cte.TAG_, "signInWithCredential:failure", task.getException());

                    }
                });
    }
}