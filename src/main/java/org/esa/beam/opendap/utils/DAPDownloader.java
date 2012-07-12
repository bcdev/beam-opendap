package org.esa.beam.opendap.utils;

import com.bc.io.FileDownloader;
import org.esa.beam.util.StringUtils;
import org.esa.beam.visat.VisatApp;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.FileWriter;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;
import ucar.nc2.dods.DODSNetcdfFile;

import javax.swing.JOptionPane;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DAPDownloader {

    final List<String> dapUris;
    final List<String> fileURIs;
    final Set<File> downloadedFiles;

    public DAPDownloader(List<String> dapUris, List<String> fileURIs) {
        this.dapUris = dapUris;
        this.fileURIs = fileURIs;
        downloadedFiles = new HashSet<File>();
    }

    public void saveProducts(File targetDir) {
        if (targetDir != null && targetDir.isDirectory()) {
            downloadFilesWithDapAccess(targetDir);
            downloadFilesWithFileAccess(targetDir);
        } else {
            JOptionPane.showMessageDialog(null, "Could not save to directory' "+targetDir+"'");
        }
    }

    // todo - remove dependency to VisatApp
    public void openProducts(VisatApp app) {
        for (File file : downloadedFiles) {
            app.openProduct(file);
        }
    }

    private void downloadFilesWithDapAccess(File targetDir) {
        for (String dapURI : dapUris) {
            final String errorMessagePrefix = "Unable to download '" + dapURI + "' due to Exception\n" +
                    "Message: ";
            try {
                downloadDapFile(targetDir, dapURI);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, errorMessagePrefix + e.getMessage());
                e.printStackTrace();
            } catch (InvalidRangeException e) {
                e.printStackTrace();
            }
        }
    }

    private void downloadDapFile(File targetDir, String dapURI) throws IOException, InvalidRangeException {
        DODSNetcdfFile netcdfFile = new DODSNetcdfFile(dapURI);

        String[] uriComponents = dapURI.split("\\?");
        String fileName = dapURI.substring(uriComponents[0].lastIndexOf("/", uriComponents[0].length() - 1));
        String constraintExpression = "";
        if(uriComponents.length > 1) {
            constraintExpression = uriComponents[1];
        }
        writeNetcdfFile(targetDir, fileName, constraintExpression, netcdfFile);
    }

    void writeNetcdfFile(File targetDir, String fileName, String constraintExpression, DODSNetcdfFile sourceNetcdfFile) throws IOException {
        final File file = new File(targetDir, fileName);
        if (StringUtils.isNullOrEmpty(constraintExpression)) {
            FileWriter.writeToFile(sourceNetcdfFile, file.getAbsolutePath());
            downloadedFiles.add(file);
            return;
        }
        /**
         * algorithm:
         *   - get all variableNames vN from constraintExpression
         *   - get all dimensions d from sourceNetcdfFile
         *   - filter dimensions and variables
         *   - in new NetcdfFileWritable: create global attributes, dimensions and variables
         *   - create();
         *   - for all variables in new file:
         *      - get corresponding CE
         *      - array = sourceNetcdfFile.readWithCE();
         *      - variable.setData(array)
         *   - close();
         */

        final List<Variable> variables = sourceNetcdfFile.getVariables();
        final List<String> variableNames = new ArrayList<String>();
        for (Variable variable : variables) {
            variableNames.add(variable.getName());
        }
        final List<String> filteredVariables = filterVariables(variableNames, constraintExpression);
        final List<Dimension> filteredDimensions = filterDimensions(filteredVariables, sourceNetcdfFile);

        final NetcdfFileWriteable targetNetCDF = NetcdfFileWriteable.createNew(file.getAbsolutePath());
        for (Dimension filteredDimension : filteredDimensions) {
            targetNetCDF.addDimension(filteredDimension.getName(), filteredDimension.getLength(),
                    filteredDimension.isShared(), filteredDimension.isUnlimited(), filteredDimension.isVariableLength());
        }
        for (String filteredVariable : filteredVariables) {
            final Variable variable = sourceNetcdfFile.findVariable(NetcdfFile.escapeName(filteredVariable));
            final Variable targetVariable = targetNetCDF.addVariable(variable.getName(), variable.getDataType(), variable.getDimensions());
            for (Attribute attribute : variable.getAttributes()) {
                targetVariable.addAttribute(attribute);
            }
        }
        for (Attribute attribute : sourceNetcdfFile.getGlobalAttributes()) {
            targetNetCDF.addGlobalAttribute(attribute);
        }
        targetNetCDF.create();

        for (String filteredVariable : filteredVariables) {
            final Variable sourceVariable = sourceNetcdfFile.findVariable(NetcdfFile.escapeName(filteredVariable));
            String ceForVariable = getConstraintExpression(filteredVariable, constraintExpression);
            final Array values = sourceNetcdfFile.readWithCE(sourceVariable, ceForVariable);
            final int[] origin = getOrigin(filteredVariable, constraintExpression, sourceVariable.getDimensions().size());
            try {
                targetNetCDF.write(NetcdfFile.escapeName(filteredVariable), origin, values);
            } catch (InvalidRangeException e) {
                //todo handle Exception
                e.printStackTrace();
            }
        }
        targetNetCDF.close();
        downloadedFiles.add(file);
    }

    static String getConstraintExpression(String sourceVariable, String constraintExpression) {
        final String[] constraintExpressions = constraintExpression.split(",");
        for (String expression : constraintExpressions) {
            if(expression.startsWith(sourceVariable + "[")) {
                return expression;
            }
        }
        throw new IllegalArgumentException(
                MessageFormat.format("Source variable ''{0}'' must be included in expression ''{1}''.",
                        sourceVariable, constraintExpression));
    }

    static int[] getOrigin(String variableName, String constraintExpression, int dimensionCount) {
        int[] origin = new int[dimensionCount];
        Arrays.fill(origin, 0);
        if (StringUtils.isNullOrEmpty(constraintExpression)) {
            return origin;
        }
        final String[] variableConstraints = constraintExpression.split(",");
        for (String variableConstraint : variableConstraints) {
            if (variableConstraint.contains(variableName)) {
                if (!variableConstraint.contains("[")) {
                    return origin;
                }

                variableConstraint = variableConstraint.replace("]", "");
                String[] rangeConstraints = variableConstraint.split("\\[");
                if (rangeConstraints.length - 1 > dimensionCount) {
                    throw new IllegalArgumentException(
                            MessageFormat.format("Illegal expression: ''{0}'' for variable ''{1}''.",
                                    constraintExpression, variableName));
                }
                for (int i = 1; i < rangeConstraints.length; i++) {
                    String rangeConstraint = rangeConstraints[i];
                    String[] rangeComponents = rangeConstraint.split(":");
                    origin[i - 1] = Integer.parseInt(rangeComponents[0]);
                }

            }
        }
        return origin;
    }

    static List<String> filterVariables(List<String> variableNames, String constraintExpression) {
        final List<String> filteredVariables = new ArrayList<String>();
        final List<String> constrainedVariableNames = getVariableNames(constraintExpression);
        if (constrainedVariableNames == null || constrainedVariableNames.isEmpty()) {
            return variableNames;
        }
        for (String variableName : variableNames) {
            if (constrainedVariableNames.contains(variableName)) {
                filteredVariables.add(variableName);
            }
        }
        if (filteredVariables.isEmpty()) {
            return variableNames;
        }
        return filteredVariables;
    }

    static List<Dimension> filterDimensions(List<String> variables, NetcdfFile netcdfFile) {
        final List<Dimension> filteredDimensions = new ArrayList<Dimension>();

        for (String variableName : variables) {
            final Variable variable = netcdfFile.findVariable(NetcdfFile.escapeName(variableName));
            for (Dimension dimension : variable.getDimensions()) {
                if (!filteredDimensions.contains(dimension)) {
                    filteredDimensions.add(dimension);
                }
            }
        }

        return filteredDimensions;
    }

    static List<String> getVariableNames(String constraintExpression) {
        if (StringUtils.isNullOrEmpty(constraintExpression)) {
            return null;
        }
        List<String> variableNames = new ArrayList<String>();
        String[] constraints = constraintExpression.split(",");
        for (String constraint : constraints) {
            if (constraint.contains("[")) {
                variableNames.add(constraint.substring(0, constraint.indexOf("[")));
            } else {
                variableNames.add(constraint);
            }
        }
        return variableNames;
    }

    private void downloadFilesWithFileAccess(File targetDir) {
        for (String fileURI : fileURIs) {
            try {
                downloadFile(targetDir, fileURI);
            } catch (IOException e) {
                //todo log warning
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    void downloadFile(File targetDir, String fileURI) throws URISyntaxException, IOException {
        final URL fileUrl = new URI(fileURI).toURL();
        final File file = FileDownloader.downloadFile(fileUrl, targetDir, null);
        downloadedFiles.add(file);
    }

}
