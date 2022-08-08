/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utilities.stringParse;

import interDB.SGBD;
import java.util.HashMap;

/**
 *
 * @author andyrazafy
 * 
 * This class implements the design pattern 'stategy' dynamically. 
 * The ParserEnum.java is used to list all the available strategies.
 * 
 * The package parserImpl contains all the strategies
 * 
 */
public class StringParser {
    /// this field contains the entry of each strategy where the key is the canonical 
    /// name of the class that the string will be parsed to and the value is 
    /// the class used to parse the string 
    private static final HashMap<String,Parser> CONTEXT;
    /// loading the strategies in CONTEXT
    static{
        CONTEXT= new HashMap<String, Parser>();
        ParserEnum[] map= ParserEnum.values();
        for(ParserEnum state : map) CONTEXT.put(state.type(), state.parser());
    }
    
    /// the general method which parse the String
    public static <T> T parse(Class<T> type,String value) throws Exception{
        Parser state= CONTEXT.get(type.getCanonicalName());
        return (T)state.parse(value);
    }
    
    public static String toSql(Object obj)throws Exception{
        Class type= obj.getClass();
        Parser state= CONTEXT.get(type.getCanonicalName());
        return state.toSql(obj);
    }
}
