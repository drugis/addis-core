package org.drugis.addis.models;

/**
 * Created by connor on 7/3/15.
 */
public class DetailsCommand {
    private String from;
    private String to;

    public DetailsCommand() {
    }

    public DetailsCommand(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }
}
