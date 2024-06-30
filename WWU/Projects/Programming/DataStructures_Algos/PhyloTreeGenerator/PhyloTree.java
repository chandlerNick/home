/*
 * PhyloTree.java
 *
 * Defines a phylogenetic tree, which is a strictly binary tree
 * that represents inferred hierarchical relationships between species
 *
 * There are weights along each edge; the weight from parent to left child
 * is the same as parent to right child.
 *
 * Students may only use functionality provided in the packages
 *     java.lang
 *     java.util
 *     java.io
 *
 * Use of any additional Java Class Library components is not permitted
 *
 * Nick Chandler & Dylan Kleven
 *
 */
import java.lang.*;
import java.util.*;
import java.io.*;

public class PhyloTree {
    private PhyloTreeNode overallRoot;    // The actual root of the overall tree
    private int printingDepth;            // How many spaces to indent the deepest
                                          // node when printing

    private MultiKeyMap<Double> mkm = new MultiKeyMap<>();

    // CONSTRUCTOR

    // PhyloTree
    // Pre-conditions:
    //        - speciesFile contains the path of a valid FASTA input file
    //        - printingDepth is a positive number
    // Post-conditions:
    //        - this.printingDepth has been set to printingDepth
    //        - A linked tree structure representing the inferred hierarchical
    //          species relationship has been created, and overallRoot points to
    //          the root of this tree
    // Notes:
    //        - A lot happens in this step!  See assignment description for details
    //          on the input format file and how to construct the tree
    //        - If you encounter a FileNotFoundException, print to standard error
    //          "Error: Unable to open file " + speciesFile
    //          and exit with status (return code) 1
    //        - Most of this should be accomplished by calls to loadSpeciesFile and buildTree
    public PhyloTree(String speciesFile, int printingDepth) {
        try{
        //call methods to use file here
            Species[] speciesArray = loadSpeciesFile(speciesFile);
            buildTree(speciesArray);
        }catch(Exception e){
            System.err.println("Error: Unable to open file " + speciesFile);
            e.printStackTrace();
            System.exit(1);
        }
        this.printingDepth = printingDepth;
        return;
    }

    // ACCESSORS

    // getOverallRoot
    // Pre-conditions:
    //    - None
    // Post-conditions:
    //    - Returns the overall root
    public PhyloTreeNode getOverallRoot() {
        return this.overallRoot;
    }

    // toString
    // Pre-conditions:
    //    - None
    // Post-conditions:
    //    - Returns a string representation of the tree
    // Notes:
    //    - See assignment description for proper format
    //        (it will be a kind of reverse in-order [RNL] traversal)
    //    - Can be a simple wrapper around the following toString
    //    - Hint: StringBuilder is much faster than repeated concatenation
    public String toString() {
        return toString(this.overallRoot, 0.0, getWeightedHeight());
    }

    // toString
    // Pre-conditions:
    //    - node points to the root of a tree you intend to print
    //    - weightedDepth is the sum of the edge weights from the
    //      overall root to the current root
    //    - maxDepth is the weighted depth of the overall tree
    // Post-conditions:
    //    - Returns a string representation of the tree
    // Notes:
    //    - See assignment description for proper format
    private String toString(PhyloTreeNode node, double weightedDepth, double maxDepth) {
        if(node == null){
            return "";
        }
        int k = (int)Math.round(this.printingDepth * (weightedDepth / maxDepth));
        StringBuilder dots = new StringBuilder(k);
        for(int i = 0; i < k; i++){
            dots.append(".");
        }
        double weighDep = getWeightedDepth(node);
        String result = toString(node.getRightChild(), weighDep, maxDepth) + "\n" +
        dots.toString() + node.toString() +toString(node.getLeftChild(), weighDep, maxDepth);
        return result;
    }


    // toTreeString
    // Pre-conditions:
    //    - None
    // Post-conditions:
    //    - Returns a "newick" string representation in tree format
    // Notes:
    //    - See assignment description for format details
    //    - Can be a simple wrapper around the following toTreeString
    public String toTreeString() {
        return toTreeString(this.overallRoot)+";";
    }


