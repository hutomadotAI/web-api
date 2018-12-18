package com.hutoma.api.memory;

import com.google.common.annotations.VisibleForTesting;
import com.hutoma.api.common.SupportedLanguage;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.common.Pair;
import com.hutoma.api.connectors.EntityRecognizerService;
import com.hutoma.api.containers.sub.MemoryVariable;
import com.hutoma.api.containers.sub.RecognizedEntity;
import com.hutoma.api.common.FeatureToggler;
import com.hutoma.api.containers.sub.ChatRequestInfo;

import org.glassfish.hk2.api.ServiceLocator;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.inject.Inject;

/**
 * External entity recognizer. Currently this uses the SimpleER for determining
 * custom entities, and an external one for system entities.
 */
public class ExternalEntityRecognizer implements IEntityRecognizer {

    private static final String NUMBER_DELIMITER = "[^\\p{Alnum}£$€,.-]";
    private static final String NUMBER_ENTITY_NAME = "sys.number";
    private final EntityRecognizerService service;
    private final ILogger logger;
    private final FeatureToggler featureToggler;

    @Inject
    public ExternalEntityRecognizer(final ServiceLocator serviceLocator,
                                    final ILogger logger,
                                    final FeatureToggler featureToggler) {
        this(serviceLocator.getService(EntityRecognizerService.class), logger, featureToggler);
    }

    ExternalEntityRecognizer(final EntityRecognizerService service,
                             final ILogger logger,
                             final FeatureToggler featureToggler) {
        this.service = service;
        this.logger = logger;
        this.featureToggler = featureToggler;
    }

    @Override
    public List<Pair<String, String>> retrieveEntities(final ChatRequestInfo chatInfo,
            final List<MemoryVariable> customEntities) {
        final String chatLine = chatInfo.getQuestion();
        final SupportedLanguage language = chatInfo.getAiIdentity().getLanguage();
        List<Pair<String, String>> result = new ArrayList<>();

        // Protect SimpleEntityRecognizer call behind a toggle
        if (featureToggler.getStateForAiid(chatInfo.getDevId(),
                chatInfo.getAiid(),
                "no-simple-er") != FeatureToggler.FeatureState.T1) {
            // Call the simple regex entity recognizer for custom entities
            result.addAll(SimpleEntityRecognizer.regexFindEntities(chatLine, customEntities));
        }

        // Call the external entity recognizer for system entities

        List<RecognizedEntity> systemEntities = this.service.getEntities(chatLine, language);
        for (RecognizedEntity re : systemEntities) {
            if (!re.getCategory().equalsIgnoreCase(NUMBER_ENTITY_NAME)) {
                result.add(new Pair<>(re.getCategory(), re.getValue()));
            }
        }
        List<String> numbers = getNumbersFromString(chatLine);
        numbers.forEach(x -> result.add(new Pair<>(NUMBER_ENTITY_NAME, x)));
        return result;
    }

    static List<String> getNumbersFromString(final String str) {
        List<String> numbers = new ArrayList<>();
        Scanner scanner = new Scanner(str);
        scanner.useDelimiter(NUMBER_DELIMITER);
        while (true) {
            if (scanner.hasNextInt()) {
                numbers.add(Integer.toString(scanner.nextInt()));
            } else if (scanner.hasNextDouble()) {
                numbers.add(Double.toString(scanner.nextDouble()));
            } else if (scanner.hasNext()) {
                scanner.next();
            } else {
                break;
            }
        }
        return numbers;
    }

    @VisibleForTesting
    ILogger getLogger() {
        return this.logger;
    }
}
