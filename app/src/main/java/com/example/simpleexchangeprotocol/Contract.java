package com.example.simpleexchangeprotocol;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import com.example.simpleexchangeprotocol.data.Image;
import com.example.simpleexchangeprotocol.data.Name;
import com.example.simpleexchangeprotocol.data.Position2D;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class Contract {
    private static final int CREATE_FILE = 2;

    private final com.example.simpleexchangeprotocol.data.Contract info;
    private final List<Image> documentPictures = new ArrayList<>();
    private Image Signature;

    private static Contract current;
    public static Contract CreateInstance(com.example.simpleexchangeprotocol.data.Contract info) {
        com.example.simpleexchangeprotocol.Contract.current = new Contract(info);

        return com.example.simpleexchangeprotocol.Contract.current;
    }
    public static Contract GetCurrent(){
        return com.example.simpleexchangeprotocol.Contract.current;
    }

    public Contract(com.example.simpleexchangeprotocol.data.Contract NewInfo) {
        info = NewInfo;
    }

    public PdfDocument GetPdfPreview(){

        PdfDocument document = new PdfDocument();

        String date = DateFormat.getDateInstance().toString();
        String number = info.Number.toString();
        String partner = String.format("%s, %s" ,info.Partner.FirstName ,info.Partner.LastName);

        document = new PdfDocument();
        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(2480, 3508, 1).create();
        PdfDocument.Page myPage = document.startPage(myPageInfo);

        Paint myPaint = new Paint();

        myPaint.setTextSize(42);

        myPage.getCanvas().drawText("Vertragsnummer: " + number,200,/*upperAnchor + */250,myPaint);//200, 600

        myPage.getCanvas().drawText("Dokumentation über Zustand verfasst am " + date + ".",
                200,/*upperAnchor + */350,myPaint);//200,850

        myPage.getCanvas().drawText("Dokumentations Bilder:",200,/*upperAnchor + */450,myPaint);//200,900

        int PositionCounter = 0;

        try {

            for (Image image:documentPictures) {
                if (image != null){
                    Bitmap resizedPic = image.getResizedBitmap(1200,500);
                    Position2D photoPosition = getPhotoPosition(PositionCounter);
                    myPage.getCanvas().drawBitmap(resizedPic,photoPosition.x,photoPosition.y, myPaint);
                    PositionCounter++;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            //Toast.makeText(this, "Unable to insert Pictures because of: " + e.toString(), Toast.LENGTH_LONG).show();
        }

        try {
            myPage.getCanvas().drawBitmap(Signature.getResizedBitmap(650, 450), 200, /*upperAnchor + */2500, myPaint);
        }catch (Exception e){
            e.printStackTrace();
            //Toast.makeText(this, "Unable to insert Signature because of: " + e.toString(), Toast.LENGTH_LONG).show();
        }
        myPaint.setTextSize(32);

        myPage.getCanvas().drawText(partner + " am " + date,200,/*upperAnchor + */2750,myPaint);//200,3200

        document.finishPage(myPage);

        return document;
    }

    public void AddSignature(Image signature){
        // ToDo: Allow to sign Contract
        Signature = signature;
    }

    public void SaveAsPdf(){
        // ToDo: Save Contract to FileSystem

        PdfDocument document = GetPdfPreview();

        File myFile = new File(Environment.getExternalStorageDirectory().getPath());
        try {
            Uri contractPath = Uri.fromFile(myFile);
            createFile(contractPath);
            //Toast.makeText(this, "Contract saved at: " + contractPath.toString(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            //Toast.makeText(this, "Could not save Pdf because of: " + e.toString(), Toast.LENGTH_SHORT).show();
            myFile.mkdir();
        }
    }

    public List<Image> getPictures() {
        return documentPictures;
    }

    public void AddImage(Image image) {
        documentPictures.add(image);
    }

    public Integer getContractNumber() {
        return info.Number;
    }

    public Name getCreator() {
        return info.Creator;
    }

    public Name getPartner() {
        return info.Partner;
    }

    private static Position2D getPhotoPosition(int photoIndex){
        /*
        Left or Right: Index % 2
        Row: int row = Math.Floor(Index/2)
         */
        return new Position2D(
                (photoIndex % 2) * 1000 + 200,
                (int) Math.floor((double) photoIndex / 2) * 650 + 1100
        );
    }

    private void createFile(Uri pickerInitialUri) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        String contractFileNumber = current.info.Number.toString();
        intent.putExtra(Intent.EXTRA_TITLE, "Vertrag" + contractFileNumber + ".pdf");
        if (contractFileNumber.equals(""))
            intent.putExtra(Intent.EXTRA_TITLE, "SampleContract.pdf");

        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when your app creates the document.
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);

        startActivityForResult(intent, CREATE_FILE);

    }

    private void SavePDF(Uri uri, PdfDocument document) {
        try {
            ParcelFileDescriptor pfd =
                    getContentResolver().openFileDescriptor(uri, "w");
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
