/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package interDB;

/**
 *
 * @author andyrazafy
 */
public class NoReference extends Exception {

    /**
     * Creates a new instance of <code>NoTable</code> without detail message.
     */
    public NoReference() {
    }

    /**
     * Constructs an instance of <code>NoTable</code> with the specified detail
     * message.
     *
     * @param msg the detail message.
     */
    public NoReference(String msg) {
        super(msg);
    }
}
