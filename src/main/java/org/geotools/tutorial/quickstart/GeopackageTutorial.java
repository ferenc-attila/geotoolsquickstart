package org.geotools.tutorial.quickstart;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.type.AttributeType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeopackageTutorial {

    public static void main(String[] args) {
        Map<String, String> geopackageParameters = new HashMap<>();

        geopackageParameters.put("dbtype", "geopkg");
        geopackageParameters.put("database", "src/main/resources/locations.gpkg");

        DataStore dataStore;
        SimpleFeatureSource featureSource;
        SimpleFeatureCollection featureCollection;

        try {
            dataStore = DataStoreFinder.getDataStore(geopackageParameters);
            if (dataStore == null) {
                throw new IllegalStateException("Couldn't connect! Check parameters!");
            }
            featureSource = dataStore.getFeatureSource("locations");
            String crs = featureSource.getSchema().getCoordinateReferenceSystem().toString();
            List<AttributeType> fields = featureSource.getSchema().getTypes();
            List<String> fieldNames = new ArrayList<>();
            for (AttributeType actual : fields) {
                fieldNames.add(actual.getName().toString());
            }

            featureCollection = featureSource.getFeatures();
        } catch (IOException ioe) {
            throw new IllegalStateException("Cannot read datastore: " + geopackageParameters.get("database") + "!");
        }
//
//        while (featureCollection.features().hasNext()) {
//            SimpleFeature feature = featureCollection.features().next();
//            System.out.println(feature.toString());
//            System.out.println(feature.getDefaultGeometry().toString());
//        }
    }
}
