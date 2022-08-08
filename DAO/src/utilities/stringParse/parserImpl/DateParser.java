/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utilities.stringParse.parserImpl;

import java.sql.Date;
import java.time.LocalDate;
import utilities.date.DateUtil;
import utilities.stringParse.Parser;

/**
 *
 * @author andyrazafy
 */
public class DateParser implements Parser {

    @Override
    public Object parse(String parameter) throws Exception {
        if(parameter.isEmpty()) return null;
        parameter=DateUtil.formatDateToSql(parameter);
        return Date.valueOf(LocalDate.parse(parameter));
    }

    @Override
    public String toSql(Object obj) throws Exception {
        return "'"+((Date)obj).toString()+"'";
    }
    
}
