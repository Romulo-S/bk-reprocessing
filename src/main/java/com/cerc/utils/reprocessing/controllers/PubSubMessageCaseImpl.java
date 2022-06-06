package com.cerc.utils.reprocessing.controllers;

import com.cerc.utils.reprocessing.models.PubSubMessage;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

public class PubSubMessageCaseImpl implements PubSubMessageCase {

    private static final Logger LOG = Logger.getLogger(PubSubMessageCaseImpl.class);

    private String topic;
    private int slot;
    private ArrayList<String> referecesSlot = new ArrayList<>();

    String[] typeAllowed = {"UPDATE", "CREATE", "FINISH", "INACTIVATE"};

    @Inject
    private ReprocessingCase reprocessingCase;


    @Override
    public void reprocess(PubSubMessage message) throws IOException {

        if(message.getTopic() != null){
            topic = "prioritizer.new";
        }

        if (!Arrays.asList(typeAllowed).contains(message.getTransactionType())) {
            LOG.infov("Wrong Type >> ", message.getTransactionType());
            return;
        }

        for(int i = 0; i < message.getContracts().length; i++){
            referecesSlot.add(message.getContracts()[i]);
            slot++;
            if(slot == 10 || i == message.getContracts().length-1){
                Instant start = Instant.now();

                reprocessingCase.searchAndSend(referecesSlot,message);

                Instant end = Instant.now();
                Duration timeElapsed = Duration.between(start, end);

            }





        }



    }
}
