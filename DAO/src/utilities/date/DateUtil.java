/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utilities.date;

/**
 *
 * @author andyrazafy
 */
public class DateUtil {
    public static String formatDateToSql(String date)throws Exception{
        date= date.replaceAll("/", "-");
        String[] parts= date.split("-");
        if(parts.length!=3) throw new Exception("invalide date : '"+date+"'");
        if(parts[0].length()<4){
            date=parts[2]+"-"+parts[1]+"-"+parts[0];
        }
        return date;
    }
    public static String formatTimestampToSql(String timestamp)throws Exception{
        String[] parts = timestamp.split(" ");
        if(parts.length==2){
            parts[0]=formatDateToSql(parts[0]);
            timestamp=parts[0]+"T"+parts[1];
        }else if(parts.length>2){
            throw new Exception("timestamp invalide : '"+timestamp+"'");
        }
        return timestamp;
    }
}
