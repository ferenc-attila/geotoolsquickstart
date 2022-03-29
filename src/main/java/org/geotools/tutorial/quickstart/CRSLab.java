package org.geotools.tutorial.quickstart;

import org.geotools.data.*;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.referencing.CRS;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.action.SafeAction;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.ProgressListener;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

public class CRSLab {

    private File sourceFile;
    private SimpleFeatureSource featureSource;
    private MapContent map;

    public static void main(String[] args) {
        CRSLab crsLab = new CRSLab();
        crsLab.displayShapeFile();
    }

    private void displayShapeFile() {
        sourceFile = JFileDataStoreChooser.showOpenFile("shp", null);
        if (sourceFile == null) {
            throw new IllegalStateException("Source file cannot be null!");
        }

        FileDataStore dataStore;
        try {
            dataStore = FileDataStoreFinder.getDataStore(sourceFile);
            featureSource = dataStore.getFeatureSource();
        } catch (IOException ioe) {
            throw new IllegalStateException("Cannot read file " + sourceFile + "!", ioe);
        }

        map = new MapContent();
        Style style = SLD.createSimpleStyle(featureSource.getSchema());
        Layer layer = new FeatureLayer(featureSource, style);
        map.layers().add(layer);

        JMapFrame mapFrame = new JMapFrame(map);
        mapFrame.enableToolBar(true);
        mapFrame.enableStatusBar(true);

        JToolBar toolBar = mapFrame.getToolBar();
        toolBar.addSeparator();
        toolBar.add(new JButton(new ValidateGeometryAction()));
        toolBar.add(new JButton(new ExportShapeFileAction()));

        mapFrame.setSize(800, 600);
        mapFrame.setVisible(true);
    }

    private class ValidateGeometryAction extends SafeAction {

        ValidateGeometryAction() {
            super("Validate geomtry");
            putValue(Action.SHORT_DESCRIPTION, "Check each geometry");
        }

        public void action(ActionEvent event) {
            int numInvalid = validateFeatureGeometry(null);
            String message;
            if (numInvalid == 0) {
                message = "All feature geometries are valid";
            } else {
                message = "Invalid geometries: " + numInvalid;
            }
            JOptionPane.showMessageDialog(null, message, "Geometry results", JOptionPane.INFORMATION_MESSAGE);
        }

        private int validateFeatureGeometry(ProgressListener progressListener) {
            SimpleFeatureCollection featureCollection;
            try {
                featureCollection = featureSource.getFeatures();
            } catch (IOException ioe) {
                throw new IllegalStateException("Cannot get features!");
            }

            class ValidationVisitor implements FeatureVisitor {
                public int numInvalidGeometries = 0;

                public void visit(Feature feature) {
                    SimpleFeature simpleFeature = (SimpleFeature) feature;
                    Geometry geometry = (Geometry) simpleFeature.getDefaultGeometry();
                    if (geometry != null && !geometry.isValid()) {
                        numInvalidGeometries++;
                        System.out.println("Invalid geometry: " + simpleFeature.getID());
                    }
                }
            }

            ValidationVisitor visitor = new ValidationVisitor();

            try {
                featureCollection.accepts(visitor, progressListener);
                return visitor.numInvalidGeometries;
            } catch (IOException ioe) {
                throw new IllegalStateException("Invalid collection of features!");
            }
        }
    }

    class ExportShapeFileAction extends SafeAction {
        ExportShapeFileAction() {
            super("Export...");
            putValue(Action.SHORT_DESCRIPTION, "Export using current crs");
        }

        public void action(ActionEvent event) {
            exportToShapeFile();
        }

        private void exportToShapeFile() {
            SimpleFeatureType schema = featureSource.getSchema();
            JFileDataStoreChooser chooser = new JFileDataStoreChooser("shp");
            chooser.setDialogTitle("Save reprojected shapefile");
            chooser.setSaveFile(sourceFile);
            int returnValue = chooser.showSaveDialog(null);
            if (returnValue != JFileDataStoreChooser.APPROVE_OPTION) {
                return;
            }
            File file = chooser.getSelectedFile();
            if (file.equals(sourceFile)) {
                JOptionPane.showMessageDialog(null, "Cannot replace " + file + "!");
                return;
            }

            CoordinateReferenceSystem dataCRS = schema.getCoordinateReferenceSystem();
            CoordinateReferenceSystem worldCRS = map.getCoordinateReferenceSystem();
            boolean lenient = true;
            MathTransform transform;
            try {
                transform = CRS.findMathTransform(dataCRS, worldCRS, lenient);
            } catch (FactoryException fe) {
                throw new IllegalStateException("Cannot find transformation for " + dataCRS.toString() + "; " + worldCRS.toString());
            }
            SimpleFeatureCollection featureCollection;
            try {
                featureCollection = featureSource.getFeatures();
            } catch (IOException ioe) {
                throw new IllegalStateException("Cannot get collection of features");
            }

            DataStoreFactorySpi dataSourceFactory = new ShapefileDataStoreFactory();
            Map<String, Serializable> createProperties = new HashMap<>();
            try {
                createProperties.put("url", file.toURI().toURL());
                createProperties.put("create spatial index", Boolean.TRUE);
            } catch (MalformedURLException mue) {
                throw new IllegalStateException("Cannot create path!", mue);
            }

            DataStore dataStore;
            String createdName;
            try {
                dataStore = dataSourceFactory.createNewDataStore(createProperties);
                SimpleFeatureType featureType = SimpleFeatureTypeBuilder.retype(schema, worldCRS);
                dataStore.createSchema(featureType);
                createdName = dataStore.getTypeNames()[0];
            } catch (IOException ioe) {
                throw new IllegalStateException("Cannot create data store", ioe);
            }

            try (Transaction transaction = new DefaultTransaction("Reproject");
                    FeatureWriter<SimpleFeatureType, SimpleFeature> featureWriter
                         = dataStore.getFeatureWriterAppend(createdName, transaction);
                 SimpleFeatureIterator iterator = featureCollection.features()) {
                while (iterator.hasNext()) {
                    SimpleFeature feature = iterator.next();
                    SimpleFeature copy = featureWriter.next();
                    copy.setAttributes(feature.getAttributes());

                    Geometry originalGeometry = (Geometry) feature.getDefaultGeometry();
                    Geometry newGeometry = JTS.transform(originalGeometry, transform);

                    copy.setDefaultGeometry(newGeometry);
                    featureWriter.write();
                }
                transaction.commit();
                JOptionPane.showMessageDialog(null, "Export to shapefile completed!");
            } catch (IOException | TransformException e) {
                throw new IllegalStateException("Cannot write feature!", e);
            }
        }
    }
}
