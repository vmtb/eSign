package com.vibeviroma.esign;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vibeviroma.esign.objects.Signature;
import com.vibeviroma.esign.tools.Cte;
import com.vibeviroma.esign.tools.Download;

import java.io.File;
import java.io.IOException;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ViewImage extends AppCompatActivity {

    private ImageView imageView;
    private FloatingActionButton fab;
    private Signature signature;
    private SweetAlertDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);
        signature= (Signature) getIntent().getExtras().getSerializable("SIGNATURE");
        initViews();
        setActions();
    }

    private void initViews(){
        imageView= findViewById(R.id.image);
        fab= findViewById(R.id.fab);
        loading= new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        loading.setCanceledOnTouchOutside(false);
    }

    private void setActions(){
        if(signature.getLien_image().isEmpty()){
            Glide.with(this).load(signature.getLien_image()).placeholder(R.drawable.logo).into(imageView);
        }else{
            imageView.setImageURI(Uri.fromFile(new File(signature.getLocal_image())));
        }

        if(signature.getType().equals(Cte.TYPE_PNG)){
            findViewById(R.id.root).setBackgroundColor(getResources().getColor(R.color.white));
        }

        fab.setOnClickListener(v -> {
            if(new File(signature.getLocal_image()).exists()){
                shareContent();
            }else{
                loading.setContentText("Téléchargement en cours...");
                loading.show();
                Download download= new Download(signature.getKey(), signature.getLien_image(), signature.getType(), ViewImage.this);
                download.setListener(new Download.downloadingListener() {
                    @Override
                    public void onProgressListener(int percentage) {
                        loading.setContentText("Téléchargement en cours...\n"+percentage+"% effectués");
                    }

                    @Override
                    public void onSuccess(String link) {
                        loading.dismiss();
                        Cte.getSignatureRef().document(signature.getKey()).update("local_image", link);
                        signature.setLocal_image(link);
                        shareContent();
                    }
                });
                try {
                    download.makeDownlaod();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void shareContent(){
        Uri uri= FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", new File(signature.getLocal_image()));
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType("image/"+signature.getType());
        startActivity(Intent.createChooser(shareIntent, "Partager la signature"));
    }
}