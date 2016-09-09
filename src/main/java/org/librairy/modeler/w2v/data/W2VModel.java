package org.librairy.modeler.w2v.data;

import lombok.Getter;
import org.apache.spark.mllib.feature.Word2VecModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;
import scala.Tuple2;
import scala.collection.JavaConversions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cbadenes on 13/01/16.
 */
public class W2VModel {

    private static final Logger logger = LoggerFactory.getLogger(W2VModel.class);

    @Getter
    private final Word2VecModel model;
    private final String id;
    private final Integer maxWords;

    public W2VModel(String id,Integer maxWords, Word2VecModel model){
        this.id = id;
        this.model = model;
        this.maxWords = maxWords;
    }

    public List<WordDistribution> find(String term){

        List<WordDistribution> similarWords = new ArrayList<>();

        try{
            Tuple2<String, Object>[] synonyms = model.findSynonyms(term, maxWords);
            for (Tuple2<String, Object> tuple: synonyms){
                WordDistribution wordDistribution = new WordDistribution();
                wordDistribution.setWord(tuple._1());
                wordDistribution.setWeight((Double) tuple._2());
                similarWords.add(wordDistribution);
            }
            logger.debug("W2V Distribution of term: '"+ term + "' in '"+id+"':" + similarWords);
        }catch (Exception e){
            logger.warn(e.getMessage() + " '" + id + "'");
        }

        return similarWords;
    }

    public float[] getVector(String word){
        Option<float[]> result = model.getVectors().get(word);
        return (result.isDefined())? result.get() : new float[]{};
    }

    public List <String> getVocabulary(){
        return new ArrayList<>(JavaConversions.asJavaCollection(model.getVectors().keys()));
    }

}

