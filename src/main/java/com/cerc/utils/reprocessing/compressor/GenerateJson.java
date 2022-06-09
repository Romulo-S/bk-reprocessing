package com.cerc.utils.reprocessing.compressor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.cerc.utils.reprocessing.models.Payload;
import com.cerc.utils.reprocessing.models.PubSubMessage;
import com.cerc.utils.reprocessing.utils.Compressor;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.TableResult;

public class GenerateJson {

    private static final org.jboss.logging.Logger LOG = org.jboss.logging.Logger.getLogger(GenerateJson.class);
    private Payload payload;
    private List<Payload> payloads;

    private final String BASE_PATH = "./payloads/";

    public GenerateJson() {
        this.payloads = new ArrayList<>();
    }

    public List<Payload> getContractsToReprocess(TableResult data, PubSubMessage message) throws ParseException {

        for (FieldValueList row : data.iterateAll()) {
            payload = new Payload();
            payload.setTransactionType(message.getTransactionType());

            extractContractJson(row);

            String requester = row.get("requester").getStringValue();
            payload.setRequester(requester);

            int requesterTransactionId = row.get("requesterTransactionId").getNumericValue().intValue();
            payload.setRequesterTransactionId(requesterTransactionId);

            String receivedAt = LocalDate.now().toString();
            payload.setReceivedAt(receivedAt);

            UUID uuid = UUID.randomUUID();
            payload.setId(uuid);

            payloads.add(payload);
        }
        return payloads;
    }

public void publish(){

}

    private void extractContractJson(FieldValueList row) throws ParseException {
        String contract = row.get("contract").getStringValue();
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(contract);
            this.payload.setContract(json);
        } catch (ParseException e) {
            LOG.error(e.getMessage());
        } catch (Exception e) {
            byte[] compress = Compressor.compress(contract.getBytes(), 500);
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(compress.toString());
            this.payload.setContract(json);
        }
    }

}
