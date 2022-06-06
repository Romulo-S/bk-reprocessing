package com.cerc.utils.reprocessing.pubsub.consumers;

import com.cerc.utils.reprocessing.models.PubSubMessage;
import com.cerc.utils.reprocessing.controllers.PubSubMessageCase;
import com.google.api.gax.core.CredentialsProvider;
import com.google.cloud.pubsub.v1.*;
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
import java.util.Optional;
import org.jboss.logging.Logger;

import java.util.stream.StreamSupport;

@ApplicationScoped
public class PubSubConsumer {

    private static final Logger LOG = Logger.getLogger(PubSubConsumer.class);

    @ConfigProperty(name = "quarkus.google.cloud.project-id")
    String projectId;// Inject the projectId property from application.properties

    private TopicName topicName;
    private Subscriber subscriber;

    @Inject
    private PubSubMessageCase pubSubMessageCase;

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
                pubSubMessageCase.reprocess(pubSubMessage);
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
