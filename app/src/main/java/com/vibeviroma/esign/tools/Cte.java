package com.vibeviroma.esign.tools;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Insets;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import static android.content.Context.WINDOW_SERVICE;

public class Cte {
    public static String TAG_="TAG_ESIGN";
    public static String TYPE_PNG="png";
    public static String TYPE_JPG="jpg";

    private static FirebaseFirestore db= FirebaseFirestore.getInstance();

    public static String object2json(Object object){
        String data="";
        try {
            data = new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static Object string2class(String value, Class <?> the_class){
        Object object=null;
        try {
            object = (new ObjectMapper()).readValue(value, the_class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return object;
    }

    public static int getSpanCount(Context ctx, Activity activity){
        /*if(PrefManager.getSpanCount(ctx)<0)*/ {
            if(Build.VERSION.SDK_INT>=30){
                final WindowMetrics metrics=((WindowManager)ctx.getSystemService(WINDOW_SERVICE)).getCurrentWindowMetrics();
                final WindowInsets windowInsets= metrics.getWindowInsets();
                Insets insets = windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars()|WindowInsets.Type.displayCutout());

                int insetsWidth= insets.right + insets.left;
                int insetsHeight= insets.top + insets.bottom;

                final Rect bounds= metrics.getBounds();
                final Size legacySize= new Size(bounds.width()-insetsWidth, bounds.height()-insetsHeight);


                int wight_view = 180;

                return (int) Math.round((legacySize.getWidth()/4.0) / wight_view);

            }
            WindowManager w=activity.getWindowManager();
            if(w==null)
                return 2;
            Display display = w.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x / 2;
            int height = size.y;
            float wight_view = 180;
            Log.d(Cte.TAG_ + "Displaying", width + "---" + wight_view);

            return (int) Math.round(width / wight_view);
        }
    }


    public static CollectionReference getUserRef(){
        return db.collection("Users");
    }
    public static CollectionReference getSignatureRef(){
        return db.collection("Signatures");
    }


    public static String createPathFromBitmap(Bitmap bitmap, File file){
        FileOutputStream fos= null;
        try {
            fos= new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if(fos!=null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file.getAbsolutePath();

    }


}
