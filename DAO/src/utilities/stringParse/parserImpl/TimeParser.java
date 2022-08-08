/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utilities.stringParse.parserImpl;

import utilities.stringParse.Parser;
import java.sql.Time;
import java.time.LocalTime;
/**
 *
 * @author andyrazafy
 */
public class TimeParser implements Parser {

    @Override
    public Object parse(String parameter) throws Exception {
        if(parameter.isEmpty()) return null;
        return Time.valueOf(LocalTime.parse(parameter));
    }
    
    @Override
    public String toSql(Object obj) throws Exception {
        return "'"+((Time)obj).toString()+"'";
    }
}
