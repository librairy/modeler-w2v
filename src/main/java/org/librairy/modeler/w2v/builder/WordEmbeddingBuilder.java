package org.librairy.modeler.w2v.builder;

import es.upm.oeg.epnoi.matching.metrics.domain.entity.RegularResource;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.feature.Word2Vec;
import org.apache.spark.mllib.feature.Word2VecModel;
import org.librairy.modeler.w2v.helper.SparkHelper;
import org.librairy.modeler.w2v.models.word.W2VModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import scala.collection.JavaConversions;

import java.util.List;

/**
 * Created by cbadenes on 13/01/16.
 */
@Component
public class WordEmbeddingBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(WordEmbeddingBuilder.class);

    @Value("${librairy.modeler.vector.dimension}")
    Integer vectorSize;

    @Value("${librairy.modeler.maxiterations}")
    Integer maxIterations;

    @Value("${librairy.modeler.similar.max}")
    Integer maxWords;

    @Autowired
    SparkHelper sparkHelper;

    public W2VModel build(String id, List<RegularResource> regularResources){


        JavaRDD<RegularResource> rrs = sparkHelper.getSc().parallelize(regularResources);

        JavaRDD<List<String>> input = rrs.map(rr -> JavaConversions.seqAsJavaList(rr.bagOfWords()));

        Word2Vec word2Vec = new Word2Vec();
        word2Vec.setVectorSize(vectorSize);
        word2Vec.setNumIterations(maxIterations);


        LOG.debug("Tokens: " + input.collect());

        Word2VecModel model = word2Vec.fit(input);
        return new W2VModel(id,maxWords,model);
    }

}
