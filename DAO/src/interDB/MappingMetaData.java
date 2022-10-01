/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package interDB;

import java.sql.ResultSetMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.Types;
import java.sql.Date;
import java.sql.Timestamp;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *an instance of this class contains all the metadatas that describe the 'subject class' relation with the database
 * @author andyrazafy
 */
public class MappingMetaData {
    
    private final Class subject;
    private String table;
    private String idName;
    private String sequence;
    private String prefix;
    private IdType idType;
    private String tempTable;
    private Set<FieldDAO> fieldsMetaData;
    /**
     * columns from database.
     * important, these columns are in upperCase
     */
    private List<String> columns;
    /**
     * fields metadata matching the columns.
     * Which means if the class is a subclass and the table in DB does not require all the fields of its parent class then this
     * list only contains the essentials
     */
    private List<FieldDAO> fieldsFromColumns;

    static private int getSQLType(String name) {
        if(int.class.getCanonicalName().compareTo(name)==0) return Types.INTEGER;
        if(Integer.class.getCanonicalName().compareTo(name)==0) return Types.INTEGER;
        if(double.class.getCanonicalName().compareTo(name)==0) return Types.DOUBLE;
        if(Double.class.getCanonicalName().compareTo(name)==0) return Types.DOUBLE;
        if(float.class.getCanonicalName().compareTo(name)==0) return Types.FLOAT;
        if(Float.class.getCanonicalName().compareTo(name)==0) return Types.FLOAT;
        if(Date.class.getCanonicalName().compareTo(name)==0) return Types.DATE;
        if(Timestamp.class.getCanonicalName().compareTo(name)==0) return Types.TIMESTAMP;
        return Types.VARCHAR;
    }
    
    public MappingMetaData(Class subject,Connection con)throws Exception {
        this.subject = subject;
        init(con);
    }

    public List<FieldDAO> getFieldsFromColumns() {
        return fieldsFromColumns;
    }

    public void setFieldsFromColumns(List<FieldDAO> fieldsFromColumns) {
        this.fieldsFromColumns = fieldsFromColumns;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }
    
    public String getTempTable() {
        return tempTable;
    }

    public void setTempTable(String tempTable) {
        this.tempTable = tempTable;
    }
    
    public Class getSubject() {
        return subject;
    }


    public String getTable() {
        return table;
    }

    public void setTable(String table)throws Exception {
        if(table==null||table.compareTo("0")==0||table.isEmpty()) throw new NoReference("Table name undifined");
        this.table = table;
    }

    public String getIdName() {
        return idName;
    }

    public void setIdName(String idName) {
        if(idName==null||idName.compareTo("0")==0||idName.isEmpty()) return;
        this.idName = idName;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        if(sequence==null||sequence.compareTo("0")==0||sequence.isEmpty()) return;
        this.sequence = sequence;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        if(prefix==null||prefix.compareTo("0")==0||prefix.isEmpty()) return;
        this.prefix = prefix;
    }

    public IdType getIdType() {
        return idType;
    }

    public void setIdType(IdType idType) {
        this.idType = idType;
    }

    public Set<FieldDAO> getFieldsMetaData() {
        return fieldsMetaData;
    }
    
    /**
     * get all the metadata of this class
     * @param con
     * @throws Exception 
     */
    private void init(Connection con)throws Exception{
        mapTable();
        mapFields(con);
    }

    /**
     * get the metadata which refers to the interaction with the database of the class
     * 
     * @throws Exception 
     */
    private void mapTable() throws Exception {
        DBTable tab = (DBTable) this.getSubject().getAnnotation(DBTable.class);
        System.out.println("tab: "+tab);
        if (tab == null) {
            throw new NoReference("Annotation table undifined");
        } else {
            this.setTable(tab.table());
        }
        ///les annotations de references d'une class
        this.setIdName(tab.id());
        this.setSequence(tab.sequence());
        this.setPrefix(tab.idDim());
        this.setIdType(tab.idType());
        if (this.getIdName()!= null) {
            if(this.getIdType()==IdType.MODIFIED){
                if (this.getSequence() == null) {
                    throw new NoReference("Sequence undifined");
                } else {
                    if (this.getPrefix()== null) {
                        throw new NoReference("id diminutif undifined");
                    }
                }
            }
        }
    }
    
    /**
     * get the all the columns in the table and store it in upperCase;
     * @param con
     * @throws Exception 
     */
    private void setColumnsFromTable(Connection con)throws Exception{
        String req= "select * from "+getTable()+" where 1>2";
        Statement stmt=null;
        ResultSet res=null;
        try {
            stmt=con.createStatement();
            res=stmt.executeQuery(req);
            ResultSetMetaData resMeta= res.getMetaData();
            this.columns=getColumnsName(resMeta);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if(res!=null) res.close();
            if(stmt!=null) stmt.close();
        }
    }
    private static List<String> getColumnsName(ResultSetMetaData meta)throws Exception {
        List<String> columnsName=new ArrayList<>();
        
        for(int i=1;i<=meta.getColumnCount();i++){
            columnsName.add(meta.getColumnName(i).toUpperCase());
        }
        return columnsName;
    }
    
