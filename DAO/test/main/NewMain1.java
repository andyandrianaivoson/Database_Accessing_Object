/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package main;

import connexion.Connexion;
import interDB.InterDB;
import interDB.SearchParameter;
import java.util.List;
import java.util.stream.Collectors;
import teste.Departement;
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
//            Departement d=new Departement("scolaire");
//            Departement d1=new Departement("management");
//            Departement d2=new Departement("commerce");
////            fananiko natao roa mihitsy le rh
//            Departement d3=new Departement("compta");
//            InterDB.insert(con.getCon(),d);
//            InterDB.insert(con.getCon(),d1);
//            InterDB.insert(con.getCon(),d2);
//            InterDB.insert(con.getCon(),d3);
            
//            
//            ------------------find----------------------------
            Departement ref=new Departement();
            ref.setId(5);
            List<Departement> liste=InterDB.find(con.getCon(),ref,new SearchParameter(),"");
            liste.get(0).setNom("teste update");
            InterDB.update(con.getCon(), liste.get(0), null);
//            
//            liste.forEach(line -> System.out.println(line.getId()+" "+line.getNom()));
//            List<Object> parameters= new ArrayList<>();
//            Personne p= new Personne();
//            //p.setNom("a");
//            SearchParameter param= new SearchParameter();
//            param.addMinInTheVariable("masse", 30);
//            param.addMaxInTheVariable("masse", 49);
//            String req="select nom,dtn,sum(masse) masse from personne group by nom,dtn";
////            List<Personne> liste = InterDB.find(con.getCon(),Personne.class, req, parameters);
//
//            List<Personne> liste = InterDB.find(con.getCon(), p, " order by masse", param);
            liste.forEach(line -> System.out.println(line.toString()));
            con.getCon().commit();
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
