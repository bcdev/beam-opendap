package org.esa.beam.opendap;

import opendap.dap.DArrayDimension;

import java.util.ArrayList;
import java.util.Arrays;

public class DAPVariable {

    private final String name;
    private final String type;
    private final String dataType;
    private final DArrayDimension[] dimensions;

    public DAPVariable(String name, String type, String dataType, DArrayDimension[] dimensions) {
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DAPVariable that = (DAPVariable) o;

        if (dataType != null ? !dataType.equals(that.dataType) : that.dataType != null) return false;
        if (!Arrays.equals(dimensions, that.dimensions)) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (dataType != null ? dataType.hashCode() : 0);
        result = 31 * result + (dimensions != null ? Arrays.hashCode(dimensions) : 0);
        return result;
    }
}
