/**
 * DFAUnion.java - Class that holds all the data to create a DFA Union
 *
 *
 * @author Jarren Calizo, Justin Cao
 * @version 25 Nov 2020
 */

import java.io.*;
import java.util.*;

public class DFAUnion {

    //Instance variables for the formal description (tuple) of a DFA
    private ArrayList<String> setOfStates = new ArrayList<>();
    private ArrayList<String> alphabet = new ArrayList<>();
    private Map<String, String> transitionTable = new HashMap<>();
    private String startState;
    private ArrayList<String> acceptStates = new ArrayList<>();

    /**
     * reads the input file to instantiate a DFA with all its
     * components.
     *
     *
     * @param fileName name of the file containing an input DFA
     *
     */
    public void readFile(String fileName) {
        //Open the file
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(fileName);
        } catch (FileNotFoundException fnfe) {
            System.err.println("ERROR:  could not open input file.");
            System.exit(-1);
        }

        //Read text from the file until there is nothing left
        Scanner fisScan = new Scanner(fis);
        ArrayList<String> lines = new ArrayList<>(); //text is saved here
        while (fisScan.hasNext()) {
            String line = fisScan.nextLine();
            lines.add(line);
        }
        fisScan.close();

        //The DFA input file is formatted in the order of Q, Σ, δ, q0, F respectively
        //Search for each state name, alphabet character, transition, and accepting states delimited by commas
        loadDFAStates(lines.get(0));
        loadAlphabet(lines.get(1));
        loadTransitionTable(lines.get(2));
        this.startState = lines.get(3);
        loadAcceptStates(lines.get(4));
    }

    /**
     * parses the DFA states from the line in the text file and stores the data
     * in an instance variable
     *
     * @param line the line we are reading the set of all DFA states from.
     *
     */
    public void loadDFAStates(String line){
        /**
         * External Citation
         *    Date: 25 November 2020
         *    Problem: Needed a solution to exclude opening and closing curly braces
         *    Resource:
         *         https://stackoverflow.com/questions/14442162/java-replace-all-square-brackets-in-a-string
         *    Solution: Used the example code to remove curly braces from the read line
         */
        //When reading each line, we excluded opening and closing curly braces
        String statesLine = line.replaceAll("\\{", "")
                .replaceAll("\\}", "").replaceAll(" ", "");
        String[] stateNames = statesLine.split(",");
        for (String s : stateNames){
            setOfStates.add(s);
        }
    }

    /**
     * parses the alphabet characters from the line in the text file and stores the data
     * in an instance variable
     *
     * @param line the line we are reading the alphabet characters from.
     *
     */
    public void loadAlphabet(String line){

        String alphabetLine = line.replaceAll("\\{", "")
                .replaceAll("\\}", "");
        String[] alphabetChar = alphabetLine.split(",");
        for (String s : alphabetChar) {
            alphabet.add(s);
        }
    }

    /**
     * parses the transitions from the line in the text file and stores the data
     * in an instance variable
     *
     * @param line the line we are reading the transitions from.
     *
     */
    public void loadTransitionTable(String line) {

        String transitionLine = line.replaceAll("\\{", "")
                .replaceAll("\\}", "");
        //Separate the transition functions Q,Σ=Q with the semicolon and single space delimiter
        String[] transitionFunctions = transitionLine.split("; ");
        for (int i = 0; i < transitionFunctions.length; i++) {
            String transitionInput = transitionFunctions[i].split("=")[0];
            String transitionOutput = transitionFunctions[i].split("=")[1];
            transitionTable.put(transitionInput, transitionOutput);
        }
    }

    /**
     * parses the accept states from the line in the text file and stores the data
     * in an instance variable
     *
     * @param line the line we are reading the accept states from.
     *
     */
    public void loadAcceptStates(String line) {
        String acceptStatesLine = line.replaceAll("\\{", "")
                .replaceAll("\\}", "");
        String[] acceptStateNames = acceptStatesLine.split(",");
        for (String s : acceptStateNames) {
            acceptStates.add(s);
        }
    }

    /**
     * Creates an instance of DFA union from the the two DFAs
     *
     * @param dfaA the first DFA being used to create a DFA union
     * @param dfaB the second DFA being used to create a DFA union
     *
     * @return union the DFA union instance
     */
    public DFAUnion buildDFAUnion(DFAUnion dfaA, DFAUnion dfaB){
        DFAUnion union = new DFAUnion();
        //use the cross-product version of union and have (#of states from dfa a) x
        // (#of states from dfa b) for the total number of states
        for (int i = 0; i < dfaA.setOfStates.size(); i++) {
            String stateA = dfaA.setOfStates.get(i);
            for (int j = 0; j < dfaB.setOfStates.size(); j++) {
                String stateB = dfaB.setOfStates.get(j);
                union.setOfStates.add(stateA + stateB);
            }
        }

        //The union contains the cross-product of dfa a and dfa b's
        //alphabets and ensures there are no duplicate characters
        //in the union's alphabet
        for (String a : dfaA.alphabet) {
            union.alphabet.add(a);
        }
        for(String b : dfaB.alphabet){
            if(union.alphabet.contains(b)) continue;
            union.alphabet.add(b);
        }

        // Since the transition table is a Hashmap with the key in the form
        // Q,Σ and the stored value Q, we distribute each state from DFA A
        // to all the states of DFA B and use the concatenation of those
        // individual states to represent the input state in the union DFA which
        // is then concatenated to each of the union DFA's alphabet characters.
        // The DFA A state and DFA B state are concatenated together, making
        // up the union DFA's next state
        for (int i = 0; i < dfaA.setOfStates.size(); i++) {
            String curStateA = dfaA.setOfStates.get(i);
            for (int j = 0; j < dfaB.setOfStates.size(); j++) {
                String curStateB = dfaB.setOfStates.get(j);
                for (int k = 0; k < union.alphabet.size(); k++) {
                    union.transitionTable.put(curStateA + curStateB + "," + union.alphabet.get(k),
                            dfaA.transitionTable.get(curStateA + "," + union.alphabet.get(k)) +
                            dfaB.transitionTable.get(curStateB + "," + union.alphabet.get(k)));
                }
            }
        }

        union.startState = dfaA.startState + dfaB.startState;

        // The individual accept states from DFA A are distributed
        // to each of the states of DFA B and added to the set of
        // accept states for the union DFA
        for (int i = 0; i < dfaA.acceptStates.size(); i++){
            String acceptStateA = dfaA.acceptStates.get(i);
            for (int j = 0; j < dfaB.setOfStates.size(); j++){
                String stateB = dfaB.setOfStates.get(j);
                union.acceptStates.add(acceptStateA + stateB);
            }
        }

        //Next we distribute the individual accept states from DFA B to
        //each of the states of DFA A. We only add new accept states and skip
        //those already in F.
        for (int i = 0; i < dfaB.acceptStates.size(); i++){
            String acceptStateB = dfaB.acceptStates.get(i);
            for (int j = 0; j < dfaA.setOfStates.size(); j++){
                String stateA = dfaA.setOfStates.get(j);
                if(union.acceptStates.contains(stateA + acceptStateB)) continue;
                union.acceptStates.add(stateA + acceptStateB);
            }
        }

        return union;
    }

    /**
     * exports the DFA's formal description data into a .txt file
     *
     *
     * @param fileName the name of the output file
     *
     */
    public void exportFile(String fileName){
        File exportFile = createFile(fileName);
        writeToFile(exportFile);
    }

    /**
     * creates the .txt file to write in a DFA's formal description
     * in the root directory
     *
     *
     * @param fileName the name of the output file
     *
     */
    public File createFile(String fileName){
        /**
         * External Citation
         *    Date: 25 November 2020
         *    Problem: Needed help with creating files.
         *    Resource:
         *         https://www.w3schools.com/java/java_files_create.asp
         *    Solution: Used the example code to create the output file
         */
        File myObj = null;
        try {
            myObj = new File(fileName);
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return myObj;
    }

    /**
     * Writes in proper format the DFA's formal description to a .txt file
     * including commas between values and curly braces to contain each set of
     * DFA attributes
     *
     * @param fi the output file instance
     *
     */
    public void writeToFile(File fi) {
        /**
         * External Citation
         *    Date: 25 November 2020
         *    Problem: Needed help with writing to files.
         *    Resource:
         *         https://www.w3schools.com/java/java_files_create.asp
         *    Solution: Used the example code to write to the output file
         */
        //Write DFA A union B's in the proper order of (Q, Σ, δ, q0, F)
        //and include curly braces for each set, along with
        //the comma delimiter (Q, Σ, F) and the semicolon delimiter (δ)
        try {
            FileWriter myWriter = new FileWriter(fi.getName());

            myWriter.write("{");
            for (int i = 0; i < this.setOfStates.size()-1; i++) {
                myWriter.write(this.setOfStates.get(i) + ",");
            }
            myWriter.write(this.setOfStates.get(this.setOfStates.size()-1));
            myWriter.write("}\n");

            myWriter.write("{");
            for (int i = 0; i < this.alphabet.size()-1; i++) {
                myWriter.write(this.alphabet.get(i) + ",");
            }
            myWriter.write(this.alphabet.get(this.alphabet.size()-1));
            myWriter.write("}\n");

            myWriter.write("{");
            /**
             * External Citation
             *    Date: 25 November 2020
             *    Problem: Needed help with sorting a map's key values alphabetically
             *    Resource:
             *         https://www.geeksforgeeks.org/sorting-hashmap-according-key-value-java/
             *    Solution: Used a TreeMap to sort the key values to make it easier to test
             *    to see if the transitions were unioned correctly.
             */
            TreeMap<String, String> sorted = new TreeMap<>();
            sorted.putAll(this.transitionTable);
            int transitionCounter = 0;
            for (Map.Entry<String, String> entry: sorted.entrySet()) {
                //The last transition should end with a curly brace
                if(transitionCounter == sorted.size()-1) {
                    myWriter.write(entry.toString() + "}\n");
                    break;
                }
                myWriter.write(entry.toString() + "; ");
                transitionCounter++;
            }

            myWriter.write(this.startState + "\n");

            myWriter.write("{");
            for (int i = 0; i < this.acceptStates.size()-1; i++) {
                myWriter.write(this.acceptStates.get(i) + ",");
            }
            myWriter.write(this.acceptStates.get(this.acceptStates.size()-1));
            myWriter.write("}\n");

            myWriter.close();
            System.out.println("Successfully wrote to the file.");

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    /**
     * The driver for building a union of two DFAs and exporting its
     * data to an output file.
     *
     *
     */
    public static void main(String[] args) {
        DFAUnion dfaA = new DFAUnion();
        dfaA.readFile("dfaA.txt");
        DFAUnion dfaB = new DFAUnion();
        dfaB.readFile("dfaB.txt");

        DFAUnion dfaAUnionB = dfaA.buildDFAUnion(dfaA, dfaB);
        dfaAUnionB.exportFile("dfaUnion.txt");

        System.exit(0);
    }
}
