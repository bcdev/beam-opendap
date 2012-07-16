package org.esa.beam.opendap;

import com.bc.ceres.core.Assert;
import opendap.dap.DArrayDimension;
import org.esa.beam.util.StringUtils;

public class DAPVariable implements Comparable<DAPVariable> {

    private final String name;
    private final String type;
    private final String dataType;
    private final DArrayDimension[] dimensions;

    public DAPVariable(String name, String type, String dataType, DArrayDimension[] dimensions) {
        Assert.argument(StringUtils.isNotNullAndNotEmpty(name), "name");
        Assert.argument(name.trim().length() > 0, "'" + name + "' is not a valid name");
        Assert.argument(StringUtils.isNotNullAndNotEmpty(type), "type");
        Assert.argument(type.trim().length() > 0, "'" + type + "' is not a valid type");
        Assert.argument(StringUtils.isNotNullAndNotEmpty(dataType), "dataType");
        Assert.argument(dataType.trim().length() > 0, "'" + dataType + "' is not a valid dataType");
        Assert.argument(dimensions != null && dimensions.length > 0, "dimensions");

        this.name = name;
        this.type = type;
        this.dataType = dataType;
        this.dimensions = dimensions;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDataType() {
        return dataType;
    }

    public DArrayDimension[] getDimensions() {
        return dimensions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DAPVariable that = (DAPVariable) o;

        if (dataType != null ? !dataType.equals(that.dataType) : that.dataType != null) {
            return false;
        }

        boolean dimensionsAreEqual = dimensionsAreEqual(that);
        if (!dimensionsAreEqual) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (type != null ? !type.equals(that.type) : that.type != null) {
            return false;
        }

        return true;
    }

    private boolean dimensionsAreEqual(DAPVariable that) {
        if (dimensions == that.dimensions) {
            return true;
        }
        if (dimensions == null || that.dimensions == null) {
            return false;
        }

        if (dimensions.length != that.dimensions.length) {
            return false;
        }
        for (int i = 0; i < dimensions.length; i++) {
            DArrayDimension dimension1 = dimensions[i];
            DArrayDimension dimension2 = that.dimensions[i];
            if (dimension1 == null ^ dimension2 == null) {
                return false;
            }
            if (dimension1 == null) {
                return true;
            }

            boolean dimsEqual = dimension1.getSize() == dimension2.getSize();
            dimsEqual &= dimension1.getName().equals(dimension2.getName());
            if (!dimsEqual) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (dataType != null ? dataType.hashCode() : 0);
        result = 31 * result + (dimensions != null ? dimensions.length : 0);
        return result;
    }

    public String getInfotext() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Name: " + getName() + "\n");
        builder.append("Type: " + getType() + "\n");
        builder.append("Dimensions: " + getNumDimensions() + "\n");
        builder.append("Datatype: " + getDataType() + "\n");
        for (int i = 0; i < dimensions.length; i++) {
            final DArrayDimension dimension = dimensions[i];
            builder.append("dim(" + dimension.getName() + ") size: " + dimension.getSize());
            if(i < dimensions.length - 1) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    public int getNumDimensions() {
        return dimensions.length;
    }

    @Override
    public int compareTo(DAPVariable o) {
        if (name.equals(o.getName()) && type.equals(o.getType()) && dataType.equals(o.getDataType()) &&
            dimensions.length == o.getNumDimensions()) {
            final DArrayDimension[] oDimensions = o.getDimensions();
            for (int i = 0; i < getNumDimensions(); i++) {
                if (!dimensions[i].equals(oDimensions[i])) {
                    return -1;
                }
            }
            return 0;
        } else {
            return -1;
        }
    }
}
