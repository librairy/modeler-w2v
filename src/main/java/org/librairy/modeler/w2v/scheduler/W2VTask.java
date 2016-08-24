package org.librairy.modeler.w2v.scheduler;

import org.librairy.model.domain.relations.EmbeddedIn;
import org.librairy.model.domain.relations.PairsWith;
import org.librairy.model.domain.relations.Relation;
import org.librairy.model.domain.resources.Resource;
import org.librairy.model.domain.resources.Word;
import org.librairy.modeler.w2v.helper.ModelingHelper;
import org.librairy.modeler.w2v.models.W2VModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by cbadenes on 13/01/16.
 */
public class W2VTask implements Runnable{

    private static final Logger LOG = LoggerFactory.getLogger(W2VTask.class);

    private final ModelingHelper helper;

    private final String domainUri;

    public W2VTask(String domainUri, ModelingHelper modelingHelper) {
        this.domainUri = domainUri;
        this.helper = modelingHelper;
    }


    @Override
    public void run() {

        try{

            LOG.info("Cleaning PAIRS_WITH relations in domain: " + domainUri);
            helper.getUdm().find(Relation.Type.PAIRS_WITH)
                    .from(Resource.Type.DOMAIN, domainUri)
                    .parallelStream()
                    .forEach(rel -> helper.getUdm().delete(Relation.Type.PAIRS_WITH).byUri(rel.getUri()));

            LOG.info("Cleaning EMBEDDED_IN relations in domain: " + domainUri);
            helper.getUdm().find(Relation.Type.EMBEDDED_IN)
                    .from(Resource.Type.DOMAIN, domainUri)
                    .parallelStream()
                    .forEach(rel -> helper.getUdm().delete(Relation.Type.EMBEDDED_IN).byUri(rel.getUri()));

            LOG.info("Building a new W2V model in domain: " + domainUri);
            W2VModel model = helper.getWordEmbeddingBuilder().build(domainUri);

            LOG.info("Creating relations between words in domain: " + domainUri);
            helper.getUdm().find(Resource.Type.WORD)
                    .from(Resource.Type.DOMAIN,domainUri)
                    .stream()
                    .map(rawRes -> helper.getUdm().read(Resource.Type.WORD).byUri(rawRes.getUri())).filter(res -> res.isPresent())
                    .map(res -> res.get().asWord())
                    .forEach(word -> relateWord(word,model));

        }catch (RuntimeException e){
            LOG.warn(e.getMessage(),e);
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
        }
    }

    private void relateWord(Word word, W2VModel model){
        // EMBEDDED_IN relation
        EmbeddedIn embeddedIn = Relation.newEmbeddedIn(word.getUri(),domainUri);
        helper.getUdm().save(embeddedIn);

        // PAIRED relations
        model.find(word.getContent())
                .stream()
                .filter(sim -> sim.getWeight() > helper.getSimilarityThreshold())
                .forEach(wordDistribution -> {
                    List<Resource> result = helper.getUdm().find(Resource.Type.WORD).by(Word.CONTENT, wordDistribution.getWord());
                    String relatedWordUri;
                    if (result.isEmpty()){
                        // Create word
                        Word relatedWord = Resource.newWord(wordDistribution.getWord());
                        relatedWordUri = helper.getUriGenerator().basedOnContent(Resource.Type.WORD, wordDistribution
                                .getWord());
                        relatedWord.setUri(relatedWordUri);
                        helper.getUdm().save(relatedWord);
                    }else{
                        relatedWordUri = result.get(0).getUri();
                    }

                    PairsWith pair = Relation.newPairsWith(word.getUri(), relatedWordUri, domainUri);
                    pair.setWeight(wordDistribution.getWeight());
                    helper.getUdm().save(pair);

                });



    }
}
