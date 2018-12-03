package com.hutoma.api.memory;

import com.hutoma.api.common.SupportedLanguage;
import com.hutoma.api.containers.sub.EntityValueType;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.common.Pair;
import com.hutoma.api.containers.sub.MemoryVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;

/**
 * Simple entity recognizer.
 */
public class SimpleEntityRecognizer implements IEntityRecognizer {

    private static final String LOGFROM = "simpleentityrecognizer";
    /**
     * Logger.
     */
    private final ILogger logger;

    /**
     * Ctor.
     * 
     * @param logger the logger
     */
    @Inject
    public SimpleEntityRecognizer(final ILogger logger) {
        this.logger = logger;
    }

    /**
     * {@inheritDoc}
     */
    public List<Pair<String, String>> retrieveEntities(final String chatLine, final SupportedLanguage language,
            final List<MemoryVariable> entities) {
        final List<Pair<String, String>> vars = regexFindEntities(chatLine, entities);
        this.logger.logDebug(LOGFROM, String.format("Found %d entities", vars.size()));
        return vars;
    }

    /**
     * Finds entities based on regex - looks for the presence of the same word (case
     * insensitive).
     * 
     * @param chatLine the text to search in
     * @param entities the list of entities to search for
     * @return the list of pairs of entities and values found
     */
    static List<Pair<String, String>> regexFindEntities(final String chatLine, final List<MemoryVariable> entities) {
        final List<Pair<String, String>> vars = new ArrayList<>();
        final String lowercaseResponse = chatLine.toLowerCase();
        for (MemoryVariable v : entities) {
            for (String key : v.getEntityKeys()) {
                // This should all be deleted soon, but ensure this isnt a regex..
                if (v.getValueType() != EntityValueType.REGEX) {
                    if (v.getName() != null) {
                        Matcher matcher = Pattern.compile("(?i:\\b" + key + "\\b)").matcher(lowercaseResponse);
                        if (matcher.find()) {
                            // it's this one
                            vars.add(new Pair<>(v.getName(), key));
                            // stop processing this entity
                            break;
                        }
                    }
                }
            }
        }
        return vars;
    }
}
