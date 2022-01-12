package com.example.simpleexchangeprotocol;

import android.Manifest;
import android.app.Activity;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Paint;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class NewContract extends AppCompatActivity {

    private final ImageView[] imageView = new ImageView[6];
    private File photoFile;
    private static final int REQUEST_CODE = 1;
    private static final int CREATE_FILE = 2;
    private Bitmap header, footer;
    private Bitmap[] documentPictures = new Bitmap[6];
    private int picCount = 0;
    private PaintView paintView;
    private final int STORAGE_PERMISSION_CODE = 1;
    private EditText contractNumber, partnerFirst, partnerSecond;
    private PdfDocument myPdfDocument;
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

        /*
        Left or Right: Index % 2
        Row: int row = Math.Floor(Index/2)
         */

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
                Vector<Integer> photoPosition = getPhotoPosition(PositionCounter);
                myPage.getCanvas().drawBitmap(resizedPic,photoPosition.get(0),photoPosition.get(1), myPaint);
                PositionCounter++;
            }
        }

        myPage.getCanvas().drawBitmap(getProperlySizedBitmap(paintView.getmBitmap(),650,450),200,upperAnchor + 2500,myPaint);

        myPaint.setTextSize(32);

        myPage.getCanvas().drawText(partner + " am " + date,200,upperAnchor + 2750,myPaint);//200,3200

        myPdfDocument.finishPage(myPage);

        new ContextWrapper(getApplicationContext());

        File myFile = new File(Environment.getExternalStorageDirectory().getPath());
        try {
            createFile(Uri.fromFile(myFile));
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

        //startActivityForResult(intent, CREATE_FILE);

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
        return Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
    }

    public static Vector<Integer> getPhotoPosition(int photoIndex){
        /*
        Left or Right: Index % 2
        Row: int row = Math.Floor(Index/2)
         */
        Vector<Integer> photoPosition = new Vector<>();

        photoPosition.add(0, (photoIndex % 2) * 1000 + 200);
        photoPosition.add(1, (int) Math.floor(photoIndex / 2) * 650 + 1100);

        return photoPosition;
    }

}