    // toTreeString
    // Pre-conditions:
    //    - node points to the root of a tree you intend to print
    // Post-conditions:
    //    - Returns a "newick" string representation in tree format
    // Notes:
    //    - See assignment description for proper format
    private String toTreeString(PhyloTreeNode node) {
        if(node == null){
            return "";
        }
        if(node.isLeaf()){
            return node.getLabel()+":"+node.getParent().getDistanceToChild();
        }else{
            if(node.getParent() == null){
                return "("+toTreeString(node.getRightChild())+","+toTreeString(node.getLeftChild())+")";
            }else{
                return "("+toTreeString(node.getRightChild())+","+toTreeString(node.getLeftChild())+"):"+node.getParent().getDistanceToChild();
            }
        }
    }

    // getHeight
    // Pre-conditions:
    //    - None
    // Post-conditions:
    //    - Returns the tree height as defined in class
    // Notes:
    //    - Can be a simple wrapper on nodeHeight
    public int getHeight() {
        return nodeHeight(this.overallRoot);
    }

    // getWeightedHeight
    // Pre-conditions:
    //    - None
    // Post-conditions:
    //    - Returns the sum of the edge weights along the
    //      "longest" (highest weight) path from the root
    //      to any leaf node.
    // Notes:
    //   - Can be a simple wrapper for weightedNodeHeight
    public double getWeightedHeight() {
        return weightedNodeHeight(this.overallRoot);
    }

    // countAllSpecies
    // Pre-conditions:
    //    - None
    // Post-conditions:
    //    - Returns the number of species in the tree
    // Notes:
    //    - Non-terminals do not represent species
    //    - This functionality is provided for you elsewhere
    //      just call the appropriate method
    public int countAllSpecies() {
        ArrayList<Species> temp = getAllSpecies();
        return temp.size();
    }

    // getAllSpecies
    // Pre-conditions:
    //    - None
    // Post-conditions:
    //    - Returns an ArrayList containing all species in the tree
    // Notes:
    //    - Non-terminals do not represent species
    // Hint:
    //    - Call getAllDescendantSpecies
    public java.util.ArrayList<Species> getAllSpecies() {
        ArrayList<Species> result = new ArrayList<>();
        getAllDescendantSpecies(this.overallRoot, result);
        return result;
    }

    // findTreeNodeByLabel
    // Pre-conditions:
    //    - label is the label of a tree node you intend to find
    //    - Assumes labels are unique in the tree
    // Post-conditions:
    //    - If found: returns the PhyloTreeNode with the specified label
    //    - If not found: returns null
    public PhyloTreeNode findTreeNodeByLabel(String label) {
        return findTreeNodeByLabel(this.overallRoot, label);
    }

    // findLeastCommonAncestor
    // Pre-conditions:
    //    - label1 and label2 are the labels of two species in the tree
    // Post-conditions:
    //    - If either node cannot be found: returns null
    //    - If both nodes can be found: returns the PhyloTreeNode of their
    //      common ancestor with the largest depth
    //      Put another way, the least common ancestor of nodes A and B
    //      is the only node in the tree where A is in the left tree
    //      and B is in the right tree (or vice-versa)
    // Notes:
    //    - Can be a wrapper around the static findLeastCommonAncestor
     public PhyloTreeNode findLeastCommonAncestor(String label1, String label2) {
        PhyloTreeNode node1 = findTreeNodeByLabel(label1);
        PhyloTreeNode node2 = findTreeNodeByLabel(label2);
        return findLeastCommonAncestor(node1, node2);
    }

    // findEvolutionaryDistance
    // Pre-conditions:
    //    - label1 and label2 are the labels of two species in the tree
    // Post-conditions:
    //    - If either node cannot be found: returns POSITIVE_INFINITY
    //    - If both nodes can be found: returns the sum of the weights
    //      along the paths from their least common ancestor to each of
    //      the two nodes
     public double findEvolutionaryDistance(String label1, String label2) {
        //if get nodeby label of label 1 or label 2 == null
        PhyloTreeNode node1 = findTreeNodeByLabel(label1);
        PhyloTreeNode node2 = findTreeNodeByLabel(label2);
        if(node1 == null || node2 == null){
            double posInf = Double.POSITIVE_INFINITY;
            return posInf;
        }else{
            //get least common ancestor
            PhyloTreeNode leastComm = findLeastCommonAncestor(label1, label2);
            //find paths to least common ancestor
            ArrayList<PhyloTreeNode> path1 = getPath(node1, leastComm);
            ArrayList<PhyloTreeNode> path2 = getPath(node2, leastComm);



            //sum weight along paths
            double sum = 0.0;
            for(PhyloTreeNode i : path1){
                if(i != null){
                    sum += i.getDistanceToChild();
                }
            }
            for(PhyloTreeNode j : path2){
                if(j != null){
                    sum += j.getDistanceToChild();

                }
            }
            return sum;
        }
    }


