package com.cerc.utils.reprocessing.models;

import lombok.Data;

@Data
public class PubSubMessage {

    private String[] contracts;
    private String transactionType;
    private Boolean skipSaveFiles;
    private String topic;
    private boolean interopAsCreate;


}
