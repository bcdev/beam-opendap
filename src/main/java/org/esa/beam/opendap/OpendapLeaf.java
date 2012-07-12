/*
 * Copyright (C) 2011 Brockmann Consult GmbH (info@brockmann-consult.de)
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.beam.opendap;

import thredds.catalog.InvDataset;

import java.util.ArrayList;

/**
* This is a container for opendap-leaf related information.
*
* @author Sabine Embacher
* @author Tonio Fincke
*
*/
public class OpendapLeaf {

    private final String name;
    private final InvDataset dataset;
    private boolean dapAccess;
    private boolean fileAccess;
    private boolean catalogReference;
    private String catalogUri;
    private String dapUri;
    private String fileUri;
    private ArrayList<DAPVariable> variables;

    public OpendapLeaf(String name) {
        this(name, null);
    }

    public OpendapLeaf(String name, InvDataset dataset) {
        this.name = name;
        this.dataset = dataset;
        this.variables = new ArrayList<DAPVariable>();
    }

    public boolean isCatalogReference() {
        return catalogReference;
    }

    public void setCatalogReference(boolean catalogReference) {
        this.catalogReference = catalogReference;
    }

    public boolean isDapAccess() {
        return dapAccess;
    }

    public boolean isFileAccess() {
        return fileAccess;
    }

    public String getDasUri() {
        return getDapUri() + ".das";
    }

    public String getDdsUri() {
        return getDapUri() + ".dds";
    }

    public String getDdxUri() {
        return getDapUri() + ".ddx";
    }

    public String getDapUri() {
        return dapUri;
    }

    public void setDapUri(String dapUri) {
        this.dapUri = dapUri;
    }

    public String getFileUri() {
        return fileUri;
    }

    public void setFileUri(String fileUri) {
        this.fileUri = fileUri;
    }

    public void setDapAccess(boolean dapAccess) {
        this.dapAccess = dapAccess;
    }

    public void setFileAccess(boolean fileAccess) {
        this.fileAccess = fileAccess;
    }

    public String getCatalogUri() {
        return catalogUri;
    }

    public void setCatalogUri(String catalogUri) {
        this.catalogUri = catalogUri;
    }

    public DAPVariable[] getDAPVariables(){
        return variables.toArray(new DAPVariable[variables.size()]);
    }

    public void addDAPVariable(DAPVariable variable){
        variables.add(variable);
    }

    public String getName() {
        return name;
    }

    public InvDataset getDataset() {
        return dataset;
    }

    @Override
    public String toString() {
        return name;
    }
}
