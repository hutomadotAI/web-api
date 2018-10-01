package com.hutoma.api.common;

import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyInvocation;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.mockito.Mockito;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class TestDataHelper {
    public static final UUID DEVID_UUID = UUID.fromString("113d39cb-7f43-40d7-8dee-17b25b205581");
    public static final String DEVID = DEVID_UUID.toString();
    public static final UUID AIID = UUID.fromString("41c6e949-4733-42d8-bfcf-95192131137e");

    public static final UUID SESSIONID = UUID.fromString("e6a7d7b4-245a-44ad-8018-5c0516583713");
    public static final UUID ALT_SESSIONID = UUID.fromString("f29a1eed-6094-464a-b335-c0885a501750");

    public static JerseyInvocation.Builder mockJerseyClient(JerseyClient fakeJerseyClient) {
        JerseyWebTarget jerseyWebTarget = Mockito.mock(JerseyWebTarget.class);
        JerseyInvocation.Builder builder = Mockito.mock(JerseyInvocation.Builder.class);
        when(fakeJerseyClient.target(any(String.class))).thenReturn(jerseyWebTarget);
        when(jerseyWebTarget.path(anyString())).thenReturn(jerseyWebTarget);
        when(jerseyWebTarget.queryParam(anyString(), anyString())).thenReturn(jerseyWebTarget);
        when(jerseyWebTarget.request()).thenReturn(builder);
        when(jerseyWebTarget.resolveTemplates(any())).thenReturn(jerseyWebTarget);
        when(jerseyWebTarget.property(any(),any())).thenReturn(jerseyWebTarget);
        return builder;
    }
}
