/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package interDB;

/**
 *
 * @author andyrazafy
 */
public enum IdType {
    SERIAL("serial"),
    MODIFIED("modified");
    private final String value;

    private IdType(String value) {
        this.value = value;
    }
    public String value(){
        return value;
    }
    
}
