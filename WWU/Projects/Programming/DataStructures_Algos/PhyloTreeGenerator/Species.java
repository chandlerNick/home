/* 
 * Species.java
 *
 * Defines a new "Species" type, which stores the information associated
 * with a species
 *
 * This class has been implemented for you. You may not modify it.
 * 
 * Brian Hutchinson
 *
 */

public class Species {
    private String name;          // A unique name associated with the species
    private String[] sequence;    // The biological sequence describing this species

    // CONSTRUCTOR

    // Species
    // Preconditions:
    //     - name is the intended name of the species
    //     - sequence is a positive-length array of strings,
    //       where each string in the array is a single character 
    //       in the genetic sequence 
    // Post-conditions
    //     - The object's fields are set to the provided values
    public Species(String name, String[] sequence) {
        this.name = name;
        this.sequence = sequence;
        return;
    }

    // ACCESSORS

    // getName
    // Pre-conditions:
    //        - None
    // Post-conditions:
    //        - Returns the name
    public String getName() {
        return this.name;
    }
    
    // getSequence
    // Pre-conditions:
    //        - None
    // Post-conditions:
    //        - Returns the sequence
    public String[] getSequence() {
        return this.sequence;
    }

    // STATIC

    // distance
    // Pre-conditions:
    //        - a and b are two non-null Species objects
    //          whose sequences have already been aligned
    // Post-conditions:
    //        - Returns the fraction of sequence elements
    //          that are different
    //        - If the sequences are not the same length,
    //          it reports and error and exits
    public static double distance(Species a, Species b) {
        String[] seq1 = a.getSequence();
        String[] seq2 = b.getSequence();
    
        if( seq1.length != seq2.length ) {
            System.err.println("Error: Sequences must already be aligned");
            System.exit(5);
        } 
        
        int numDiffs = 0;
        for( int i=0; i<seq1.length; i++ ) {
            if( !seq1[i].equals(seq2[i]) ) {
                numDiffs++;
            }
        }

        double salt = 1.0/(a.getName().hashCode()^b.getName().hashCode());
        
        return ((double)numDiffs)/seq1.length + salt;
    }
}
