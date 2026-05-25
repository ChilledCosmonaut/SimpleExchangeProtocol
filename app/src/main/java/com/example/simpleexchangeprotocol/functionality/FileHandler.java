package com.example.simpleexchangeprotocol.functionality;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.simpleexchangeprotocol.data.Contract;

import java.io.FileOutputStream;
import java.io.IOException;

public class FileHandler {
    public static final int CREATE_FILE = 2;
    public static final int STORAGE_PERMISSION_CODE = 1;

    private static boolean checkStoragePermission(AppCompatActivity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PackageManager.PERMISSION_GRANTED);
            return false;
        }
        return true;
    }

    public static void createFile(Activity activity, Uri pickerInitialUri, Contract contract) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        String contractFileNumber = contract.Number.toString();
        if (!contractFileNumber.isEmpty())
            intent.putExtra(Intent.EXTRA_TITLE, "Vertrag" + contractFileNumber + ".pdf");
        else
            intent.putExtra(Intent.EXTRA_TITLE, "SampleContract.pdf");
        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when your app creates the document.
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);

        startActivityForResult(activity, intent, CREATE_FILE, null);

    }

    public static void SavePDF(Activity activity, Uri uri, PdfDocument document) {
        try {
            ParcelFileDescriptor pfd =
                    activity.getContentResolver().openFileDescriptor(uri, "w");
            assert pfd != null;
            FileOutputStream fileOutputStream =
                    new FileOutputStream(pfd.getFileDescriptor());
            document.writeTo(fileOutputStream);
            //fileOutputStream.write(("Overwritten at " + System.currentTimeMillis() +"\n").getBytes());
            // Let the document provider know you're done by closing the stream.
            fileOutputStream.close();
            pfd.close();
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
