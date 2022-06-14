package com.cerc.utils.reprocessing.resources;

import com.cerc.utils.reprocessing.models.PubSubMessage;
import com.cerc.utils.reprocessing.pubsub.consumers.PubSubConsumer;
import io.quarkus.arc.runtime.BeanContainer;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/reprocessar")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MessageResources {

    @Inject
    Instance<PubSubConsumer> paymentProcessor;

    @POST
    public Response sendPubSubMsg(PubSubMessage message) throws IOException, InterruptedException {

        paymentProcessor.get().sendToPubSub(message);

        return  Response.ok(message).build();
    }
}
