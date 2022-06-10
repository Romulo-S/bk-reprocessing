package com.cerc.utils.reprocessing.controllers;

import com.cerc.utils.reprocessing.bigquery.BigQueryCase;
import com.cerc.utils.reprocessing.compressor.GenerateJson;
import com.cerc.utils.reprocessing.models.Payload;
import com.cerc.utils.reprocessing.models.PubSubMessage;
import com.cerc.utils.reprocessing.pubsub.consumers.PubSubConsumer;
import com.google.cloud.bigquery.TableResult;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReprocessingCaseImpl implements ReprocessingCase{

    @Inject
    private BigQueryCase bigQueryCase;

    @Inject
    private GenerateJson generateJson;

    @Override
    public void searchAndSend(ArrayList<String> referecesSlot, PubSubMessage message, PubSubConsumer projectId) {

        try {
            TableResult data = bigQueryCase.getData();

            List<Payload> contractsToReprocess = generateJson.getContractsToReprocess(data, message);

            try {
                JSONObject temp = new JSONObject();
                projectId.pubsub(temp);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        } catch (InterruptedException | ParseException e) {
            e.printStackTrace();
        }

    }
}
