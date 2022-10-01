/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package utilities.stringParse;

import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Time;
import utilities.stringParse.parserImpl.DateParser;
import utilities.stringParse.parserImpl.DoubleClassParser;
import utilities.stringParse.parserImpl.DoubleParser;
import utilities.stringParse.parserImpl.FloatClassParser;
import utilities.stringParse.parserImpl.FloatParser;
import utilities.stringParse.parserImpl.IntParser;
import utilities.stringParse.parserImpl.IntegerParser;
import utilities.stringParse.parserImpl.LongClassParser;
import utilities.stringParse.parserImpl.LongParser;
import utilities.stringParse.parserImpl.TimeParser;
import utilities.stringParse.parserImpl.TimestampParser;

/**
 *
 * @author andyrazafy
 * 
 * this Enum contains all the strategies to parse a String
 * 
 * to add a new strategy you have to provide the type of the parse's result as a string
 * and the an object which is the implementation of the parsing method;
 */
public enum ParserEnum {
    INT(int.class.getCanonicalName(),new IntParser()),
    DOUBLE(double.class.getCanonicalName(),new DoubleParser()),
    DDOUBLE(Double.class.getCanonicalName(),new DoubleClassParser()),
    FLOAT(float.class.getCanonicalName(),new FloatParser()),
    FFLOAT(Float.class.getCanonicalName(),new FloatClassParser()),
    LONG(long.class.getCanonicalName(),new LongParser()),
    LLONG(Long.class.getCanonicalName(),new LongClassParser()),
    DATE(Date.class.getCanonicalName(),new DateParser()),
    TIMESTAMP(Timestamp.class.getCanonicalName(),new TimestampParser()),
    TIME(Time.class.getCanonicalName(),new TimeParser()),
    INTEGER(Integer.class.getCanonicalName(),new IntegerParser());
    
    private final String type;
    private final Parser parser;
    private ParserEnum(String type,Parser parser){
        this.type=type;
        this.parser=parser;
    }
    public Parser parser(){
        return this.parser;
    }
    public String type(){
        return this.type;
    }
}
