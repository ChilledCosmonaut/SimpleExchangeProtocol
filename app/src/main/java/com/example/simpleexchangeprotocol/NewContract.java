package com.example.simpleexchangeprotocol;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class NewContract extends AppCompatActivity {

    private ImageView[] imageView = new ImageView[6];
    private File photoFile;
    private static int REQUEST_CODE = 1;
    private Bitmap header, footer;
    private ArrayList<Bitmap> documentPictures = new ArrayList<>();
    private int picCount = 0;
    private PaintView paintView;
    private int STORAGE_PERMISSION_CODE = 1;

    private String myEditText = "Hello there /nGeneral Kenobi!";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_contract);
        imageView[0] = findViewById(R.id.imageView);
        imageView[1] = findViewById(R.id.imageView1);
        imageView[2] = findViewById(R.id.imageView2);
        imageView[3] = findViewById(R.id.imageView3);
        imageView[4] = findViewById(R.id.imageView4);
        imageView[5] = findViewById(R.id.imageView5);
        paintView = findViewById(R.id.paintView);
        DisplayMetrics displayMetrics = new DisplayMetrics();

        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        paintView.initialise(displayMetrics);

        ActivityCompat.requestPermissions(NewContract.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PackageManager.PERMISSION_GRANTED);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            //Bitmap takenImage = (Bitmap) data.getExtras().get("data");
            Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            imageView[picCount].setImageBitmap(takenImage);
            documentPictures.add(takenImage);
            picCount++;
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void takePicture(View view) throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String FILE_NAME = "photo" + picCount + ".jpg";
        photoFile = getPhotoFile(FILE_NAME);

        Uri fileProvider = FileProvider.getUriForFile(this, "com.example.fileprovider", photoFile);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_CODE);
        } else {
            Toast.makeText(this, "Unable to open camera", Toast.LENGTH_SHORT).show();
        }
    }

    private File getPhotoFile(String fileName) throws IOException {
        //Use 'getExternaFilesDir' on Context to access package-specific directories.
        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(fileName, ".jpg", storageDirectory);
    }

    private void requestStoragePermission() {

        System.out.println("Request Permisssion");

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("Needed to save image")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            ActivityCompat.requestPermissions(NewContract.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);

                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();

                        }

                    })
                    .create().show();

        } else {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == STORAGE_PERMISSION_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "Access granted", Toast.LENGTH_LONG).show();

            } else {

                Toast.makeText(this, "Access denied", Toast.LENGTH_LONG).show();

            }

        }

    }

    public void ClearDrawingPad(View view) {
        paintView.clear();
    }

    public void createMyPDF(View view) {

        PdfDocument myPdfDocument = new PdfDocument();
        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(2480, 3508, 1).create();
        PdfDocument.Page myPage = myPdfDocument.startPage(myPageInfo);

        Paint myPaint = new Paint();

        //myPage.getCanvas().drawBitmap(imageView[1],);

        myPdfDocument.finishPage(myPage);

        ContextWrapper cw = new ContextWrapper(getApplicationContext());

        File myFilePath = cw.getDir("documentDir", Context.MODE_PRIVATE);//Environment.getExternalStorageDirectory().getPath() + "/myPDFFile.pdf";
        File myFile = new File(myFilePath,"Contract" + ".pdf");
        try {
            myPdfDocument.writeTo(new FileOutputStream(myFile));
            Toast.makeText(this,"Pdf saved at: " + myFilePath, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Could not save Pdf because of: " + e.toString(), Toast.LENGTH_LONG).show();
            myFile.mkdir();
        }

        myPdfDocument.close();

        try {
            //openPdf(myFile);
            openFolder(myFilePath.toString());
        }catch (Exception e){
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG);
        }
    }

    private void openPdf(File fileToOpen) {

        //File file = new File("mnt/sdcard.test.pdf");
        Uri path = Uri.fromFile(fileToOpen);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(path);
        intent.setType("application/pdf");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No application found",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void openFolder(String location)
    {
        // location = "/sdcard/my_folder";
        Toast.makeText(this, "Trying to Open Folder", Toast.LENGTH_LONG);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri mydir = Uri.parse("file://"+location);
        intent.setDataAndType(mydir,"application/*");    // or use */*
        startActivity(intent);
    }

    /*public void CreatePDF(View view) throws IOException {

        this.Save();

        PdfDocument document = new PdfDocument();

        Paint picturePaint = new Paint();
        Paint textPaint = new Paint();

        int height = 3508;
        int width = 2480;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width, height,1).create();

        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();

        //canvas.drawBitmap(header,0,0,picturePaint);

        //canvas.drawBitmap(documentPictures.get(0),width/2,height/2,picturePaint);

        document.finishPage(page);

        int pdfCount = 0;
        String filename = "/Vertrag" + pdfCount + ".pdf";

        File file = getExternalFilesDir(Environment.DIRECTORY_PICTURES + "Contract" + pdfCount + ".pdf");

        document.close();

        try {

            document.writeTo(new FileOutputStream(file));

        } catch (IOException e){

            e.printStackTrace();
            Toast.makeText(this, "Saving failed", Toast.LENGTH_LONG);

        }



        File pdffile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +"/"+ filename);
        Intent target = new Intent(Intent.ACTION_VIEW);
        target.setDataAndType(Uri.fromFile(file),"application/pdf");
        target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        Intent intent = Intent.createChooser(target, "Open File");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
           Toast.makeText(this, "Please install pdfViewer", Toast.LENGTH_LONG).show();
        }
    }*/

    private static final int CREATE_FILE = 1;

    private void createFile(File pickerInitialUri) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, "invoice.pdf");

        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when your app creates the document.
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);

        startActivityForResult(intent, CREATE_FILE);

    }


        private void Save () {

        System.out.println("Trying to save");

        if (ContextCompat.checkSelfPermission(NewContract.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            System.out.println("Request permission");
            requestStoragePermission();
        }

        paintView.saveImage();

        /*Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);*/

    }
}