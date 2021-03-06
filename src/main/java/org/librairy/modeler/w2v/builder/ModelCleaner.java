/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.modeler.w2v.builder;

import org.librairy.boot.storage.UDM;
import org.librairy.boot.storage.system.column.repository.UnifiedColumnRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by cbadenes on 13/01/16.
 */
@Component
public class ModelCleaner {

    private static final Logger LOG = LoggerFactory.getLogger(ModelCleaner.class);

    @Autowired
    UDM udm;

    @Autowired
    UnifiedColumnRepository columnRepository;


    public void delete(String domainUri){

        //TODO remove it
//        LOG.debug("Deleting pair relations in domain: " + domainUri + " ..");
//        columnRepository.findBy(Relation.Type.PAIRS_WITH, "domain", domainUri).forEach(rel->columnRepository.delete
//                (Relation.Type.PAIRS_WITH,rel.getUri()));
//
//        LOG.debug("and now from graph-database...");
//        graphExecutor.execute("match ()-[r:PAIRS_WITH { domain : {0} }]->() delete r", ImmutableMap.of("0", domainUri));
//
//        columnRepository.findBy(Relation.Type.EMBEDDED_IN, "domain", domainUri).forEach(rel -> {
//            String wordUri = rel.getStartUri();
//
//            // Delete relation
//            udm.delete(Relation.Type.EMBEDDED_IN).byUri(rel.getUri());
//
//            // Delete Word
//            LOG.info("deleting word " + wordUri + " from domain: " + domainUri);
//            udm.delete(Resource.Type.WORD).byUri(wordUri);
//        });

        LOG.debug("all word pairing deleted");
    }


}
