package com.hutoma.api.memory;

import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.Pair;
import com.hutoma.api.containers.sub.MemoryVariable;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by pedrotei on 06/10/16.
 */
public class SimpleEntityRecognizer implements IEntityRecognizer {

    private static final String LOGFROM = "simpleentityrecognizer";
    /** Logger. */
    private ILogger logger;

    /**
     * Ctor.
     * @param logger the logger
     */
    @Inject
    public SimpleEntityRecognizer(ILogger logger) {
        this.logger = logger;
    }

    /**
     * {@inheritDoc}
     */
    public List<Pair<String, String>> retrieveEntities(final String chatLine, final List<MemoryVariable> entities) {
        final List<Pair<String, String>> vars = new ArrayList<>();
        final String lowercaseResponse = chatLine.toLowerCase();
        for(MemoryVariable v: entities) {
            for (String key: v.getEntityKeys()) {
                Matcher m = Pattern.compile("(?i:\\b" + key + "\\b)").matcher(lowercaseResponse);
                if (m.find()) {
                    // it's this one
                    vars.add(new Pair<>(v.getName(), key));
                    // stop processing this entity
                    break;
                }
            }
        }
        this.logger.logDebug(LOGFROM, String.format("Found %d entities", vars.size()));
        return vars;
    }
}
