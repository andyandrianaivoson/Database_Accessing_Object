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
 * this annotation is used to specify a field that is a reference to a numerical field 
 * why? => to specify whether a numerical field is already set or not;
 * the value is the referenced field name
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NumberRef {
    public String value() default "";
}
