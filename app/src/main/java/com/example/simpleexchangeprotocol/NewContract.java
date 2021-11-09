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
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class NewContract extends AppCompatActivity {

    private ImageView[] imageView = new ImageView[6];
    private int[][] position = new int[6][2];
    private File photoFile;
    private static final int REQUEST_CODE = 1;
    private static final int CREATE_FILE = 2;
    private Bitmap header, footer;
    private Bitmap[] documentPictures = new Bitmap[6];
    private int picCount = 0;
    private PaintView paintView;
    private int STORAGE_PERMISSION_CODE = 1;
    private EditText contractNumber, partnerFirst, partnerSecond;
    private PdfDocument myPdfDocument;
    private int newWidth = 1200;
    private int newHeight = 500;
    private int upperAnchor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_contract);
        for(int imageCounter = 0; imageCounter < imageView.length; imageCounter++) {
            imageView[imageCounter] = findViewById(R.id.imageView);

            if(ContractStorage.getDocumentPictures()[imageCounter] != null)
                imageView[imageCounter].setImageBitmap(ContractStorage.getDocumentPictures()[imageCounter]);
        }
        paintView = findViewById(R.id.paintView);
        DisplayMetrics displayMetrics = new DisplayMetrics();

        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        paintView.initialise(displayMetrics);

        position[0][0] = 200;
        position[0][1] = 1100;
        position[1][0] = 1200;
        position[1][1] = 1100;
        position[2][0] = 200;
        position[2][1] = 1750;
        position[3][0] = 1200;
        position[3][1] = 1750;
        position[4][0] = 200;
        position[4][1] = 2400;
        position[5][0] = 1200;
        position[5][1] = 2400;

        contractNumber = findViewById(R.id.ContractNumberInput);
        contractNumber.setText(ContractStorage.getContractNumber());
        partnerFirst = findViewById(R.id.ContractPartnerFirstName);
        partnerFirst.setText(ContractStorage.getPartnerFirst());
        partnerSecond = findViewById(R.id.ContractPartnerName);
        partnerSecond.setText(ContractStorage.getPartnerSecond());

        try {
            InputStream headerInput = getAssets().open("biefkopfcutout.png");
            Bitmap headerCache = BitmapFactory.decodeStream(headerInput);
            header = enlargeBitmap(headerCache, 2040);
            upperAnchor = header.getHeight();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ActivityCompat.requestPermissions(NewContract.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PackageManager.PERMISSION_GRANTED);

        Toast.makeText(this, "Created new Activity", Toast.LENGTH_LONG).show();
    }

    //Saves Pictures when destroyed
    @Override
    protected void onDestroy(){
        super.onDestroy();
        for (int i = 0; i < documentPictures.length; i++){
            ContractStorage.setDocumentPicturesAtIndex(documentPictures[i],i);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            //Bitmap takenImage = (Bitmap) data.getExtras().get("data");
            Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            imageView[picCount].setImageBitmap(takenImage);
            documentPictures[picCount] = takenImage;
            picCount++;
        } else if (requestCode == CREATE_FILE && resultCode == Activity.RESULT_OK) {
            Uri PDFPath = data.getData();
            System.out.println(PDFPath);
            SavePDF(PDFPath);
        }else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void takePicture(View view) throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String FILE_NAME = "photo" + picCount + ".jpg";
        photoFile = getPhotoFile(FILE_NAME);

        Uri fileProvider = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            System.out.println("Camera Exists");
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

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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

        String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        String number = contractNumber.getText().toString();
        String partner = partnerFirst.getText().toString() + "," + partnerSecond.getText().toString();

        myPdfDocument = new PdfDocument();
        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(2480, 3508, 1).create();
        PdfDocument.Page myPage = myPdfDocument.startPage(myPageInfo);

        Paint myPaint = new Paint();

        myPage.getCanvas().drawBitmap(header,200,100, myPaint); //200, 100

        myPaint.setTextSize(42);

        myPage.getCanvas().drawText("Vertragsnummer: " + number,200,upperAnchor + 250,myPaint);//200, 600

        myPage.getCanvas().drawText("Dokumentation über Zustand verfasst am " + date + ".",
                200,upperAnchor + 350,myPaint);//200,850

        myPage.getCanvas().drawText("Dokumentations Bilder:",200,upperAnchor + 450,myPaint);//200,900

        int PositionCounter = 0;

        for (Bitmap PdfPic:documentPictures) {
            if (PdfPic != null){
                Bitmap resizedPic = getProperlySizedBitmap(PdfPic,1200,500);
                myPage.getCanvas().drawBitmap(resizedPic,position[PositionCounter][0],position[PositionCounter][1], myPaint);
                PositionCounter++;
            }
        }

        myPage.getCanvas().drawBitmap(getProperlySizedBitmap(paintView.getmBitmap(),650,450),200,upperAnchor + 2500,myPaint);

        myPaint.setTextSize(32);

        myPage.getCanvas().drawText(partner + " am " + date,200,upperAnchor + 2750,myPaint);//200,3200

        myPdfDocument.finishPage(myPage);

        ContextWrapper cw = new ContextWrapper(getApplicationContext());

        //File myFilePath = cw.getDir("documentDir", Context.MODE_PRIVATE);
        //Environment.getExternalStorageDirectory().getPath() + "/myPDFFile.pdf";
        File myFile = new File(Environment.getExternalStorageDirectory().getPath());
        try {
            createFile(Uri.fromFile(myFile));
            //Toast.makeText(this, myFile.toString()/*myFilePath*/, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println( "Could not save Pdf because of: " + e.toString());
            myFile.mkdir();
        }
    }

    private void createFile(Uri pickerInitialUri) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        String contractFileNumber = contractNumber.getText().toString();
        if (!contractFileNumber.equals(""))
            intent.putExtra(Intent.EXTRA_TITLE, "Vertrag" + contractFileNumber + ".pdf");
        else
            intent.putExtra(Intent.EXTRA_TITLE, "SampleContract.pdf");
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

    private void SavePDF(Uri uri) {
        try {
            ParcelFileDescriptor pfd =
                    getContentResolver().openFileDescriptor(uri, "w");
            FileOutputStream fileOutputStream =
                    new FileOutputStream(pfd.getFileDescriptor());
            myPdfDocument.writeTo(fileOutputStream);
            //fileOutputStream.write(("Overwritten at " + System.currentTimeMillis() +"\n").getBytes());
            // Let the document provider know you're done by closing the stream.
            fileOutputStream.close();
            pfd.close();
            myPdfDocument.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Bitmap getProperlySizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        System.out.println(width + "," + height);
        float resolution = ((float)width)/height;
        System.out.println(resolution);
        float resWidth = resolution * newHeight;
        System.out.println(resWidth);
        float futureWidth, futureHeight;
        if (resWidth > newWidth){
            futureHeight = resWidth / resolution;
            futureWidth = newWidth;
            System.out.println("Width bigger than " + newWidth);
        }else{
            futureHeight = newHeight;
            futureWidth = resWidth;
        }
        System.out.println(futureWidth + "," + futureHeight);
        return rescaleBitmap(bm, futureWidth,futureHeight,width,height);
    }

    private Bitmap enlargeBitmap(Bitmap bm, int newWidth){
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = newWidth/width;
        float newHeight = height * scaleWidth;
        return  rescaleBitmap(bm,newWidth,newHeight,width,height);
    }

    private Bitmap rescaleBitmap(Bitmap bm, float newWidth, float newHeight, int width, int height){
        float scaleWidth = newWidth / width;
        float scaleHeight = newHeight / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
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

    public void CreatePDF(View view) throws IOException {

        this.Save();

        PdfDocument document = new PdfDocument();

        Paint picturePaint = new Paint();
        Paint textPaint = new Paint();

        int height = 3508;
        int width = 2480;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width, height,1).create();

        PdfDocument.Page page = document.startPage(pageInfo);

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
    }
}