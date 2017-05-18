package com.hutoma.api.memory;

import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.Pair;
import com.hutoma.api.connectors.EntityRecognizerService;
import com.hutoma.api.containers.sub.MemoryVariable;
import com.hutoma.api.containers.sub.RecognizedEntity;

import org.glassfish.hk2.api.ServiceLocator;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

/**
 * External entity recognizer.
 * Currently this uses the SimpleER for determining custom entities, and an external one for system entities.
 */
public class ExternalEntityRecognizer implements IEntityRecognizer {

    private final EntityRecognizerService service;
    private final ILogger logger;

    @Inject
    public ExternalEntityRecognizer(final ServiceLocator serviceLocator, final ILogger logger) {
        this(serviceLocator.getService(EntityRecognizerService.class), logger);
    }

    ExternalEntityRecognizer(final EntityRecognizerService service, final ILogger logger) {
        this.service = service;
        this.logger = logger;
    }

    @Override
    public List<Pair<String, String>> retrieveEntities(final String chatLine,
                                                       final List<MemoryVariable> customEntities) {
        List<Pair<String, String>> result = new ArrayList<>();
        // Call the simple regex entity recognizer for custom entities
        result.addAll(SimpleEntityRecognizer.regexFindEntities(chatLine, customEntities));

        // Call the external entity recognizer for system entities

        List<RecognizedEntity> systemEntities = this.service.getEntities(chatLine);
        for (RecognizedEntity re: systemEntities) {
            result.add(new Pair<>(re.getCategory(), re.getValue()));
        }
        return result;
    }
}
