package com.cerc.utils.reprocessing.compressor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.cerc.utils.reprocessing.models.Contract;
import com.cerc.utils.reprocessing.models.PubSubMessage;
import com.cerc.utils.reprocessing.utils.Compressor;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.TableResult;

public class GenerateJson {

    private static final org.jboss.logging.Logger LOG = org.jboss.logging.Logger.getLogger(GenerateJson.class);
    private Contract contract;
    private List<Contract> contracts;

    public GenerateJson() {
        this.contracts = new ArrayList<>();
    }

    public List<Contract> getContractsToReprocess(TableResult data, PubSubMessage message) throws ParseException {

        for (FieldValueList row : data.iterateAll()) {
            contract = new Contract();
            contract.setTransactionType(message.getTransactionType());

            extractContractJson(row);

            String requester = row.get("requester").getStringValue();
            contract.setRequester(requester);

            int requesterTransactionId = row.get("requesterTransactionId").getNumericValue().intValue();
            contract.setRequesterTransactionId(requesterTransactionId);

            String receivedAt = LocalDate.now().toString();
            contract.setReceivedAt(receivedAt);

            UUID uuid = UUID.randomUUID();
            contract.setId(uuid);

            contracts.add(contract);
        }
        return contracts;
    }

    public void saveContractToFile(){

    }
    private void extractContractJson(FieldValueList row) throws ParseException {
        String contract = row.get("contract").getStringValue();
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(contract);
            this.contract.setContract(json);
        }
        catch (ParseException e) {
            LOG.error(e.getMessage());
        }
        catch (Exception e) {
            byte[] compress = Compressor.compress(contract.getBytes(), 500);
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(compress.toString());
            this.contract.setContract(json);
        }
    }


}
