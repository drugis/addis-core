package org.drugis.addis.models;

/**
 * Created by connor on 6/24/15.
 */
public class ModelCommand {

    String title;
    String linearModel;
    String modelType;

    public ModelCommand() {
    }

    public ModelCommand(String title, String linearModel, String modelType) {
        this.title = title;
        this.linearModel = linearModel;
        this.modelType = modelType;
    }

    public String getTitle() {
        return title;
    }

    public String getLinearModel() {
        return linearModel;
    }

    public String getModelType() {
        return modelType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModelCommand that = (ModelCommand) o;

        if (!title.equals(that.title)) return false;
        if (!linearModel.equals(that.linearModel)) return false;
        return modelType.equals(that.modelType);

    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + linearModel.hashCode();
        result = 31 * result + modelType.hashCode();
        return result;
    }
}
