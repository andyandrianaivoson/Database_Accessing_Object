/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import teste.Departement;

/**
 *
 * @author andyrazafy
 */
public class TestCollectors {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        Arrays.asList(Departement.class.getDeclaredMethods()).forEach(line ->{
            System.out.println(line.getName());
            System.out.println(line.getReturnType().getCanonicalName());
        });
    }
    
}
