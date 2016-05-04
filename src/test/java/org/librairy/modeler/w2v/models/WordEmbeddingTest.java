package org.librairy.modeler.w2v.models;

import es.cbadenes.lab.test.IntegrationTest;
import es.upm.oeg.epnoi.matching.metrics.domain.entity.RegularResource;
import org.librairy.model.domain.relations.EmbeddedIn;
import org.librairy.model.domain.relations.PairsWith;
import org.librairy.model.domain.relations.Relation;
import org.librairy.model.domain.resources.*;
import org.librairy.modeler.w2v.Config;
import org.librairy.modeler.w2v.helper.ModelingHelper;
import org.librairy.modeler.w2v.models.word.W2VModel;
import org.librairy.storage.UDM;
import org.librairy.storage.generator.URIGenerator;
import org.librairy.storage.system.document.repository.WordDocumentRepository;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by cbadenes on 26/01/16.
 */
@Category(IntegrationTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Config.class)
@TestPropertySource(properties = {"librairy.modeler.delay = 200000"})
public class WordEmbeddingTest {

    private static final Logger LOG = LoggerFactory.getLogger(WordEmbeddingTest.class);

    @Autowired
    ModelingHelper helper;


    @Autowired
    URIGenerator uriGenerator;

    @Autowired
    UDM udm;

    @Autowired
    WordDocumentRepository wordDocumentRepository;

    @Test
    public void validation(){

        String dUri = "http://librairy.org/domains/382130c5-1d84-4b21-a591-90d2c235f0a5";

        List<String> words = udm.find(Resource.Type.WORD).from(Resource.Type.DOMAIN,dUri);

        words.stream().map(uri -> wordDocumentRepository.findOne(uri)).forEach(wd -> System.out.println("WordDocument: " + wd.getDomain()));

    }


    @Test
    public void pair(){

        String dUri = "http://librairy.org/domains/382130c5-1d84-4b21-a591-90d2c235f0a5";
        Optional<Resource> result = udm.read(Resource.Type.DOMAIN).byUri(dUri);

        if (!result.isPresent()){
            LOG.error("Domain not found: " + dUri);
        }

        Domain domain = (Domain) result.get();

        try{
            //TODO Optimize using Spark.parallel
            List<RegularResource> regularResources = helper.getUdm().find(Resource.Type.DOCUMENT).from(Resource.Type.DOMAIN,domain.getUri()).stream().
                    map(uri -> helper.getUdm().read(Resource.Type.DOCUMENT).byUri(uri)).
                    filter(res -> res.isPresent()).map(res -> (Document) res.get()).
                    map(document -> helper.getRegularResourceBuilder().from(document.getUri(), document.getTitle(), document.getAuthoredOn(), helper.getAuthorBuilder().composeFromMetadata(document.getAuthoredBy()), document.getTokens())).
                    collect(Collectors.toList());

            if ((regularResources == null) || (regularResources.isEmpty()))
                throw new RuntimeException("No documents found in domain: " + domain.getUri());


            // Clean Similar relations
//            helper.getUdm().deleteSimilarsBetweenWordsInDomain(domain.getUri());

            // Clean Embedded relations
//            helper.getUdm().deleteEmbeddingWordsInDomain(domain.getUri());

            String analysisUri = uriGenerator.newFor(Resource.Type.ANALYSIS);


            // Build W2V Model
            W2VModel model = helper.getWordEmbeddingBuilder().build(analysisUri, regularResources);

            List<Word> words = helper.getUdm().find(Resource.Type.WORD).from(Resource.Type.DOMAIN,domain.getUri()).stream().map(uri -> helper.getUdm().read(Resource.Type.WORD).byUri(uri)).filter(res -> res.isPresent()).map(res -> (Word) res.get()).collect(Collectors.toList());

            LOG.info("Number of words: " + words.size());

            // Then relate
            words.stream().forEach(word -> relateWord(word,model,dUri));

        }catch (RuntimeException e){
            LOG.warn(e.getMessage(),e);
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
        }

    }


    private void relateWord(Word word, W2VModel model, String domainUri){
        // EMBEDDED relation
        float[] vector = model.getRepresentation(word.getContent());

        EmbeddedIn embedded = Relation.newEmbeddedIn(word.getUri(), domainUri);
        //embedded.setVector(vector);
        //TODO Save Vector in Column Repository
        helper.getUdm().save(embedded);

        // PAIRED relations
        List<WordDistribution> words = model.find(word.getContent()).stream().filter(sim -> sim.getWeight() > helper.getSimilarityThreshold()).collect(Collectors.toList());
        for (WordDistribution wordDistribution : words){
            List<String> result = helper.getUdm().find(Resource.Type.WORD).by(Word.CONTENT,wordDistribution.getWord());
            String wordUri;
            if (result == null || result.isEmpty()){
                // Create word
                Word wordRef = new Word();
                wordRef.setUri(uriGenerator.newFor(Resource.Type.WORD));
                wordRef.setContent(wordDistribution.getWord());
                udm.save(wordRef);

                wordUri = wordRef.getUri();

                // Embedd to Domain
                EmbeddedIn embeddedRef = Relation.newEmbeddedIn(wordRef.getUri(), domainUri);
                //embeddedRef.setVector(model.getRepresentation(wordRef.getContent()));
                //TODO Save Vector in Column Repository
                helper.getUdm().save(embeddedRef);

            }else{
                wordUri = result.get(0);
            }

            // Pair words
            PairsWith pair = Relation.newPairsWith(word.getUri(), wordUri);
            pair.setWeight(wordDistribution.getWeight());
            helper.getUdm().save(pair);
        }

    }

}
