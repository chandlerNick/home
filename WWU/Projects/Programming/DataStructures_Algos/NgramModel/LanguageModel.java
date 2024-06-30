/* 
 * LanguageModel.java
 *
 * Implements methods for training a language model from a text file,
 * writing a vocabulary, and randomly completing sentences 
 *
 * Students may only use functionality provided in the packages
 *     java.lang
 *     java.util 
 *     java.io
 * 
 * Use of any additional Java Class Library components is not permitted 
 * 
 * Nicholas Chandler
 * Winter 2022
 *
 */

/*Note(s) to self:
    Next time working begin with the Random Completion portion -- Ask these questions:
        1. How do I pseduocode this? -- As you're getting towards the user side, look at the program1.java code
        2. How do I sort a hashmap in descending alphabetical order?
        3. What should I do about testing some of these things? It has no compile time errors as of now.

*/



import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.io.*;
import java.lang.*;
import java.util.*;

public class LanguageModel {
    HashMap<String,Double> p;         // maps ngrams to conditional probabilities
    ArrayList<String> vocab;          // stores the unique words in the input text
    int maxOrder;                     // maximum n-gram order to compute
    java.util.Random generator;       // a random number generator object
    HashMap<String,Integer> ngramCounts;
    HashMap<String,Integer> historyCounts;

    // Constructor
    
    // LanguageModel
    // Preconditions:
    //  - textFilename is the name of a plaintext training file
    //  - maxOrder is the maximum n-gram order for which to estimate counts
    //  - generator is java.util.Random object
    //  - vocabFilename is the name where the vocab file will be written
    //        vocabFilename can also be null
    //  - countsFilename is the name where the counts will be written
    //        countsFilename can also be null
    // Postconditions:
    //  - this.p maps ngrams (h,w) to the the maximum likelihood estimates 
    //    of P(w|h) for all n-grams up to maxOrder 
    //    Only non-zero probabilities should be stored in this map
    //  - this.vocab contains each word appearing in textFilename exactly once
    //    in case-insensitive ascending alphabetic order
    //  - this.maxOrder is assigned maxOrder
    //  - this.generator is assigned generator
    //  - If vocabFilename is non-null, the vocabulary words are printed to it,
    //    one per line, in order
    //  - If countsFilename is non-null, the ngram counts words are printed to
    //    countsFilename, in order each line has the ngram, then a tab, then
    //    the number of times that ngram appears these should be printed in
    //    case-insensitive ascending alphabetic order by the n-grams
    // Notes:
    //  - n-gram and history counts should be computed with a call to getCounts
    //  - File saving should be accomplished by calls to saveVocab and saveCounts
    //  - convertCountsToProbabilities should be used to then get the probabilities
    //  - If opening any file throws a FileNotFoundException, print to standard error:
    //        "Error: Unable to open file " + filename
    //        (where filename contains the name of the problem file)
    //      and then exit with value 1 (i.e. System.exit(1))
    public LanguageModel( String textFilename, int maxOrder, java.util.Random generator, String vocabFilename, String countsFilename ) {
        this.ngramCounts = new HashMap<>(); 
        this.historyCounts = new HashMap<>(); 
        this.p = new HashMap<>();
        this.vocab = new ArrayList<>();
        this.maxOrder = maxOrder;
        this.generator = generator;
        File trainingFile = new File(textFilename);
        try{
            Scanner input = new Scanner(trainingFile);
            getCounts(input, ngramCounts, historyCounts, vocab, maxOrder);
            input.close();
        }catch(Exception e){
            System.err.println("There was an error creating the input scanner: " + trainingFile);
            System.exit(1);
        }
        convertCountsToProbabilities(ngramCounts, historyCounts);
        saveVocab(vocabFilename);
        saveCounts(countsFilename, this.ngramCounts); 
        return;
    }

    // Accessors

    // getMaxOrder
    // Preconditions:
    //  - None
    // Postconditions:
    //  - this.maxOrder is returned
    public int getMaxOrder() {
        return this.maxOrder;
    }

