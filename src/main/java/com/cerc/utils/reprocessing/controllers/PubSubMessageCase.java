package com.cerc.utils.reprocessing.controllers;

import com.cerc.utils.reprocessing.models.PubSubMessage;

import java.io.IOException;

public interface PubSubMessageCase {

    void reprocess(PubSubMessage message) throws IOException;
}
