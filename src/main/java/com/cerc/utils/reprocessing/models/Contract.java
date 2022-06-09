package com.cerc.utils.reprocessing.models;

import java.time.LocalDate;
import java.util.UUID;

import org.json.simple.JSONObject;

public class Contract {

    private String id;

    private String transactionType;

    private JSONObject contract;

    private String requester;

    private String requesterTransactionId;

    private LocalDate receivedAt;

    public String getId() {
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

    public JSONObject getContract() {
        return contract;
    }

    public void setContract(JSONObject contract) {
        this.contract = contract;
    }

    public String getRequester() {
        return requester;
    }

    public void setRequester(String requester) {
        this.requester = requester;
    }

    public String getRequesterTransactionId() {
        return requesterTransactionId;
    }

    public void setRequesterTransactionId(int requesterTransactionId) {
        this.requesterTransactionId = requesterTransactionId;
    }

    public LocalDate getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(String receivedAt) {
        this.receivedAt = receivedAt;
    }
}
