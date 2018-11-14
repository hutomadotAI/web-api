package com.hutoma.api.containers.sub;

public class IntegrationDataFacebook extends IntegrationData {

    private static final String MESSAGE_ORIGINATOR_ID = "originator_id";
    private static final String PAGE_TOKEN = "page_token";

    public IntegrationDataFacebook(final IntegrationData integrationData) {
        super(integrationData);
    }

    public IntegrationDataFacebook() {
        super(IntegrationType.FACEBOOK);
    }

    public String getMessageOriginatorId() {
        return getData().get(MESSAGE_ORIGINATOR_ID);
    }

    public String getPageToken() {
        return getData().get(PAGE_TOKEN);
    }

    public void setMessageOriginatorId(final String originatorId) {
        getData().put(MESSAGE_ORIGINATOR_ID, originatorId);
    }

    public void setPageToken(final String pageToken) {
        getData().put(PAGE_TOKEN, pageToken);
    }
}
