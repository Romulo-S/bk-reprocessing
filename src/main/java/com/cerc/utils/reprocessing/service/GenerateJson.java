package com.cerc.utils.reprocessing.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.simple.parser.ParseException;

import com.cerc.utils.reprocessing.models.Contract;
import com.cerc.utils.reprocessing.models.Payload;
import com.cerc.utils.reprocessing.models.PubSubMessage;
import com.cerc.utils.reprocessing.utils.Compressor;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.TableResult;
import com.google.gson.Gson;

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

            LOG.info("ContractReference: " + payload.getContract()
                .getReference() + "RequesterTransactionId: " + payload.getRequesterTransactionId() + "TransactionId: " + payload.getId());
            payloads.add(payload);
        }
        return payloads;
    }


    private void extractContractJson(FieldValueList row) {
        String contract = row.get("contract").getStringValue();
        Gson g = new Gson();
        try {
            Contract contractJson = g.fromJson(contract, Contract.class);
            this.payload.setContract(contractJson);
        } catch (Exception e) {
            byte[] compressedJsonContract = Compressor.compress(contract.getBytes(), 500);
            Contract contractJson = g.fromJson(compressedJsonContract.toString(), Contract.class);
            this.payload.setContract(contractJson);
        }
    }

}
