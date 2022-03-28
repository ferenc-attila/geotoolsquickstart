package org.geotools.tutorial.quickstart;

import org.geotools.data.*;
import org.geotools.data.collection.SpatialIndexFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.data.JFileDataStoreChooser;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

public class Quickstart {

    public static void main(String[] args) {
        File file = JFileDataStoreChooser.showOpenFile("shp", null);
        if (file == null) {
            return;
        }

        Map<String, Object> params = new HashMap<>();
        try {
            params.put("url", file.toURI().toURL());
            params.put("create spatial index", true);
            params.put("memory mapped buffer", false);
            params.put("charset", "ISO-8859-2");
        } catch (MalformedURLException mue) {
            throw new IllegalStateException("Malformed url!", mue);
        }

        SimpleFeatureSource featureSource;
        SimpleFeatureSource cachedSource;

        try {
            DataStore dataStore = DataStoreFinder.getDataStore(params);
            featureSource = dataStore.getFeatureSource(dataStore.getTypeNames()[0]);
            cachedSource =
                    DataUtilities.source(
                            new SpatialIndexFeatureCollection(featureSource.getFeatures()));
        } catch (IOException ioe) {
            throw new IllegalStateException("Cannot read shapefile!", ioe);
        }



        MapContent map = new MapContent();
        map.setTitle("Quickstart using cached features");

        Style style = SLD.createSimpleStyle(featureSource.getSchema());
        Layer layer = new FeatureLayer(cachedSource, style);

        map.addLayer(layer);

        JMapFrame.showMap(map);
    }
}
