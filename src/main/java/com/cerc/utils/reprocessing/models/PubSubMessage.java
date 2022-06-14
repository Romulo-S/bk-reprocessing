package com.cerc.utils.reprocessing.models;

import lombok.Data;

@Data
public class PubSubMessage {

    private String[] references;
    private String transactionType;
    private boolean interopAsCreate;

}
