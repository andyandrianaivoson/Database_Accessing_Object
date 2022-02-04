/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package interDB;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 *
 * @author andyrazafy
 *
 * this class serve as a DAO
 *
 * TABLE_MAPPING field is used as reference mapping to all the classes that have
 * already used it, therefore we have to use reflection no more mapping=
 * class.canonicalName : table,,,id,,,sequence,,,idDim
 *
 *
 *
 * FIELD_MAPPING is used as a mapping of fields of class mapping=
 * class.canonicalName : list of fields the method mapTable is used to map the
 * new class unmapped; the method setFieldMap is used to find the set field and
 * provide their getters
 *
 *
 * to do: findAll find findWithrequest select count getDateNow search
 */
public class InterDB {

    private static final HashMap<String, String> TABLE_MAPPING = new HashMap<String, String>();
    private static final HashMap<String, Set<FieldDAO>> FIELD_MAPPING = new HashMap<String, Set<FieldDAO>>();


    private static String structureId(Connection con, String sequence, String idDim) throws Exception {
        String req = "select nextval(?)";
        String valiny = idDim;
        PreparedStatement stmt = con.prepareStatement(req);
        stmt.setObject(1, sequence);
        ResultSet res = stmt.executeQuery();
        while (res.next()) {
            valiny += res.getString(1);
        }
        res.close();
        stmt.close();
        return valiny;
    }

