/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package utilities.stringParse;

import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Time;
import utilities.stringParse.parserImpl.DateParser;
import utilities.stringParse.parserImpl.DoubleDParser;
import utilities.stringParse.parserImpl.DoubledParser;
import utilities.stringParse.parserImpl.FloatFParser;
import utilities.stringParse.parserImpl.FloatfParser;
import utilities.stringParse.parserImpl.IntParser;
import utilities.stringParse.parserImpl.IntegerParser;
import utilities.stringParse.parserImpl.LongLParser;
import utilities.stringParse.parserImpl.LonglParser;
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
    DOUBLE(double.class.getCanonicalName(),new DoubledParser()),
    DDOUBLE(Double.class.getCanonicalName(),new DoubleDParser()),
    FLOAT(float.class.getCanonicalName(),new FloatfParser()),
    FFLOAT(Float.class.getCanonicalName(),new FloatFParser()),
    LONG(long.class.getCanonicalName(),new LonglParser()),
    LLONG(Long.class.getCanonicalName(),new LongLParser()),
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
