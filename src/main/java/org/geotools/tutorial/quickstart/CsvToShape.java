package org.geotools.tutorial.quickstart;

import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvToShape {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel((UIManager.getSystemLookAndFeelClassName()));
        } catch (ClassNotFoundException | IllegalAccessException | UnsupportedLookAndFeelException | InstantiationException e) {
            throw new IllegalStateException("cannot create user interface!", e);
        }

        File file = JFileDataStoreChooser.showOpenFile("csv", null);
        if (file == null) {
            throw new IllegalStateException("File is null!");
        }

        SimpleFeatureType type;
        try {
            type = DataUtilities.createType(
                    "Location",
                    "the_geom:Point:srid=4326," + "name:String," + "number:Integer");
        } catch (SchemaException sce) {
            throw new IllegalStateException("Cannot create geotools schema!", sce);
        }

        List<SimpleFeature> features = new ArrayList<>();

        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(type);

        try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() > 0) {
                    String[] cells = line.split(",");
                    double latitude = Double.parseDouble(cells[0].trim());
                    double longitude = Double.parseDouble(cells[1].trim());
                    String name = cells[2].trim();
                    int number = Integer.parseInt(cells[3].trim());

                    Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));

                    featureBuilder.add(point);
                    featureBuilder.add(name);
                    featureBuilder.add(number);
                    SimpleFeature feature = featureBuilder.buildFeature(null);
                    features.add(feature);
                }
            }
        } catch (IOException ioe) {
            throw new IllegalStateException("Cannot read file. " + file + "!", ioe);
        }

        File newFile = getNewShapeFile(file);

        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        Map<String, Serializable> params = new HashMap<>();
        try {
            params.put("url", newFile.toURI().toURL());
            params.put("create spatial index", Boolean.TRUE);
        } catch (MalformedURLException mue) {
            throw new IllegalStateException("Cannot create file!", mue);
        }

        ShapefileDataStore shapeDataStore;
        try {
            shapeDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
            shapeDataStore.createSchema(type);
        } catch (IOException ioe) {
            throw new IllegalStateException("Cannot create shapefile datastore!", ioe);
        }

        Transaction transaction = new DefaultTransaction("create");

        SimpleFeatureSource featureSource;

        try {
            String typeName = shapeDataStore.getTypeNames()[0];
            featureSource = shapeDataStore.getFeatureSource(typeName);
            SimpleFeatureType shapeType = featureSource.getSchema();
        } catch (IOException ioe) {
            throw new IllegalStateException("Cannot get datastore typenames!", ioe);
        }

        SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
        SimpleFeatureCollection collection = new ListFeatureCollection(type, features);
        featureStore.setTransaction(transaction);
        try {
            featureStore.addFeatures(collection);
            transaction.commit();
        } catch (IOException ioe) {
            throw new IllegalStateException("Cannot write features into shapefile!", ioe);
        }
    }

    private static File getNewShapeFile(File csvFile) {
        String path = csvFile.getAbsolutePath();
        String newPath = path.substring(0, path.length() - 3) + "shp";

        JFileDataStoreChooser chooser = new JFileDataStoreChooser("shp");
        chooser.setDialogTitle("Save shapefile");
        chooser.setSelectedFile(new File(newPath));

        int returnValue = chooser.showSaveDialog(null);

        if (returnValue != JFileDataStoreChooser.APPROVE_OPTION) {
            System.exit(0);
        }

        File newFile = chooser.getSelectedFile();

        if (newFile.equals(csvFile)) {
            throw new IllegalStateException("Cannot replace " + csvFile);
        }

        return newFile;
    }
}
