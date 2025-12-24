package com.example.simpleexchangeprotocol;

import com.example.simpleexchangeprotocol.structs.ContractInfo;

import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.text.DateFormat;
import java.util.Vector;

public class Contract {

    private final ContractInfo info;
    private final Bitmap[] documentPictures = new Bitmap[6];

    private PdfDocument document;

    private static Contract current;
    public static Contract CreateInstance(ContractInfo info) {
        Contract.current = new Contract(info);

        return Contract.current;
    }
    public static Contract GetCurrent(){
        return Contract.current;
    }

    public Contract(ContractInfo NewInfo) {
        info = NewInfo;
    }

    public PdfDocument GetDocument(){

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

            for (Bitmap PdfPic:documentPictures) {
                if (PdfPic != null){
                    Bitmap resizedPic = getProperlySizedBitmap(PdfPic,1200,500);
                    Vector<Integer> photoPosition = getPhotoPosition(PositionCounter);
                    myPage.getCanvas().drawBitmap(resizedPic,photoPosition.get(0),photoPosition.get(1), myPaint);
                    PositionCounter++;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, "Unable to insert Pictures because of: " + e.toString(), Toast.LENGTH_LONG).show();
        }

        try {
            myPage.getCanvas().drawBitmap(getProperlySizedBitmap(paintView.getmBitmap(), 650, 450), 200, /*upperAnchor + */2500, myPaint);
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, "Unable to insert Signature because of: " + e.toString(), Toast.LENGTH_LONG).show();
        }
        myPaint.setTextSize(32);

        myPage.getCanvas().drawText(partner + " am " + date,200,/*upperAnchor + */2750,myPaint);//200,3200

        document.finishPage(myPage);


        return document;
    }

    public void Sign(){
        // ToDo: Allow to sign Contract
    }

    public void WriteOut(){
        // ToDo: Save Contract to FileSystem

        new ContextWrapper(getApplicationContext());


        File myFile = new File(Environment.getExternalStorageDirectory().getPath());
        try {
            Uri contractPath = Uri.fromFile(myFile);
            createFile(contractPath);
            //Toast.makeText(this, "Contract saved at: " + contractPath.toString(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Could not save Pdf because of: " + e.toString(), Toast.LENGTH_SHORT).show();
            myFile.mkdir();
        }
    }

    public Bitmap[] getPictures() {
        return documentPictures;
    }

    public void setPictureAtIndex(Bitmap documentPicture, int index) {
        documentPictures[index] = documentPicture;
    }

    public Integer getContractNumber() {
        return info.Number;
    }

    public String getCreator() {
        return info.Creator;
    }

    public String getPartner() {
        return info.Partner;
    }
}
