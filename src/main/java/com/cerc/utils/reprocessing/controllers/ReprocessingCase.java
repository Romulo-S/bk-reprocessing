package com.cerc.utils.reprocessing.controllers;

import com.cerc.utils.reprocessing.models.PubSubMessage;

import java.util.ArrayList;

public interface ReprocessingCase {

    void searchAndSend(ArrayList<String> referecesSlot, PubSubMessage message);
}