    /**
     * get all the fieldDAO from the fields that match the table columns in the database
     * @param con
     * @throws Exception 
     */
    private void mapFields(Connection con) throws Exception {
        setColumnsFromTable(con);
        Field[] fields = this.getSubject().getDeclaredFields();
        Set<FieldDAO> fieldDAOs = new HashSet<>();
        Set<String> mapped = new HashSet<>();
        boolean idFound=false;
        if(this.getIdName()==null){
            idFound=true;
        }
        for (Field curField : fields) {
            /**
             * NumberRef is used to refer a number field if set or not.
             * it is a boolean annotation
             */
            NumberRef numAnnotation = (NumberRef) curField.getAnnotation(NumberRef.class);
            if (numAnnotation != null) {
                if (numAnnotation.value().compareTo("") == 0) {
                    throw new NoReference("Field name undifined");
                }

                Field referedField = this.getSubject().getDeclaredField(numAnnotation.value());
                if(mapped.contains(referedField.getName())){
                    fieldDAOs.removeIf(line -> line.getField().compareTo(referedField.getName())==0);
                    mapped.remove(referedField.getName());
                }
                
                Method setter = this.getSubject().getDeclaredMethod("set" + toUp(referedField.getName()), referedField.getType());

                Method getter = this.getSubject().getDeclaredMethod("get" + toUp(referedField.getName()));
                Method numRefGetter = this.getSubject().getDeclaredMethod("get" + toUp(curField.getName()));

                FieldDAO temp = new FieldDAO();

                temp.setNumRef(curField.getName());
                temp.setNumRefGetter(numRefGetter);

                temp.setField(referedField.getName());
                temp.setGetter(getter);
                temp.setSetter(setter);
                
                if (this.idName!=null&&referedField.getName().compareTo(this.getIdName()) == 0) {
                    temp.setId(true);
                    idFound=true;
                }

                fieldDAOs.add(temp);
                mapped.add(referedField.getName());
            } else {
                if (!mapped.contains(curField.getName())&&columns.contains(curField.getName().toUpperCase())) {
                    FieldDAO temp = new FieldDAO();

                    Method getter = this.getSubject().getMethod("get" + toUp(curField.getName()));
                    Class[] types = new Class[1];
                    types[0] = curField.getType();
                    Method setter = this.getSubject().getDeclaredMethod("set" + toUp(curField.getName()), types);
                    temp.setField(curField.getName());
                    temp.setGetter(getter);
                    temp.setSetter(setter);

                    if (this.idName!=null&&curField.getName().compareTo(this.getIdName()) == 0) {
                        temp.setId(true);
                        idFound=true;
                    }

                    fieldDAOs.add(temp);
                    mapped.add(curField.getName());
                }
            }
        }
        if(!idFound){
            throw new NoReference("id not found "+this.getIdName());
        }
        fieldsMetaData=fieldDAOs;
    }
    
    public List<FieldDAO> getFieldsWithoutIDSet(Object o)throws Exception{
        return getFieldsWithoutReferenceSet(o, this.getIdName());
    }
    /**
     * used to get the metadata of the field that are set in this current class exept the specified reference field
     * Usually used in insert or update.
     * @param o
     * @param ref
     * @return
     * @throws Exception 
     */
    public List<FieldDAO> getFieldsWithoutReferenceSet(Object o,String ref)throws Exception{
        return fieldsMetaData.stream()
                .filter(line -> {
                    if(line.getField().compareTo(ref)==0) return false;
                    return line.isSet(o);
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }
    
    /**
     * used to get the metadata of the field that are set in this current class 
     * @param o
     * @return
     * @throws Exception 
     */
    public List<FieldDAO> getFieldsSet(Object o)throws Exception{
        return fieldsMetaData.stream()
                .filter(line -> line.isSet(o))
                .collect(Collectors.toCollection(ArrayList::new));
    }
    
    /**
     * used to fill the fields(param) with the fields metadata that match the columns and are in this mapping metadata. 
     * @param o
     * @param columns this param is the columns in the table but in upperCase
     * @param fields
     * @throws Exception 
     */
    public void getFields(List<String> columns,List<FieldDAO> fields)throws Exception{
        if(fields==null)
            fields= new ArrayList<>();
        for (int i = 0; i < columns.size(); i++) {
            for(FieldDAO field:this.fieldsMetaData){
                if(field.getField().toUpperCase().compareTo(columns.get(i))==0){
                    fields.add(i, field);
                    break;
                }
            }
        }
    }
    private static String toUp(String s) {
        char[] l = s.toCharArray();
        l[0] = Character.toUpperCase(l[0]);
        String valiny = new String(l);
        return valiny;
    }
}
