/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package interDB;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author andyrazafy
 * 
 * table is used to provide the table of the mapping class
 * id is generally the id that the class uses to refer to the original id of the table
 * id dim is used to set the id value in the table (this is to avoid a merely numerical value of an id)
 * sequence is the sequence used to set the id value of the table
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DBTable {
    public String table() default "0";
    public String id() default "0";
    public String idDim() default"0";
    public String sequence() default "0";
}
