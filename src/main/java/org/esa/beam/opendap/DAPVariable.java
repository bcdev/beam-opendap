package org.esa.beam.opendap;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import opendap.dap.DArrayDimension;
import org.esa.beam.util.Guardian;

public class DAPVariable {

    private final String name;
    private final String type;
    private final String dataType;
    private final DArrayDimension[] dimensions;

    public DAPVariable(String name, String type, String dataType, DArrayDimension[] dimensions) {
        Guardian.assertNotNullOrEmpty("name", name);
        Guardian.assertTrue("'" + name + "' is not a valid name", name.trim().length() > 0);
//        Guardian.assertNotNullOrEmpty("type", type);
//        Guardian.assertTrue("'" + type + "' is not a valid type", type.trim().length() > 0);
//        Guardian.assertNotNullOrEmpty("dataType", dataType);
//        Guardian.assertTrue("'" + dataType + "' is not a valid dataType", dataType.trim().length() > 0);
//        Guardian.assertNotNullOrEmpty("dimensions", dimensions);

        this.name = name;
        this.type = type;
        this.dataType = dataType;
        this.dimensions = dimensions;
    }

    public String getName() {
        return name;
    }

//    public String getType() {
//        return type;
//    }
//
//    public String getDataType() {
//        return dataType;
//    }
//
//    public DArrayDimension[] getDimensions() {
//        return dimensions;
//    }

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
        if (!Arrays.equals(dimensions, that.dimensions)) {
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

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (dataType != null ? dataType.hashCode() : 0);
        result = 31 * result + (dimensions != null ? Arrays.hashCode(dimensions) : 0);
        return result;
    }

    public String getInfotext() {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        pw.println("Name: " + getName());
//        pw.println("Type: " + getType());
//        pw.println("Dimensions: " + getNumDimensions());
//        pw.println("Datatype: " + getDataType());
//        for (DArrayDimension dimension : dimensions) {
//            pw.println("dim(" + dimension.getName() + ") size: " + dimension.getSize());
//        }
        pw.close();
        return sw.toString();
    }

//    public int getNumDimensions() {
//        return dimensions.length;
//    }
}
