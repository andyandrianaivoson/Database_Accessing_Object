/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package interDB;

import java.lang.reflect.Method;

/**
 *
 * @author andyrazafy
 */
public class FieldDAO {
    private boolean id=false;
    private String numRef;
    private String field;
    private Method getter;
    private Method setter;
    private Method numRefGetter;

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
    
}
