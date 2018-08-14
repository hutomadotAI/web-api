package com.hutoma.api.common;

import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.connectors.db.DatabaseFeatures;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logic.chat.ConditionEvaluator;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The Feature Toggler class enables running experiments throughout the code.
 * Any feature is considered in "Control" by default, and toggling a Treatment enables the experiment
 * to be turned on within the code. There are multiple treatments that an experiment can be at the same time
 * (for instance, feature FEATURE1 can have an experiment where T1 displays STRING1 to the user,
 * where T2 displays STRING2).
 */
@Singleton
public class FeatureToggler {

    private static final String LOGFROM = "featuretoggler";

    private final DatabaseFeatures databaseFeatures;
    private final ILogger logger;
    private final Config config;

    private final Map<String, FeatureState> globalFeatures = new HashMap<>();
    private final Map<Pair<UUID, String>, FeatureState> devFeatures = new HashMap<>();
    private final Map<Pair<UUID, String>, FeatureState> aiidFeatures = new HashMap<>();

    private long nextReadFromStorage;

    @Inject
    FeatureToggler(final DatabaseFeatures databaseFeatures,
                   final ILogger logger,
                   final Config config) {
        this.databaseFeatures = databaseFeatures;
        this.logger = logger;
        this.config = config;

        this.nextReadFromStorage = 0;
        this.loadFeaturesFromStorage();
    }

    public FeatureState getStateforDev(final UUID devId, final String feature) {
        this.loadFeaturesFromStorage();
        Pair<UUID, String> key = new Pair<>(devId, feature.toUpperCase());
        if (this.devFeatures.containsKey(key)) {
            return this.devFeatures.get(key);
        }
        return getState(feature);
    }

    public FeatureState getStateForAiid(final UUID devId, final UUID aiid, final String feature) {
        this.loadFeaturesFromStorage();
        Pair<UUID, String> key = new Pair<>(aiid, feature.toUpperCase());
        if (this.aiidFeatures.containsKey(key)) {
            return this.aiidFeatures.get(key);
        }
        return getStateforDev(devId, feature);
    }

    public FeatureState getState(final String feature) {
        this.loadFeaturesFromStorage();
        if (this.globalFeatures.containsKey(feature.toUpperCase())) {
            return this.globalFeatures.get(feature.toUpperCase());
        }
        return FeatureState.C;
    }

    private void loadFeaturesFromStorage() {
        long thisEpoch = System.currentTimeMillis();
        if (thisEpoch > this.nextReadFromStorage) {

            this.globalFeatures.clear();
            this.devFeatures.clear();
            this.aiidFeatures.clear();

            try {
                List<DatabaseFeatures.DatabaseFeature> features = this.databaseFeatures.getAllFeatures();
                for (DatabaseFeatures.DatabaseFeature feature : features) {
                    FeatureState state = FeatureState.valueOf(feature.getState());
                    if (feature.getDevId() == null && feature.getAiid() == null) {
                        // global
                        globalFeatures.put(feature.getFeature(), state);
                    } else if (feature.getAiid() == null && feature.getDevId() != null) {
                        // User-wide feature
                        devFeatures.put(new Pair<>(feature.getDevId(), feature.getFeature()), state);
                    } else if (feature.getAiid() != null) {
                        // AIID-only feature
                        aiidFeatures.put(new Pair<>(feature.getAiid(), feature.getFeature()), state);
                    }
                }
            } catch (DatabaseException ex) {
                this.logger.logException(LOGFROM, ex);
            }

            this.nextReadFromStorage = thisEpoch + this.config.getFeatureToggleReadIntervalSec() * 1000;
        }
    }

    public enum FeatureState {
        C,      // Control - this is the "default behaviour" for a feature
        T1,     // Treatment1 - apply "treatment 1" to the experiment
        T2
    }
}
