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
        PdfDocument.Page currentPage = myPdfDocument.startPage(myPageInfo);

        Paint myPaint = new Paint();

        int maxContentWidth = 2480 - 2 * 200;

        InputStream headerInput = activity.getAssets().open("autovermietung_header.png");
        Bitmap headerCache = BitmapFactory.decodeStream(headerInput);
        Bitmap header = PictureHandler.getResizedBitmap(headerCache, 2040);
        int headerHeight = header.getHeight();

        InputStream footerInput = activity.getAssets().open("autovermietung_footer.png");
        Bitmap footerCache = BitmapFactory.decodeStream(footerInput);
        Bitmap footer = PictureHandler.getResizedBitmap(footerCache, 2040);
        int footerHeight = footer.getHeight();

        int usablePageSize = 3508 - headerHeight - footerHeight; // Add proper parameterized padding!

        int leftPageSize = usablePageSize;

        int pagePointer = headerHeight;

        currentPage.getCanvas().drawBitmap(header,200,100, myPaint); //200, 100

        myPaint.setTextSize(42);

        currentPage.getCanvas().drawText("Vertragsnummer: " + number,200,headerHeight + 250,myPaint);//200, 600

        currentPage.getCanvas().drawText("Dokumentation über Zustand verfasst am " + date + ".",
                200,headerHeight + 350,myPaint);//200,850

        currentPage.getCanvas().drawText("Dokumentations Bilder:",200,headerHeight + 450,myPaint);//200,900

        pagePointer += 500;

        int PositionCounter = 0;

        for (String imagePath:contract.Images) {
            Bitmap Image = BitmapFactory.decodeFile(imagePath);
            if (Image == null) {
                continue;
            }

            if (leftPageSize <= 500) {

                currentPage.getCanvas().drawBitmap(footer,200,3508-footerHeight-100, myPaint); //200, 100
                myPdfDocument.finishPage(currentPage);

                myPageInfo = new PdfDocument.PageInfo.Builder(2480, 3508, 1).create();
                currentPage = myPdfDocument.startPage(myPageInfo);
                currentPage.getCanvas().drawBitmap(header,200,100, myPaint); //200, 100

                leftPageSize = usablePageSize;
                pagePointer = headerHeight;
            }

            Bitmap resizedPic = PictureHandler.getResizedBitmap(Image, 1200, 500);
            currentPage.getCanvas().drawBitmap(resizedPic, (PositionCounter % 2) * 1000 + 200, pagePointer, myPaint);
            PositionCounter++;
            if (PositionCounter % 2 == 0) pagePointer += 650;
            leftPageSize -= 500;
        }
        if (PositionCounter % 2 == 1) pagePointer += 650;

        myPaint.setTextSize(32);

        currentPage.getCanvas().drawText(partner + " am " + date ,200 ,pagePointer ,myPaint);//200,3200
        pagePointer += 100;

        currentPage.getCanvas().drawBitmap(PictureHandler.getResizedBitmap(view.getmBitmap(), 650, 600), 200, /*upperAnchor + */pagePointer, myPaint);

        currentPage.getCanvas().drawBitmap(footer,200,3508-footerHeight-100, myPaint); //200, 100
        myPdfDocument.finishPage(currentPage);

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
