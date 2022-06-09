package com.cerc.utils.reprocessing.models;

import java.util.UUID;

import org.json.simple.JSONObject;

public class Payload {

    private UUID id;

    private String transactionType;

    private Contract contract;

    private String transaction;

    private String requester;

    private int requesterTransactionId;

    private String receivedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public String getRequester() {
        return requester;
    }

    public void setRequester(String requester) {
        this.requester = requester;
    }

    public int getRequesterTransactionId() {
        return requesterTransactionId;
    }

    public void setRequesterTransactionId(int requesterTransactionId) {
        this.requesterTransactionId = requesterTransactionId;
    }

    public String getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(String receivedAt) {
        this.receivedAt = receivedAt;
    }
}
