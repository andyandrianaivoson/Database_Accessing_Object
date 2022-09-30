/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package interDB;

import java.sql.ResultSetMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
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

    public MappingMetaData(Class subject,Connection con)throws Exception {
        this.subject = subject;
        init(con);
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
    
    private void init(Connection con)throws Exception{
        mapTable();
        mapFields(con);
    }

    private void mapTable() throws Exception {
        DBTable tab = (DBTable) this.getSubject().getAnnotation(DBTable.class);
        System.out.println("tab: "+tab);
        String table = null;
        if (tab == null) {
            throw new NoReference("Annotation table undifined");
        } else {
            this.setTable(table);
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
    
    private Set<String> getColumnsFromTable(Connection con)throws Exception{
        String req= "select * from "+getTable()+" where 1>2";
        Statement stmt=null;
        ResultSet res=null;
        Set<String> columns=null;
        try {
            stmt=con.createStatement();
            res=stmt.executeQuery(req);
            ResultSetMetaData resMeta= res.getMetaData();
            columns=getColumnsName(resMeta);
            return columns;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if(res!=null) res.close();
            if(stmt!=null) stmt.close();
        }
    }
    private static Set<String> getColumnsName(ResultSetMetaData meta)throws Exception {
        Set<String> columnsName=new HashSet<>();
        
        for(int i=1;i<=meta.getColumnCount();i++){
            columnsName.add(meta.getColumnName(i).toUpperCase());
        }
        return columnsName;
    }
    private void mapFields(Connection con) throws Exception {
        Set<String> columns= getColumnsFromTable(con);
        Field[] fields = this.getSubject().getDeclaredFields();
        Set<FieldDAO> fieldDAOs = new HashSet<>();
        Set<String> mapped = new HashSet<>();
        for (Field curField : fields) {
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
                
                if (referedField.getName().compareTo(this.getIdName()) == 0) {
                    temp.setId(true);
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

                    if (curField.getName().compareTo(this.getIdName()) == 0) {
                        temp.setId(true);
                    }

                    fieldDAOs.add(temp);
                    mapped.add(curField.getName());
                }
            }
        }
        fieldsMetaData=fieldDAOs;
    }
    
    private static String toUp(String s) {
        char[] l = s.toCharArray();
        l[0] = Character.toUpperCase(l[0]);
        String valiny = new String(l);
        return valiny;
    }
}
