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
//import teste.Signalement;

/**
 *
 * @author andyrazafy
 */
public class NewMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Connexion con= new Connexion();
        try {
//            Olona olona= new Olona("andy",15);
//            InterDB.insert(con.getCon(), olona);
//            System.out.println(olona.toString());
//            con.getCon().commit();
//            Signalement s= new Signalement();
//            s.setTitle("titra");
////            s.setId(5);
//            s.setLatitude(17.4582);
//            s.setLongitude(54.4582);
//            s.setIdstatus(1);
//            s.setIdtypesignal(1);
//            s.setIdutilisateur(1);
//             InterDB.insert(con.getCon(), s);
//            con.getCon().commit();
//            InterDB.update(con.getCon(), s, null);
//            List<Signalement> liste= InterDB.findAll(con.getCon(), Signalement.class, 0, 0).stream()
//                    .map(line -> (Signalement)line)
//                    .collect(Collectors.toList());
//            liste.forEach(line-> System.out.println(line.toString()));
//            System.out.println("-------------------------");
//            Utilisateur u= new Utilisateur();
//            u.setId(1);
//            Utilisateur lu=InterDB.find(con.getCon(), u).stream()
//                    .map(line -> (Utilisateur)line)
//                    .findFirst().orElseThrow();
//            System.out.println(lu.toString());
//            List<Role> roles= lu.getRoles(con.getCon());
//            roles.forEach(line -> System.out.println(line.toString()));
//            
//            
//            VSignalement ss= new VSignalement();
//            ss.setIdutilisateur(lu.getId());
//            ss.setId(5);
//            System.out.println(ss.getIdutilisateur());
            
            
//            VSignalement ls=InterDB.find(con.getCon(), ss).stream().map(line-> (VSignalement) line).findFirst().orElseThrow();
//            Image im= new Image();
//            im.setIdsignalement(5);
//            ls.setImages(InterDB.find(con.getCon(), im).stream().map(line -> (Image)line).collect(Collectors.toList()));
//            ls.getImages().forEach(image-> System.out.println(image.getNom()));
            
            
            
            
//              String req="select roleid from v__role";
//              PreparedStatement stmt=con.getCon().prepareStatement(req);
//              ResultSet res=stmt.executeQuery();
//              while(res.next()){
//                  System.out.println(res.getString("nom"));
//              }
// -----------------------------------------------------------
//            SearchParameter param=new SearchParameter();
////            param.addInTheVariable("idregion", 9);
////            param.addInTheVariable("idtypesignal", 1);
//            param.addInTheVariable("idstatus", null);
////            System.out.println(param.isVariableEmpty());
//            List<Signalement> liste=InterDB.search(con.getCon(), Signalement.class, param).stream()
//                    .map(line -> (Signalement)line)
//                    .collect(Collectors.toList());
//            liste.forEach(line -> System.out.println(line.toString()));

//            String req= "select * from signalement where idstatus is ?";
//            PreparedStatement stmt= con.getCon().prepareStatement(req);
//            stmt.setString(1, null);
//            ResultSet res=stmt.executeQuery();
//            while(res.next()){
//                System.out.println(res.getString("id")+" "+res.getString("title"));
//            }
            
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
