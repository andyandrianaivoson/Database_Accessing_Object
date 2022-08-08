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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        PreparedStatement stmt=null;
        ResultSet res=null;
        try{
            stmt = con.prepareStatement(req);
            stmt.setObject(1, sequence);
            res = stmt.executeQuery();
        while (res.next()) {
            valiny += res.getString(1);
        }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }finally{
            if(res!=null)
                res.close();
            if(stmt!=null)
                stmt.close();
        }
        return valiny;
        
    }

    public static void insert(Connection con, Object obj) throws Exception {
        Class c = obj.getClass();
        FieldDAO fieldId = null;
        String table = null, idOb = null, seq = null, idDim = null,idtype=null;
        if (!FIELD_MAPPING.containsKey(c.getCanonicalName()))
            mapFields(c);
            
        String[] ref = TABLE_MAPPING.get(c.getCanonicalName()).split(",,,");
        table = ref[0];
        idOb = ref[1];
        seq = ref[2];
        idDim = ref[3];
        idtype = ref[4];
        
        HashMap<String, Method> map = InterDB.fieldsGetterMap(c, obj);
        if (map.isEmpty()) {
            throw new NoReference("Empty object");
        }
        String req = "insert into " + table + " (";

        if (idOb.compareTo("0") != 0) {
            fieldId = FIELD_MAPPING.get(c.getCanonicalName()).stream()
                    .filter(line -> line.isId())
                    .findFirst()
                    .orElse(null);
            if (fieldId == null) {
                throw new NoReference("fatal error");
            }

            if(idtype.compareTo(IdType.SERIAL.value())!=0){
                req += idOb + ",";
                String id = structureId(con, seq, idDim);
                fieldId.getSetter().invoke(obj, id);
            }
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
        if (idOb.compareTo("0") != 0 && idtype.compareTo(IdType.SERIAL.value())!=0) {
            req += "?,";
        }

        for (int i = 0; i < keys.length; i++) {
            if (i == keys.length - 1) {
                req += "?)";
            } else {
                req += "?,";
            }
        }
        System.out.println(req);
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(req);
            int ii = 1;
            if (idOb.compareTo("0") != 0 && idtype.compareTo(IdType.SERIAL.value())!=0) {
                stmt.setObject(1, fieldId.getGetter().invoke(obj));
                ii ++;
            }
            for (int i = 0; i < keys.length; i++) {
                stmt.setObject(ii, map.get(keys[i]).invoke(obj));
                ii++;
            }
            int status=stmt.executeUpdate();
            if(idtype.compareTo(IdType.SERIAL.value())==0)
                getSerialId(con, table ,obj, map, fieldId);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }finally{
            stmt.close();
        }
    }
    
    public static void insertVao(Connection con,Object obj) throws Exception{
        
    }
    
    public static void getSerialId(Connection con,String table,Object obj,HashMap<String, Method> map,FieldDAO fieldId)throws Exception{
        String req="select "+fieldId.getField()+" from "+table+" where ";
        List<Object> params = new ArrayList<>();
        String[] keys=map.keySet().toArray(new String[0]);
        for(int i=0;i<keys.length;i++){
            if(i==0)
                req+= " "+keys[i]+"=? ";
            else
                req+=" and "+keys[i]+"=? ";
        }
        req+= " order by "+fieldId.getField()+" desc limit 1";
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
            while(res.next()){
                fieldId.getSetter().invoke(obj, res.getObject(1));
            }
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }finally{
            if(res!=null)
                res.close();
            if(stmt!=null)
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
            e.printStackTrace();
            throw e;
        }finally{
            if(stmt!=null){
                stmt.close();
            }
        }
    }

    public static <T> List<T> find(Connection con, T obj) throws Exception {
        List<T> valiny = new ArrayList<>();
        Class<T> c=(Class<T>)obj.getClass();
        Constructor<T> constru=c.getConstructor();
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
                req+=" and "+keys[i]+"=? ";
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
            List<String> listeCol=getColumnsName(meta);
            Set<FieldDAO> fields=fieldsSetterMap(c, listeCol);
                if(fields.isEmpty())
                    throw new NoReference("no match found between  class mapping :"+c.getCanonicalName()+" and the table");
            while(res.next()){
                T temp=constru.newInstance();
                for(FieldDAO field:fields){
                    
                    field.getSetter().invoke(temp, res.getObject(field.getField()));
                }
                valiny.add(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }finally{
            if(res!=null)
                res.close();
            if(stmt!=null)
                stmt.close();
        }
        return valiny;
    }
    
    public static <T> List<T> findAll(Connection con, Class<T> c,int start,int count)throws Exception{
        List<T> valiny= new ArrayList<>();
        Constructor<T> constru=c.getConstructor();
        if(constru==null)
                throw new NoSuchMethodException("unfinded constructor with no arguments");
        if(!FIELD_MAPPING.containsKey(c.getCanonicalName()))
            mapFields(c);
        String table=TABLE_MAPPING.get(c.getCanonicalName()).split(",,,")[0];
        
        String req="select * from "+table;
        if(start>0)
            req+=" offset "+start;
        if(count>0)
            req+= " limit "+count;
        
        PreparedStatement stmt=null;
        ResultSet res=null;
        try {
            stmt=con.prepareStatement(req);
            res=stmt.executeQuery();
            ResultSetMetaData meta=res.getMetaData();
            ///mbola tsy vita
            List<String> listeCol=getColumnsName(meta);
            Set<FieldDAO> fields=fieldsSetterMap(c, listeCol);
                if(fields.isEmpty())
                    throw new NoReference("no match found between  class mapping :"+c.getCanonicalName()+" and the table");
            while(res.next()){
                T temp=constru.newInstance();
                for(FieldDAO field:fields){
                    
                    field.getSetter().invoke(temp, res.getObject(field.getField()));
                }
                valiny.add(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }finally{
            if(res!=null)
                res.close();
            if(stmt!=null)
                stmt.close();
        }
        return valiny;
        
    }
    
    public static int count(Connection con,String req,List<Object> parameters)throws Exception{
        int valiny=0;
        PreparedStatement stmt=null;
        ResultSet res=null;
        try {
            stmt=con.prepareStatement(req);
            int ii=1;
            for(int i=0;i<parameters.size();i++){
                stmt.setObject(ii, parameters.get(i));
                ii++;
            }
            res=stmt.executeQuery();
            while(res.next()){
                valiny=res.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }finally{
            if(res!=null)
                res.close();
            if(stmt!=null)
                stmt.close();
        }
        return valiny;
    }
    
    public static Date getDate(Connection con,String req,List<Object> parameters)throws Exception{
        Date valiny=null;
        PreparedStatement stmt=null;
        ResultSet res=null;
        try {
            stmt=con.prepareStatement(req);
            int ii=1;
            for(int i=0;i<parameters.size();i++){
                stmt.setObject(ii, parameters.get(i));
                ii++;
            }
            res=stmt.executeQuery();
            while(res.next()){
                valiny=res.getDate(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }finally{
            if(res!=null)
                res.close();
            if(stmt!=null)
                stmt.close();
        }
        return valiny;
    }
    
    public static float sum(Connection con,String req,List<Object> parameters)throws Exception{
        float valiny=0;
        PreparedStatement stmt=null;
        ResultSet res=null;
        try {
            stmt=con.prepareStatement(req);
            int ii=1;
            for(int i=0;i<parameters.size();i++){
                stmt.setObject(ii, parameters.get(i));
                ii++;
            }
            res=stmt.executeQuery();
            while(res.next()){
                valiny=res.getFloat(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }finally{
            if(res!=null)
                res.close();
            if(stmt!=null)
                stmt.close();
        }
        return valiny;
    }
   
    public static <T> List<T> find(Connection con,Class<T> c,String req,List<Object> parameters)throws Exception{
        List<T> valiny= new ArrayList<>();
        Constructor<T> constru=c.getConstructor();
        if(constru==null)
                throw new NoSuchMethodException("unfinded constructor with no arguments");
        PreparedStatement stmt=null;
        ResultSet res=null;
        try {
            stmt= con.prepareStatement(req);
            int ii=1;
            for(int i=0;i<parameters.size();i++){
                stmt.setObject(ii, parameters.get(i));
                ii++;
            }
            res=stmt.executeQuery();
            ResultSetMetaData meta=res.getMetaData();
            
            List<String> listeCol=getColumnsName(meta);
            Set<FieldDAO> fields=fieldsSetterMap(c, listeCol);
                if(fields.isEmpty())
                    throw new NoReference("no match found between  class mapping :"+c.getCanonicalName()+" and the table");
            while(res.next()){
                T temp=constru.newInstance();
                for(FieldDAO field:fields){
                    field.getSetter().invoke(temp, res.getObject(field.getField()));
                }
                valiny.add(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }finally{
            if(res!=null)
                res.close();
            if(stmt!=null)
                stmt.close();
        }
        return valiny;
    }
    
    public static <T> List<T> search(Connection con,Class<T> c, SearchParameter params)throws Exception{
        List<T> valiny = new ArrayList<>();
        Constructor<T> constru=c.getConstructor();
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
        HashMap<String,List<Object>> var=params.getVariable();
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
                            if(var.get(varKeys[i]).get(ii)==null)
                                req+=" "+varKeys[i]+" is null ";
                            else
                                req+=" "+varKeys[i]+"=? ";
                            
                        else
                            if(var.get(varKeys[i]).get(ii)==null)
                                req+=" or "+varKeys[i]+" is null ";
                            else
                                req+=" or "+varKeys[i]+"=? ";
                            
                    }
                    req+=" ) ";
                }else{
                    req+=" and ( ";
                    for(int ii=0;ii<var.get(varKeys[i]).size();ii++){   
                        if(ii==0)
                            if(var.get(varKeys[i]).get(ii)==null)
                                req+=" "+varKeys[i]+" is null ";
                            else
                                req+=" "+varKeys[i]+"=? ";
                            
                        else
                            if(var.get(varKeys[i]).get(ii)==null)
                                req+=" or "+varKeys[i]+" is null ";
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
        
//        System.out.println(" ito ny req ="+req);
        
        PreparedStatement stmt=null;
        ResultSet res=null;
        try {
            stmt= con.prepareStatement(req);
            int indice=1;

            if(!isLikeEmpty){
                for(int i=0;i<likeKeys.length;i++){
                    stmt.setObject(indice,like.get(likeKeys[i]));
                    indice++;
                }
            }
            if(!isVarEmpty){
                
                for(int i=0;i<varKeys.length;i++){
                    List<Object> v=var.get(varKeys[i]);
                    for(int ii=0;ii<v.size();ii++){  
//                        System.out.println(v.get(ii));
                        if(v.get(ii)==null)
                            continue;
                        stmt.setObject(indice,v.get(ii));
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
            List<String> listeCol=getColumnsName(meta);
            Set<FieldDAO> fields=fieldsSetterMap(c, listeCol);
                if(fields.isEmpty())
                    throw new NoReference("no match found between  class mapping :"+c.getCanonicalName()+" and the table");
            while(res.next()){
                T temp=constru.newInstance();
                for(FieldDAO field:fields){
                    field.getSetter().invoke(temp, res.getObject(field.getField()));
                }
                valiny.add(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }finally{
            if(res!=null)
                res.close();
            if(stmt!=null)
                stmt.close();
        }
        return valiny;
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
    private static List<String> getColumnsName(ResultSetMetaData meta)throws Exception {
        List<String> columnsName=new ArrayList<String>();
        
        for(int i=1;i<=meta.getColumnCount();i++){
            columnsName.add(meta.getColumnName(i));
        }
        return columnsName;
    }
    private static Set<FieldDAO> fieldsSetterMap(Class c,List<String> listeCol)throws Exception{
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
                            if(listeCol.get(i).compareToIgnoreCase(line.getField())==0){
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
                            if(listeCol.get(i).compareToIgnoreCase(line.getField())==0){
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
        IdType idtype= tab.idType();
        if (idOb.compareTo("0") != 0) {
            if(idtype==IdType.MODIFIED){
                if (seq.compareTo("0") == 0) {
                    throw new NoReference("Sequence undifined");
                } else {
                    if (idDim.compareTo("0") == 0) {
                        throw new NoReference("id diminutif undifined");
                    }
                }
            }
        }
        String value = table + ",,," + idOb + ",,," + seq + ",,," + idDim+ ",,,"+idtype.value();
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
        for (Field curField : fields) {
            NumberRef numAnnotation = (NumberRef) curField.getAnnotation(NumberRef.class);
            if (numAnnotation != null) {
                if (numAnnotation.value().compareTo("") == 0) {
                    throw new NoReference("Field name undifined");
                }

                Field referedField = c.getDeclaredField(numAnnotation.value());
                if(mapped.contains(referedField.getName())){
                    fieldDAOs.removeIf(line -> line.getField().compareTo(referedField.getName())==0);
                    mapped.remove(referedField.getName());
                }
                Class[] types = new Class[1];
                types[0] = referedField.getType();
                Method setter = c.getDeclaredMethod("set" + InterDB.toUp(referedField.getName()), types);

                Method getter = c.getDeclaredMethod("get" + InterDB.toUp(referedField.getName()));
                Method numRefGetter = c.getDeclaredMethod("get" + InterDB.toUp(curField.getName()));

                FieldDAO temp = new FieldDAO();

                temp.setNumRef(curField.getName());
                temp.setNumRefGetter(numRefGetter);

                temp.setField(referedField.getName());
                temp.setGetter(getter);
                temp.setSetter(setter);
                
                if (referedField.getName().compareTo(elements[1]) == 0) {
                    temp.setId(true);
                }

                fieldDAOs.add(temp);
                mapped.add(referedField.getName());
            } else {
                if (!mapped.contains(curField.getName())) {
                    FieldDAO temp = new FieldDAO();

                    Method getter = c.getMethod("get" + InterDB.toUp(curField.getName()));
                    Class[] types = new Class[1];
                    types[0] = curField.getType();
                    Method setter = c.getDeclaredMethod("set" + InterDB.toUp(curField.getName()), types);
                    temp.setField(curField.getName());
                    temp.setGetter(getter);
                    temp.setSetter(setter);

                    if (curField.getName().compareTo(elements[1]) == 0) {
                        temp.setId(true);
                    }

                    fieldDAOs.add(temp);
                    mapped.add(curField.getName());
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
