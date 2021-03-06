/***********************************************************************

	This file is part of KEEL-software, the Data Mining tool for regression, 
	classification, clustering, pattern mining and so on.

	Copyright (C) 2004-2010
	
	F. Herrera (herrera@decsai.ugr.es)
    L. Sánchez (luciano@uniovi.es)
    J. Alcalá-Fdez (jalcala@decsai.ugr.es)
    S. García (sglopez@ujaen.es)
    A. Fernández (alberto.fernandez@ujaen.es)
    J. Luengo (julianlm@decsai.ugr.es)

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see http://www.gnu.org/licenses/
  
**********************************************************************/



package keel.Algorithms.Subgroup_Discovery.SDIGA.SDIGA;

import keel.Algorithms.Subgroup_Discovery.SDIGA.Calculate.*;

import keel.Dataset.*;
import org.core.*;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.StringTokenizer;

/**
* <p>
     * SDIGA
     * </p>
     * <p>
     * Algorithm for the discovery of rules describing subgroups
     * </p>
 * <p>
 * @author Writed by Pedro González (University of Jaen) 15/02/2004
 * @author Modified by Pedro González (University of Jaen) 4/08/2007
 * @author Modified by Cristóbal J. Carmona (University of Jaen) 20/04/2010
 * @version 2.0
 * @since JDK1.5
 * </p>
 */
public class SDIGA {

    private static int seed;             // Seed for the random generator
    private static String nombre_alg;    // Algorithm Name
    private static boolean claseSelec;   // Indicates if there is a selected class to run the algorithm or not

    private static String input_file_ref;   // Input mandatory file training
    private static String input_file_tra;   // Input mandatory file training
    private static String input_file_tst;   // Input mandatory file test
    private static String output_file_tra;   // Output mandatory file training
    private static String output_file_tst;   // Output mandatory file test
    private static String rule_file;        // Auxiliary output file for rules
    private static String measure_file;     // Auxiliary output file for quality measures of the rules
    private static String seg_file;         // Auxiliary output file for tracking
    private static String qmeasure_file;    // Output quality measure file

    private static boolean echo=true;    // Write or not seg file; default=true

    // Structures
    static InstanceSet Data;
    static TableVar Variables;     // Set of variables of the dataset and their characteristics
    static TableDat Examples;      // Set of instances of the dataset
    static Genetic AG;             // Genetic Algorithm
    

    /**
     * <p>
     * Auxiliar Gets the name for the output files, eliminating "" and skiping "="
     * </p>
     * @param s                 String of the output files
     */
    private static void GetOutputFiles(StringTokenizer s) {
        String val   = s.nextToken(); // skip "=" 
        output_file_tra = s.nextToken().replace('"',' ').trim();
        output_file_tst = s.nextToken().replace('"',' ').trim();
        rule_file    = s.nextToken().replace('"',' ').trim();
        measure_file = s.nextToken().replace('"',' ').trim();
        seg_file     = s.nextToken().replace('"',' ').trim();
        qmeasure_file= s.nextToken().replace('"',' ').trim();
    }    

    /**
     * <p>
     * Auxiliar Gets the name for the input files, eliminating "" and skiping "="
     * </p>
     * @param s                 String of the input files
     */
    private static void GetInputFiles(StringTokenizer s) {
        String val   = s.nextToken(); // skip "="
        input_file_ref = s.nextToken().replace('"',' ').trim();
        input_file_tra = s.nextToken().replace('"',' ').trim();
        input_file_tst = s.nextToken().replace('"',' ').trim();
    }
    
