/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package teste;

import interDB.DBTable;
import interDB.IdType;

/**
 *
 * @author andyrazafy
 */
@DBTable(id = "id",table = "departement",idType = IdType.SERIAL)
public class Departement {
    private Integer id;
    private String nom;

    public Departement() {
    }

    public Departement(Integer id, String nom) {
        this.id = id;
        this.nom = nom;
    }

    public Departement(String nom) {
        this.nom = nom;
    }
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }
    
}