    //retrun path of two nodes
    //pre: nodes are connected w/ root above bottom
    //post: array list of all nodes from bottom upto including root (excluding bottom) is returned.
    private ArrayList<PhyloTreeNode> getPath(PhyloTreeNode bottom, PhyloTreeNode root){
        ArrayList<PhyloTreeNode> path = new ArrayList<>();
        PhyloTreeNode temp = bottom;
        while(temp != root){
            path.add(temp.getParent());
            temp = temp.getParent();
        }
        return path;
    }

    private double getWeightedDepth(PhyloTreeNode node){
        ArrayList<PhyloTreeNode> path = getPath(node, this.overallRoot);
        double sum = 0.0;
        for(PhyloTreeNode i : path){
            sum += i.getDistanceToChild();
        }
        return sum;
    }



    // MODIFIER

    // buildTree
    // Pre-conditions:
    //    - species contains the set of species for which you want to infer
    //      a phylogenetic tree
    // Post-conditions:
    //    - A linked tree structure representing the inferred hierarchical
    //      species relationship has been created, and overallRoot points to
    //      the root of said tree
    // Notes:
    //    - A lot happens in this step!  See assignment description for details
    //      on how to construct the tree.
    //    - Be sure to use the tie-breaking conventions described in the pdf
    //    - Important hint: although the distances are defined recursively, you
    //      do NOT want to implement them recursively, as that would be very inefficient
    private void buildTree(Species[] species) {
        int speciesLength = species.length;
        ArrayList<Species> speciesAL = new ArrayList<>(Arrays.asList(species));

        //construct initial mkm & "forest"
        buildMap(species);
        HashMap<String, PhyloTreeNode> forest = buildForest(species);


        //while hashmap.size > 1
        while(forest.size() > 1){
            Double smallest = 2.0;
            String spec1 = "0000000000000000000000";     //reliant on program working properly to not throw errors
            String spec2 = "0000000000000000000000";
            PhyloTreeNode t1 = null;
            PhyloTreeNode t2 = null;

            //Find first minimum
            for(Map.Entry<String, PhyloTreeNode> outerEntry : forest.entrySet()){
                String outerKey = outerEntry.getKey();
                PhyloTreeNode outerValue = outerEntry.getValue();
                for(Map.Entry<String, PhyloTreeNode> innerEntry : forest.entrySet()){
                    String innerKey = innerEntry.getKey();
                    PhyloTreeNode innerValue = innerEntry.getValue();

                    if(!innerKey.equals(outerKey)){                 //duplicates not in map
                        Double dist = mkm.get(outerKey, innerKey);
                        if(dist < smallest){
                            smallest = dist;
                            if(outerKey.compareToIgnoreCase(innerKey) <= 0){
                                t1 = outerValue;
                                t2 = innerValue;
                                spec1 = t1.getLabel();
                                spec2 = t2.getLabel();
                            }
                            else{
                                t1 = innerValue;
                                t2 = outerValue;
                                spec1 = t1.getLabel();
                                spec2 = t2.getLabel();
                            }
                        }
                    }
                }
            }

        //create first Tnew
        String firstName = spec1+"+"+spec2;
        Double firstDist = smallest/2;
        PhyloTreeNode TNew = new PhyloTreeNode(firstName, null, t1, t2, firstDist);

        //add parent for t1 and t2
        t1.setParent(TNew);
        t2.setParent(TNew);

        forest.remove(spec1);
        forest.remove(spec2);

        populateMkm(forest, TNew);

        forest.put(firstName, TNew);
        if(forest.size() == 1){
            this.overallRoot = TNew;
            //System.out.println(this.overallRoot.getDistanceToChild());
            break;
        }
        }


        //compute distance between combo and every other node in the forest. Store label in the MkM
        //remove t1 t2 from the forest and mkm after computing new distance.
        //find smallest again between the Tnew and the Tother
        //create a new Tnew (parent) and determine T1 and T2 from Tother and Tnew
        //repeat until the size of the HashMap is 2 --> add last node.
        //return overall root


        //this.overallRoot = overall root (last node to fuse)
        return;
    }