    /**
     * <p>
     * Auxiliar Gets the name for the input files, eliminating "" and skiping "="
     * </p>
     * @param nFile                 String of the input files
     */
    public static void ReadParameters (String nFile) {
       claseSelec = false;  // By default, there is a selected target class to run the algorihtm
        String contents;
       
        try {
            int nl;  // Aux var to reed the param file
            String file, linea, tok;
            StringTokenizer lineasFichero, tokens;
            file = Files.readFile(nFile);
            file = file.toLowerCase() + "\n ";
            lineasFichero = new StringTokenizer(file,"\n\r");

            // True initialization of LSearch
            AG.setLSearch (true);   // l_search

            for (nl=0, linea=lineasFichero.nextToken(); lineasFichero.hasMoreTokens(); linea=lineasFichero.nextToken()) {
                nl++;
                tokens = new StringTokenizer(linea," ,\t");
                if (tokens.hasMoreTokens()) {
                    tok = tokens.nextToken();
                    if (tok.equalsIgnoreCase("algorithm")) 
                        nombre_alg = Utils.GetParamString(tokens);
                    else if (tok.equalsIgnoreCase("inputdata"))
                        GetInputFiles(tokens);
                    else if (tok.equalsIgnoreCase("outputdata"))   
                        GetOutputFiles(tokens);
                    else if (tok.equalsIgnoreCase("seed"))
                        seed = Utils.GetParamInt(tokens);
                    else if (tok.equalsIgnoreCase("targetClass")) {
                        Variables.setNameClassObj(Utils.GetParamString(tokens));
                        claseSelec=true;
                        }
                    else if (tok.equalsIgnoreCase("nLabels"))
                        Variables.setNLabel(Utils.GetParamInt(tokens));
                    else if (tok.equalsIgnoreCase("nEval")) 
                        AG.setNEval(Utils.GetParamInt(tokens));
                    else if (tok.equalsIgnoreCase("popLength"))
                        AG.setLenghtPop(Utils.GetParamInt(tokens));
                    else if (tok.equalsIgnoreCase("crossProb")) 
                        AG.setProbCross(Utils.GetParamFloat(tokens));
                    else if (tok.equalsIgnoreCase("mutProb")) 
                        AG.setProbMut(Utils.GetParamFloat(tokens));
                    else if (tok.equalsIgnoreCase("minConf"))
                        AG.setMinConf(Utils.GetParamFloat(tokens));
                    else if (tok.equalsIgnoreCase("RulesRep"))
                        AG.setRulesRep(Utils.GetParamString(tokens).toUpperCase());
                    else if (tok.equalsIgnoreCase("Obj1"))
                        AG.setObj1(Utils.GetParamString(tokens).toUpperCase());
                    else if (tok.equalsIgnoreCase("Obj2"))
                        AG.setObj2(Utils.GetParamString(tokens).toUpperCase());
                    else if (tok.equalsIgnoreCase("Obj3"))
                        AG.setObj3(Utils.GetParamString(tokens).toUpperCase());
                    else if (tok.equalsIgnoreCase("W1"))
                        AG.setW1(Utils.GetParamFloat(tokens));
                    else if (tok.equalsIgnoreCase("W2"))
                        AG.setW2(Utils.GetParamFloat(tokens));
                    else if (tok.equalsIgnoreCase("W3"))
                        AG.setW3(Utils.GetParamFloat(tokens));

                    else if (tok.equalsIgnoreCase("lSearch"))
                        AG.setLSearch (Utils.GetParamString(tokens).equals("yes"));     // l_search
                    else if (tok.equalsIgnoreCase("echo"))
                        echo = Utils.GetParamString(tokens).equals("yes");     // seg echo 
                    else  throw new IOException("Syntax error on line "+nl+": ["+tok+"]\n");
                }
            }
        } 
	catch(FileNotFoundException e) {
            System.err.println(e+" Parameter file");
        } 
        catch(IOException e) {
            System.err.println(e+"Aborting program");
            System.exit(-1);
        }

        // Echo of the parameters
        Files.writeFile(seg_file,""); // Creates tracking file
        
        contents = "--------------------------------------------\n";
        contents+= "|              Parameters Echo             |\n";
        contents+= "--------------------------------------------\n";
        contents+= "Algorithm name: " + nombre_alg + "\n";
        contents+= "Input file name training: " + input_file_tra + "\n";
        contents+= "Input file name test: " + input_file_tst + "\n";
        contents+= "Rules file name: " + rule_file + "\n";
        contents+= "Tracking file name: " + seg_file + "\n";
        contents+= "Random generator seed: " + seed + "\n";
        contents+= "Selected class of the target variable: ";
        if (claseSelec)
           contents+= Variables.getNameClassObj() + "\n";
        else
           contents+= "not established\n";
        contents+= "Number of labels for the continuous variables: " + Variables.getNLabel() + "\n";
        contents+= "Number of evaluations: " + AG.getNEval() + "\n";
        contents+= "Number of individuals in the Population: " + AG.getLenghtPop() + "\n";
        contents+= "Cross probability: " + AG.getProbCross() + "\n";
        contents+= "Mutation probability: " + AG.getProbMut() + "\n";
        contents+= "Minimum confidence: " + AG.getMinConf() + "\n";
        contents+= "Rules representation: " + AG.getRulesRep() + "\n";
        contents+= "Objective 1: " + AG.getObj1() + "(Weight: " + AG.getW1() + ")\n";
        contents+= "Objective 2: " + AG.getObj2() + "(Weight: " + AG.getW2() + ")\n";
        if(AG.getObj3().compareTo("NULL")!=0) {contents+= "Objective 3: " + AG.getObj3() + "(Weight: " + AG.getW3() + ")\n";}
        contents+= "Perform Local Search: " + AG.getLSearch() + "\n";
        if (echo)
            Files.addToFile(seg_file,contents); // Creates and writes on tracking file
       
    }

    
   /**
    * <p>
    * Read the dataset and stores the values
    * </p>
     * @throws java.io.IOException if the dataset can not be read.
    */
    public static void capturaDataset () throws IOException   {
    	
       try {

        // Declaration of the dataset and load in memory
        Data = new InstanceSet();
        Data.readSet(input_file_ref,true);
       
        // Check that there is only one output variable
        if (Attributes.getOutputNumAttributes()>1) {
  		System.out.println("This algorithm can not process MIMO datasets");
  		System.out.println("All outputs but the first one will be removed");
	}
	boolean noOutputs=false;
	if (Attributes.getOutputNumAttributes()<1) {
  		System.out.println("This algorithm can not process datasets without outputs");
  		System.out.println("Zero-valued output generated");
  		noOutputs=true;
	}

        // Chek that the output variable is nominal
        if (Attributes.getOutputAttribute(0).getType()!=Attribute.NOMINAL) {
            // If the output variables is not enumeratad, the algorithm can not be run
            try {
                throw new IllegalAccessException("Finish");
            } catch( IllegalAccessException term) {
                System.err.println("Target variable is not a discrete one.");
                System.err.println("Algorithm can not be run.");
                System.out.println("Program aborted.");
                System.exit(-1);
            }
        }
        
        // Set the number of classes of the output attribute - this attribute must be nominal
        Variables.setNClass(Attributes.getOutputAttribute(0).getNumNominalValues());
        
        // Screen output of the output variable and selected class
        System.out.println ( "Output variable: " + Attributes.getOutputAttribute(0).getName());

        // Creates the space for the variables and load the values.
        Variables.Load (Attributes.getInputNumAttributes());

        // Setting and file writing of fuzzy sets characteristics for continuous variables
        String nombreF;
        if (echo) nombreF = seg_file;
        else nombreF = "";
        Variables.InitSemantics (nombreF);
        
        // Creates the space for the examples and load the values
        Examples.Load(Data,Variables);
        
     } catch (Exception e) {
       System.out.println("DBG: Exception in readSet");
       e.printStackTrace();
       }

    }


