/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package interDB;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.sql.JDBCType;

/**
 *
 * @author andyrazafy
 */
public class FieldDAO {
    private final static Set<String> PRIMITIVES; 
    private boolean id=false;
    private String numRef;
    private String field;
    private Method getter;
    private Method setter;
    private Method numRefGetter;
    
    static{
        PRIMITIVES=new HashSet<>();
        PRIMITIVES.add(int.class.getCanonicalName());
        PRIMITIVES.add(double.class.getCanonicalName());
        PRIMITIVES.add(long.class.getCanonicalName());
        PRIMITIVES.add(float.class.getCanonicalName());
    }

    
    public boolean isId() {
        return id;
    }

    public void setId(boolean id) {
        this.id = id;
    }

    
    public String getNumRef() {
        return numRef;
    }

    public void setNumRef(String numRef) {
        this.numRef = numRef;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Method getGetter() {
        return getter;
    }

    public void setGetter(Method getter) {
        this.getter = getter;
    }

    public Method getSetter() {
        return setter;
    }

    public void setSetter(Method setter) {
        this.setter = setter;
    }

    public Method getNumRefGetter() {
        return numRefGetter;
    }

    public void setNumRefGetter(Method numRefGetter) {
        this.numRefGetter = numRefGetter;
    }
    
    /**
    *check if this field is set
     * @param o object to check if this field is set
     * @return 
    */
    public boolean isSet(Object o){
        try {
            if(this.numRef!=null){
                return (boolean) getNumRefGetter().invoke(o);
            }
            return getGetter().invoke(o)!=null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean isString(){
        return getGetter().getReturnType().getCanonicalName().compareTo(String.class.getCanonicalName())==0;
    }
    /**
     * if this field is of primitive type and the boolean reference is null then it is not possible to determine if
     * this field is set or not.
     * @return 
     */
    public boolean isUnsafe(){
        return PRIMITIVES.contains(getGetter().getReturnType().getCanonicalName())&& this.numRef==null;
    }
    
}