    public static void insert(Connection con, Object obj) throws Exception {
        Class c = obj.getClass();
        FieldDAO fieldId = null;
        String table = null, idOb = null, seq = null, idDim = null;
        if (!FIELD_MAPPING.containsKey(c.getCanonicalName()))
            mapFields(c);
            
        String[] ref = TABLE_MAPPING.get(c.getCanonicalName()).split(",,,");
        table = ref[0];
        idOb = ref[1];
        seq = ref[2];
        idDim = ref[3];
        
        HashMap<String, Method> map = InterDB.fieldsGetterMap(c, obj);
        if (map.isEmpty()) {
            throw new NoReference("Empty object");
        }
        String req = "insert into " + table + " (";

        if (idOb.compareTo("") != 0) {
            req += idOb + ",";
            fieldId = FIELD_MAPPING.get(c.getCanonicalName()).stream()
                    .filter(line -> line.isId())
                    .findFirst()
                    .orElse(null);
            if (fieldId == null) {
                throw new NoReference("fatal error");
            }

            String id = structureId(con, seq, idDim);
            fieldId.getSetter().invoke(obj, id);
        }


        String[] keys = map.keySet().toArray(new String[0]);

        for (int i = 0; i < keys.length; i++) {
            if (i == keys.length - 1) {
                req += keys[i] + ") ";
            } else {
                req += keys[i] + ",";
            }
        }
        req += "values (";
        if (idOb.compareTo("") != 0) {
            req += "?,";
        }

        for (int i = 0; i < keys.length; i++) {
            if (i == keys.length - 1) {
                req += "?)";
            } else {
                req += "?,";
            }
        }
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(req);
            int ii = 1;
            if (idOb.compareTo("") != 0) {
                stmt.setObject(1, fieldId.getGetter().invoke(obj));
                ii ++;
            }
            for (int i = 0; i < keys.length; i++) {
                stmt.setObject(ii, map.get(keys[i]).invoke(obj));
                ii++;
            }
            stmt.executeUpdate();
        } catch (Exception e) {
            throw e;
        }finally{
            stmt.close();
        }
    }

    public static void update(Connection con, Object obj,String ref) throws Exception {
        Class c = obj.getClass();
        if(!TABLE_MAPPING.containsKey(c.getCanonicalName()))
            mapFields(c);
        
        FieldDAO fieldRef=null;
        if(ref==null){
            fieldRef=FIELD_MAPPING.get(c.getCanonicalName()).stream()
                    .filter(line -> line.isId())
                    .findFirst()
                    .orElse(null);
        }else{
            fieldRef=FIELD_MAPPING.get(c.getCanonicalName()).stream()
                    .filter(line -> line.getField().compareTo(ref)==0)
                    .findFirst()
                    .orElse(null);
        }
        
        if(fieldRef==null)
            throw new NoReference("reference not found");
        
        String[] elements=TABLE_MAPPING.get(c.getCanonicalName()).split(",,,");
        String table=elements[0];
        
        HashMap<String, Method> map = InterDB.fieldsGetterMap(c, obj);
        if (map.isEmpty()) {
            throw new NoReference("Empty object");
        }
        if (!map.containsKey(fieldRef.getField())) {
            throw new NoReference("Id value unset");
        } else {
            map.remove(fieldRef.getField());
        }

        String[] keys = map.keySet().toArray(new String[0]);
        String req = "update " + table + " set ";
        
        for (int i=0;i<keys.length;i++) {            
            if (i == 0) {
                req += keys[i] + "=?";
            } else {
                req += "," + keys[i] + "=?";
            }
        }
        
        req += " where " + fieldRef.getField() + "=?";

        PreparedStatement stmt=null;
        try {
            stmt = con.prepareStatement(req);
            int ii = 1;
            for (int i = 0; i < keys.length; i++) {
                stmt.setObject(ii, map.get(keys[i]).invoke(obj));
                ii++;
            }
            stmt.setObject(ii,fieldRef.getGetter().invoke(obj));
            stmt.executeUpdate();
        } catch (Exception e) {
            throw e;
        }finally{
            if(stmt!=null){
                stmt.close();
            }
        }
    }

    public static Vector find(Connection con, Object obj) throws Exception {
        Vector valiny = new Vector();
        Class c=obj.getClass();
        Constructor constru=c.getConstructor();
        if(constru==null)
                throw new NoSuchMethodException("unfinded constructor with no arguments");
        if(!FIELD_MAPPING.containsKey(c.getCanonicalName()))
            mapFields(c);
        String table=TABLE_MAPPING.get(c.getCanonicalName()).split(",,,")[0];
        
        HashMap<String,Method> map=fieldsGetterMap(c, obj);
        String req="select * from "+table;
        if(!map.isEmpty()){
            req+=" where ";
        }
        String[] keys=map.keySet().toArray(new String[0]);
        for(int i=0;i<keys.length;i++){
            if(i==0)
                req+= " "+keys[i]+"=? ";
            else
                req+=" and"+keys[i]+"=? ";
        }
        PreparedStatement stmt=null;
        ResultSet res=null;
        try {
            stmt= con.prepareStatement(req);
            int i=1;
            for(String key:keys){
                stmt.setObject(i, map.get(key).invoke(obj));
                i++;
            }
            res=stmt.executeQuery();
            ResultSetMetaData meta=res.getMetaData();
            ///mbola tsy vita
            Vector<String> listeCol=getColumnsName(meta);
            Set<FieldDAO> fields=fieldsSetterMap(c, listeCol);
                if(fields.isEmpty())
                    throw new NoReference("no match found between  class mapping :"+c.getCanonicalName()+" and the table");
            while(res.next()){
                Object temp=constru.newInstance();
                for(FieldDAO field:fields){
                    
                    field.getSetter().invoke(temp, res.getObject(field.getField()));
                }
                valiny.addElement(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }finally{
            if(stmt!=null)
                stmt.close();
            if(res!=null)
                res.close();
            return valiny;
        }
    }
    
    public static int count(Connection con,String req,Vector parameters)throws Exception{
        int valiny=0;
        PreparedStatement stmt=null;
        ResultSet res=null;
        try {
            stmt=con.prepareStatement(req);
            int ii=1;
            for(int i=0;i<parameters.size();i++){
                stmt.setObject(ii, parameters.elementAt(i));
                ii++;
            }
            res=stmt.executeQuery();
            while(res.next()){
                valiny=res.getInt(1);
            }
        } catch (Exception e) {
            throw e;
        }finally{
            if(res!=null)
                res.close();
            if(stmt!=null)
                stmt.close();
            return valiny;
        }
    }
    
    public static Date getDate(Connection con,String req,Vector parameters)throws Exception{
        Date valiny=null;
        PreparedStatement stmt=null;
        ResultSet res=null;
        try {
            stmt=con.prepareStatement(req);
            int ii=1;
            for(int i=0;i<parameters.size();i++){
                stmt.setObject(ii, parameters.elementAt(i));
                ii++;
            }
            res=stmt.executeQuery();
            while(res.next()){
                valiny=res.getDate(1);
            }
        } catch (Exception e) {
            throw e;
        }finally{
            if(res!=null)
                res.close();
            if(stmt!=null)
                stmt.close();
            return valiny;
        }
    }
    
    public static float sum(Connection con,String req,Vector parameters)throws Exception{
        float valiny=0;
        PreparedStatement stmt=null;
        ResultSet res=null;
        try {
            stmt=con.prepareStatement(req);
            int ii=1;
            for(int i=0;i<parameters.size();i++){
                stmt.setObject(ii, parameters.elementAt(i));
                ii++;
            }
            res=stmt.executeQuery();
            while(res.next()){
                valiny=res.getFloat(1);
            }
        } catch (Exception e) {
            throw e;
        }finally{
            if(res!=null)
                res.close();
            if(stmt!=null)
                stmt.close();
            return valiny;
        }
    }
   
    public static Vector find(Connection con,Class c,String req,Vector parameters)throws Exception{
        Vector valiny= new Vector();
        Constructor constru=c.getConstructor();
        if(constru==null)
                throw new NoSuchMethodException("unfinded constructor with no arguments");
        PreparedStatement stmt=null;
        ResultSet res=null;
        try {
            stmt= con.prepareStatement(req);
            int ii=1;
            for(int i=0;i<parameters.size();i++){
                stmt.setObject(ii, parameters.elementAt(i));
                ii++;
            }
            res=stmt.executeQuery();
            ResultSetMetaData meta=res.getMetaData();
            
            Vector<String> listeCol=getColumnsName(meta);
            Set<FieldDAO> fields=fieldsSetterMap(c, listeCol);
                if(fields.isEmpty())
                    throw new NoReference("no match found between  class mapping :"+c.getCanonicalName()+" and the table");
            while(res.next()){
                Object temp=constru.newInstance();
                for(FieldDAO field:fields){
                    field.getSetter().invoke(temp, res.getObject(field.getField()));
                }
                valiny.addElement(temp);
            }
        } catch (Exception e) {
            throw e;
        }finally{
            if(stmt!=null)
                stmt.close();
            if(res!=null)
                res.close();
            return valiny;
        }
    }
    
    public static Vector search(Connection con,Class c, SearchParameter params)throws Exception{
        Vector valiny = new Vector();
        Constructor constru=c.getConstructor();
        if(constru==null)
                throw new NoSuchMethodException("unfinded constructor with no arguments");
        if(!FIELD_MAPPING.containsKey(c.getCanonicalName()))
            mapFields(c);
        String table=TABLE_MAPPING.get(c.getCanonicalName()).split(",,,")[0];
        
        String req="select * from "+table;
        if(!params.isEmpty()){
            req+=" where ";
        }
        
//        String[] keys=map.keySet().toArray(new String[0]);
//        for(int i=0;i<keys.length;i++){
//            if(i==0)
//                req+= " "+keys[i]+"=? ";
//            else
//                req+=" and"+keys[i]+"=? ";
//        }
        boolean isLikeEmpty=params.isLikeEmpty();
        HashMap<String,String> like=params.getLike();
        String[] likeKeys=like.keySet().toArray(new String[0]);
        
        boolean isVarEmpty=params.isVariableEmpty();
        HashMap<String,Vector> var=params.getVariable();
        String[] varKeys=var.keySet().toArray(new String[0]);
        
        String dateRef=params.getDateRef();
        Date dateMax=params.getDateMax();
        Date dateMin=params.getDateMin();
       
        boolean isFirst=true;
        
        if(!isLikeEmpty){
            isFirst=false;
            req+=" ( ";
            for(int i=0;i<likeKeys.length;i++){
                if(i==0){
                    req+=" "+likeKeys[i]+" like ?";
                }else{
                    req+=" or "+likeKeys[i]+" like ?";
                }
            }
            req+=" ) ";
        }
        if(!isVarEmpty){
            if(!isFirst){
                req+=" and ";
            }
            req+=" ( ";
            for(int i=0;i<varKeys.length;i++){
                if(i==0){
                    req+=" ( ";
                    for(int ii=0;ii<var.get(varKeys[i]).size();ii++){   
                        if(ii==0)
                            req+=" "+varKeys[i]+"=? ";
                        else
                            req+=" or "+varKeys[i]+"=? ";
                    }
                    req+=" ) ";
                }else{
                    req+=" and ( ";
                    for(int ii=0;ii<var.get(varKeys[i]).size();ii++){   
                        if(ii==0)
                            req+=" "+varKeys[i]+"=? ";
                        else
                            req+=" or "+varKeys[i]+"=? ";
                    }
                    req+=" ) ";
                }
                
            }
            req+=" ) ";
        }
            
        if(dateRef!=null){
            if(dateMin!=null){
                if(!isFirst)
                    req+= " and ";
                req+=" "+dateRef+"<=?";
            }
            if(dateMax!=null){
                if(!isFirst)
                    req+= " and ";
                req+=" "+dateRef+">=?";
            }
        }
        
        PreparedStatement stmt=null;
        ResultSet res=null;
        try {
            stmt= con.prepareStatement(req);
            int indice=1;

            if(!isLikeEmpty){
                for(int i=0;i<likeKeys.length;i++){
                    stmt.setObject(indice, like.get(likeKeys[i]));
                    indice++;
                }
            }
            if(!isVarEmpty){
                
                for(int i=0;i<varKeys.length;i++){
                    Vector v=var.get(varKeys[i]);
                    for(int ii=0;ii<v.size();ii++){   
                        stmt.setObject(indice,v.elementAt(ii));
                        indice++;
                    }
                }

            }
            if(dateRef!=null){
                if(dateMin!=null){
                    stmt.setObject(indice, dateMin);
                    indice++;
                }
                if(dateMax!=null){
                    stmt.setObject(indice, dateMax);
//                    indice++;
                }
            }
            res=stmt.executeQuery();
            ResultSetMetaData meta=res.getMetaData();
            ///mbola tsy vita
            Vector<String> listeCol=getColumnsName(meta);
            Set<FieldDAO> fields=fieldsSetterMap(c, listeCol);
                if(fields.isEmpty())
                    throw new NoReference("no match found between  class mapping :"+c.getCanonicalName()+" and the table");
            while(res.next()){
                Object temp=constru.newInstance();
                for(FieldDAO field:fields){
                    field.getSetter().invoke(temp, res.getObject(field.getField()));
                }
                valiny.addElement(temp);
            }
        } catch (Exception e) {
            throw e;
        }finally{
            if(stmt!=null)
                stmt.close();
            if(res!=null)
                res.close();
            return valiny;
        }
    }
    
    /*
        Probleme cout en memoire
    
    solution Crée une class qui repertorie les fields avec ses get set
    
     */
    private static HashMap<String, Method> fieldsGetterMap(Class c, Object obj) throws Exception {
        HashMap<String, Method> map = null;
        Class sup=c.getSuperclass();
        if(sup!=null&& !sup.equals(Object.class))
            map=fieldsGetterMap(sup, obj);
        else
            map=new HashMap<String, Method>();
        if (!FIELD_MAPPING.containsKey(c.getCanonicalName())) {
            mapFields(c);
        }
        Set<FieldDAO> fdaos = FIELD_MAPPING.get(c.getCanonicalName());
        for (FieldDAO fdao : fdaos) {
            if (fdao.getNumRef() != null) {
                boolean isset = (boolean) fdao.getNumRefGetter().invoke(obj);
                if (isset) {
                    map.put(fdao.getField(), fdao.getGetter());
                }

            } else {
                if (fdao.getGetter().invoke(obj) != null) {
                    map.put(fdao.getField(), fdao.getGetter());
                }
            }
        }

        return map;
    }
    private static Vector<String> getColumnsName(ResultSetMetaData meta)throws Exception {
        Vector<String> columnsName=new Vector<String>();
        for(int i=1;i<=meta.getColumnCount();i++){
            columnsName.addElement(meta.getColumnName(i));
        }
        return columnsName;
    }
    private static Set<FieldDAO> fieldsSetterMap(Class c,Vector<String> listeCol)throws Exception{
        Set<FieldDAO> valiny=null;
        Class sup=c.getSuperclass();
        if(sup!=null && !sup.equals(Object.class))
            valiny=fieldsSetterMap(sup, listeCol);
        
        if(!FIELD_MAPPING.containsKey(c.getCanonicalName()))
            mapFields(c);
        
        if(valiny!=null){
            valiny.addAll(FIELD_MAPPING.get(c.getCanonicalName()).stream()
                    .filter(line -> {
                        boolean match = false;
                        for(int i=0;i<listeCol.size();i++){
                            if(listeCol.elementAt(i).compareToIgnoreCase(line.getField())==0){
                                match= true;
                                break;
                            }
                        }
                        return match;
                    })
                    .collect(Collectors.toSet())
            );
        }else{
            valiny=FIELD_MAPPING.get(c.getCanonicalName()).stream()
                    .filter(line -> {
                        boolean match = false;
                        for(int i=0;i<listeCol.size();i++){
                            if(listeCol.elementAt(i).compareToIgnoreCase(line.getField())==0){
                                match= true;
                                break;
                            }
                        }
                        return match;
                    })
                    .collect(Collectors.toSet());
        }
        return valiny;
    }
    
    private static void mapTable(Class c) throws Exception {
        DBTable tab = (DBTable) c.getAnnotation(DBTable.class);
        System.out.println("tab: "+tab);
        String table = null;
        if (tab == null) {
            throw new NoReference("Annotation table undifined");
        } else {
            if (tab.table().compareTo("0") == 0) {
                throw new NoReference("Table name undifined");
            } else {
                table = tab.table();
            }
        }
        ///les annotations de references d'une class
        String idOb = tab.id();
        String seq = tab.sequence();
        String idDim = tab.idDim();
        if (idOb.compareTo("0") != 0) {
            if (seq.compareTo("0") == 0) {
                throw new NoReference("Sequence undifined");
            } else {
                if (idDim.compareTo("0") == 0) {
                    throw new NoReference("id diminutif undifined");
                }
            }
        }
        String value = table + ",,," + idOb + ",,," + seq + ",,," + idDim;
        TABLE_MAPPING.put(c.getCanonicalName(), value);
    }
    
    private static void mapFields(Class c) throws Exception {
        if (!TABLE_MAPPING.containsKey(c.getCanonicalName())) {
            mapTable(c);
        }

        String[] elements = TABLE_MAPPING.get(c.getCanonicalName()).split(",,,");

        Field[] fields = c.getDeclaredFields();
        Set<FieldDAO> fieldDAOs = new HashSet<>();
        Set<String> mapped = new HashSet<>();
        for (Field f : fields) {
            NumberRef num = (NumberRef) f.getAnnotation(NumberRef.class);
            if (num != null) {
                if (num.value().compareTo("") == 0) {
                    throw new NoReference("Field name undifined");
                }

                Field field = c.getDeclaredField(num.value());
                if(mapped.contains(field.getName())){
                    fieldDAOs.removeIf(line -> line.getField().compareTo(field.getName())==0);
                    mapped.remove(field.getName());
                }
                Class[] types = new Class[1];
                types[0] = field.getType();
                Method setter = c.getDeclaredMethod("set" + InterDB.toUp(field.getName()), types);

                Method getter = c.getDeclaredMethod("get" + InterDB.toUp(field.getName()));
                Method numRefGetter = c.getDeclaredMethod("get" + InterDB.toUp(f.getName()));

                FieldDAO temp = new FieldDAO();

                temp.setNumRef(f.getName());
                temp.setNumRefGetter(numRefGetter);

                temp.setField(field.getName());
                temp.setGetter(getter);
                temp.setSetter(setter);

                fieldDAOs.add(temp);
                mapped.add(field.getName());
            } else {
                if (!mapped.contains(f.getName())) {
                    FieldDAO temp = new FieldDAO();

                    Method getter = c.getMethod("get" + InterDB.toUp(f.getName()));
                    Class[] types = new Class[1];
                    types[0] = f.getType();
                    Method setter = c.getDeclaredMethod("set" + InterDB.toUp(f.getName()), types);
                    temp.setField(f.getName());
                    temp.setGetter(getter);
                    temp.setSetter(setter);

                    if (f.getName().compareTo(elements[1]) == 0) {
                        temp.setId(true);
                    }

                    fieldDAOs.add(temp);
                    mapped.add(f.getName());
                }
            }
        }
        FIELD_MAPPING.put(c.getCanonicalName(), fieldDAOs);
    }

    private static String toUp(String s) {
        char[] l = s.toCharArray();
        l[0] = Character.toUpperCase(l[0]);
        String valiny = new String(l);
        return valiny;
    }

}