    //populate mkm
    //pre: ComboNode is created, forest does not contain the child nodes of combonode
    //post: ComboNode has pairwise distance with all other nodes in hashmap
    private void populateMkm(HashMap<String, PhyloTreeNode> forest, PhyloTreeNode comboNode){
        //for node in forest
        for(Map.Entry<String, PhyloTreeNode> outerEntry : forest.entrySet()){
            String key = outerEntry.getKey();
            PhyloTreeNode value = outerEntry.getValue();
        //comupte distance between combonode and all other free nodes
            Double dist = computeDistance(value, comboNode);
        //put each combo in the hashmap (both permutations).
            this.mkm.put(value.getLabel(), comboNode.getLabel(), dist);
            this.mkm.put(comboNode.getLabel(), value.getLabel(), dist);
        }
        return;
    }



    //Compute distance
    //pre: The distance between other and the combonode's left and right children are in mkm
    //post: combonodes children are not in the MKM and the distance between combonode and other are computed
    private Double computeDistance(PhyloTreeNode other, PhyloTreeNode comboNode){
        //Find distance between combonode and other
        //remove t1 and t2, t1 and combonode, t2 and combonode permutations from the mkm.

        // pull values from mkm -- all distances should be in when needed for this algorithm


        Double weight1 = (comboNode.getLeftChild().getNumLeafs()) * 1.0 / (comboNode.getLeftChild().getNumLeafs() + comboNode.getRightChild().getNumLeafs());
        Double weight2 = (comboNode.getRightChild().getNumLeafs()) * 1.0 / (comboNode.getLeftChild().getNumLeafs() + comboNode.getRightChild().getNumLeafs());

        Double term1 = weight1 * this.mkm.get(other.getLabel(), comboNode.getLeftChild().getLabel());
        Double term2 = weight2 * this.mkm.get(other.getLabel(), comboNode.getRightChild().getLabel());

        Double result = term1 + term2;


        //never going to compute distance between nodes that aren't in the forest
        this.mkm.remove(comboNode.getLeftChild().getLabel(), comboNode.getRightChild().getLabel());
        this.mkm.remove(comboNode.getRightChild().getLabel(), comboNode.getLeftChild().getLabel());

        return result;
    }




    //Builds a multi key map from an array of species (all permutations and respective distances)
    //Pre: species array is instantiated and non null
    //post: MKM<double> is returned
    private void buildMap(Species[] species){
        //MultiKeyMap<Double> mkm = new MultiKeyMap<>(); --> shouldn't need b/c we have class map
        for(int i = 0; i < species.length; i++){
            for(int j = 0; j < species.length; j++){
                if(species[i] != species[j]){
                    Double dist = Species.distance(species[i], species[j]);
                    this.mkm.put(species[i].getName(), species[j].getName(), dist);
                }
            }
        }
        return;
    }

    //Builds a HashMap<String, PhyloTree> with all "free"-nodes in the forest
    //pre: species array is instantiated and non null
    //post: HashMap<String PhyloTreeNode> is
    private HashMap<String, PhyloTreeNode> buildForest(Species[] species){
        HashMap<String, PhyloTreeNode> forest = new HashMap<>();
        for(int i = 0; i < species.length; i++){
            forest.put(species[i].getName(), new PhyloTreeNode(null, species[i]));
        }
        return forest;
    }

    //find first pair
    //finds the smallest distance in MKM, removes the nodes from the forest


    //find multinode distance



    // STATIC

    // nodeDepth
    // Pre-conditions:
    //    - node is null or the root of tree (possibly subtree)
    // Post-conditions:
    //    - If null: returns -1
    //    - Else: returns the depth of the node within the overall tree
    public static int nodeDepth(PhyloTreeNode node) {
        if(node == null){
            return -1;
        }else{
            return 1 + nodeDepth(node.getParent());
        }
    }

    // nodeHeight
    // Pre-conditions:
    //    - node is null or the root of tree (possibly subtree)
    // Post-conditions:
    //    - If null: returns -1
    //    - Else: returns the height subtree rooted at node
    public static int nodeHeight(PhyloTreeNode node) {
        //if node is null -- return -1
        if(node == null){
            return -1;
        }else{
            int ldep = nodeHeight(node.getLeftChild());
            int rdep = nodeHeight(node.getRightChild());
            if(rdep > ldep){
                return rdep + 1;
            }else{
                return ldep + 1;
            }
        }
    }

