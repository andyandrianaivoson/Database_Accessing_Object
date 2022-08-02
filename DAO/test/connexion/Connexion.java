/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connexion;

import java.sql.Connection;
import java.sql.DriverManager;


/**
 *
 * @author andyrazafy
 */
public class Connexion {
    Connection con=null;
    
    public Connexion(){
        
        String url="jdbc:postgresql://localhost:5432/teste";
//        String url="jdbc:mysql://localhost:3306/test";
        String user="db_admin";
//        String user="root";
        String mdp="mdpprom13";
//        String mdp="";
        try {
            Class.forName("org.postgresql.Driver");
//            Class.forName("com.mysql.cj.jdbc.Driver");
            setCon(DriverManager.getConnection(url, user, mdp));
            
            if(con!=null)
                con.setAutoCommit(false);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void setCon(Connection c){
        if(getCon()==null){
            con=c;
        }
    }
    public Connection getCon(){
        return con;
    }
    public void close(){
        try {
            con.close();
            con=null;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
}