/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package interDB;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 *
 * @author andyrazafy
 *
 * to do: add element in the vector of the variable string setter for the dates
 */
public class SearchParameter {

    private static final Set<String> DATE_SEPARATOR = new HashSet<>();

    private HashMap<String, String> like = new HashMap<>();
    private HashMap<String, List<Object>> variable = new HashMap<>();
    private String dateRef;
    private Date dateMin;
    private Date dateMax;

    public SearchParameter() {
        insertSeparators();
    }
    
    
    
    private void insertSeparators() {
        DATE_SEPARATOR.add("-");
        DATE_SEPARATOR.add("/");
    }

    public void putInLike(String key, String value) {
        if(!value.isEmpty()&& value!=null)
            like.put(key, "%"+value+"%");
    }

    public void addInTheVariable(String k, Object v) {
        if (!"".equals(v) && !"default".equals(v) && !"none".equals(v)) {
            if (variable.get(k) == null) {
                List<Object> el = new ArrayList<>();
                variable.put(k, el);
            }
            variable.get(k).add(v);
        }
    }

    public boolean isEmpty() {
        boolean valiny = true;
        if (!isLikeEmpty()) {
            valiny = false;
        }
        if (!isVariableEmpty()) {
            valiny = false;
        }
        if (dateRef != null) {
            if (dateMin != null) {
                valiny = false;
            }
            if (dateMax != null) {
                valiny = false;
            }
        }
        return valiny;
    }

    private Date setDate(String d) throws Exception {
        String separator = DATE_SEPARATOR.stream()
                .filter(line -> d.contains(line))
                .findFirst()
                .orElse(null);
        if (separator == null) {
            throw new Exception("date invalide :" + d);
        }
        String[] elem = d.split(separator);
        if (elem.length != 3) {
            throw new Exception("date format error :" + d);
        }
        String date=d;
        if (separator.compareTo("/") == 0) {
            date = d.replaceAll("/", "-");
        }
        return Date.valueOf(date);
    }

    public void setDateMax(String d) throws Exception {
        setDateMax(this.setDate(d));
    }

    public void setDateMin(String d) throws Exception {
        setDateMin(this.setDate(d));
    }

    public HashMap<String, String> getLike() {
        return like;
    }
    
    public boolean isLikeEmpty(){
        return like.isEmpty();
    }
    public boolean isVariableEmpty(){
        return variable.isEmpty();
    }

    public HashMap<String, List<Object>> getVariable() {
        return variable;
    }

    public String getDateRef() {
        return dateRef;
    }

    public void setDateRef(String dateRef) {
        this.dateRef = dateRef;
    }

    public Date getDateMin() {
        return dateMin;
    }

    public void setDateMin(Date dateMin) throws Exception {
        if (this.dateRef == null) {
            throw new Exception("Date ref undifined");
        }
        this.dateMin = dateMin;
    }

    public Date getDateMax() {
        return dateMax;
    }

    public void setDateMax(Date dateMax) throws Exception {
        if (this.dateRef == null) {
            throw new Exception("Date ref undifined");
        }
        this.dateMax = dateMax;
    }

}
