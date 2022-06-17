package com.cerc.utils.reprocessing.pubsub.consumers;

import com.cerc.utils.reprocessing.controllers.PubSubMessageCaseImpl;
import com.cerc.utils.reprocessing.models.Payload;
import com.cerc.utils.reprocessing.models.PubSubMessage;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.api.gax.core.CredentialsProvider;
import com.google.cloud.pubsub.v1.*;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.*;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.jboss.logging.Logger;
import org.json.simple.JSONArray;

import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

@ApplicationScoped
public class PubSubConsumer {

    private static final Logger LOG = Logger.getLogger(PubSubConsumer.class);

    @ConfigProperty(name = "quarkus.google.cloud.project-id")
    String projectId;// Inject the projectId property from application.properties

    @ConfigProperty(name = "quarkus.topic.consumer")
    String consumerTopic;

    @ConfigProperty(name = "quarkus.topic.producer")
    String producerTopic;

    TopicName consumerTopicName;
    TopicName producerTopicName;
    Subscriber consumerSubscriber;
    Subscriber producerSubscriber;

    PubSubMessageCaseImpl pubSubMessageCase = new PubSubMessageCaseImpl();

    @Inject
    CredentialsProvider consumerCredentialsProvider;

    @Inject
    CredentialsProvider producerCredentialsProvider;


    void onStart(@Observes StartupEvent ev) throws IOException {
        // Init topic and subscription, the topic must have been created before

        consumerTopicName = TopicName.of(projectId, consumerTopic);
        producerTopicName = TopicName.of(projectId, producerTopic);

        ProjectSubscriptionName subscriptionConsumerName = initConsumerSubscription(consumerTopic);
        ProjectSubscriptionName subscriptionProducerName = initProducerSubscription(producerTopic);

        Jsonb jsonb = JsonbBuilder.create();

        // Subscribe to PubSub
        MessageReceiver receiver = (message, consumer) -> {
            PubSubMessage pubSubMessage = jsonb.fromJson(message.getData().toStringUtf8(), PubSubMessage.class);
            LOG.infov("Got message {0}", message.getData().toStringUtf8());
            try {
                pubSubMessageCase.reprocess(pubSubMessage,this);
            } catch (IOException e) {
                consumer.nack();
            }
            consumer.ack();

        };
        consumerSubscriber = Subscriber.newBuilder(subscriptionConsumerName, receiver).build();
        consumerSubscriber.startAsync().awaitRunning();

        producerSubscriber = Subscriber.newBuilder(subscriptionProducerName, receiver).build();
//        producerSubscriber.startAsync().awaitRunning();
    }

    void onStop(@Observes ShutdownEvent ev) {
        // Stop the subscription at destroy time
        if (consumerSubscriber != null) {
            consumerSubscriber.stopAsync();
        }
    }

    public void pubsub(List<Payload> temp) throws IOException, InterruptedException {
        // Init a publisher to the topic
        Publisher publisher = Publisher.newBuilder(producerTopicName)
                .setCredentialsProvider(consumerCredentialsProvider)
                .build();
        try {
            String jsonStr = JSONArray.toJSONString(temp);
            ByteString data = ByteString.copyFromUtf8(jsonStr);// Cretate a new message
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();
            ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);// Publish the message
            ApiFutures.addCallback(messageIdFuture, new ApiFutureCallback<String>() {// Wait for message submission and log the result
                public void onSuccess(String messageId) {
                    LOG.infov("published with message id {0}", messageId);
                }

                public void onFailure(Throwable t) {
                    LOG.warnv("failed to publish: {0}", t);
                }
            }, MoreExecutors.directExecutor());
        } finally {
            publisher.shutdown();
            publisher.awaitTermination(1, TimeUnit.MINUTES);
        }
    }


    public void sendToPubSub(PubSubMessage temp) throws IOException, InterruptedException {
        // Init a publisher to the topic
        Publisher publisher = Publisher.newBuilder(producerTopicName)
                .setCredentialsProvider(consumerCredentialsProvider)
                .build();
        try {

            ByteString data = ByteString.copyFromUtf8(new Gson().toJson(temp));// Create a new message
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();
            ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);// Publish the message
            ApiFutures.addCallback(messageIdFuture, new ApiFutureCallback<String>() {// Wait for message submission and log the result
                public void onSuccess(String messageId) {
                    LOG.infov("published with message id {0}", messageId);
                }

                public void onFailure(Throwable t) {
                    LOG.warnv("failed to publish: {0}", t);
                }
            }, MoreExecutors.directExecutor());
        } finally {
            publisher.shutdown();
            publisher.awaitTermination(1, TimeUnit.MINUTES);
        }
    }


    private ProjectSubscriptionName initConsumerSubscription(String topic) throws IOException {
        // List all existing subscriptions and create the 'test-subscription' if needed
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(projectId, topic);
        SubscriptionAdminSettings subscriptionAdminSettings = SubscriptionAdminSettings.newBuilder()
                .setCredentialsProvider(consumerCredentialsProvider)
                .build();
        try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create(subscriptionAdminSettings)) {
            Iterable<Subscription> subscriptions = subscriptionAdminClient.listSubscriptions(ProjectName.of(projectId))
                    .iterateAll();
            Optional<Subscription> existing = StreamSupport.stream(subscriptions.spliterator(), false)
                    .filter(sub -> sub.getName().equals(subscriptionName.toString()))
                    .findFirst();
            if (!existing.isPresent()) {
                subscriptionAdminClient.createSubscription(subscriptionName, consumerTopic, PushConfig.getDefaultInstance(), 0);
            }
        }
        return subscriptionName;
    }

    private ProjectSubscriptionName initProducerSubscription(String topic) throws IOException {
        // List all existing subscriptions and create the 'test-subscription' if needed
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(projectId, topic);
        SubscriptionAdminSettings subscriptionAdminSettings = SubscriptionAdminSettings.newBuilder()
                .setCredentialsProvider(producerCredentialsProvider)
                .build();
        try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create(subscriptionAdminSettings)) {
            Iterable<Subscription> subscriptions = subscriptionAdminClient.listSubscriptions(ProjectName.of(projectId))
                    .iterateAll();
            Optional<Subscription> existing = StreamSupport.stream(subscriptions.spliterator(), false)
                    .filter(sub -> sub.getName().equals(subscriptionName.toString()))
                    .findFirst();
            if (!existing.isPresent()) {
                subscriptionAdminClient.createSubscription(subscriptionName, consumerTopic, PushConfig.getDefaultInstance(), 0);
            }
        }
        return subscriptionName;
    }


}
