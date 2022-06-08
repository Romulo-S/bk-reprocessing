package com.cerc.utils.reprocessing.controllers;

import com.cerc.utils.reprocessing.models.PubSubMessage;
import com.cerc.utils.reprocessing.pubsub.consumers.PubSubConsumer;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;


public interface PubSubMessageCase {

    void reprocess(PubSubMessage message, PubSubConsumer projectId) throws IOException;
}
