package com.hutoma.api;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.SupportedLanguage;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.aiservices.ServiceStatusConnector;
import com.hutoma.api.containers.ApiServersAvailable;
import com.hutoma.api.containers.ServiceIdentity;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerMonitor extends TimerTask {

    // set to false to stop the timer
    private final AtomicBoolean runMonitor;

    private Timer timer;
    private final ILogger logger;
    private final Config config;
    private final ServiceStatusConnector controllerConnector;
    private final JsonSerializer jsonSerializer;

    // keep track of the status, last we checked
    private HashMap<SupportedLanguage, LanguageStatus> lastKnownStatus;

    private static final String LOGFROM = "servermonitor";

    @Inject
    public ServerMonitor(ILogger logger, Config config,
                         ServiceStatusConnector controllerConnector, JsonSerializer jsonSerializer) {
        this.logger = logger;
        this.config = config;
        this.controllerConnector = controllerConnector;
        this.jsonSerializer = jsonSerializer;
        this.runMonitor = new AtomicBoolean(true);
        this.lastKnownStatus = new HashMap<>();
    }

    /***
     * Start the scheduled timer every few seconds
     */
    public void initialise() {
        this.timer = new Timer();
        this.timer.schedule(this, 0, config.getControllerHealthCheckEveryMs());
    }

    @Override
    public void run() {
        if (!runMonitor.get()) {
            this.cancel();
            this.timer.cancel();
        } else {
            try {
                monitorServer();
            } catch (Exception ex) {
                this.logger.logException(LOGFROM, ex);
            }
        }
    }

    protected void monitorServer() {

        // get info on available services from the controller instance
        ApiServersAvailable serversAvailable = controllerConnector.getServiceIdentities(this.jsonSerializer);

        // build the current status
        HashMap<SupportedLanguage, LanguageStatus> currentStatus = new HashMap<>();
        // for each language, build a LanguageStatus
        for (ServiceIdentity serviceIdentity: serversAvailable.getServers()) {
            currentStatus.computeIfAbsent(serviceIdentity.getLanguage(),
                    k -> new LanguageStatus(k)).addServer(serviceIdentity);
        }

        // combine all the languages used in this and the last status
        HashSet<SupportedLanguage> allLanguages = new HashSet<>(lastKnownStatus.keySet());
        allLanguages.addAll(currentStatus.keySet());

        // process per language
        for (SupportedLanguage language: allLanguages) {
            // can we chat now?
            boolean canChatNow = currentStatus.containsKey(language) && currentStatus.get(language).canChat();
            // could we chat last time we checked?
            boolean canChatLast = lastKnownStatus.containsKey(language) && lastKnownStatus.get(language).canChat();

            if (canChatNow && (!canChatLast)) {
                // something new has come up
                this.languageStatusUp(currentStatus.get(language));
            } else {
                if (!canChatNow && canChatLast) {
                    // something has gone down
                    this.languageStatusDown(language);
                } else {
                    if (canChatNow && canChatLast) {
                        // the service was running and is still running
                        // check if anything has changed
                        LanguageStatus langStatusNow = currentStatus.get(language);
                        LanguageStatus langStatusLast = lastKnownStatus.get(language);
                        // compare service signatures
                        if (!langStatusNow.getSignature().equals(langStatusLast.getSignature())) {
                            this.languageStatusChanged(langStatusNow);
                        }
                    }
                }
            }
        }

        this.lastKnownStatus = currentStatus;
    }

    /***
     * Called when a new service comes up
     * @param languageStatus
     */
    protected void languageStatusUp(LanguageStatus languageStatus) {
        this.logger.logInfo(LOGFROM, String.format("Ready to serve chat in language:%s%s",
                    languageStatus.getLanguage(), languageStatus.hasAiml() ? " (+AIML)" : ""),
                LogMap.map("lang", languageStatus.getLanguage()).put("status", languageStatus.getSignature()));
    }

    /***
     * Called when a running service goes down
     * @param language
     */
    protected void languageStatusDown(SupportedLanguage language) {
        this.logger.logInfo(LOGFROM, String.format("Chat server offline for language:%s", language),
                LogMap.map("lang", language));
    }

    /***
     * Called when a service keeps running but the status changes
     * e.g. we had AIML and now we don't
     * or if a server with a new version comes up
     * @param languageStatus
     */
    protected void languageStatusChanged(LanguageStatus languageStatus) {
        this.logger.logInfo(LOGFROM, String.format("Status changed serving chat in language:%s%s",
                languageStatus.getLanguage(), languageStatus.hasAiml() ? " (+AIML)" : ""),
                LogMap.map("lang", languageStatus.getLanguage()).put("status", languageStatus.getSignature()));
    }

    /***
     * Internal class that keeps track of status per language
     */
    public static class LanguageStatus {

        private final HashMap<BackendServerType, Set<String>> available;
        private final SupportedLanguage language;

        public LanguageStatus(SupportedLanguage language) {
            this.language = language;
            this.available = new HashMap<>();
        }

        /***
         * Add this server's capabilities to the language status
         * @param serviceIdentity
         */
        public void addServer(ServiceIdentity serviceIdentity) {
            this.available.computeIfAbsent(serviceIdentity.getServerType(),
                    k -> new HashSet<>()).add(serviceIdentity.getVersion());
        }

        /***
         * Create a string representation of this language status
         * This string will change if anything significant changes
         * @return
         */
        public String getSignature() {
            ArrayList<String> descriptions = new ArrayList<>();
            // describe each server in text
            for (Map.Entry<BackendServerType, Set<String>> entry : this.available.entrySet()) {
                for (String version: entry.getValue()) {
                    descriptions.add(String.format("%s:%s", entry.getKey().value(), version));
                }
            }
            // ensure we always use the same order
            descriptions.sort(Comparator.naturalOrder());
            // join them all together into one
            return String.join(",", descriptions);
        }

        /***
         * Are we AIML capable?
         * @return
         */
        public boolean hasAiml() {
            return this.available.containsKey(BackendServerType.AIML);
        }

        /***
         * Is there a working embedding server?
         * @return
         */
        public boolean hasEmb() {
            return this.available.containsKey(BackendServerType.EMB);
        }

        /***
         * Can we chat in this language?
         * @return
         */
        public boolean canChat() {
            return this.hasEmb();
        }

        public SupportedLanguage getLanguage() {
            return language;
        }
    }

}
