package com.cerc.utils.reprocessing.pubsub.producers;

import com.cerc.utils.reprocessing.pubsub.consumers.PubSubConsumer;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.api.gax.core.CredentialsProvider;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.*;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

public class PubSubProducer {

    private static final Logger LOG = Logger.getLogger(PubSubConsumer.class);

    private TopicName topicName;
    private Subscriber subscriber;

    @Inject
    CredentialsProvider credentialsProvider;

    public void sendMessage(String projectId) throws IOException, InterruptedException {
        // Init a publisher to the topic

        ProjectSubscriptionName subscriptionName = initSubscription(projectId);

        topicName = TopicName.of(projectId, "contracts-created-topic");

        Publisher publisher = Publisher.newBuilder(topicName)
                .setCredentialsProvider(credentialsProvider)
                .build();
        try {
            ByteString data = ByteString.copyFromUtf8("my-message");// Create a new message
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

    private ProjectSubscriptionName initSubscription(String projectId) throws IOException {
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