   /**
    * <p>
    * Dataset file writting to output file
    * </p>
    * @param filename           Output file
    */
    public static void WriteOutDataset (String filename) {
        String contents;
        contents = Data.getHeader();
        contents+= Attributes.getInputHeader() + "\n";
        contents+= Attributes.getOutputHeader() + "\n\n";
        contents+= "@data \n";
        Files.writeFile(filename, contents);
    }


   /**
    * <p>
    * Dataset file writting to tracking file
    * </p>
    * @param filename           Tracking file
    */
    public static void WriteSegDataset (String filename) {
        String contents="\n";
        contents+= "--------------------------------------------\n";
        contents+= "|               Dataset Echo               |\n";
        contents+= "--------------------------------------------\n";
        contents+= "Number of examples: " + Examples.getNEx() + "\n";
        contents+= "Number of variables: " + Variables.getNVars()+ "\n";
        contents+= Data.getHeader() + "\n";
        if (filename!="")
            Files.addToFile(filename, contents);
    }

    
    /**
     * <p>
     * Writes the canonical rule and the quality measures
     * </p>
     * @param NumRule
     * @param nobj                      Number of generated rules
     * @param rule                      Chromosome to write
     * @param Result                    Quality measures of the individual
     * @param fileRule                  File to write the rule
     * @param fileQuality               File to write the quality measures
     */
    static void WriteRuleCAN (int NumRule, CromCAN rule, QualityMeasures Result, String fileRule, String fileQuality) {
        String contents;
        
        // Rule File
        contents = "GENERATED RULE " + NumRule + "\n";
        contents+= "   Antecedent\n";
        for (int i=0; i<Variables.getNVars(); i++) {
            if (!Variables.getContinuous(i)) {
                // Discrete Variable
                if (rule.getCromElem(i)<Variables.getNLabelVar(i)) {
                    System.out.println ("\tVariable (C) " + Attributes.getInputAttribute(i).getName() + " = ..." );
                    contents+= "\tVariable " + Attributes.getInputAttribute(i).getName() + " = " ;
                    contents+= Attributes.getInputAttribute(i).getNominalValue(rule.getCromElem(i)) + "\n";
                }
            }
            else {  
                // Continuous Variable
                if (rule.getCromElem(i)<Variables.getNLabelVar(i)) {
                    System.out.println ("\tVariable (D) " + Attributes.getInputAttribute(i).getName() + " = ..." );
                    contents+= "\tVariable " + Attributes.getInputAttribute(i).getName() + " = ";
                    contents+= "Label " + rule.getCromElem(i);
                    contents+= " \t (" + Variables.getX0(i,(int) rule.getCromElem(i));
                    contents+= " " + Variables.getX1(i,(int) rule.getCromElem(i));
                    contents+= " " + Variables.getX3(i,(int) rule.getCromElem(i)) +")\n";
                }
            }
        }
        contents+= "   Consecuent: " + Variables.getNameClassObj();

        contents+= "\n\n";
        Files.addToFile(fileRule, contents);

        // Write the quality measures of the rule
        DecimalFormat sixDecimals = new DecimalFormat("0.000000");

        //  The head is defined in "main" method:
        contents = "" + Variables.getNumClassObj();
        contents+= "\t" + sixDecimals.format(Result.getSup());
        contents+= "\t" + sixDecimals.format(Result.getCnf());
        if(AG.getObj3().compareTo("NULL")!=0) contents+= "\t" + sixDecimals.format(Result.getVal3());
        contents+= "\t" + sixDecimals.format(Result.getFitness()) + "\n";
        Files.addToFile(measure_file, contents);

    }
    
