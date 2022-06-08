package com.cerc.utils.reprocessing.compressor;

import java.util.ArrayList;

import javax.inject.Inject;

import com.google.gson.Gson;

import model.ContractPayload;

public class PayloadCompressor {

    private ContractPayload contractPayload;

    private ArrayList<String> payload;

    public PayloadCompressor(ArrayList<String> queryRows) {
        this.payload = queryRows;
    }

    public void convertToJson(){
        for (String row : payload) {
            contractPayload = new ContractPayload();

        }
    }

//    public Gson getJson(String){
//
//    }
    public ArrayList<String> getPayload() {
        return payload;
    }

    public void setPayload(ArrayList<String> payload) {
        this.payload = payload;
    }


}
