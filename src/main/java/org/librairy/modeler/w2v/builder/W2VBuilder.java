package org.librairy.modeler.w2v.builder;

import com.google.common.collect.ImmutableMap;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.ml.feature.RegexTokenizer;
import org.apache.spark.ml.feature.StopWordsRemover;
import org.apache.spark.mllib.feature.Word2Vec;
import org.apache.spark.mllib.feature.Word2VecModel;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.cassandra.CassandraSQLContext;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.util.SizeEstimator;
import org.librairy.model.domain.resources.Item;
import org.librairy.model.domain.resources.Resource;
import org.librairy.modeler.w2v.spark.AbstractSparkHelper;
import org.librairy.modeler.w2v.helper.StorageHelper;
import org.librairy.modeler.w2v.models.W2VModel;
import org.librairy.storage.UDM;
import org.librairy.storage.generator.URIGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.FileAlreadyExistsException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by cbadenes on 13/01/16.
 */
@Component
public class W2VBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(W2VBuilder.class);

    @Value("#{environment['LIBRAIRY_W2V_MODEL_DIMENSION']?:${librairy.w2v.model.dimension}}")
    Integer vectorSize;

    @Value("#{environment['LIBRAIRY_W2V_MODEL_ITERATIONS']?:${librairy.w2v.model.iterations}}")
    Integer maxIterations;

    @Value("#{environment['LIBRAIRY_W2V_COMPARATOR_SYNONYMS']?:${librairy.w2v.comparator.synonyms}}")
    Integer maxWords;

    @Autowired
    UDM udm;

    @Autowired
    StorageHelper storageHelper;

    @Autowired
    AbstractSparkHelper sparkHelper;

    public W2VModel build(String domainUri){

        // Reading Uris
        LOG.info("Finding items in domain:" + domainUri);
        List<String> uris = udm.find(Resource.Type.ITEM)
                .from(Resource.Type.DOMAIN, domainUri)
                .parallelStream()
                .map(res -> res.getUri())
                .collect(Collectors.toList());

        if (uris.isEmpty()) throw new RuntimeException("No Items found in domain: " + domainUri);

        String domainId = URIGenerator.retrieveId(domainUri);

        // Train model
        W2VModel model = build(domainId,uris);

        // Persist model
        persist(model.getModel(),domainId);

        return model;
    }


    public W2VModel build(String id, List<String> uris){

        // Create a Data Frame from Cassandra query
        CassandraSQLContext cc = new CassandraSQLContext(sparkHelper.getContext().sc());

        // Define a schema
        StructType schema = DataTypes
                .createStructType(new StructField[] {
                        DataTypes.createStructField(Resource.URI, DataTypes.StringType, false),
                        DataTypes.createStructField(Item.TOKENS, DataTypes.StringType, false)
                });

        DataFrame df = cc
                .read()
                .format("org.apache.spark.sql.cassandra")
                .schema(schema)
                .option("inferSchema", "false") // Automatically infer data types
                .option("charset", "UTF-8")
                .option("mode","DROPMALFORMED")
                .options(ImmutableMap.of("table", "items", "keyspace", "research"))
                .load();

        LOG.info("Splitting each document into words ..");
        DataFrame words = new RegexTokenizer()
                .setPattern("[\\W_]+")
                .setMinTokenLength(4) // Filter away tokens with length < 4
                .setInputCol(Item.TOKENS)
                .setOutputCol("words")
                .transform(df);


        String stopwordPath = storageHelper.path(id,"stopwords.txt");
        List<String> stopwords = storageHelper.exists(stopwordPath)?
                sparkHelper.getContext().textFile(storageHelper.absolutePath(stopwordPath)).collect() : Collections
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
                .filter(row -> uris.contains(row.getString(0)))
                .map(row -> Arrays.asList(row.getString(1).split(" ")))
                .cache();


        LOG.info("Building a new W2V Model [dim="+vectorSize+"|maxIter="+maxIterations+"] from " + uris.size() + " " +
                "documents");

        long estimatedBytes = SizeEstimator.estimate(input);

        long bytesPerTask   = 500000;

        int estimatedPartitions = Long.valueOf(estimatedBytes / bytesPerTask).intValue();

        LOG.info("Estimated Partitions set to: " + estimatedPartitions);

        LOG.info("Training a Word2Vec model with the documents from: " + id);
        Word2Vec word2Vec = new Word2Vec();
        word2Vec.setNumPartitions(estimatedPartitions);
        word2Vec.setVectorSize(vectorSize);
        word2Vec.setNumIterations(maxIterations);
        Word2VecModel model = word2Vec.fit(input);

        return new W2VModel(id,maxWords,model);
    }


    public void persist(Word2VecModel model, String id ){
        try {

            String path = storageHelper.path(id,"w2v");
            storageHelper.deleteIfExists(path);

            String absolutePath = storageHelper.absolutePath(path);
            LOG.info("Saving (or updating) the model at: " + absolutePath);
            model.save(sparkHelper.getContext().sc(), absolutePath);

        }catch (Exception e){
            if (e instanceof FileAlreadyExistsException) {
                LOG.warn(e.getMessage());
            }else {
                LOG.error("Error saving model", e);
            }
        }

    }

    public W2VModel load(String id){
        String path = storageHelper.absolutePath(storageHelper.path(id,"w2v"));
        LOG.info("loading word2vec model from :" + path);
        Word2VecModel model = Word2VecModel.load(sparkHelper.getContext().sc(), path);
        return new W2VModel(id,maxWords,model);
    }


}
