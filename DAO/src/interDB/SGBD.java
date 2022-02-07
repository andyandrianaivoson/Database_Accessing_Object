/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package interDB;

/**
 *
 * @author andyrazafy
 */
public enum SGBD{
        POSTGRES("postgresql"),
        MYSQL("mysql"),
        MSSQL("sqlserver");
        
        private final String value;
        private SGBD(String value){
            this.value=value;
        }
        public String value(){
            return this.value;
        }
   }
