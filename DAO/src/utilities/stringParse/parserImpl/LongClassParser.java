/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utilities.stringParse.parserImpl;

import utilities.stringParse.Parser;

/**
 *
 * @author andyrazafy
 */
public class LongClassParser implements Parser {

    @Override
    public Object parse(String parameter) throws Exception {
        if(parameter.isEmpty()) return null;
        return Long.valueOf(parameter);
    }
    
    @Override
    public String toSql(Object obj) throws Exception {
        return ((Long)obj).toString();
    }
}
