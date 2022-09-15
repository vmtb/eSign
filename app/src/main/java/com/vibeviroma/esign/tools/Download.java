package com.vibeviroma.esign.tools;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class Download {
    String path, extension, key;
    private static downloadingListener listener;
    private Context context;

    public void setListener(downloadingListener listener) {
        Download.listener = listener;
    }

    public Download(String key, String path, String extension, Context context){
        this.path= path;
        this.key= key;
        this.context=context;
        this.extension= extension;
    }

    public void makeDownlaod() throws IOException {
        final File file =  File.createTempFile(key, "."+extension);
        if(path==null || path.trim().isEmpty())
            return;
        final StorageReference riversRef = FirebaseStorage.getInstance().getReferenceFromUrl(path);
        riversRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                listener.onSuccess(file.getAbsolutePath());
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        })
        .addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                listener.onProgressListener((int) (taskSnapshot.getBytesTransferred()*100/taskSnapshot.getTotalByteCount()));
            }
        });
    }

    public interface downloadingListener{
        void onProgressListener(int percentage);
        void onSuccess(String link);
    }
}
