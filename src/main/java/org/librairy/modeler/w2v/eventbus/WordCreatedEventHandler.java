package org.librairy.modeler.w2v.eventbus;

import org.librairy.model.Event;
import org.librairy.model.domain.resources.Resource;
import org.librairy.model.modules.BindingKey;
import org.librairy.model.modules.EventBus;
import org.librairy.model.modules.EventBusSubscriber;
import org.librairy.model.modules.RoutingKey;
import org.librairy.modeler.w2v.services.W2VModelingService;
import org.librairy.storage.UDM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Created by cbadenes on 11/01/16.
 */
@Component
public class WordCreatedEventHandler implements EventBusSubscriber {

    private static final Logger LOG = LoggerFactory.getLogger(WordCreatedEventHandler.class);

    @Autowired
    protected EventBus eventBus;

    @Autowired
    W2VModelingService modelingService;

    @Autowired
    UDM udm;

    @PostConstruct
    public void init(){
        BindingKey bindingKey = BindingKey.of(RoutingKey.of(Resource.Type.TOPIC, Resource.State.CREATED),
                "w2v-modeler-item");
        LOG.info("Trying to register as subscriber of '" + bindingKey + "' events ..");
        eventBus.subscribe(this,bindingKey );
        LOG.info("registered successfully");
    }

    @Override
    public void handle(Event event) {
        LOG.debug("new TOPIC event received: " + event);
        try{
            Resource resource = event.to(Resource.class);

            // BUNDLES relation
            List<Resource> domain = udm.find(Resource.Type.DOMAIN).from(Resource.Type.TOPIC, resource
                    .getUri());

            if (domain.isEmpty()) {
                LOG.warn("No domain found from topic: " + resource.getUri());
                return;
            }

            modelingService.buildModel(domain.get(0).getUri());
        } catch (Exception e){
            LOG.error("Error scheduling a new w2v model from domain: " + event, e);
        }
    }
}