    // randomCompletion
    // Preconditions:
    //  - history contains an initial history to complete
    //  - order is the n-gram order to use when completing the sentence
    // Postconditions:
    //  - history must not be modified (i.e. make a copy of it)
    //  - Starting with an empty string, until </s> or <fail> is drawn:
    //    1) Draw a new word w according to P(w|h)
    //    2) Append a space and then w to the string you're accumulating
    //    3) w is added to the history h
    //    Once </s> or <fail> is reached, append it to the string and return the string
    // Notes:
    //  - Call randomNextWord to draw each new word
    public String randomCompletion( ArrayList<String> history, int order ) {
        ArrayList<String> dcHistory = new ArrayList<String>(history.size());
        for(String i : history){
            dcHistory.add(i);
        }
        String result = "";
        if((dcHistory.size() >= getMaxOrder()) || (dcHistory.size() >= order)){    //remove excess words
                dcHistory.subList(order - 1, dcHistory.size()).clear();
        }
        while(true){
            String drawnWord = randomNextWord(dcHistory, order); 
                if (drawnWord.contains("</s>") || drawnWord.contains("<fail>")){
                    result = result + " " + drawnWord;
                    break;
                }else{
                    result = result + " " + drawnWord;
                    dcHistory.add(drawnWord);
                    if(dcHistory.size() >= order){ 
                        dcHistory.remove(0);
                    }
                }
        }
        return result;
    }

    // Private Helper Methods

    // saveVocab
    // Preconditions:
    //  - vocabFilename is the name where the vocab file will be written, or null
    // Postconditions:
    //  - if null, do nothing
    //  - else, this.vocab contains each word appearing in textFilename exactly
    //    once in case-insensitive ascending alphabetic order
    //  - If opening the file throws a FileNotFoundException, print to standard error:
    //        "Error: Unable to open file " + vocabFilename
    //      and then exit with value 1 (i.e. System.exit(1))
    private void saveVocab(String vocabFilename) {
        if(vocabFilename == null){
           return;
        }else{
            try{
                FileWriter vocabWriter = new FileWriter(vocabFilename);
                for(String i : vocab){
                    vocabWriter.write(i + "\n");
                }
                vocabWriter.close();
            }catch(IOException e){
                System.err.println("Error: Unable to open file " + vocabFilename);
                System.exit(1);
            }
            return;
        }
    }

    // saveCounts
    // Preconditions:
    //  - countsFilename is the name where the counts will be written, or null
    //  - ngramCounts.get(ngram) returns the number of times ngram appears
    //    (ngrams with count 0 are not included)
    // Postconditions:
    //  - If countsFilename is non-null, the ngram counts words are printed to
    //    countsFilename, each line has the ngram, then a tab, then the number
    //    of times that ngram appears. ngrams should be printed in
    //    case-insensitive ascending alphabetic order
    // Notes:
    //  - If opening the file throws a FileNotFoundException, print to standard error:
    //       "Error: Unable to open file " + countsFilename
    //      and then exit with value 1 (i.e. System.exit(1))
    private void saveCounts(String countsFilename, HashMap<String,Integer> ngramCounts) {
        if(countsFilename == null){
            return;
        }else{
            ArrayList<String> countKeys = new ArrayList<>(ngramCounts.size());  
            for(String key : ngramCounts.keySet()){                             
                    countKeys.add(key);
                }
            Collections.sort(countKeys, String.CASE_INSENSITIVE_ORDER);
            try{
                FileWriter countWriter = new FileWriter(countsFilename);
                for(String ngram : countKeys){
                    countWriter.write(ngram+"\t"+ngramCounts.get(ngram)+"\n"); 
                }
                countWriter.close();
            }catch (IOException e){
                System.err.println("Error: Unable to open file " + countsFilename);
                System.exit(1);
            }
        }
        return;
    }

    // randomNextWord
    // Preconditions:
    //  - history is the history on which to condition the draw
    //  - order is the order of n-gram to use 
    //      (i.e. no more than n-1 history words)
    //  - this.generator is the generator passed to the constructor
    // Postconditions:
    //  - A new word is drawn (see assignment description for the algorithm to use)
    //  - If no word follows that history for the specified order, return "<fail>"
    // Notes:
    //  - The nextDouble() method draws a random number between 0 and 1
    //  - ArrayList has a subList method to return an array slice
    private String randomNextWord( ArrayList<String> history, int order) {
        Random gen = this.generator;
        Double d = gen.nextDouble(); 
        Double cumulativeSum = 0.0;
        String historyWords = arrayToString(history);
        for(String i : this.vocab){
            String ngram = historyWords.trim() + " " + i.trim();
            if(p.get(ngram) != null){               //dont try to add null to a double
                cumulativeSum += p.get(ngram);
            }
            if(cumulativeSum > d){
                return i; 
            }
        }
        return "<fail>";
    } 

