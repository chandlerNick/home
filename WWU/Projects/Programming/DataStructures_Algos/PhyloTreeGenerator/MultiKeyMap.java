/* 
 * MultiKeyMap.java
 *
 * Defines a very limited map type that supports two keys
 * It is a generic class, so for exmample MultiKeyMap<Integer> 
 * corresponds to a mapping from two strings to an Integer
 * 
 * A two key map acts similar to HashMap, except that it supports
 * having two keys If you define a MultiKeyMap<Integer> variable named
 * someMap, then
 *
 * someMap.put("hi","dog",5);
 * 
 * is equivalent to
 * 
 * someMap.put("dog","hi",5);
 * 
 * and the value associated with these two keys could be retrieved by either
 * 
 * someMap.get("hi","dog") 
 *
 * or 
 * 
 * someMap.get("dog","hi")
 *
 * This class has been implemented for you, but you may extend or
 * modify it if you want If you modify, you must check this file
 * in to your repository.
 * 
 * Brian Hutchinson
 *
 * If modified, student name goes here and modification date go here
 *
 */

public class MultiKeyMap<V> {
    private java.util.HashMap<String,V> map;

    // MultiKeyMap
    // Pre-conditions:
    //      - None
    // Post-conditions:
    //      - An empty MultiKeyMap has been created
    public MultiKeyMap() {
        this.map = new java.util.HashMap<String,V>();
        return;
    }

    // put
    // Pre-conditions:
    //        - k1 and k2 are the two keys
    //          neither can contain the bar ("|") symbol
    //        - value is the value you wish you store
    // Post-conditions:
    //        - {k1,k2} now maps to value in the multikey map
    //        - If either key contained a bar character, an error
    //          message is printed and the program exits 
    public void put(String k1, String k2, V value) {
        if( k1.contains("|") || k2.contains("|") ) {
            System.err.println("Error: Keys in a MultiKeyMap cannot contain the bar character (\"|\")");
            System.exit(10);
        }
        map.put(k1 + "|" + k2,value);
        if( !k1.equals(k2) ) {
            map.remove(k2 + "|" + k1);
        }
        return;
    }

    // get
    // Pre-conditions:
    //        - k1 and k2 are the two keys
    //          neither can contain the bar ("|") symbol
    // Post-conditions:
    //        - If the multikey map contains an entry for multikey {k1,k2}, the value is returned
    //        - Else: it returns null
    //        - If either key contained a bar character, an error
    //          message is printed and the program exits 
    public V get(String k1, String k2) {
        if( k1.contains("|") || k2.contains("|") ) {
            System.err.println("Error: Keys in a MultiKeyMap cannot contain the bar character (\"|\")");
            System.exit(10);
        }
        if( map.containsKey(k1 + "|" + k2) ) {
            return map.get(k1 + "|" + k2);
        } else {
            return map.get(k2 + "|" + k1);
        }
    }

    // remove
    // Pre-conditions:
    //        - k1 and k2 are two keys whose associated value you wish to remove
    //          neither can contain the bar ("|") symbol
    // Post-conditions:
    //        - No value is associated with multikey {k1,k2}
    //        - If either key contained a bar character, an error
    //          message is printed and the program exits 
    public void remove(String k1, String k2) {
        if( k1.contains("|") || k2.contains("|") ) {
            System.err.println("Error: Keys in a MultiKeyMap cannot contain the bar character (\"|\")");
            System.exit(10);
        }
        map.remove(k1 + "|" + k2);
        map.remove(k2 + "|" + k1);
        return;
    }
}
