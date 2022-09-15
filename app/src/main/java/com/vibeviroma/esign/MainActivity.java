package com.vibeviroma.esign;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.google.firebase.firestore.DocumentSnapshot;
import com.vibeviroma.esign.adapters.SignatureAdapters;
import com.vibeviroma.esign.objects.Signature;
import com.vibeviroma.esign.tools.Cte;
import com.vibeviroma.esign.tools.FileUtils;
import com.vibeviroma.esign.tools.PrefManager;
import com.vibeviroma.esign.tools.Upload;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity {


    private SweetAlertDialog loading;
    private Bitmap bitmap;
    private RecyclerView recyclerView;
    private String type="jpg";
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initClick();
        loadSignatures();
    }

    private void loadSignatures() {
        Cte.getSignatureRef().whereEqualTo("fromUserId", PrefManager.getUserId(this))
                .addSnapshotListener((value, error) -> {
                    if(value==null)
                        return;
                    List<Signature> signatureList= new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot: value.getDocuments()){
                        Signature signature= documentSnapshot.toObject(Signature.class);
                        signatureList.add(signature);
                    }
                    if (signatureList.size()!=0){
                        findViewById(R.id.empty).setVisibility(View.GONE);
                    }
                    displaySignatures(signatureList);
                });

    }

    private void displaySignatures(List<Signature> signatureList) {
        recyclerView.setLayoutManager(new GridLayoutManager(this, Cte.getSpanCount(this, this)));
        recyclerView.setAdapter(new SignatureAdapters(this, signatureList));
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
    }

    private void initClick(){
        loading = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        loading.setCanceledOnTouchOutside(false);

        recyclerView= findViewById(R.id.recyclerView);

        findViewById(R.id.fab).setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    Options options= Options.init()
                            .setRequestCode(100)
                            .setCount(1)
                            .setFrontfacing(false)
                            .setPreSelectedUrls(new ArrayList<>())
                            .setExcludeVideos(true)
                            .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)
                            .setPath("eSign/images");
                    Pix.start(MainActivity.this, options);
                }else{
                    showSignatureGesture();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        showSignatureGesture();
        if (resultCode == Activity.RESULT_OK && requestCode == 100) {
            assert data != null;
            ArrayList<String> returnValue = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            assert returnValue != null;
            showSignatureGesture();
        }else if (requestCode == 1
                && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            Uri uri;
            if (data != null) {
                uri = data.getData();
                try {
                    continuetask(uri, new File(Objects.requireNonNull(FileUtils.getPath(this, uri))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void showSignatureGesture() {
        View view= LayoutInflater.from(this).inflate(R.layout.signature, null, false);
        final TextView save_= view.findViewById(R.id.save);
        TextView rep_= view.findViewById(R.id.back);
        final SignaturePad overlayView= view.findViewById(R.id.gesture);
        overlayView.setPenColor(Color.BLUE);
        overlayView.setDrawingCacheEnabled(true);
        save_.setEnabled(false);
        overlayView.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {

            }

            @Override
            public void onSigned() {

                save_.setEnabled(true);
            }

            @Override
            public void onClear() {
                save_.setEnabled(false);
            }
        });

        alertDialog= new AlertDialog.Builder(this)
                .setView(view)
                .create();
        alertDialog.show();
        alertDialog.setCancelable(true);
        save_.setOnClickListener(view1 -> {

            alertDialog.dismiss();
            CharSequence[] charSequence= {"Fond plein", "Fond transparent"};
            AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                    .setItems(charSequence, (dialog1, which) -> {
                        type= Cte.TYPE_JPG;
                        bitmap= overlayView.getSignatureBitmap();
                        if(which==1) {
                            type = Cte.TYPE_PNG;
                            bitmap= overlayView.getTransparentSignatureBitmap();
                        }


                        String path= Environment.getExternalStorageDirectory().toString();
                        path+= File.separator;
                        path+= "eSign";
                        path+= File.separator;
                        path += UUID.randomUUID().toString()+"."+type;
                        File file= new File(path);
                        try {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                intent.setType("application/"+type);
                                intent.putExtra(Intent.EXTRA_TITLE, UUID.randomUUID().toString()+"."+type);

                                startActivityForResult(intent, 1);
                            }else{
                                file.createNewFile();
                                continuetask(Uri.fromFile(file), file);
                            }
                        } catch (IOException e) {
                            Log.d(Cte.TAG_, e.getMessage()+" Error");
                            e.printStackTrace();
                        }
                    }).create();
            dialog.show();

        });

        rep_.setOnClickListener(view12 -> overlayView.clear());

    }

    private void continuetask(Uri uri, File file) throws IOException {
        alertDialog.dismiss();
        String path= file.getAbsolutePath();

        Log.d(Cte.TAG_, path+" PATH");

        OutputStream fileOutputStream= getContentResolver().openOutputStream(uri);
        bitmap.compress(type.equals(Cte.TYPE_PNG)?Bitmap.CompressFormat.PNG:Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
        fileOutputStream.flush();
        fileOutputStream.close();

        //Cte.createPathFromBitmap(bitmap, file);

        String signId= Cte.getSignatureRef().document().getId();
        Signature signature= new Signature(signId, "", path, type, PrefManager.getUserId(MainActivity.this), Calendar.getInstance().getTimeInMillis());

        loading.setContentText("Sauvegarde en cours...");
        loading.show();
        Upload upload= new Upload(path, type);
        upload.setListener(new Upload.uploadingListener() {
            @Override
            public void onProgressListener(int percentage) {
                loading.setContentText("Sauvegarde en cours...\n"+percentage+"% effectués");
            }

            @Override
            public void onSuccess(String link) {
                signature.setLien_image(link);
                Cte.getSignatureRef().document(signature.getKey()).set(signature).addOnSuccessListener(unused -> {
                    loading.dismiss();
                    Toast.makeText(MainActivity.this, "Sauvegardée avec succès !", Toast.LENGTH_SHORT).show();
                });
            }
        });
        upload.makeUplaod();

    }


}