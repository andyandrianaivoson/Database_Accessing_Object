/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package interDB;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * used as a high level parameter for the fetch statement in the DB where 'like'
 * contains all the parameters used with like keyword 'variable' contains the
 * interval parameters; 'listeVariable' is the keySet of 'variable' translated
 * to List.
 *
 * @author andyrazafy
 *
 */
public class SearchParameter {

    private HashMap<String, String> like = null;
    private HashMap<String, Object[]> variable = null;
    private String[] listeVariable = null;

    /**
     * add a parameter like for the search
     *
     * @param key
     * @param value
     */
    public void putInLike(String key, String value) {
        if (like == null) {
            like = new HashMap<>();
        }
        if (!value.isEmpty() && value != null) {
            like.put(key, "%" + value + "%");
        }
    }

    /**
     * get the list of the interval parameter
     *
     * @return
     */
    public String[] getListeVariable() {
        if (listeVariable == null) {
            listeVariable = variable.keySet().toArray(new String[0]);
        }
        return listeVariable;
    }

    /**
     * add min to an interval parameter. Make sure the this provided value is a
     * valide object for the key's type
     *
     * @param k
     * @param v
     */
    public void addMinInTheVariable(String k, Object v) {
        if (v == null) {
            return;
        }
        if (variable == null) {
            variable = new HashMap<>();
        }
        if (!"".equals(v) && !"default".equals(v) && !"none".equals(v)) {
            if (variable.get(k) == null) {
                Object[] el = new Object[2];
                el[0] = v;
                variable.put(k, el);
            }
            Object[] el = variable.get(k);
            el[0] = v;
        }
    }

    /**
     * add max to an interval parameter. Make sure the this provided value is a
     * valide object for the key's type
     *
     * @param k
     * @param v
     */
    public void addMaxInTheVariable(String k, Object v) {
        if (v == null) {
            return;
        }
        if (variable == null) {
            variable = new HashMap<>();
        }
        if (!"".equals(v) && !"default".equals(v) && !"none".equals(v)) {
            if (variable.get(k) == null) {
                Object[] el = new Object[2];
                el[1] = v;
                variable.put(k, el);
            }
            Object[] el = variable.get(k);
            el[1] = v;
        }
    }

    /**
     * check if min is set to the interval parameter
     *
     * @param k
     * @return
     */
    public boolean isMinSet(String k) {
        boolean valiny = true;
        if (variable != null) {
            Object[] el = variable.get(k);
            if (el == null) {
                return false;
            }
            if (el[0] == null) {
                return false;
            }
        }
        return valiny;
    }

    /**
     * check if min is set to the interval parameter
     *
     * @param k
     * @return
     */
    public boolean isMaxSet(String k) {
        boolean valiny = true;
        if (variable != null) {
            Object[] el = variable.get(k);
            if (el == null) {
                return false;
            }
            if (el[1] == null) {
                return false;
            }
        }
        return valiny;
    }

    public Object getMin(String k) {
        return variable.get(k)[0];
    }

    public Object getMax(String k) {
        return variable.get(k)[1];
    }

    /**
     * check if this search parameter empty
     *
     * @return
     */
    public boolean isEmpty() {
        boolean valiny = true;
        if (!isLikeEmpty()) {
            valiny = false;
        }
        if (!isVariableEmpty()) {
            valiny = false;
        }
        return valiny;
    }

    public HashMap<String, String> getLike() {
        return like;
    }

    public boolean isLikeEmpty() {
        if (like == null) {
            return true;
        }
        return like.isEmpty();
    }

    public boolean isVariableEmpty() {
        if (variable == null) {
            return true;
        }
        return variable.isEmpty();
    }

    /**
     * this function will create both the 'after where' of the request and
     * prepare the list of the parameters for the preparedStatement.Make sure
     * that the parameters set in this searchParameter is not set within the
     * object because this searchParameter has the privilege over the object.
     *
     * @param o
     * @param req the request already preformed
     * @param fields the fields metadata
     * @param params this fields will contains all the parameters needed in the
     * preparedStatement so it must be initialized empty
     * @param columnsType
     * @return
     * @throws Exception
     */
    public String makeParameter(Object o, String req, List<FieldDAO> fields, List<Object> params) throws Exception {
        String query = req;
        for (int i = 0; i < fields.size(); i++) {
            FieldDAO field = fields.get(i);
            Method getter = fields.get(i).getGetter();
            String key = fields.get(i).getField();
            if (field.isId()) {
                if (field.isUnsafe()) {
                    Number idValue =  (Number)getter.invoke(o);
                    if (idValue.longValue() > 0) {
                        query += " and id=? ";
                        params.add(idValue);
                    }
                } else {
                    if (field.isSet(o)) {
                        query += " and id=? ";
                        params.add(getter.invoke(o));
                    }
                }
            } else {
                if (!isLikeEmpty() && like.containsKey(fields)) {
                    query += " and upper(" + key + ") like(upper(?)) ";
                    params.add(like.get(key));
                } else {
                    if (field.isString()) {
                        if (field.isSet(o)) {
                            query += " and upper(" + key + ") like(upper(?)) ";
                            params.add("%" + getter.invoke(o) + "%");
                        }
                    } else {
                        if (!isVariableEmpty() && variable.containsKey(key)) {
                            if (isMinSet(key)) {
                                query += " and " + key + " >= ? ";
                                params.add(getMin(key));
                            }
                            if (isMaxSet(key)) {
                                query += " and " + key + " <= ? ";
                                params.add(getMax(key));
                            }
                        } else {
                            if (!field.isUnsafe()) {
                                if (field.isSet(o)) {
                                    query += " and " + key + " >= ? ";
                                    query += " and " + key + " <= ? ";
                                    params.add(getter.invoke(o));
                                    params.add(getter.invoke(o));
                                }
                            }
                        }
                    }
                }
            }
        }

        return query;
    }

}
