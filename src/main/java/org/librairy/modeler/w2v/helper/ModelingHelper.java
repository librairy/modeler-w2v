/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.modeler.w2v.helper;

import lombok.Data;
import org.librairy.computing.helper.SparkHelper;
import org.librairy.modeler.w2v.builder.*;
import org.librairy.modeler.w2v.cache.ModelCache;
import org.librairy.storage.UDM;
import org.librairy.storage.generator.URIGenerator;
import org.librairy.storage.system.column.repository.UnifiedColumnRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by cbadenes on 12/01/16.
 */
@Data
@Component
public class ModelingHelper {

    @Autowired
    SparkHelper sparkHelper;

    @Autowired
    URIGenerator uriGenerator;

    @Autowired
    ModelTrainer wordEmbeddingBuilder;

    @Autowired
    ModelCleaner cleaner;

    @Autowired
    ModelPairing pairing;

    @Autowired
    UnifiedColumnRepository columnRepository;

    @Autowired
    ModelCache modelCache;

    @Autowired
    UDM udm;


}
