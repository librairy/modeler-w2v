package org.librairy.modeler.w2v.models.word;

import es.upm.oeg.epnoi.matching.metrics.domain.entity.RegularResource;
import org.librairy.model.domain.relations.EmbeddedIn;
import org.librairy.model.domain.relations.PairsWith;
import org.librairy.model.domain.relations.Relation;
import org.librairy.model.domain.resources.*;
import org.librairy.modeler.w2v.helper.ModelingHelper;
import org.librairy.modeler.w2v.models.WordDistribution;
import org.librairy.modeler.w2v.scheduler.ModelingTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by cbadenes on 11/01/16.
 */
public class WordEmbeddingModeler extends ModelingTask {

    private static final Logger LOG = LoggerFactory.getLogger(WordEmbeddingModeler.class);

    private final ModelingHelper helper;

    private final String domainUri;

    public WordEmbeddingModeler(String domainUri, ModelingHelper modelingHelper) {
        this.domainUri = domainUri;
        this.helper = modelingHelper;
    }


    @Override
    public void run() {

        try{
            //TODO Optimize using Spark.parallel
            List<RegularResource> regularResources = helper.getUdm().find(Resource.Type.ITEM).from(Resource.Type.DOMAIN,domainUri).stream().
                    map(uri -> helper.getUdm().read(Resource.Type.ITEM).byUri(uri)).
                    filter(res -> res.isPresent()).map(res -> (Item) res.get()).
                    map(item -> helper.getRegularResourceBuilder().from(item.getUri(), item.getTitle(), item.getAuthoredOn(), helper.getAuthorBuilder().composeFromMetadata(item.getAuthoredBy()), item.getTokens())).
                    collect(Collectors.toList());

            if ((regularResources == null) || (regularResources.isEmpty()))
                throw new RuntimeException("No documents found in domain: " + domainUri);

            // Clean Similar relations
            helper.getUdm().delete(Relation.Type.PAIRS_WITH).in(Resource.Type.DOMAIN, domainUri);

            // Clean Embedded relations
            helper.getUdm().delete(Relation.Type.EMBEDDED_IN).in(Resource.Type.DOMAIN, domainUri);

            // Create the analysis
            Analysis analysis = newAnalysis("Word-Embedding","W2V",Resource.Type.WORD.name(),domainUri);

            // Build W2V Model
            W2VModel model = helper.getWordEmbeddingBuilder().build(domainUri, regularResources);

            // Make relations
            // First Create
            // TODO use all words of the vocabulary instead of only the existing ones
            List<Word> words = helper.getUdm().find(Resource.Type.WORD).from(Resource.Type.DOMAIN,domainUri).stream().map(uri -> helper.getUdm().read(Resource.Type.WORD).byUri(uri)).filter(res -> res.isPresent()).map(res -> (Word) res.get()).collect(Collectors.toList());
            //List<Word> words = model.getVocabulary().stream().map(word -> findOrCreateWord(word)).collect(Collectors.toList());
            // Then relate
            words.stream().forEach(word -> relateWord(word,model));

            // Save the analysis
            helper.getUdm().save(analysis);

        }catch (RuntimeException e){
            LOG.warn(e.getMessage(),e);
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
        }
    }

    private void relateWord(Word word, W2VModel model){
        // EMBEDDED relation
        float[] vector = model.getRepresentation(word.getContent());
        EmbeddedIn embeddedIn = Relation.newEmbeddedIn(word.getUri(),domainUri);
        //TODO save Vector in COlumnRepository
        //embeddedIn.setVector(vector);
        helper.getUdm().save(embeddedIn);

        // PAIRED relations
        List<WordDistribution> words = model.find(word.getContent()).stream().filter(sim -> sim.getWeight() > helper.getSimilarityThreshold()).collect(Collectors.toList());
        for (WordDistribution wordDistribution : words){
            List<String> result = helper.getUdm().find(Resource.Type.WORD).by(Word.CONTENT, wordDistribution.getWord());
            if (result != null && !result.isEmpty()){

                PairsWith pair = Relation.newPairsWith(word.getUri(), result.get(0));
                pair.setWeight(wordDistribution.getWeight());
                pair.setDomain(domainUri);
                helper.getUdm().save(pair);
            }
            // TODO Create word when not exist
        }

    }


}
