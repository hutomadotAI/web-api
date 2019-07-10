package com.hutoma.api;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.SupportedLanguage;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.aiservices.ControllerConnector;
import com.hutoma.api.containers.ApiServersAvailable;
import com.hutoma.api.containers.ServiceIdentity;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerMonitor extends TimerTask {

    private final AtomicBoolean runMonitor;
    private final Timer timer;
    private final ILogger logger;
    private final Config config;
    private final ControllerConnector controllerConnector;
    private final JsonSerializer jsonSerializer;
    private HashMap<SupportedLanguage, LanguageStatus> lastKnownStatus;

    private static final String LOGFROM = "servermonitor";

    @Inject
    public ServerMonitor(Timer timer, ILogger logger, Config config,
                         ControllerConnector controllerConnector, JsonSerializer jsonSerializer) {
        this.timer = timer;
        this.logger = logger;
        this.config = config;
        this.controllerConnector = controllerConnector;
        this.jsonSerializer = jsonSerializer;
        this.runMonitor = new AtomicBoolean(true);
        this.lastKnownStatus = new HashMap<>();
    }

    public void initialise() {
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

        ApiServersAvailable serversAvailable = controllerConnector.getServiceIdentities(this.jsonSerializer);

        HashMap<SupportedLanguage, LanguageStatus> currentStatus = new HashMap<>();
        for(ServiceIdentity serviceIdentity: serversAvailable.getServers()) {
            currentStatus.computeIfAbsent(serviceIdentity.getLanguage(),
                    k -> new LanguageStatus(k)).addServer(serviceIdentity);
        }

        HashSet<SupportedLanguage> allLanguages = new HashSet<>(lastKnownStatus.keySet());
        allLanguages.addAll(currentStatus.keySet());

        for (SupportedLanguage language: allLanguages) {
            boolean canChatNow = currentStatus.containsKey(language) && currentStatus.get(language).canChat();
            boolean canChatLast = lastKnownStatus.containsKey(language) && lastKnownStatus.get(language).canChat();
            if (canChatNow && (!canChatLast)) {
                this.languageStatusUp(currentStatus.get(language));
            } else {
                if (!canChatNow && canChatLast) {
                    this.languageStatusDown(language);
                } else {
                    if (canChatNow && canChatLast) {
                        LanguageStatus langStatusNow = currentStatus.get(language);
                        LanguageStatus langStatusLast = lastKnownStatus.get(language);
                        if (!langStatusNow.getSignature().equals(langStatusLast.getSignature())) {
                            this.languageStatusChanged(langStatusNow);
                        }
                    }
                }
            }
        }

        this.lastKnownStatus = currentStatus;
    }

    protected void languageStatusUp(LanguageStatus languageStatus) {
        this.logger.logInfo(LOGFROM, String.format("Ready to serve chat in language:%s%s",
                    languageStatus.getLanguage(), languageStatus.hasAiml()? " (+AIML)": ""),
                LogMap.map("lang", languageStatus).put("status", languageStatus.getSignature()));
    }

    protected void languageStatusDown(SupportedLanguage language) {
        this.logger.logInfo(LOGFROM, String.format("Chat server offline for language:%s", language),
                LogMap.map("lang", language));
    }

    protected void languageStatusChanged(LanguageStatus languageStatus) {
        this.logger.logInfo(LOGFROM, String.format("Status changed serving chat in language:%s%s",
                languageStatus.getLanguage(), languageStatus.hasAiml()? " (+AIML)": ""),
                LogMap.map("lang", languageStatus).put("status", languageStatus.getSignature()));
    }

    public class LanguageStatus {

        private final HashMap<BackendServerType, Set<String>> available;
        private final SupportedLanguage language;

        public LanguageStatus(SupportedLanguage language) {
            this.language = language;
            this.available = new HashMap<>();
        }

        public void addServer(ServiceIdentity serviceIdentity) {
            this.available.computeIfAbsent(serviceIdentity.getServerType(),
                    k -> new HashSet<>()).add(serviceIdentity.getVersion());
        }

        public String getSignature() {
            ArrayList<String> descriptions = new ArrayList<>();
            for (Map.Entry<BackendServerType, Set<String>> entry : this.available.entrySet()) {
                for(String version: entry.getValue()) {
                    descriptions.add(String.format("%s:%s", entry.getKey().value(), version));
                }
            }
            descriptions.sort(Comparator.naturalOrder());
            return String.join(",", descriptions);
        }

        public boolean hasAiml() {
            return this.available.containsKey(BackendServerType.AIML);
        }

        public boolean hasEmb() {
            return this.available.containsKey(BackendServerType.EMB);
        }

        public boolean canChat() {
            return this.hasEmb();
        }

        public SupportedLanguage getLanguage() {
            return language;
        }
    }

}
