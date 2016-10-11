/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.modeler.w2v.builder;

import com.google.common.collect.ImmutableMap;
import org.librairy.model.domain.relations.Relation;
import org.librairy.model.domain.resources.Resource;
import org.librairy.storage.UDM;
import org.librairy.storage.system.column.repository.UnifiedColumnRepository;
import org.librairy.storage.system.graph.template.TemplateExecutor;
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

    @Autowired
    TemplateExecutor graphExecutor;

    public void delete(String domainUri){

        LOG.info("Deleting pair relations in domain: " + domainUri + " ..");
        columnRepository.findBy(Relation.Type.PAIRS_WITH, "domain", domainUri).forEach(rel->columnRepository.delete
                (Relation.Type.PAIRS_WITH,rel.getUri()));

        LOG.info("and now from graph-database...");
        graphExecutor.execute("match ()-[r:PAIRS_WITH { domain : {0} }]->() delete r", ImmutableMap.of("0", domainUri));

//        LOG.info("deleting words in domain: " + domainUri + " ...");
//        columnRepository.findBy(Relation.Type.EMBEDDED_IN, "domain", domainUri).forEach(rel -> {
//            String wordUri = rel.getStartUri();
//
//            // Delete relation
//            udm.delete(Relation.Type.EMBEDDED_IN).byUri(rel.getUri());
//
//            // Delete Word
//            udm.delete(Resource.Type.WORD).byUri(wordUri);
//        });

        LOG.info("all word pairing deleted");
    }


}
