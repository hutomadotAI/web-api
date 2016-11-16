package com.hutoma.api.containers.sub;

/**
 * Created by mauriziocibelli on 29/10/16.
 */
public class MeshVariable {

    private final String aiid;
    private final String aiid_mesh;
    private final String name;
    private final String description;
    private final String licenceType;
    private final Float licenceFee;
    private final Float rating;
    private final int numberOfActivations;
    private final boolean isBanned;
    private final String iconPath;
    private final String widgetColor;

    public MeshVariable(final String aiid, final String aiidMesh, final String name, final String description,
                        final String licenceType, final Float licenceFee, final Float rating,
                        final int numberOfActivations, final boolean isBanned, final String iconPath,
                        final String widgetColor) {

        this.aiid = aiid;
        this.aiid_mesh = aiidMesh;
        this.name = name;
        this.description = description;
        this.licenceType = licenceType;
        this.licenceFee = licenceFee;
        this.rating = rating;
        this.numberOfActivations = numberOfActivations;
        this.isBanned = isBanned;
        this.iconPath = iconPath;
        this.widgetColor = widgetColor;
    }

    public String getAiid() {
        return this.aiid;
    }

    public String getAiidMesh() {
        return this.aiid_mesh;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getLicenceType() {
        return this.licenceType;
    }

    public float getLicenceFee() {
        return this.licenceFee;
    }

    public float getRating() {
        return this.rating;
    }

    public int getNumberOfActivations() {
        return this.numberOfActivations;
    }

    public boolean isBanned() {
        return this.isBanned;
    }

    public String getIconPath() {
        return this.iconPath;
    }

    public String getWidgetColor() {
        return this.widgetColor;
    }

}
