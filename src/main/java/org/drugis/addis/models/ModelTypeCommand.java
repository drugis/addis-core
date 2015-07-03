package org.drugis.addis.models;

/**
 * Created by connor on 7/3/15.
 */
public class ModelTypeCommand {
    private String type;
    private DetailsCommand details;

    public ModelTypeCommand() {
    }

    public ModelTypeCommand(String type) {
        this.type = type;
    }

    public ModelTypeCommand(String type, DetailsCommand details) {
        this.type = type;
        this.details = details;
    }

    public String getType() {
        return type;
    }

    public DetailsCommand getDetails() {
        return details;
    }
}
