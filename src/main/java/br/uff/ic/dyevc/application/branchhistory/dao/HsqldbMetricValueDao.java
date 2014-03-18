/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.application.branchhistory.dao;

import br.uff.ic.dyevc.application.branchhistory.metric.Metric;
import br.uff.ic.dyevc.application.branchhistory.model.Revision;
import br.uff.ic.dyevc.application.branchhistory.model.VersionedItem;
import br.uff.ic.dyevc.application.branchhistory.model.VersionedProject;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author wallace
 */
public class HsqldbMetricValueDao implements MetricValueDao {

    private final static String DB_DIR = System.getProperty("user.home") + "/.dyevcDB/METRIC_VALUE_DB_DIR/";
    private Connection connection;

    public HsqldbMetricValueDao() {
        try {
            File file = new File(DB_DIR);
            if(!file.exists()){
                file.mkdirs();               
            }
            Class.forName("org.hsqldb.jdbcDriver");
            connection = DriverManager.getConnection("jdbc:hsqldb:file:" + DB_DIR, "dyevc", "123");
            connection.setAutoCommit(true);
            DatabaseMetaData dbData = connection.getMetaData();
            ResultSet tables = dbData.getTables(null, null, "METRIC_VALUES", null);
            //System.out.println("NEXT "+tables.next());
            if (!tables.next()) {


                System.out.println("NAO POSSUI TABELA");
                Statement stm = connection.createStatement();

                stm.executeUpdate("create table METRIC_VALUES (project_name varchar(255) , "
                        + "metric varchar(255),"
                        + "revision_id varchar(255),"
                        + "versioned_item varchar(1000),"
                        + "value varchar(255),"
                        + "timestamp bigint);");
            }else{
                System.out.println("TABELA JAH EXISTE");
            }

            //stm.execute("SHUTDOWN");

        } catch (ClassNotFoundException e) {
            System.out.println("Erro ao carregar o driver JDBC. ");
        } catch (SQLException e) {
            System.out.println("Erro de SQL: " + e);
            //e.printStackTrace();
        } catch (Exception e) {
            System.out.println("ERRO " + e.getMessage());
        }


    }

    @Override
    public void close() {
        try {

            connection.close();
        } catch (SQLException e) {
            System.out.println("ERRO: " + e.getMessage());
        }
    }

    @Override
    public void save(MetricValue metricValue) {
        try {
            
            Statement stm = connection.createStatement();
            System.out.println("insert into metric_values (project_name, metric, revision_id, versioned_item, value, timestamp)"
                    + " VALUES ('" + metricValue.getVersionedItem().getVersionedProject().getName() + "' , '"
                    + metricValue.getMetricSignature() + "' , '"
                    + metricValue.getRevisionId() + "' , '"
                    + metricValue.getVersionedItem().getRelativePath() + "' , '"
                    + metricValue.getValue() + "' , "
                    + System.currentTimeMillis() + ");");
            stm.executeUpdate("insert into metric_values (project_name, metric, revision_id, versioned_item, value, timestamp)"
                    + " VALUES ('" + metricValue.getVersionedItem().getVersionedProject().getName() + "' , '"
                    + metricValue.getMetricSignature() + "' , '"
                    + metricValue.getRevisionId() + "' , '"
                    + metricValue.getVersionedItem().getRelativePath() + "' , '"
                    + metricValue.getValue() + "' , "
                    + System.currentTimeMillis() + ");");

        } catch (SQLException e) {
            System.out.println("ERRO: " + e.getMessage());
        }
    }

    @Override
    public void delete(MetricValue metricValue) {
        try {
            Statement stm = connection.createStatement();
            ResultSet rs = stm.executeQuery("delete from metric_values where project_name='" + metricValue.getVersionedItem().getVersionedProject().getName() + "'"
                    + " and metric='" + metricValue.getMetricSignature() + "'"
                    + " and versioned_item='" + metricValue.getVersionedItem().getRelativePath() + "'"
                    + " and revision_id='" + metricValue.getRevisionId()+ "' ;");
            System.out.println("delete from metric_values where project_name='" + metricValue.getVersionedItem().getVersionedProject().getName() + "'"
                    + " and metric='" + metricValue.getMetricSignature() + "'"
                    + " and versioned_item='" + metricValue.getVersionedItem().getRelativePath() + "'"
                    + " and revision_id='" + metricValue.getRevisionId()+ "' ;");
        } catch (SQLException e) {
            System.out.println("ERRO: " + e.getMessage());
        }
    }

    @Override
    public MetricValue find(Metric metric, Revision revision, VersionedItem versionedItem) {
        try {
            Statement stm = connection.createStatement();
            ResultSet rs = stm.executeQuery("select * from metric_values ;");
            int i = 0;
            while (rs.next()) {
                System.out.println("revision_id: " + rs.getString("revision_id"));
                System.out.println("versioned_item: " + rs.getString("versioned_item"));
                System.out.println("value: " + rs.getString("value"));
                i++;
            }
            //stm.execute("SHUTDOWN");
            System.out.println("QUANTIDADE: " + i);
        } catch (Exception e) {
            System.out.println("ERRO: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<MetricValue> findByMetricAndVersionedItem(Metric metric, VersionedItem versionedItem) {
        List<MetricValue> metricValues = new LinkedList<MetricValue>();
        try {
            Statement stm = connection.createStatement();
            ResultSet rs = stm.executeQuery("select * from metric_values where project_name='" + versionedItem.getVersionedProject().getName() + "'"
                    + " and metric='" + metric.getSignature() + "'"
                    + " and versioned_item='" + versionedItem.getRelativePath() + "' ;");
            System.out.println("select * from metric_values where project_name='" + versionedItem.getVersionedProject().getName() + "'"
                    + " and metric='" + metric.getSignature() + "'"
                    + " and versioned_item='" + versionedItem.getRelativePath() + "' ;");

            while (rs.next()) {
                String revisionId = rs.getString("revision_id");
                String value = rs.getString("value");
                MetricValue metricValue = new MetricValue(revisionId, versionedItem, metric.getSignature(), value);
                metricValues.add(metricValue);

            }
            //stm.execute("SHUTDOWN");
        } catch (Exception e) {
            System.out.println("ERRO FIND: " + e.getMessage());
        }
        return metricValues;
    }
}
