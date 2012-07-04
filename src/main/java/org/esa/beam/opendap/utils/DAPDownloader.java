package org.esa.beam.opendap.utils;

import com.bc.io.FileDownloader;
import org.esa.beam.visat.VisatApp;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * User: tonio
 * Date: 04.07.12
 * Time: 15:54
 */
public class DAPDownloader {

    private final List<String> downloadUris;

    public DAPDownloader(List<String> downloadUris) {
        this.downloadUris = downloadUris;
    }

    public void saveAndOpenProducts() {
        final File targetDir = fetchTargetDirectory();
        if (targetDir != null && targetDir.isDirectory()) {
            final File[] files = downloadTo(targetDir);
            for (File file : files) {
                VisatApp.getApp().openProduct(file);
            }
        }
    }

    private File[] downloadTo(File targetDir) {
        final List<File> files = new ArrayList<File>();
        for (String dapURI : downloadUris) {
            final String errorMessagePrefix = "Unable to download '" + dapURI + "' due to Exception\n" +
                                              "Message: ";
            try {
                final URL fileUrl = new URI(dapURI).toURL();
                final File file = FileDownloader.downloadFile(fileUrl, targetDir, null);
                files.add(file);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, errorMessagePrefix + e.getMessage());
                e.printStackTrace();
            } catch (URISyntaxException e) {
                JOptionPane.showMessageDialog(null, errorMessagePrefix + e.getMessage());
                e.printStackTrace();
            }
        }
        return files.toArray(new File[files.size()]);
    }

    private File fetchTargetDirectory() {
        final JFileChooser chooser = new JFileChooser();
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select Target Directory");
        final int i = chooser.showDialog(null, "Save to directory");
        if (i == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }


}
