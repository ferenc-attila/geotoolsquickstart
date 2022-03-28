package org.geotools.tutorial.quickstart;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GeopackageTutorial {

    public static void main(String[] args) {
        Map<String, String> geopackageParameters = new HashMap<>();

        geopackageParameters.put("dbtype", "geopkg");
        geopackageParameters.put("database", "sample.gpkg");

        DataStore dataStore;

        try {
            dataStore = DataStoreFinder.getDataStore(geopackageParameters);
        } catch (IOException ioe) {
            throw new IllegalStateException("Cannot read file!", ioe);
        }

        SimpleFeatureType featureType;

        try {
            featureType = dataStore.getSchema("sampleLayer");
        } catch (IOException ioe) {
            throw new IllegalStateException("Cannot read layer!", ioe);
        }
    }
}
