/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package utilities.stringParse;

/**
 *
 * @author andyrazafy
 * 
 * T
 */
public interface Parser {
    Object parse(String parameter)throws Exception;
    String toSql(Object obj)throws Exception;
}
