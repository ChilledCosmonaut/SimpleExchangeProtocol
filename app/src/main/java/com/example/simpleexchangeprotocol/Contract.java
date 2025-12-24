package com.example.simpleexchangeprotocol;

import android.graphics.Bitmap;

public class Contract {

    private final ContractInfo info;
    private final Bitmap[] documentPictures = new Bitmap[6];

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

    public void Sign(){
        // ToDo: Allow to sign Contract
    }

    public void WriteOut(){
        // ToDo: Save Contract to FileSystem
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
