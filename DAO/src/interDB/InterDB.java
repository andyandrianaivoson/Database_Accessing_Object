/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package interDB;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author andyrazafy
 *
 * this class serve as a DAO
 *MAPPING_METADATA serves a context which contains all the metadata of each class that uses this DAO
 * 
 */
public class InterDB {

    private static final HashMap<String, MappingMetaData> MAPPING_METADATA = new HashMap<>();

    /**
     * used to structure id when it is a string
     * @param con
     * @param sequence
     * @param idDim
     * @return
     * @throws Exception 
     */
    private static String structureId(Connection con, String sequence, String idDim) throws Exception {
        String req = "select nextval(?)";
        String valiny = idDim;
        PreparedStatement stmt = null;
        ResultSet res = null;
        try {
            stmt = con.prepareStatement(req);
            stmt.setObject(1, sequence);
            res = stmt.executeQuery();
            while (res.next()) {
                valiny += res.getString(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (res != null) {
                res.close();
            }
            if (stmt != null) {
                stmt.close();
            }
        }
        return valiny;

    }

    public static void insert(Connection con, Object obj) throws Exception {
        Class c = obj.getClass();
        FieldDAO fieldId = null;
        
        MappingMetaData meta=MAPPING_METADATA.get(c.getCanonicalName());
        if(meta==null){
            //mapClas is used to get all the metadata of the class and store it in MAPPING_METADATA field
            meta=mapClass(c, con);
        }
        //get the metadata which contains all the description of the class
        
        //get all the declared fields which are not null
        List<FieldDAO> fields= meta.getFieldsWithoutIDSet(obj);
        if (fields.isEmpty()) {
            throw new NoReference("Empty object");
        }
        
        String reqSt1= "insert into "+meta.getTable()+" ( ";
        String reqSt2= " ) values ( ";
        
        if(meta.getIdName()!=null){
            fieldId=meta.getFieldsMetaData().stream()
                    .filter(FieldDAO::isId)
                    .findFirst()
                    .orElse(null);
            if (fieldId == null) {
                throw new NoReference("fatal error");
            }
            if(meta.getIdType()!=IdType.SERIAL) {
                reqSt1 += meta.getIdName() + ",";
                String id = structureId(con, meta.getSequence(), meta.getPrefix());
                fieldId.getSetter().invoke(obj, id);
                reqSt2+=" ?,";
            }
        }
        
        for (int i = 0; i < fields.size(); i++) {
            if (i == fields.size() - 1) {
                reqSt1 += fields.get(i).getField() + " ";
                reqSt2 += "? )";
            } else {
                reqSt1 += fields.get(i).getField() + ",";
                reqSt2 += "?,";
            }
        }
        String query=reqSt1+reqSt2;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            int ii = 1;
            if(meta.getIdName()!=null && meta.getIdType()!= IdType.SERIAL){
                stmt.setObject(1, fieldId.getGetter().invoke(obj));
                ii++;
            }
            for (int i = 0; i < fields.size(); i++) {
                stmt.setObject(ii, fields.get(i).getGetter().invoke(obj));
                ii++;
            }
            int status = stmt.executeUpdate();
            if(meta.getIdType()==IdType.SERIAL){
                getSerialId(con, meta.getTable(), obj, fields, fieldId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if(stmt!=null){
                stmt.close();
            }
        }
    }

    /**
     * get the id of an object which is serial
     * @param con
     * @param table
     * @param obj
     * @param fields
     * @param fieldId
     * @throws Exception 
     */
    public static void getSerialId(Connection con, String table, Object obj, List<FieldDAO> fields, FieldDAO fieldId) throws Exception {
        String req = "select " + fieldId.getField() + " from " + table + " where ";
        List<Object> params = new ArrayList<>();
        for (int i = 0; i < fields.size(); i++) {
            if (i == 0) {
                req += " " + fields.get(i).getField() + "=? ";
            } else {
                req += " and " + fields.get(i).getField() + "=? ";
            }
        }
        req += " order by " + fieldId.getField() + " desc limit 1";
        PreparedStatement stmt = null;
        ResultSet res = null;
        try {
            stmt = con.prepareStatement(req);
            int i = 1;
            for (FieldDAO field : fields) {
                stmt.setObject(i, field.getGetter().invoke(obj));
                i++;
            }
            res = stmt.executeQuery();
            while (res.next()) {
                fieldId.getSetter().invoke(obj, res.getObject(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (res != null) {
                res.close();
            }
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    public static void update(Connection con, Object obj, String ref) throws Exception {
        Class c = obj.getClass();
        
        MappingMetaData meta=MAPPING_METADATA.get(c.getCanonicalName());
        if(meta==null){
            //mapClas is used to get all the metadata of the class and store it in MAPPING_METADATA field
            meta=mapClass(c, con);
        }

        FieldDAO fieldRef = null;
        if (ref == null||ref.isEmpty()) {
            fieldRef = meta.getFieldsMetaData().stream()
                    .filter(FieldDAO::isId)
                    .findFirst()
                    .orElse(null);
        } else {
            fieldRef = meta.getFieldsMetaData().stream()
                    .filter(line -> line.getField().compareTo(ref) == 0)
                    .findFirst()
                    .orElse(null);
        }

        if (fieldRef == null) {
            throw new NoReference("reference not found :"+ref);
        }
        
        String table = meta.getTable();

        List<FieldDAO> fields = meta.getFieldsWithoutReferenceSet(obj, fieldRef.getField());
        if (fields.isEmpty()) {
            throw new NoReference("Empty object");
        }
        
        String req = "update " + table + " set ";

        for (int i = 0; i < fields.size(); i++) {
            if (i == 0) {
                req += fields.get(i).getField() + "=?";
            } else {
                req += "," + fields.get(i).getField() + "=?";
            }
        }

        req += " where " + fieldRef.getField() + "=?";

        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(req);
            int ii = 1;
            for (int i = 0; i < fields.size(); i++) {
                stmt.setObject(ii, fields.get(i).getGetter().invoke(obj));
                ii++;
            }
            stmt.setObject(ii, fieldRef.getGetter().invoke(obj));
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    @Deprecated
    public static <T> List<T> find(Connection con, T obj) throws Exception {
        List<T> valiny = new ArrayList<>();
        Class<T> c = (Class<T>) obj.getClass();
        Constructor<T> constru = c.getConstructor();
        if (constru == null) {
            throw new NoSuchMethodException("unfinded constructor with no arguments");
        }
        MappingMetaData meta=MAPPING_METADATA.get(c.getCanonicalName());
        if (meta==null) {
            meta=mapClass(c,con);
        }
        String table = meta.getTable();

        List<FieldDAO> fields= getFieldDAOs(meta);
        String req = "select * from " + table;
        
        req += " where 1<2 ";
       
        
        for (int i = 0; i < fields.size(); i++) {
            if (i == 0) {
                if(fields.get(i).isSet(obj))
                    req += " and " + fields.get(i).getField() + "=? ";
            } else {
                if(fields.get(i).isSet(obj))
                req += " and " + fields.get(i).getField() + "=? ";
            }
        }
        System.out.println(req);
        PreparedStatement stmt = null;
        ResultSet res = null;
        try {
            stmt = con.prepareStatement(req);
            int i = 1;
            for (int ii=0;ii<fields.size();ii++) {
                if(fields.get(ii).isSet(obj)){
                    stmt.setObject(i, fields.get(ii).getGetter().invoke(obj));
                    i++; 
                }
            }
            res = stmt.executeQuery();
            while (res.next()) {
                T temp = constru.newInstance();
                for (int ii=0;ii<fields.size();ii++) {
                    fields.get(ii).getSetter().invoke(temp, res.getObject(ii+1));
                }
                valiny.add(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (res != null) {
                res.close();
            }
            if (stmt != null) {
                stmt.close();
            }
        }
        return valiny;
    }
    
    public static <T> List<T> find(Connection con, T obj,SearchParameter params,String afterWhere) throws Exception {
        List<T> valiny = new ArrayList<>();
        Class<T> c = (Class<T>) obj.getClass();
        Constructor<T> constru = c.getConstructor();
        if (constru == null) {
            throw new NoSuchMethodException("unfinded constructor with no arguments");
        }
        MappingMetaData meta=MAPPING_METADATA.get(c.getCanonicalName());
        if (meta==null) {
            meta=mapClass(c,con);
        }
        String table = meta.getTable();

        List<FieldDAO> fields= getFieldDAOs(meta);
        if(params==null){
            params=new SearchParameter();
        }
        List<Object> preparedParams= new ArrayList<>();
        String req = "select * from " + table;
        
        req += " where 1<2 ";
       
        
        req=params.makeParameter(obj, req, fields, preparedParams);
        
        req+=" "+afterWhere;
        System.out.println(req);
        PreparedStatement stmt = null;
        ResultSet res = null;
        try {
            stmt = con.prepareStatement(req);
            int i = 1;
            for (int ii=0;ii<preparedParams.size();ii++) {
                stmt.setObject(i, preparedParams.get(ii));
                i++;
            }
            res = stmt.executeQuery();
            while (res.next()) {
                T temp = constru.newInstance();
                for (int ii=0;ii<fields.size();ii++) {
                    fields.get(ii).getSetter().invoke(temp, res.getObject(ii+1));
                }
                valiny.add(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (res != null) {
                res.close();
            }
            if (stmt != null) {
                stmt.close();
            }
        }
        return valiny;
    }

    public static <T> List<T> findAll(Connection con, Class<T> c, int start, int count) throws Exception {
        List<T> valiny = new ArrayList<>();
        Constructor<T> constru = c.getConstructor();
        if (constru == null) {
            throw new NoSuchMethodException("unfinded constructor with no arguments");
        }
        MappingMetaData meta=MAPPING_METADATA.get(c.getCanonicalName());
        if (meta==null) {
            meta=mapClass(c,con);
        }
        String table = meta.getTable();
        
        List<FieldDAO> fields= getFieldDAOs(meta);
        String req = "select * from " + table;
        if (start > 0) {
            req += " offset " + start;
        }
        if (count > 0) {
            req += " limit " + count;
        }

        PreparedStatement stmt = null;
        ResultSet res = null;
        try {
            stmt = con.prepareStatement(req);
            res = stmt.executeQuery();
            if (fields.isEmpty()) {
                throw new NoReference("no match found between  class mapping :" + c.getCanonicalName() + " and the table");
            }
            while (res.next()) {
                T temp = constru.newInstance();
                for (int ii=0;ii<fields.size();ii++) {
                    fields.get(ii).getSetter().invoke(temp, res.getObject(ii+1));
                }
                valiny.add(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (res != null) {
                res.close();
            }
            if (stmt != null) {
                stmt.close();
            }
        }
        return valiny;

    }

    public static int count(Connection con, String req, List<Object> parameters) throws Exception {
        int valiny = 0;
        PreparedStatement stmt = null;
        ResultSet res = null;
        try {
            stmt = con.prepareStatement(req);
            int ii = 1;
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(ii, parameters.get(i));
                ii++;
            }
            res = stmt.executeQuery();
            while (res.next()) {
                valiny = res.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (res != null) {
                res.close();
            }
            if (stmt != null) {
                stmt.close();
            }
        }
        return valiny;
    }

    public static Date getDate(Connection con, String req, List<Object> parameters) throws Exception {
        Date valiny = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        try {
            stmt = con.prepareStatement(req);
            int ii = 1;
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(ii, parameters.get(i));
                ii++;
            }
            res = stmt.executeQuery();
            while (res.next()) {
                valiny = res.getDate(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (res != null) {
                res.close();
            }
            if (stmt != null) {
                stmt.close();
            }
        }
        return valiny;
    }

    public static float sum(Connection con, String req, List<Object> parameters) throws Exception {
        float valiny = 0;
        PreparedStatement stmt = null;
        ResultSet res = null;
        try {
            stmt = con.prepareStatement(req);
            int ii = 1;
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(ii, parameters.get(i));
                ii++;
            }
            res = stmt.executeQuery();
            while (res.next()) {
                valiny = res.getFloat(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (res != null) {
                res.close();
            }
            if (stmt != null) {
                stmt.close();
            }
        }
        return valiny;
    }

    /**
     * get the fields metadata of the class according to the table in the class' hierarchy
     * @param metadata
     * @return
     * @throws Exception 
     */
    private static List<FieldDAO> getFieldDAOs(MappingMetaData metadata) throws Exception {
        if(metadata.getFieldsFromColumns()!=null)
            return metadata.getFieldsFromColumns();
        final List<String> columns=metadata.getColumns();
        List<FieldDAO> fields = new ArrayList<>(columns.size());
        Class current= metadata.getSubject();
        Class sup = current.getSuperclass();
        do {            
            MappingMetaData meta= MAPPING_METADATA.get(current.getCanonicalName());
            if(meta!=null){
                meta.getFields(columns, fields);
            }
            current=sup;
            sup=sup.getSuperclass();
        } while (sup != null && !sup.equals(Object.class));
        metadata.setFieldsFromColumns(fields);
        return fields;
    }

    
    /**
     * add the class and the parents in the MAPPING_METADATA context
     * @param c
     * @param con
     * @return
     * @throws Exception 
     */
    private static MappingMetaData mapClass(Class c,Connection con) throws Exception{
        Class sup = c.getSuperclass();
        if (sup != null && !sup.equals(Object.class)) {
            mapClass(sup,con);
        }
        MappingMetaData meta=null;
        if(!MAPPING_METADATA.containsKey(c.getCanonicalName())){
            meta= new MappingMetaData(c, con);
            MAPPING_METADATA.put(c.getCanonicalName(), meta);
        }
        return meta;
    }

}
