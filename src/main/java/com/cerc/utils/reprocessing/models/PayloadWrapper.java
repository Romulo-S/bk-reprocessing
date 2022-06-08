package com.cerc.utils.reprocessing.models;

import java.time.LocalDate;

import io.quarkus.vertx.http.runtime.devmode.Json;

public class PayloadWrapper {

    private String id;

    private String transactionType;

    private Json contract;

    private String requester;

    private String requesterTransactionId;

    private LocalDate receivedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public Json getContract() {
        return contract;
    }

    public void setContract(Json contract) {
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

    public void setRequesterTransactionId(String requesterTransactionId) {
        this.requesterTransactionId = requesterTransactionId;
    }

    public LocalDate getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDate receivedAt) {
        this.receivedAt = receivedAt;
    }
}
