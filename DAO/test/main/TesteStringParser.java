/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package main;

import java.sql.Date;
import utilities.stringParse.StringParser;

/**
 *
 * @author andyrazafy
 */
public class TesteStringParser {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        try {
            float d=StringParser.parse(float.class, "15.8");
            System.out.println(d+2);
            Date date=StringParser.parse(Date.class, "2022-11-25");
            System.out.println(StringParser.toSql(date));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
}
