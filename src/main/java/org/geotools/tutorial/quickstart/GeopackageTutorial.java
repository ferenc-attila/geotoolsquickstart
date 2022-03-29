package org.geotools.tutorial.quickstart;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureReader;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geopkg.GeoPackage;
import org.geotools.geopkg.GeoPkgDataStoreFactory;
import org.geotools.geopkg.mosaic.GeoPackageReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GeopackageTutorial {

    public static void main(String[] args) {
        Map<String, String> geopackageParameters = new HashMap<>();

        geopackageParameters.put("dbtype", "geopkg");
        geopackageParameters.put("database", "E:\\feren\\Development\\Java\\geotoolstutorials\\quickstart\\src\\main\\resources\\locations.gpkg");

        DataStore dataStore;
        SimpleFeatureSource featureSource;
        SimpleFeatureCollection featureCollection;

        try {
            dataStore = DataStoreFinder.getDataStore(geopackageParameters);
            featureSource = dataStore.getFeatureSource("locations");
            featureCollection = featureSource.getFeatures();
        } catch (IOException ioe) {
            throw new IllegalStateException("Cannot read datastore: " + geopackageParameters.get("database") + "!");
        }

        while (featureCollection.features().hasNext()) {
            SimpleFeature feature = featureCollection.features().next();
            System.out.println(feature.getDefaultGeometry().toString());
        }
    }
}
