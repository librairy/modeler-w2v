/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.modeler.w2v.eventbus;

import org.librairy.boot.model.Event;
import org.librairy.boot.model.domain.relations.Relation;
import org.librairy.boot.model.domain.resources.Resource;
import org.librairy.boot.model.modules.BindingKey;
import org.librairy.boot.model.modules.EventBus;
import org.librairy.boot.model.modules.EventBusSubscriber;
import org.librairy.boot.model.modules.RoutingKey;
import org.librairy.boot.storage.system.column.repository.UnifiedColumnRepository;
import org.librairy.modeler.w2v.services.ModelingService;
import org.librairy.boot.storage.UDM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 09/09/16:
 *
 * @author cbadenes
 */
@Component
public class ItemUpdatedEventHandler implements EventBusSubscriber {

    private static final Logger LOG = LoggerFactory.getLogger(ItemUpdatedEventHandler.class);

    @Autowired
    protected EventBus eventBus;

    @Autowired
    ModelingService modelingService;

    @Autowired
    UDM udm;

    @Value("#{environment['LIBRAIRY_W2V_EVENT_DELAY']?:${librairy.w2v.event.delay}}")
    protected Long delay;

    @Autowired
    UnifiedColumnRepository unifiedColumnRepository;

    ConcurrentHashMap<String,Object> records;

    @PostConstruct
    public void init(){
        BindingKey bindingKey = BindingKey.of(RoutingKey.of(Resource.Type.ITEM, Resource.State.UPDATED),
                "w2v-modeler-item-updated-train-model");
        LOG.info("Trying to register as subscriber of '" + bindingKey + "' events ..");
        eventBus.subscribe(this,bindingKey );
        records = new ConcurrentHashMap<>();
        LOG.info("registered successfully");
    }

    @Override
    public void handle(Event event) {
        LOG.debug("New topic created event received: " + event);
        try{
            if (!records.isEmpty() && (System.currentTimeMillis()- (Long) records.get("time") < 5000)){
                ((Iterable<Relation>) records.get("domains"))
                        .forEach(rel -> modelingService.train(rel.getStartUri(), delay));
                return;
            }

            LOG.debug("Item bundled event received: " + event);
            try{
                Resource resource = event.to(Resource.class);

                Iterable<Relation> domains = unifiedColumnRepository
                        .findBy(Relation.Type.CONTAINS_TO_ITEM, "end", resource.getUri());

                domains.forEach(rel -> modelingService.train(rel.getStartUri(), delay));

                records.put("time", System.currentTimeMillis());
                records.put("domains", domains);

            } catch (Exception e){
                // TODO Notify to event-bus when source has not been added
                LOG.error("Error scheduling a new topic model for Items from domain: " + event, e);
            }

        } catch (Exception e){
            LOG.error("Error scheduling a new topic model for Items from domain: " + event, e);
        }
    }
}