    // weightedNodeHeight   ---Error is in here
    // Pre-conditions:
    //    - node is null or the root of tree (possibly subtree)
    // Post-conditions:
    //    - If null: returns NEGATIVE_INFINITY
    //    - Else: returns the weighted height subtree rooted at node
    //     (i.e. the sum of the largest weight path from node
    //     to a leaf; this might NOT be the same as the sum of the weights
    //     along the longest path from the node to a leaf)
    public static double weightedNodeHeight(PhyloTreeNode node) {           //rework
        if (node == null){
            double result = Double.NEGATIVE_INFINITY;
            return result;
        }else if(node.isLeaf()){
            return 0;
        }else{
            double weight = node.getDistanceToChild();  //it makes it to here -- distances are computed!

            double ldep = weightedNodeHeight(node.getLeftChild());
            //System.out.println("ldep" + ldep);
            double rdep = weightedNodeHeight(node.getRightChild());
            //System.out.println("rdep" + rdep);
            if(rdep > ldep){
                return rdep + weight;
            }else{
                return ldep + weight;
            }
        }
    }

    // loadSpeciesFile
    // Pre-conditions:
    //    - filename contains the path of a valid FASTA input file
    // Post-conditions:
    //    - Creates and returns an array of species objects representing
    //      all valid species in the input file
    // Notes:
    //    - Species without names are skipped
    //    - See assignment description for details on the FASTA format
    // Hints:
    //    - Because the bar character ("|") denotes OR, you need to escape it
    //      if you want to use it to split a string, i.e. you can use "\\|"
    public static Species[] loadSpeciesFile(String filename) {
        ArrayList<String> fileLines = null;
        //Put file lines in arraylist
        try{
            File mainFile = new File(filename);
            Scanner speciesScanner = new Scanner(mainFile);
            fileLines = fileToLines(speciesScanner);    //fileLines has all the lines of the file
        }catch(Exception e){
            System.err.println("Error: Unable to open file " + filename);
            e.printStackTrace();
            System.exit(1);
        }


        // collect titles and pepetide sequences
        HashMap<String, String> data = new HashMap<>();
        for(int i = 0; i < fileLines.size(); i++){
          if(fileLines.get(i).startsWith(">")){
            String title = getTitle(fileLines.get(i));
            String pepStr = "";
            for(int j = i + 1; j < fileLines.size(); j++){
                if(fileLines.get(j).startsWith(">")){
                    break;
                }else{
                    pepStr += fileLines.get(j);
                }
            }
            data.put(title,pepStr);
          }
        }

        //iterate hashmap and create species objects and put them in array
        Species[] result = new Species[data.size()];
        int counter = 0;
        for(String i : data.keySet()){
            String pepSeq = data.get(i);
            String[] sequence = pepSeq.split("");
            Species a = new Species(i, sequence);        //create new species object
            result[counter] = a;
            counter++;
        }
        return result;
    }

    //gets the title from a 7 element line of input data
    //preconditions: data contains a Fasta line of entry data
    //postconditions: species name returned
    private static String getTitle(String data){
        String result = "";

        //data = data.substring(1);



        //System.out.println(data);
        String[] arr = data.split("\\|");





        //System.out.println(Arrays.toString(arr));
        if(arr.length == 7){
            result = arr[6];
            //System.out.println(result);
            return result;

        }
        //System.out.println("Something went wrong with the get title method.");
        return result;

    }

    // getAllDescendantSpecies  --works!
    // Pre-conditions:
    //    - node points to a node in a phylogenetic tree structure
    //    - descendants is a non-null reference variable to an empty arraylist object
    // Post-conditions:
    //    - descendants is populated with all species in the subtree rooted at node
    //      in in-/pre-/post-order (they are equivalent here)
    private static void getAllDescendantSpecies(PhyloTreeNode node, java.util.ArrayList<Species> descendants) {
        if(node == null){
            return;
        }else if(node.isLeaf()){
            descendants.add(node.getSpecies());
            return;
        }
        getAllDescendantSpecies(node.getLeftChild(), descendants);
        getAllDescendantSpecies(node.getRightChild(), descendants);
        return;
    }

