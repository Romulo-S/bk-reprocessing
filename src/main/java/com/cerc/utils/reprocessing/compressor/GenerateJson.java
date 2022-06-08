package com.cerc.utils.reprocessing.compressor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.cerc.utils.reprocessing.models.PayloadWrapper;
import com.cerc.utils.reprocessing.models.PubSubMessage;
import com.cerc.utils.reprocessing.utils.Compressor;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.TableResult;

public class GenerateJson {

    private PayloadWrapper payloadWrapper;

    private ArrayList<String> payload;

    public GenerateJson(ArrayList<String> queryRows) {
        this.payloadWrapper = queryRows;
    }

    public void convertToJson(TableResult data, PubSubMessage message) throws ParseException {

        for (FieldValueList row : data.iterateAll()) {
            payloadWrapper = new PayloadWrapper();

            payloadWrapper.setTransactionType(message.getTransactionType());

            String contract = row.get("contract").getStringValue();
            try {
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(contract);
            } catch (Exception e) {
                byte[] compress = Compressor.compress(contract.getBytes(), 500);
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(compress.toString());
            }

            String requester = row.get("requester").getStringValue();
            int requesterTransactionId = row.get("requesterTransactionId").getNumericValue().intValue();

            String receivedAt = LocalDate.now().toString();

            UUID uuid = UUID.randomUUID();
            String uuidAsString = uuid.toString();

            //Save file validation
        }
    }

    public ArrayList<String> getPayload() {
        return payloadWrapper;
    }

    public void setPayload(ArrayList<String> payload) {
        this.payloadWrapper = payload;
    }

}
