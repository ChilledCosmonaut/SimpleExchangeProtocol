package com.example.simpleexchangeprotocol.functionality;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;

import com.example.simpleexchangeprotocol.PaintView;
import com.example.simpleexchangeprotocol.data.Contract;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

public class PdfGenerator {
    public static PdfDocument createMyPDF(Activity activity, Contract contract, PaintView view) throws IOException {

        String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String number = contract.Number.toString();
        String partner = contract.Partner.firstName + "," + contract.Partner.lastName;

        PdfDocument myPdfDocument = new PdfDocument();
        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(2480, 3508, 1).create();
        PdfDocument.Page myPage = myPdfDocument.startPage(myPageInfo);

        Paint myPaint = new Paint();

        InputStream headerInput = activity.getAssets().open("biefkopfcutout.png");
        Bitmap headerCache = BitmapFactory.decodeStream(headerInput);
        Bitmap header = PictureHandler.getResizedBitmap(headerCache, 2040);
        int upperAnchor = header.getHeight();

        myPage.getCanvas().drawBitmap(header,200,100, myPaint); //200, 100

        myPaint.setTextSize(42);

        myPage.getCanvas().drawText("Vertragsnummer: " + number,200,upperAnchor + 250,myPaint);//200, 600

        myPage.getCanvas().drawText("Dokumentation über Zustand verfasst am " + date + ".",
                200,upperAnchor + 350,myPaint);//200,850

        myPage.getCanvas().drawText("Dokumentations Bilder:",200,upperAnchor + 450,myPaint);//200,900

        int PositionCounter = 0;

        for (String imagePath:contract.Images) {
            Bitmap Image = BitmapFactory.decodeFile(imagePath);
            if (Image != null) {
                Bitmap resizedPic = PictureHandler.getResizedBitmap(Image, 1200, 500);
                Vector<Integer> photoPosition = getPhotoPosition(PositionCounter);
                myPage.getCanvas().drawBitmap(resizedPic, photoPosition.get(0), photoPosition.get(1), myPaint);
                PositionCounter++;
            }
        }

        myPage.getCanvas().drawBitmap(PictureHandler.getResizedBitmap(view.getmBitmap(), 650, 450), 200, /*upperAnchor + */2500, myPaint);

        myPaint.setTextSize(32);

        myPage.getCanvas().drawText(partner + " am " + date,200,upperAnchor + 2750,myPaint);//200,3200

        myPdfDocument.finishPage(myPage);

        return  myPdfDocument;
    }

    private static Vector<Integer> getPhotoPosition(int photoIndex){
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