    // Preconditions:
    //  - input is an initialized Scanner object associated with the text input file
    //  - ngramCounts is an empty (but non-null) HashMap
    //  - historyCounts is an empty (but non-null) HashMap
    //  - vocab is an empty (but non-null) ArrayList
    //  - maxOrder is the maximum order n-gram for which to extract counts
    // Postconditions:
    //  - ngramCounts.get(ngram) contains the number of times that ngram
    //    appears in the input ngram must be 2+ words long (e.g. "<s> i")
    //  - historyCounts.get(history) contains the number of times that ngram
    //    history appears in the input histories can be a single word (e.g.,
    //    "<s>")
    //  - vocab contains each word (token) in the input file exactly once, in
    //    case-insensitive ascending alphabetic order
    // Notes:
    //  - You may find it useful to implement helper function incrementHashMap
    //    and use it
    private void getCounts(java.util.Scanner input, HashMap<String,Integer> ngramCounts, HashMap<String,Integer> historyCounts, ArrayList<String> vocab, int maxOrder) {
        ArrayList<String> listLines = new ArrayList<String>();
        ArrayList<ArrayList<String>> fileWords = new ArrayList<ArrayList<String>>();
        try{
            listLines = fileToLines(input);
        }catch(Exception e){
            System.err.println("Something went wrong with the fileToLines process");
            e.printStackTrace();
        }
        fileWords = listOfWords(listLines);
        this.ngramCounts = makerFunction(ngramCounts, fileWords, maxOrder);   //Note: this function also makes the vocab list and the historyCounts HashMap.
        return;
    }

    // convertCountsToProbabilities
    // Preconditions:
    //  - ngramCounts.get(ngram) contains the number of times that ngram
    //    appears in the input
    //  - historyCounts.get(history) contains the number of times that ngram
    //    history appears in the input
    // Postconditions:
    //  - this.p.get(ngram) contains the conditional probability P(w|h) for
    //    ngram (h,w) only non-zero probabilities are stored in this.p -- thereby the size of p is NOT equal to the size of ngramCounts.
    private void convertCountsToProbabilities(HashMap<String,Integer> ngramCounts, HashMap<String,Integer> historyCounts) {
        iterateMap:
            for(String item : ngramCounts.keySet()){
                String ngram = item;
                ArrayList<String> nGramArray = new ArrayList<>();
                nGramArray = stringToArray(ngram);
                nGramArray.remove(nGramArray.size() - 1);   //ngram w/o last word -- for making the history
                String history = arrayToString(nGramArray);
                Double numerator = Double.valueOf(ngramCounts.get(item));
                Double denominator = Double.valueOf(historyCounts.get(history));
                Double value = numerator / denominator;
                if(value <= 0){
                    continue iterateMap;
                }
                p.put(ngram, value);
            }
        return;
    }


//Helper methods -- I implemented most of these.

    //FileToString -- return an arraylist of sentences as strings
    /* takes a file and puts lines in an arraylist
    *Preconditions: Scanner is initialized with a text file attached.
    *Postconditions: ArrayList string containing all lines of the file returned*/
    private ArrayList<String> fileToLines(java.util.Scanner input){
        ArrayList<String> linesFromFile = new ArrayList<>();
            do{
                linesFromFile.add(input.nextLine());
            }while(input.hasNextLine());
            //System.out.println(arrayToString(linesFromFile));
        return linesFromFile;
    }

    //Lines to words -- returns a 2D arraylist of lines with a sublist of words
    /*Preconditions: ArrayList<String> with all lines of an input document is given
    * Postconditions: An ArrayList<ArrayList<String>>(); with the outer arraylist being lines and the inner arraylist being words from the file*/
    private ArrayList<ArrayList<String>> listOfWords(ArrayList<String> lines){
        ArrayList<ArrayList<String>> listOfWords = new ArrayList<ArrayList<String>>(lines.size());
        for(int i = 0; i < lines.size(); i++){
            listOfWords.add(new ArrayList<String>());
        }
        for(int j = 0; j < lines.size(); j++){
            try{
                Scanner scLines = new Scanner(lines.get(j));
                while(scLines.hasNext()){
                    listOfWords.get(j).add(scLines.next());
                }
            }catch (Exception e){
                System.err.println("Error in the listOfWords method");
                e.printStackTrace();
            }  
        }
        return listOfWords;
    }


