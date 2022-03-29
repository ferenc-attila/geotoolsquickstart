package org.geotools.tutorial.quickstart;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureReader;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeType;
import org.opengis.geometry.Geometry;

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

        SimpleFeatureIterator iterator = featureCollection.features();

        while(iterator.hasNext()) {
            SimpleFeature feature = iterator.next();
            String geometryString = feature.getDefaultGeometry().toString().replace("(", "").replace(")", "");
            String[] geometry = geometryString.split(" ");
            Location actual = new Location(
                    Long.parseLong(feature.getID().substring(feature.getID().indexOf('.') + 1)),
                    Double.parseDouble(geometry[1]),
                    Double.parseDouble(geometry[2]),
                    feature.getAttribute("name").toString(),
                    Integer.parseInt(feature.getAttribute("number").toString())
            );
        }
    }
}
