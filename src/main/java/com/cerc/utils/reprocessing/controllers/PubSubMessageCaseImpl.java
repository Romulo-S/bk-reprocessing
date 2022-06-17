package com.cerc.utils.reprocessing.controllers;

import com.cerc.utils.reprocessing.models.PubSubMessage;
import com.cerc.utils.reprocessing.pubsub.consumers.PubSubConsumer;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

public class PubSubMessageCaseImpl implements PubSubMessageCase {

    private static final Logger LOG = Logger.getLogger(PubSubMessageCaseImpl.class);

    private int slot;
    private ArrayList<String> referecesSlot = new ArrayList<>();

    String[] typeAllowed = { "UPDATE", "CREATE", "FINISH", "INACTIVATE" };

    ReprocessingCaseImpl reprocessingCaseImpl = new ReprocessingCaseImpl();

    @Override
    public void reprocess(PubSubMessage message, PubSubConsumer projectId) throws IOException {

        if (!Arrays.asList(typeAllowed).contains(message.getTransactionType())) {
            LOG.infov("Wrong Type >> ", message.getTransactionType());
            return;
        }

        for (int i = 0; i < message.getReferences().length; i++) {
            referecesSlot.add(message.getReferences()[i]);
            slot++;
            if (slot == 10 || i == message.getReferences().length - 1) {
                Instant start = Instant.now();

                reprocessingCaseImpl.searchAndSend(referecesSlot, message, projectId);

                Instant end = Instant.now();
                Duration timeElapsed = Duration.between(start, end);
                LOG.infov("DURATION: ", timeElapsed.toString());
            }

        }

    }

}