    //Make N-gram Counts Hashmap, Vocab ArrayList, and historyCounts 
    /* This method returns a hashmap with the number of occurences for each n-gram from an input file. It also updates the class instances of vocab and historyCounts
        Preconditions: ArrayList<ArrayList<String>> words contains all tokens from the input file as arraylist indicies, ngramCounts, historyCounts, and vocab have been intialized
        Postconditions: HashMap ngramCounts has the number of counts for each ngram up to maxOrder ngram order -- historyCounts and vocab have been created according to prog1 specifications*/
    private HashMap<String,Integer> makerFunction(HashMap<String,Integer> ngramCounts, ArrayList<ArrayList<String>> words, int maxOrder){
        HashMap<String, Integer> result = new HashMap<>();
        HashMap<String, Integer> resultHistory = new HashMap<>();
        ArrayList<String> resultVocab = new ArrayList<>();
        for(ArrayList<String> sentence : words){
            for(String i : sentence){
                if (! i.contains("</s>")){
                    Integer HCValue = incrementHashMap(resultHistory, i);
                    resultHistory.put(i, HCValue);
                }  
            }
            for(int i = 2; i <= maxOrder; i++){
                int oLCO = i - 1;               //one less than current order
                ArrayList<String> memoryList = new ArrayList<String>(oLCO);
                memoryList.clear();
            worditerator:
                for(int j = 0; j < sentence.size(); j++){
                    String word = sentence.get(j);
        //add word to vocab list
                    if(!resultVocab.contains(word)){
                        resultVocab.add(word);
                    }
        //add words to ngramCounts
                    if(memoryList.size() < oLCO){ 
                        memoryList.add(word);
                    }else{
        //add n-1 order words to history counts
                        if ((! memoryList.contains("</s>")) && memoryList.size() > 1){
                            String HCKey = arrayToString(memoryList);
                            Integer HCValue = incrementHashMap(resultHistory, HCKey);
                            resultHistory.put(HCKey, HCValue);
                        }         
                        memoryList.add(word);
                        String key = arrayToString(memoryList);
                        Integer value = incrementHashMap(result, key);
                        result.put(key, value);
                        memoryList.remove(0);
                    }
                }

            }
        }
        if(!resultVocab.contains("</s>")){  //Reassurance
            resultVocab.add("</s>");
        }
        Collections.sort(resultVocab, String.CASE_INSENSITIVE_ORDER);
        this.vocab = resultVocab;
        this.historyCounts = resultHistory;
        return result;
    }

    // incrementHashMap
    // Preconditions:
    //  - map is a non-null HashMap 
    //  - key is a key that may or may not be in map
    // Postconditions:
    //  - If key was already in map, map.get(key) returns 1 more than it did before
    //  - If key was not in map, map.get(key) returns 1
    // Notes
    //  - This method is useful, but optional
    private Integer incrementHashMap(HashMap<String,Integer> map, String key) {
        if(map.get(key) == null){
            return 1;
        }else{
            return (map.get(key) + 1);
        }
    }

    // Static Methods

    // arrayToString
    // Preconditions:
    //  - sequence is a List (e.g. ArrayList) of Strings
    // Postconditions:
    //  - sequence is returned in string form, each element joined by a single space
    //  - If sequence was length 0, the empty string is returned
    // Notes:
    //  - Already implemented for you
    public static String arrayToString(List<String> sequence) {
        java.lang.StringBuilder builder = new java.lang.StringBuilder();
        if( sequence.size() == 0 ) {
            return "";
        }
        builder.append(sequence.get(0));
        for( int i=1; i<sequence.size(); i++ ) {
            builder.append(" " + sequence.get(i));
        }
        return builder.toString();
    }

    // stringToArray
    // Preconditions: 
    //  - s is a string of words, each separated by a single space
    // Postconditions:
    //  - An ArrayList is returned containing the words in s
    // Notes:
    //  - Already implemented for you
    public static ArrayList<String> stringToArray(String s) {
        return new ArrayList<String>(java.util.Arrays.asList(s.split(" ")));
    }
}