    /**
     * <p>
     * Writes the DNF rule and the quality measures
     * </p>
     * @param NumRule
     * @param nobj                      Number of generated rules
     * @param rule                      Chromosome to write
     * @param Result                    Quality measures of the individual
     * @param fileRule                  File to write the rule
     * @param fileQuality               File to write the quality measures
     */
    static void WriteRuleDNF (int NumRule, CromDNF rule, QualityMeasures Result, String fileRule, String fileQuality) {
        String contents;

        // Rule File
        contents = "GENERATED RULE " + NumRule + "\n";
        contents+= "   Antecedent\n";
        for (int i=0; i<Variables.getNVars(); i++) {
            if (rule.getCromElemGene(i,Variables.getNLabelVar(i))!=0) {
                // Variable takes part in the rule
                if (!Variables.getContinuous(i)) {
                    // Variable discreta
                    System.out.println ("\tVariable (D) " + Attributes.getInputAttribute(i).getName() + " = ..." );
                    contents+= "\tVariable " + Attributes.getInputAttribute(i).getName() + " = " ;
                    for (int j=0; j<Variables.getNLabelVar(i); j++) {
                        if (rule.getCromElemGene(i,j)==1)
                            contents+= Attributes.getInputAttribute(i).getNominalValue(j) + " ";
                    }
                    contents+= "\n";
                }
                else {  
                    // Variable continua
                    System.out.println ("\tVariable (C) " + Attributes.getInputAttribute(i).getName() + " = ..." );
                    contents+= "\tVariable " + Attributes.getInputAttribute(i).getName() + " = ";
                    for (int j=0; j<Variables.getNLabelVar(i); j++) {
                        if (rule.getCromElemGene(i,j)==1) {
                            contents+= "Label " + j;
                            contents+= " (" + Variables.getX0(i,(int) rule.getCromElemGene(i,j));
                            contents+= " " + Variables.getX1(i,(int) rule.getCromElemGene(i,j));
                            contents+= " " + Variables.getX3(i,(int) rule.getCromElemGene(i,j)) +")\t";
                        }
                    }
                    contents+= "\n";
                }
            }
        }
        contents+= "   Consecuent: " + Variables.getNameClassObj();

        contents+= "\n";
        Files.addToFile(fileRule, contents);

        // Write the quality measures of the rule
        DecimalFormat sixDecimals = new DecimalFormat("0.000000");
        contents = "" + Variables.getNumClassObj();
        contents+= "\t" + sixDecimals.format(Result.getSup());
        contents+= "\t" + sixDecimals.format(Result.getCnf());
        if(AG.getObj3().compareTo("NULL")!=0) contents+= "\t" + sixDecimals.format(Result.getVal3());
        contents+= "\t" + sixDecimals.format(Result.getFitness()) + "\n";
        Files.addToFile(measure_file, contents);

    }
    
