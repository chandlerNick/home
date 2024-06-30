/* 
 * Program2.java
 *
 * The driver program for CSCI 241's Program 2
 *
 * Creates PhyloTree objects for each FASTA file in alignment list input file
 * Infers the hierarhical structure, computes all pairwise evolutionary 
 * distances, and reports other tree statistics
 * 
 * Do not modify this file.  
 *
 * Brian Hutchinson
 *
 * ----------------------------------------------------------------------------
 *
 * usage:
 *
 * java Program2 fastaListFilename outputDir
 *
 * where the argument is
 * 
 *   fastaListFilename      a plaintext file with one line per FASTA alignment file
 *   outputDir              a directory where the trees and statistics will be written
 *
 *
 * Note: This will not work unless your outputDir has already been created!
 *
*/

public class Program2 {
    private static final int PRINTING_DEPTH = 100;
    private static final String slash = "/"; // You may need to change this to "\" on Windows...

    public static void main(String[] args) {
        if( args.length != 2 ) {
            System.err.println("Error: Wrong number of arguments.");
            System.exit(2);
        }
    
        String fastaListFilename = args[0];
        String outputDir         = args[1];
        java.util.Scanner input = null;
        java.io.File inputFile = new java.io.File(fastaListFilename);
        try {
            input = new java.util.Scanner(inputFile);
        } catch( java.io.FileNotFoundException e ) {
            System.err.println("Error: Unable to open file " + fastaListFilename);
            System.exit(1);
        }

        int numFiles = 0;
        while( input.hasNext() ) {
            String fastaFilename = input.next();
            numFiles++;
            System.err.print("\nLoading tree " + numFiles);

            java.io.File fastaFile = new java.io.File(fastaFilename);

            PhyloTree tree = new PhyloTree(fastaFilename,PRINTING_DEPTH);
            System.err.println(" done");

            java.io.File treeOutFile = new java.io.File(outputDir + slash + fastaFile.getName() + ".tree");
            java.io.File distOutFile = new java.io.File(outputDir + slash + fastaFile.getName() + ".distances");
            java.io.PrintStream treeOut = null;
            java.io.PrintStream distOut = null;
            try {
                treeOut = new java.io.PrintStream(treeOutFile);
                distOut = new java.io.PrintStream(distOutFile);
            } catch( java.io.FileNotFoundException e ) {
                System.err.println("Error: Unable to open output file for writing" + e);
                System.exit(1);
            }

            System.out.print(tree);
            treeOut.print(tree.toTreeString());

            java.util.ArrayList<Species> speciesList = tree.getAllSpecies();
            if( speciesList != null ) {
                for( int i=0; i<speciesList.size(); i++ ) {
                    for( int j=0; j<speciesList.size(); j++ ) {
                        String label1 = speciesList.get(i).getName();
                        String label2 = speciesList.get(j).getName();
                        distOut.format("EvDistance(%s,%s) = %.2f\n",label1,label2,tree.findEvolutionaryDistance(label1,label2)); 
                    }
                }
            }
            System.out.println("# species is " + tree.countAllSpecies());
            System.out.println("Tree height is " + tree.getHeight());
            System.out.format("Weighted height is %.2f\n",tree.getWeightedHeight());
        }
        return;
    }
}
