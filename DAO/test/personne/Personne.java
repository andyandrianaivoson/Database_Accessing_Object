/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package personne;
import interDB.DBTable;
import interDB.IdType;
import java.sql.Date;
/**
 *
 * @author andyrazafy
 */
@DBTable(table="personne")
public class Personne {
    private String nom;
    private Date dtn;
    private Double masse;

    @Override
    public String toString() {
        return "Personne{" + "nom=" + nom + ", dtn=" + dtn + ", masse=" + masse + '}';
    }

    
    public Personne() {
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }
    
    

    public Date getDtn() {
        return dtn;
    }

    public void setDtn(Date dtn) {
        this.dtn = dtn;
    }

    public Double getMasse() {
        return masse;
    }

    public void setMasse(Double masse) {
        this.masse = masse;
    }
    
    
}
