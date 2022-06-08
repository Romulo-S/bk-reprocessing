package com.cerc.utils.reprocessing.controllers;

import com.cerc.utils.reprocessing.models.PubSubMessage;
import com.cerc.utils.reprocessing.pubsub.consumers.PubSubConsumer;
import com.cerc.utils.reprocessing.pubsub.producers.PubSubProducer;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;


public class PubSubMessageCaseImpl implements PubSubMessageCase {

    private static final Logger LOG = Logger.getLogger(PubSubMessageCaseImpl.class);

    private String topic;
    private int slot;
    private ArrayList<String> referecesSlot = new ArrayList<>();

    String[] typeAllowed = {"UPDATE", "CREATE", "FINISH", "INACTIVATE"};

    @Inject
    private ReprocessingCase reprocessingCase;

    PubSubProducer pubSubProducer = new PubSubProducer();

    PubSubConsumer pubSub;


    @Override
    public void reprocess(PubSubMessage message, PubSubConsumer projectId) throws IOException {

//        if(message.getTopic() != null){
//            topic = "prioritizer.new";
//        }
//
//        if (!Arrays.asList(typeAllowed).contains(message.getTransactionType())) {
//            LOG.infov("Wrong Type >> ", message.getTransactionType());
//            return;
//        }
//
//        for(int i = 0; i < message.getContracts().length; i++){
//            referecesSlot.add(message.getContracts()[i]);
//            slot++;
//            if(slot == 10 || i == message.getContracts().length-1){
//                Instant start = Instant.now();
//
//                reprocessingCase.searchAndSend(referecesSlot,message);
//
//                Instant end = Instant.now();
//                Duration timeElapsed = Duration.between(start, end);
//
//            }


        try {
            projectId.pubsub();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }




}
