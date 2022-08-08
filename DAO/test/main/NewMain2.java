/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package main;

import interDB.SGBD;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import personne.Personne;

/**
 *
 * @author andyrazafy
 */
public class NewMain2 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        SGBD[] sgbds=SGBD.values();
//        for(SGBD sgbd:sgbds) System.out.println(sgbd.name()+" "+sgbd.value());
        List<Personne> pers= new ArrayList<>();
        Class c= pers.getClass();
        Arrays.asList(c.getGenericInterfaces()).forEach(line -> System.out.println(line.getTypeName()));
    }
    
}
