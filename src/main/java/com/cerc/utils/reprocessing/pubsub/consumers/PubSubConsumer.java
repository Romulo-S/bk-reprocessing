package com.cerc.utils.reprocessing.pubsub.consumers;

import com.cerc.utils.reprocessing.controllers.PubSubMessageCaseImpl;
import com.cerc.utils.reprocessing.models.Payload;
import com.cerc.utils.reprocessing.models.PubSubMessage;
import com.cerc.utils.reprocessing.controllers.PubSubMessageCase;
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
import org.json.simple.JSONObject;

import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

@ApplicationScoped
public class PubSubConsumer {

    private static final Logger LOG = Logger.getLogger(PubSubConsumer.class);

    @ConfigProperty(name = "quarkus.google.cloud.project-id")
    String projectId;// Inject the projectId property from application.properties

    private TopicName topicName;
    private Subscriber subscriber;


    PubSubMessageCaseImpl pubSubMessageCase = new PubSubMessageCaseImpl();

    @Inject
    CredentialsProvider credentialsProvider;


    void onStart(@Observes StartupEvent ev) throws IOException {
        // Init topic and subscription, the topic must have been created before

        topicName = TopicName.of(projectId, "contracts-created-topic");
        ProjectSubscriptionName subscriptionName = initSubscription();

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
        subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
        subscriber.startAsync().awaitRunning();
    }

    void onStop(@Observes ShutdownEvent ev) {
        // Stop the subscription at destroy time
        if (subscriber != null) {
            subscriber.stopAsync();
        }
    }

    public void pubsub(List<Payload> temp) throws IOException, InterruptedException {
        // Init a publisher to the topic
        Publisher publisher = Publisher.newBuilder(topicName)
                .setCredentialsProvider(credentialsProvider)
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
        Publisher publisher = Publisher.newBuilder(topicName)
                .setCredentialsProvider(credentialsProvider)
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


    private ProjectSubscriptionName initSubscription() throws IOException {
        // List all existing subscriptions and create the 'test-subscription' if needed
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(projectId, "contracts-created-topic");
        SubscriptionAdminSettings subscriptionAdminSettings = SubscriptionAdminSettings.newBuilder()
                .setCredentialsProvider(credentialsProvider)
                .build();
        try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create(subscriptionAdminSettings)) {
            Iterable<Subscription> subscriptions = subscriptionAdminClient.listSubscriptions(ProjectName.of(projectId))
                    .iterateAll();
            Optional<Subscription> existing = StreamSupport.stream(subscriptions.spliterator(), false)
                    .filter(sub -> sub.getName().equals(subscriptionName.toString()))
                    .findFirst();
            if (!existing.isPresent()) {
                subscriptionAdminClient.createSubscription(subscriptionName, topicName, PushConfig.getDefaultInstance(), 0);
            }
        }
        return subscriptionName;
    }


}
