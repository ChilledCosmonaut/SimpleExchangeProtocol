package com.example.simpleexchangeprotocol;

import android.graphics.Bitmap;

public class ContractStorage {

    private static Bitmap[] documentPictures = new Bitmap[6];

    private static String contractNumber, partnerFirst, partnerSecond;

    public static Bitmap[] getDocumentPictures() {
        return documentPictures;
    }

    public static void setDocumentPictures(Bitmap[] documentPictures) {
        ContractStorage.documentPictures = documentPictures;
    }

    public static String getContractNumber() {
        return contractNumber;
    }

    public static void setContractNumber(String contractNumber) {
        ContractStorage.contractNumber = contractNumber;
    }

    public static String getPartnerFirst() {
        return partnerFirst;
    }

    public static void setPartnerFirst(String partnerFirst) {
        ContractStorage.partnerFirst = partnerFirst;
    }

    public static String getPartnerSecond() {
        return partnerSecond;
    }

    public static void setPartnerSecond(String partnerSecond) {
        ContractStorage.partnerSecond = partnerSecond;
    }
}
