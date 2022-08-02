/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package main;

import connexion.Connexion;
import interDB.InterDB;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import personne.Personne;
//import teste.Signalement;

/**
 *
 * @author andyrazafy
 */
public class NewMain1 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Connexion con= new Connexion();
        try {
//            -------------------Insert-------------------------
//            Departement d=new Departement("finance");
//            Departement d1=new Departement("comptabilite");
//            Departement d2=new Departement("rh");
////            fananiko natao roa mihitsy le rh
//            Departement d3=new Departement("rh");
//            InterDB.insert(con.getCon(),d);
//            InterDB.insert(con.getCon(),d1);
//            InterDB.insert(con.getCon(),d2);
//            InterDB.insert(con.getCon(),d3);
            
//            con.getCon().commit();
//            ------------------find----------------------------
//            Departement ref=new Departement("rh");
//            List<Departement> liste=InterDB.find(con.getCon(), ref).stream()
//                    .map(line -> (Departement)line)
//                    .collect(Collectors.toList());
//            
//            
//            liste.forEach(line -> System.out.println(line.getId()+" "+line.getNom()));
            List<Object> parameters= new ArrayList<>();
            Personne p= new Personne();
            p.setNom("zafy");
            String req="select nom,dtn,sum(masse) masse from personne group by nom,dtn";
//            List<Personne> liste = InterDB.find(con.getCon(),Personne.class, req, parameters);
            List<Personne> liste = InterDB.find(con.getCon(), p);
            liste.forEach(line -> System.out.println(line.getMasse()));
        } catch (Exception e) {
            try {
                con.getCon().rollback();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            con.close();
        }
    }
    
}
