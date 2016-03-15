package org.librairy.modeler.w2v.helper;

import lombok.Data;
import org.librairy.modeler.w2v.builder.*;
import org.librairy.storage.UDM;
import org.librairy.storage.generator.URIGenerator;
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
    AuthorBuilder authorBuilder;

    @Autowired
    ModelBuilder modelBuilder;

    @Autowired
    RegularResourceBuilder regularResourceBuilder;

    @Autowired
    WordEmbeddingBuilder wordEmbeddingBuilder;

    @Autowired
    UDM udm;

    @Value("${librairy.comparator.threshold}")
    Double similarityThreshold;
}
