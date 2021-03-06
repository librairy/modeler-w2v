/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.modeler.w2v.builder;

import org.librairy.boot.model.domain.relations.PairsWith;
import org.librairy.boot.model.domain.relations.Relation;
import org.librairy.boot.model.domain.resources.Resource;
import org.librairy.boot.model.domain.resources.Word;
import org.librairy.boot.storage.UDM;
import org.librairy.boot.storage.system.column.repository.UnifiedColumnRepository;
import org.librairy.computing.cluster.ComputingContext;
import org.librairy.modeler.w2v.cache.ModelCache;
import org.librairy.modeler.w2v.data.W2VModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by cbadenes on 13/01/16.
 */
@Component
public class ModelPairing {

    private static final Logger LOG = LoggerFactory.getLogger(ModelPairing.class);

    @Value("#{environment['LIBRAIRY_W2V_COMPARATOR_THRESHOLD']?:'${librairy.w2v.comparator.threshold}'}")
    Double similarityThreshold;

    @Autowired
    UDM udm;

    @Autowired
    UnifiedColumnRepository columnRepository;

    @Autowired
    ModelCache cache;

    public void relateWord(ComputingContext context, Word word, String domainUri){
        // PAIRED relations
        W2VModel model;
        try {
            model = cache.get(context, domainUri);
        }catch (Exception e){
            LOG.warn("No W2V model found for domain: " + domainUri, e);
            return;
        }

        AtomicInteger counter = new AtomicInteger();
        model.find(word.getContent())
                .stream()
                .filter(sim -> sim.getWeight() > similarityThreshold)
                .forEach(wordDistribution -> {

                    Iterable<Resource> relWords = columnRepository.findBy(Resource.Type.WORD, "content", wordDistribution.getWord());
                    String relatedWordUri;
                    Boolean addToDomain = true;
                    if (!relWords.iterator().hasNext()){
                        // Create word
                        Word relatedWord = Resource.newWord(wordDistribution.getWord());
                        udm.save(relatedWord);
                        LOG.debug("New word created: " + relatedWord.getUri() + " as pair-word of: " + word.getUri());

                        relatedWordUri = relatedWord.getUri();
                    }else{
                        relatedWordUri = relWords.iterator().next().getUri();

                        List<Relation> domains = StreamSupport.stream(
                                columnRepository.findBy(Relation.Type.EMBEDDED_IN, "word", relatedWordUri)
                                        .spliterator(), false)
                                .filter(rel -> rel.getEndUri().equalsIgnoreCase(domainUri)).collect(Collectors.toList());

                        addToDomain =  ((domains == null) || domains.isEmpty());
                    }

                    if (addToDomain){
                        // EmbeddedIn in domain
                        udm.save(Relation.newEmbeddedIn(relatedWordUri,domainUri));
                    }

                    // pair words
                    PairsWith pair = Relation.newPairsWith(word.getUri(), relatedWordUri, domainUri);
                    pair.setWeight(wordDistribution.getWeight());
                    udm.save(pair);
                    counter.incrementAndGet();
                });

        LOG.info(counter.get() + " similar words found for: " + word.getUri() + " in domain: " + domainUri);

    }

}