     /**
      * <p>
      * Main method of the algorithm
      * </p>
     * @param args Arguments of the program (a configuration script, generally)  
     * @throws java.lang.Exception  if the algorithm can not be executed.
      **/
    public static void main(String[] args) throws Exception {
        
        int NumReglasGeneradas;           // Number of generated rules (number of iteration of the GA
        QualityMeasures Resultados;       // Stores the quality values of the rule evaluation
        String contents;                  // Strings for the files contents
        float savePrMut;                  // To backup the mutation probability
        
        boolean mejora_local = false;  // Indicates if the local search (if applied) has improved the rule
        boolean terminar = false;      // Indicates no more repetition for the rule generation of diferent classes

        if (args.length != 1) {
          System.err.println("Syntax error. Usage: java AGI <parameterfile.txt>" );
          return;
        }

        // Initial echo
        System.out.println(); System.out.println("Iterative Genetic Algorithm");

        Variables = new TableVar();
        Examples = new TableDat();
        AG= new Genetic();
        
        // Read parameter file and initialize parameters
        ReadParameters (args[0]);

        // Saves the mutation probability
        savePrMut = AG.getProbMut();
        
        // Read the dataset, store values and echo to output and seg files
        capturaDataset ();
        if (echo) WriteSegDataset (seg_file);

        // Create and initilize gain information array
        if (echo) Variables.GainInit(Examples, seg_file);
        else Variables.GainInit(Examples, "");

        // Screen output of same parameters
        System.out.println ("\nSeed: " + seed);    // Random Seed
        System.out.println ("Local Search?: " + AG.getLSearch());   // LocalImprovemente
        System.out.println ("\nOutput variable: " + Attributes.getOutputAttribute(0).getName() ); // Output variable

        // Generation of rules for one class or all the classes
        if (claseSelec) {
            // If there is one class indicated as a parameter, only generate rules of thas class
            terminar = true;
            // Set the number and the name of the selected class of the output variable from its value
            Variables.setNumClassObj(-1);
            // To assure an invalid value if the class name in the param file is invalid
            for (int z=0; z<Variables.getNClass(); z++) {
                if (Attributes.getOutputAttribute(0).getNominalValue(z).equalsIgnoreCase(Variables.getNameClassObj())) {
                    Variables.setNameClassObj(Attributes.getOutputAttribute(0).getNominalValue(z));
                    Variables.setNumClassObj(z);
                }
            }
            // If the value is invalid, generate rules for all the classes
            if (Variables.getNumClassObj()==-1) {
                System.out.println ( "Class name invalid (" + Variables.getNameClassObj() + "). Generate rules for all the classes");
                claseSelec=false;
                terminar = false;
                Variables.setNumClassObj(0);
            }
            else 
               System.out.println ( "Generate rules for class " + Variables.getNameClassObj() + " only");
        }
        else {
            // No class indicated, so generate rules of all the classes
            Variables.setNumClassObj(0);
            System.out.println ( "Generate rules for all the classes");
        }

        // Initialise rule file
        Files.writeFile(rule_file, "");
        
        // Initialise measure file
        contents = "Measures used as objectives: ";
        contents += AG.getObj1()+", ";
        contents += AG.getObj2()+", ";
        if (AG.getObj3().compareTo("NULL")!=0) contents += AG.getObj3()+";";

        // Include in this header the measures to be written in the quality measures file
        contents += "\nClass \t";
        contents += AG.getObj1()+"\t";
        contents += AG.getObj2();
        if (AG.getObj3().compareTo("NULL")!=0) contents += "\t"+AG.getObj3();
        contents += "\tFITNESS";
        contents += "\n";

        Files.writeFile(measure_file, contents);

        // Execution Header for the tracking file
        contents = "\n";
        contents+= "--------------------------------------------\n";
        contents+= "|             Execution results            |\n";
        contents+= "--------------------------------------------\n";
        Files.addToFile(seg_file, contents);
        
        do {        
            // Initialisation of random generator seed. Done after load param values
            if (seed!=0) Randomize.setSeed (seed);

            // If no class especified, define the class for each iteration
            if (!claseSelec)
                // Set the nominal value of the class
                Variables.setNameClassObj(Attributes.getOutputAttribute(0).getNominalValue(Variables.getNumClassObj()));

            // Tracking to file and "seg" file
            System.out.println ("\nTarget class number: " + Variables.getNumClassObj() + " (value " + Variables.getNameClassObj() + ")");
            Files.addToFile(seg_file, "\nClass " + Variables.getNumClassObj() + ":\n");

            // Restores the original value for the mutation probability
            AG.setProbMut(savePrMut);
            
            // Set the mutation prob
            AG.setProbMut(AG.getProbMut() / Variables.getNVars());

            // Gets the next gene to be muted
            if (AG.getProbMut() < 1)
                AG.setMuNext ((int) Math.ceil (Math.log(Randomize.Rand()) / Math.log(1.0 - AG.getProbMut())));
            else
                AG.setMuNext(1);

            // Set all the examples as not covered
            for (int ej=0; ej<Examples.getNEx(); ej++)
                Examples.setCovered (ej,false);
                // Set example to not covered

            // Load the number of examples of the target class
            Examples.setExamplesClassObj(Variables.getNumClassObj());

            // Variables Initialization
            NumReglasGeneradas = 0;              // Number of generated rules 
            Examples.setExamplesCovered(0);    // Number of covered examples

            // Iterative GA run
            do {
                System.out.print ("# " + NumReglasGeneradas + ": ");

                // GA execution
                AG.GeneticAlgorithm(Variables, Examples, seg_file);

                // Store the support (comp) value
                Resultados = AG.getQualityMeasures(AG.getBestGuy(), seg_file);
                float oldsup = Resultados.getSup();

                // Local search to improve the rule
                if (AG.getLSearch()){
                    if(AG.getRulesRep().compareTo("CAN")==0){
                        mejora_local = AG.LocalImprovementCAN(Variables, Examples);
                    } else {
                        mejora_local = AG.LocalImprovementDNF(Variables, Examples);
                    }
                }
                // Evaluates the better rule
                AG.evalIndiv(AG.getBestGuy(), AG, Variables, Examples, true);

                // Obtain the results of the better rule
                Resultados = AG.getQualityMeasures(AG.getBestGuy(), seg_file);

                if (NumReglasGeneradas==0 ||(Resultados.getCnf()>=AG.getMinConf() && Resultados.getLSup()>0))
                    if(AG.getRulesRep().compareTo("CAN")==0){
                        WriteRuleCAN(NumReglasGeneradas, AG.getIndivCromCAN(AG.getBestGuy()), Resultados, rule_file, measure_file);
                    } else {
                        WriteRuleDNF(NumReglasGeneradas, AG.getIndivCromDNF(AG.getBestGuy()), Resultados, rule_file, measure_file);
                    }
                
                // Local search info to "seg" file
                if (AG.getLSearch()) { 
                    // Local search applied
                    if (mejora_local) 
                        // The rule was improved
                        contents = "\tRule support improved by local search from " + oldsup + " to " + Resultados.getSup();
                    else  
                        // The rule was not improved
                        contents = "\tRule not improved by Local search. Support = " + oldsup;
                }
                else  // Local search not applied
                    contents = "\tLocal search not applied. Support = " + oldsup;
                contents+= "\n";
                if (!(NumReglasGeneradas==0 ||(Resultados.getCnf()>=AG.getMinConf() && Resultados.getLSup()>0))) {
                    // If the rule does not overcomes minimum confidence and support
                    contents = "\tRule not stored (low confidence or support)\n";
                    System.out.print (contents);
                }
                Files.addToFile(seg_file, contents);
                
                // Increments the number of generated rules
                NumReglasGeneradas++;
                
            // While reaches minimum confidence and covers non-covered examples
            } while (Resultados.getCnf()>=AG.getMinConf() &&  Resultados.getLSup()>0 );
            
            if (!claseSelec) {
                // No class indicated to generate de rules (generate rules for all the classes)
            	// Set num_clase as the next class
            	Variables.setNumClassObj(Variables.getNumClassObj()+1);
            	// If there are no more classes, set terminar as true
            	if (Variables.getNumClassObj()>=Variables.getNClass())
            	    terminar = true;
            }

       
       } while (terminar==false);
        
       System.out.println("Algorithm terminated\n");
       System.out.println("--------------------\n");
       System.out.println("Calculating values of the quality measures\n");

       //Calculate the quality measures
       Files.writeFile(output_file_tra,Data.getHeader());
       Files.writeFile(output_file_tst,Data.getHeader());
       Calculate.Calculate(output_file_tra, output_file_tst, input_file_tra, input_file_tst, rule_file, qmeasure_file, Variables.getNLabel());
        
  }

    
}
