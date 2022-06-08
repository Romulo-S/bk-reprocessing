package com.cerc.utils.reprocessing.controllers;

import com.cerc.utils.reprocessing.bigquery.BigQueryCase;
import com.cerc.utils.reprocessing.compressor.GenerateJson;
import com.cerc.utils.reprocessing.models.PubSubMessage;
import com.google.cloud.bigquery.TableResult;

import javax.inject.Inject;
import java.util.ArrayList;

public class ReprocessingCaseImpl implements ReprocessingCase{

    @Inject
    private BigQueryCase bigQueryCase;

    @Inject
    private GenerateJson generateJson;

    @Override
    public void searchAndSend(ArrayList<String> referecesSlot, PubSubMessage message) {

        try {
            TableResult data = bigQueryCase.getData();
            generateJson.convertToJson(data,message);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
