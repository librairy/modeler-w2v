package org.librairy.modeler.w2v.builder;

import com.google.common.collect.ImmutableMap;
import org.apache.spark.api.java.JavaRDD;
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
import org.librairy.modeler.w2v.helper.SparkHelper;
import org.librairy.modeler.w2v.helper.StorageHelper;
import org.librairy.modeler.w2v.models.W2VModel;
import org.librairy.modeler.w2v.services.StorageService;
import org.librairy.storage.UDM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.FileAlreadyExistsException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by cbadenes on 13/01/16.
 */
@Component
public class W2VBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(W2VBuilder.class);

    @Value("${librairy.modeler.vector.dimension}")
    Integer vectorSize;

    @Value("${librairy.modeler.maxiterations}")
    Integer maxIterations;

    @Value("${librairy.modeler.similar.max}")
    Integer maxWords;

    @Autowired
    UDM udm;

    @Autowired
    StorageService storageService;

    @Autowired
    SparkHelper sparkHelper;

    public W2VModel build(String domainUri){

        // Reading Uris
        LOG.info("Finding items in domain:" + domainUri);
        List<String> uris = udm.find(Resource.Type.ITEM)
                .from(Resource.Type.DOMAIN, domainUri)
                .parallelStream()
                .map(res -> res.getUri())
                .collect(Collectors.toList());

        if (uris.isEmpty()) throw new RuntimeException("No Items found in domain: " + domainUri);

        // Train model
        W2VModel model = build(domainUri,uris);

        // Persist model
        persist(model.getModel(),domainUri);

        return model;
    }


    public W2VModel build(String id, List<String> uris){

        // Create a Data Frame from Cassandra query
        CassandraSQLContext cc = new CassandraSQLContext(sparkHelper.getSc().sc());

        // Define a schema
        StructType schema = DataTypes
                .createStructType(new StructField[] {
                        DataTypes.createStructField(Resource.URI, DataTypes.StringType, false),
                        DataTypes.createStructField(Item.CONTENT, DataTypes.StringType, false)
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


        JavaRDD<List<String>> input = df
                .toJavaRDD()
                .filter(row -> uris.contains(row.getString(0)))
                .map(row -> Arrays.asList(row.getString(1).split(" ")))
                .cache();


        LOG.info("Building a new W2V Model [dim="+vectorSize+"|maxIter="+maxIterations+"] from " + uris.size() + " " +
                "documents");
        int processors = Runtime.getRuntime().availableProcessors()*3; //2 or 3 times
        int numPartitions = Math.max(processors, uris.size()/processors);
        LOG.info("Num Partitions set to: " + numPartitions);

        Word2Vec word2Vec = new Word2Vec();
        word2Vec.setNumPartitions(numPartitions);
        word2Vec.setVectorSize(vectorSize);
        word2Vec.setNumIterations(maxIterations);
        Word2VecModel model = word2Vec.fit(input);

        return new W2VModel(id,maxWords,model);
    }


    public void persist(Word2VecModel model, String domainUri ){
        LOG.info("Persist W2VModel");
        StorageHelper storageHelper = storageService.getHelper();

        try {
            String path = storageHelper.getPath(domainUri);

            LOG.info("Deleting previous model if exists: " + path);
            storageHelper.deleteIfExists(path);

            LOG.info("Saving the new model at: " + path);
            model.save(sparkHelper.getSc().sc(), path);

        }catch (Exception e){
            if (e instanceof FileAlreadyExistsException) {
                LOG.warn(e.getMessage());
            }else {
                LOG.error("Error saving model", e);
            }
        }

    }

    public W2VModel load(String id){
        StorageHelper storageHelper = storageService.getHelper();
        String path = storageHelper.getPath(id);
        LOG.info("loading word2vec model from :" + path);
        Word2VecModel model = Word2VecModel.load(sparkHelper.getSc().sc(), path);
        return new W2VModel(id,maxWords,model);
    }


}
