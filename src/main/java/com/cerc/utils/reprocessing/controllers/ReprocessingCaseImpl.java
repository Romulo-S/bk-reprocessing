package com.cerc.utils.reprocessing.controllers;

import com.cerc.utils.reprocessing.bigquery.BigQueryCase;
import com.cerc.utils.reprocessing.models.PubSubMessage;

import javax.inject.Inject;
import java.util.ArrayList;

public class ReprocessingCaseImpl implements ReprocessingCase{

    @Inject
    private BigQueryCase bigQueryCase;

    @Override
    public void searchAndSend(ArrayList<String> referecesSlot, PubSubMessage message) {

        try {
            bigQueryCase.getData();

            //TODO insercao de compressao

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
