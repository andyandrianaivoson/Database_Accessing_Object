/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utilities.stringParse.parserImpl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import utilities.date.DateUtil;
import utilities.stringParse.Parser;

/**
 *
 * @author andyrazafy
 */
public class TimestampParser implements Parser {

    @Override
    public Object parse(String parameter) throws Exception {
        if(parameter.isEmpty()) return null;
        parameter=DateUtil.formatTimestampToSql(parameter);
        return Timestamp.valueOf(LocalDateTime.parse(parameter));
    }
    
    @Override
    public String toSql(Object obj) throws Exception {
        return "'"+((Timestamp)obj).toString()+"'";
    }
}
