package com.cerc.utils.reprocessing.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.parser.ParseException;

import com.cerc.utils.reprocessing.bigquery.BigQueryCaseImpl;
import com.cerc.utils.reprocessing.models.Payload;
import com.cerc.utils.reprocessing.models.PubSubMessage;
import com.cerc.utils.reprocessing.pubsub.consumers.PubSubConsumer;
import com.cerc.utils.reprocessing.service.GenerateJson;
import com.google.cloud.bigquery.TableResult;

public class ReprocessingCaseImpl implements ReprocessingCase{

    BigQueryCaseImpl bigQueryCase = new BigQueryCaseImpl();
    GenerateJson generateJson = new GenerateJson();

    @Override
    public void searchAndSend(ArrayList<String> referecesSlot, PubSubMessage message, PubSubConsumer projectId) {

        try {
            TableResult data = bigQueryCase.getData();

            List<Payload> contractsToReprocess = generateJson.getContractsToReprocess(data, message);

            try {
                projectId.pubsub(contractsToReprocess);
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
