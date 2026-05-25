package com.example.simpleexchangeprotocol.functionality;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.simpleexchangeprotocol.R;
import com.example.simpleexchangeprotocol.data.Contract;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PictureHandler {

    private static final int REQUEST_CAMERA_PERMISSION = 100;

    public static ActivityResultLauncher<Uri> registerPictureIntent(AppCompatActivity activity, File file, ActivityResultCallback<Boolean> onResult) throws Exception {
        // Check for camera permission first
        if (checkCameraPermission(activity)) {
            return activity.registerForActivityResult(new ActivityResultContracts.TakePicture(), onResult);
        }
        throw new Exception("Requires camera permission!");
    }

    public static String copyTempToPermanent(Activity activity, File photo){
        if (!photo.exists()) return null;

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File permanentFile = new File(activity.getExternalFilesDir("Pictures"), "photo_" + timestamp + ".jpg");

        try {
            Files.copy(photo.toPath(), permanentFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return permanentFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap getResizedBitmap(Bitmap bitmap, int desiredWidth, int heightLimit) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float futureWidth = desiredWidth;

        float aspectRatio = ((float)height)/width;
        float futureHeight = aspectRatio * desiredWidth;

        if (futureHeight > heightLimit){
            System.out.println("Reached given height limit! Restricting rescaling!");
            futureWidth = (int) (heightLimit / aspectRatio);
        }

        float scaleFactor = futureWidth / width;

        return rescaleBitmap(bitmap, scaleFactor);
    }

    private static Bitmap getResizedBitmap(Bitmap bm, int newWidth){
        int width = bm.getWidth();
        float scale = newWidth/width;
        return  rescaleBitmap(bm, scale);
    }

    private static Bitmap rescaleBitmap(Bitmap bitmap, float scale){
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        return Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
    }

    private static boolean checkCameraPermission(AppCompatActivity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
            return false;
        }
        return true;
    }
}
