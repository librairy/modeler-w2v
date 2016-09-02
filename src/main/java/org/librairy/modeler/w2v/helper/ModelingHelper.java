package org.librairy.modeler.w2v.helper;

import lombok.Data;
import org.librairy.computing.helper.SparkHelper;
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
    W2VBuilder wordEmbeddingBuilder;

    @Autowired
    UDM udm;

    @Value("#{environment['LIBRAIRY_W2V_COMPARATOR_THRESHOLD']?:'${librairy.w2v.comparator.threshold}'}")
    Double similarityThreshold;
}
