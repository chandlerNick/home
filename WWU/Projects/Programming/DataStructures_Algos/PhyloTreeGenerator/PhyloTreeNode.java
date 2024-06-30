/* 
 * PhyloTreeNode.java
 *
 * Defines a phylogenetic tree node type, which stores the information associated
 * with a node in the tree
 *
 * This class has been implemented for you.  You may not modify it.
 * 
 * Brian Hutchinson
 *
 */

public class PhyloTreeNode {
    private PhyloTreeNode parent;        // Reference variable for parent node (null for root)
    private PhyloTreeNode leftChild;     // Reference variable for left child (null if empty)
    private PhyloTreeNode rightChild;    // Reference variable for right child (null if empty)    
    private String label;                // A unique string label for the species or non-terminal
    private Species species;             // Reference variable for a species object (null for non-terminals)
    private double distanceToChild;      // Edge weight to left child (which is also the edge weight to the right child) (Use 0 for terminals)
    private int numLeafs;                // Caches the # leaves in the tree -- can't change after Constructor

    // CONSTRUCTORS

    // PhyloTree - constructor for a leaf
    // Pre-conditions:
    //      - parent node points to a PhyloTree (or null)
    //      - species points to a Species object
    // Post-conditions:
    //      - A new leaf PhyloTree node is created 
    public PhyloTreeNode(PhyloTreeNode parent, Species species) {
        this.parent = parent;
        this.species = species;
        this.label = species.getName();    
        this.numLeafs = 1;
        return;
    }

    // PhyloTree - constructor for a non-terminal
    // Pre-conditions:
    //        - label is leftlabel+rightlabel, where leftlabel is the label
    //        of the left child and rightlabel is the label of the right child
    //        - parent is a PhyloTreeNode (or null) to be the node's parent
    //        - leftlabel and rightChild are PhyloTreeNodes for the left and right children
    //        - distanceToChild is the edge weight from this new node to each of its children
    //          and will be set to X/2, where X is the distance between the two children at the
    //          point that they are merged during buildTree
    // Post-conditions:
    //      - A new non-terminal PhyloTree node is created 
    //        - species will be set to null, since non-terminals are not associated with species
    //        - this.numLeafs will be computed given the children
    public PhyloTreeNode(String label, PhyloTreeNode parent, PhyloTreeNode leftChild, PhyloTreeNode rightChild,double distanceToChild) {
        this.label = label;
        this.parent = parent;
        this.leftChild = leftChild;
        this.rightChild = rightChild;
        this.distanceToChild = distanceToChild;
        this.numLeafs = 0;
        if( leftChild != null ) {
            this.numLeafs += leftChild.getNumLeafs();
        }
        if( rightChild != null ) {
            this.numLeafs += rightChild.getNumLeafs();
        }
    }
    
    // ACCESSORS

    // getParent
    // Pre-conditions:
    //        - None
    // Post-conditions:
    //        - Returns the parent
    public PhyloTreeNode getParent() {
        return this.parent;
    }

    // getLeftChild
    // Pre-conditions:
    //        - None
    // Post-conditions:
    //        - Returns the leftChild
    public PhyloTreeNode getLeftChild() {
        return this.leftChild;
    }

    // getRightChild
    // Pre-conditions:
    //        - None
    // Post-conditions:
    //        - Returns the rightChild
    public PhyloTreeNode getRightChild() {
        return this.rightChild;
    }

    // getLabel
    // Pre-conditions:
    //        - None
    // Post-conditions:
    //        - Returns the label
    public String getLabel() {
        return this.label;
    }

    // getSpecies
    // Pre-conditions:
    //        - None
    // Post-conditions:
    //        - Returns the species
    public Species getSpecies() {
        return this.species;
    }

    // getDistanceToChild
    // Pre-conditions:
    //        - None
    // Post-conditions:
    //        - Returns the distanceToChild
    public double getDistanceToChild() {
        return this.distanceToChild;
    }

    // getNumLeafs
    // Pre-conditions:
    //        - None
    // Post-conditions:
    //        - Returns the number of leaves in the subtree rooted at this
    public int getNumLeafs() {
        return this.numLeafs;
    }

    // isLeaf 
    // Pre-conditions:
    //        - None
    // Post-conditions:
    //        - If the node is a leaf: return true
    //        - Else: return false
    public boolean isLeaf() {
        if( this.leftChild == null && this.rightChild == null ) {
            return true;
        } else {
            return false;
        }
    }

    // toString
    // Pre-conditions:
    //        - None
    // Post-conditions:
    //        - Produces a string representation of the node
    //        - For leafs, returns the label
    //        - For non-terminals, returns [NONTERM $distance]
    //          where $distance is the distanceToChild
    public String toString() {
        if( this.isLeaf() ) {
            return this.getLabel();    
        } else {
            return String.format("[NONTERM %.2f]",this.getDistanceToChild());
        }
    }

    // MODIFIER

    // setParent
    // Pre-conditions:
    //        - parent is a PhyloTreeNode or null
    // Post-conditions:
    //        - Sets this.parent to parent
    public void setParent(PhyloTreeNode parent) {
        this.parent = parent;
        return;
    }
}