    // findTreeNodeByLabel
    // Pre-conditions:
    //    - node points to a node in a phylogenetic tree structure
    //    - label is the label of a tree node that you intend to locate
    // Post-conditions:
    //    - If no node with the label exists in the subtree, return null
    //    - Else: return the PhyloTreeNode with the specified label
    // Notes:
    //    - Assumes labels are unique in the tree
    private static PhyloTreeNode findTreeNodeByLabel(PhyloTreeNode node, String label) {
        //if node.getLabel() == label -> return node
        if(node != null){
            if(node.getLabel() == label){
                return node;
            }else{
                PhyloTreeNode temp = findTreeNodeByLabel(node.getLeftChild(), label);
                if(temp == null){
                    temp = findTreeNodeByLabel(node.getRightChild(), label);
                }
                return temp;
            }
        }else{
            return null;
        }

    }


    //determines whether or not the node is in the tree
    private static boolean inTree(PhyloTreeNode node, String label){
        if(node == null){
            return false;
        }else if(node.getLabel() == label){
            return true;
        }else{
            return inTree(node.getLeftChild(), label) || inTree(node.getRightChild(), label);
        }
    }


    // findLeastCommonAncestor
    // Pre-conditions:
    //    - node1 and node2 point to nodes in the phylogenetic tree
    // Post-conditions:
    //    - If node1 or node2 are null, return null
    //    - Else: returns the PhyloTreeNode of their common ancestor
    //      with the largest depth
     private static PhyloTreeNode findLeastCommonAncestor(PhyloTreeNode node1, PhyloTreeNode node2) {
        if(node1 == null || node2 == null){
            return null;
        }else{
            ArrayList<PhyloTreeNode> path1 = getPathStat(node1);
            ArrayList<PhyloTreeNode> path2 = getPathStat(node2);

            for(PhyloTreeNode i : path1){
                if(i == node2){             //check if node2 is the ancestor
                    return node2;
                }
                if(path2.contains(i)){      //check for first shared ancestor
                    return i;
                }
            }

            for(PhyloTreeNode i : path2){       //check if node 1 is the ancestor
                if(i == node1){
                    return node1;
                }
            }

            return path1.get(path1.size() - 1); //return last node of the path

        }
    }

    /*
    //finds shared parent
    //pre: two nodes in the same tree are passed in with a connnection between.
    //post: first join of the two nodes is returned
    private static PhyloTreeNode findSharedParent(PhyloTreeNode node1, PhyloTreeNode node2){
        if(node1 == null || node2 == null){
            return null;
        }else{
            //iterate through the parent of each until we reach equal parent
            PhyloTreeNode parent = null;
            if(node1 == node2){
                parent = node1;
                return parent;
            }else if (node1.getParent() == node2){
                parent = node2;
                return parent;
            }else if(node2.getParent() == node1){
                parent = node1;
                return parent;
            }else{

                ArrayList<PhyloTreeNode> path1 = getPathStat(node1);
                ArrayList<PhyloTreeNode> path2 = getPathStat(node2);

                for(PhyloTreeNode i : path1){
                    if(path2.contains(i)){
                        return i;
                    }
                }
            return null;
            }

        }
    }*/

    //retrun path of two nodes
    //pre: nodes are connected w/ root above bottom
    //post: array list of all nodes from bottom upto including root (excluding bottom) is returned.
    private static ArrayList<PhyloTreeNode> getPathStat(PhyloTreeNode bottom){
        ArrayList<PhyloTreeNode> path = new ArrayList<>();
        PhyloTreeNode temp = bottom;
        path.add(bottom);
        while(temp.getParent() != null){
            path.add(temp.getParent());
            temp = temp.getParent();
        }
        return path;
    }

    //Read File -> Arraylist<String> Contains lines of file.
    //FileToString -- return an arraylist of sentences as strings
    /* takes a file and puts lines in an arraylist
    *Preconditions: Scanner is initialized with a text file attached.
    *Postconditions: ArrayList string containing all lines of the file returned*/
    private static ArrayList<String> fileToLines(Scanner input){
        ArrayList<String> linesFromFile = new ArrayList<>();
        try{
            do{
                linesFromFile.add(input.nextLine());
            }while(input.hasNextLine());
        }catch(NoSuchElementException e){
            System.out.println("\n\nTHE FILE YOU SUBMITTED WAS EMPTY! \n\n The error was: " + e);
            System.exit(1);
        }
        return linesFromFile;
    }
}
