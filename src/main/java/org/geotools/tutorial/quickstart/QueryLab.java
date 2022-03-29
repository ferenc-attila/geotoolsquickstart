package org.geotools.tutorial.quickstart;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.Query;
import org.geotools.data.postgis.PostgisNGDataStoreFactory;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.swing.action.SafeAction;
import org.geotools.swing.data.JDataStoreWizard;
import org.geotools.swing.table.FeatureCollectionTableModel;
import org.geotools.swing.wizard.JWizard;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Map;

@SuppressWarnings("serial")
public class QueryLab extends JFrame {

    private DataStore dataStore;
    private JComboBox<String> featureTypeCBox;
    private JTable table;
    private JTextField textField;

    public QueryLab() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        textField = new JTextField(80);
        textField.setText("include");
        getContentPane().add(textField, BorderLayout.NORTH);

        table = new JTable();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setModel(new DefaultTableModel(5, 5));
        table.setPreferredScrollableViewportSize(new Dimension(500, 200));

        JScrollPane scrollPane = new JScrollPane(table);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        featureTypeCBox = new JComboBox<>();
        menuBar.add(featureTypeCBox);

        JMenu dataMenu = new JMenu();
        menuBar.add(dataMenu);
        pack();

        fileMenu.add(
                new SafeAction("Open shapefile...") {
                    @Override
                    public void action(ActionEvent actionEvent) {
                        connect(new ShapefileDataStoreFactory());
                    }
                });

        fileMenu.add(
                new SafeAction("Connect to PostGIS database ...") {
                    @Override
                    public void action(ActionEvent actionEvent) {
                        connect(new PostgisNGDataStoreFactory());
                    }
                });

        fileMenu.add(
                new SafeAction("Connect to DataStore") {
                    @Override
                    public void action(ActionEvent actionEvent) {
                        connect(null);
                    }
                });

        fileMenu.addSeparator();

        fileMenu.add(
                new SafeAction("Exit") {
                    @Override
                    public void action(ActionEvent actionEvent) {
                        System.exit(0);
                    }
                });

        dataMenu.add(
                new SafeAction("Get features") {
                    @Override
                    public void action(ActionEvent actionEvent) {
                        filterFeatures();
                    }
                });

        dataMenu.add(
                new SafeAction("Count") {
                    @Override
                    public void action(ActionEvent actionEvent) {
                        countFeatures();
                    }
                });

        dataMenu.add(
                new SafeAction("Geometry") {
                    @Override
                    public void action(ActionEvent actionEvent) {
                        queryFeatures();
                    }
                });
    }

    private SimpleFeatureCollection getFeatureCollection(SimpleFeatureSource featureSource, Filter filter) {
        SimpleFeatureCollection featureCollection;
        try {
            featureCollection = featureSource.getFeatures(filter);
        } catch (IOException ioe) {
            throw new IllegalStateException("Cannot get features!", ioe);
        }
        return featureCollection;
    }

    private Filter getFilter() {
        Filter filter;
        try {
            filter = CQL.toFilter(textField.getText());
        } catch (CQLException ce) {
            throw new IllegalStateException("Invalid expression: " + textField.getText(), ce);
        }
        return filter;
    }

    private SimpleFeatureSource getFeatureSource(String typeName) {
        SimpleFeatureSource featureSource;
        try {
            featureSource = dataStore.getFeatureSource(typeName);
        } catch (IOException ioe) {
            throw new IllegalStateException("Cannot get feature source from datastore", ioe);
        }
        return featureSource;
    }

    private void connect(DataStoreFactorySpi format) {
        JDataStoreWizard wizard = new JDataStoreWizard(format);
        int result = wizard.showModalDialog();
        if (result == JWizard.FINISH) {
            Map<String, Object> connectionParameters = wizard.getConnectionParameters();
            try {
                dataStore = DataStoreFinder.getDataStore(connectionParameters);
            } catch (IOException ioe) {
                throw new IllegalStateException("Cannot find data store");
            }
            if (dataStore == null) {
                JOptionPane.showMessageDialog(null, "Couldn't connect! Check parameters!");
            }
            updateUI();
        }
    }

    private void updateUI() {
        try {
            ComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>(dataStore.getTypeNames());
            featureTypeCBox.setModel(comboBoxModel);
            table.setModel(new DefaultTableModel());
        } catch (IOException ioe) {
            throw new IllegalStateException("Cannot find data store");
        }
    }

    private void filterFeatures() {
        String typeName = (String) featureTypeCBox.getSelectedItem();

        SimpleFeatureSource featureSource = getFeatureSource(typeName);

        Filter filter = getFilter();

        SimpleFeatureCollection featureCollection = getFeatureCollection(featureSource, filter);
        FeatureCollectionTableModel tableModel = new FeatureCollectionTableModel(featureCollection);
        table.setModel(tableModel);
    }

    private void countFeatures() {
        String typeName = (String) featureTypeCBox.getSelectedItem();

        SimpleFeatureSource featureSource = getFeatureSource(typeName);

        Filter filter = getFilter();

        SimpleFeatureCollection featureCollection = getFeatureCollection(featureSource, filter);

        int count = featureCollection.size();
        JOptionPane.showMessageDialog(textField, "Number of selected features: " + count);
    }

    private void queryFeatures() {
        String typeName = (String) featureTypeCBox.getSelectedItem();

        SimpleFeatureSource featureSource = getFeatureSource(typeName);

        FeatureType featureType = featureSource.getSchema();
        String geometryColumnName = featureType.getGeometryDescriptor().getLocalName();

        Filter filter = getFilter();

        Query query = new Query(typeName, filter, new String[] {geometryColumnName});

        SimpleFeatureCollection featureCollection;
        try {
            featureCollection = featureSource.getFeatures(query);
        } catch (IOException ioe) {
            throw new IllegalStateException("Cannot get features!", ioe);
        }

        FeatureCollectionTableModel tableModel = new FeatureCollectionTableModel(featureCollection);
        table.setModel(tableModel);
    }

    public static void main(String[] args) {
        JFrame frame = new QueryLab();
        frame.setVisible(true);
    }
}
