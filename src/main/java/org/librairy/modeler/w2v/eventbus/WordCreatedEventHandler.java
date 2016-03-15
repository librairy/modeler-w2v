package org.librairy.modeler.w2v.eventbus;

import org.librairy.model.Event;
import org.librairy.model.domain.resources.Resource;
import org.librairy.model.modules.BindingKey;
import org.librairy.model.modules.EventBus;
import org.librairy.model.modules.EventBusSubscriber;
import org.librairy.model.modules.RoutingKey;
import org.librairy.modeler.w2v.cache.CacheManager;
import org.librairy.modeler.w2v.services.WordEmbeddingModelingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by cbadenes on 11/01/16.
 */
@Component
public class WordCreatedEventHandler implements EventBusSubscriber {

    private static final Logger LOG = LoggerFactory.getLogger(WordCreatedEventHandler.class);

    @Autowired
    protected EventBus eventBus;

    @Autowired
    WordEmbeddingModelingService modelingService;

    @Autowired
    CacheManager cacheManager;

    @PostConstruct
    public void init(){
        BindingKey bindingKey = BindingKey.of(RoutingKey.of(Resource.Type.WORD, Resource.State.CREATED), "w2v-modeler-item");
        LOG.info("Trying to register as subscriber of '" + bindingKey + "' events ..");
        eventBus.subscribe(this,bindingKey );
        LOG.info("registered successfully");
    }

    @Override
    public void handle(Event event) {
        LOG.info("Document created event received: " + event);
        try{
            Resource resource = event.to(Resource.class);

            // BUNDLES relation
            String domainUri = cacheManager.getDomain(resource.getUri());

            modelingService.buildModel(domainUri);
        } catch (Exception e){
            // TODO Notify to event-bus when source has not been added
            LOG.error("Error scheduling a new w2v model from domain: " + event, e);
        }
    }
}
