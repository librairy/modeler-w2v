/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.modeler.w2v.builder;

import com.google.common.collect.ImmutableMap;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.ml.feature.RegexTokenizer;
import org.apache.spark.ml.feature.StopWordsRemover;
import org.apache.spark.mllib.feature.Word2Vec;
import org.apache.spark.mllib.feature.Word2VecModel;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.librairy.boot.model.domain.resources.Item;
import org.librairy.boot.model.domain.resources.Resource;
import org.librairy.boot.storage.dao.DBSessionManager;
import org.librairy.boot.storage.generator.URIGenerator;
import org.librairy.computing.cluster.ComputingContext;
import org.librairy.modeler.w2v.cache.DimensionCache;
import org.librairy.modeler.w2v.cache.IterationsCache;
import org.librairy.modeler.w2v.data.W2VModel;
import org.librairy.modeler.w2v.helper.ModelingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.FileAlreadyExistsException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by cbadenes on 13/01/16.
 */
@Component
public class ModelTrainer {

    private static final Logger LOG = LoggerFactory.getLogger(ModelTrainer.class);

    @Autowired
    DimensionCache dimCache;

    @Autowired
    IterationsCache iterationsCache;

    @Value("#{environment['LIBRAIRY_W2V_COMPARATOR_SYNONYMS']?:${librairy.w2v.comparator.synonyms}}")
    Integer maxWords;

    @Autowired
    ModelingHelper helper;


    public W2VModel build(ComputingContext context, String domainUri){

        // Train model
        W2VModel model = buildModel(context, domainUri);

        // Persist model
        persist(context, model.getModel(),URIGenerator.retrieveId(domainUri));

        return model;
    }


    private W2VModel buildModel(ComputingContext context, String domainUri){


        DataFrame df = context.getCassandraSQLContext()
                .read()
                .format("org.apache.spark.sql.cassandra")
                .schema(DataTypes
                        .createStructType(new StructField[]{
                                DataTypes.createStructField(Resource.URI, DataTypes.StringType, false),
                                DataTypes.createStructField(Item.TOKENS, DataTypes.StringType, false)
                        }))
                .option("inferSchema", "false") // Automatically infer data types
                .option("charset", "UTF-8")
                .option("mode", "DROPMALFORMED")
                .options(ImmutableMap.of("table", "items", "keyspace", DBSessionManager.getKeyspaceFromUri(domainUri)))
                .load()
                .repartition(context.getRecommendedPartitions())
                .cache();

        LOG.info("Splitting each document into words ..");
        DataFrame words = new RegexTokenizer()
                .setPattern("[\\W_]+")
                .setMinTokenLength(4) // Filter away tokens with length < 4
                .setInputCol(Item.TOKENS)
                .setOutputCol("words")
                .transform(df);

        df.unpersist();

        String id = URIGenerator.retrieveId(domainUri);
        String stopwordPath = helper.getStorageHelper().path(id,"stopwords.txt");
        List<String> stopwords = helper.getStorageHelper().exists(stopwordPath)?
                context.getSparkContext().textFile(helper.getStorageHelper().absolutePath(stopwordPath)).collect() : Collections
                .EMPTY_LIST;

        LOG.info("Filtering by stopwords ["+stopwords.size()+"]");
        DataFrame filteredWords = new StopWordsRemover()
                .setInputCol("words")
                .setOutputCol("filtered")
                .setStopWords(stopwords.toArray(new String[]{}))
                .setCaseSensitive(false)
                .transform(words);

        JavaRDD<List<String>> input = filteredWords
                .toJavaRDD()
                .map(row -> Arrays.asList(row.getString(1).split(" ")))
                .cache();

        input.take(1); // force cache

        Integer vectorSize = dimCache.getDimension(domainUri);
        Integer maxIterations = iterationsCache.getIterations(domainUri);

        LOG.info("Building a new W2V Model [dim="+vectorSize+"|maxIter="+maxIterations+"]");


        LOG.info("Training a Word2Vec model with the documents from: " + domainUri);
        Word2Vec word2Vec = new Word2Vec();
        word2Vec.setNumPartitions(context.getRecommendedPartitions());
        word2Vec.setVectorSize(vectorSize);
        word2Vec.setNumIterations(maxIterations);
        Word2VecModel model = word2Vec.fit(input);

        input.unpersist();

        return new W2VModel(id,maxWords,model);
    }


    public void persist(ComputingContext context, Word2VecModel model, String id ){
        try {
            helper.getStorageHelper().create(helper.getStorageHelper().absolutePath(helper.getStorageHelper().path
                    (id, "")));

            String path = helper.getStorageHelper().path(id,"w2v");
            helper.getStorageHelper().deleteIfExists(path);

            String absolutePath = helper.getStorageHelper().absolutePath(helper.getStorageHelper().path(id,
                    "w2v/model"));
            LOG.info("Saving (or updating) the model at: " + absolutePath);
            model.save(context.getSparkContext().sc(), absolutePath);
            LOG.info("W2V model saved successfully!");
        }catch (Exception e){
            if (e instanceof FileAlreadyExistsException) {
                LOG.warn(e.getMessage());
            }else {
                LOG.error("Error saving model", e);
            }
        }

    }

    public W2VModel load(ComputingContext context, String id){
        String path = helper.getStorageHelper().absolutePath(helper.getStorageHelper().path(id,"w2v/model"));
        LOG.info("loading word2vec model from :" + path);
        Word2VecModel model = Word2VecModel.load(context.getSparkContext().sc(), path);
        return new W2VModel(id,maxWords,model);
    }


}
