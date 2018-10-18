package com.hutoma.api.memory;

import com.hutoma.api.common.Pair;
import com.hutoma.api.common.SupportedLanguage;
import com.hutoma.api.containers.sub.MemoryVariable;

import java.util.List;

/**
 * Created by pedrotei on 06/10/16.
 */
public interface IEntityRecognizer {

    String SYSTEM_ENTITY_PREFIX = "sys.";

    /**
     * Retrieve entities from a chat line.
     * 
     * @param chatLine the chat line
     * @param entities the available entities
     * @return list of pairs of {entity name, entity value}
     */
    List<Pair<String, String>> retrieveEntities(final String chatLine, final SupportedLanguage language,
            final List<MemoryVariable> entities);
}
