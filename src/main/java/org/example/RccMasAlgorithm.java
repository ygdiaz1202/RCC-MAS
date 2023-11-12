package org.example;
import com.opencsv.CSVWriter;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
public class RccMasAlgorithm {
    public static boolean[][] bm;

    public static LinkedList<String[]>  execution;
    public static int numbColsBM;
    public static int numbRowsBM;
    public static int [] indexMap;
    public static LinkedList<Integer> [] repIndex;//comment this line and uncomment the nex line if you want only the number of constructs
//    public static int [] repIndex;
    public static DecisionSystem ds;
    public static long numbConstructs = 0L;
    public static long numbCandidates = 0L;
    public static int MAX=-1;
    public static LinkedList<Integer[]> constructs;
    public RccMasAlgorithm(@NotNull DecisionSystem otherDS) {
        ds = otherDS;
        Object[] solution =Util.readCols(ds.getBinaryMatrix());
        indexMap=(int[])solution[1];
        repIndex=(LinkedList<Integer>[])solution[2];
        bm = (boolean[][]) solution[0];
        numbColsBM = (bm[0]).length;
        numbRowsBM = bm.length;
    }
    public RccMasAlgorithm(boolean[][] otherBM) {
        Object[] solution =Util.readCols(otherBM);
        indexMap=(int[])solution[1];
        repIndex=(LinkedList<Integer>[])solution[2];
        bm = (boolean[][]) solution[0];
        numbColsBM = (bm[0]).length;
        numbRowsBM = bm.length;
    }

    /**
     * @param i index of the row in the matrix
     * @param attributes_set the actual candidate attribute set
     * @return true if the row contains only zeros
     * using only the attributes in attributes_set
     */
    private boolean isZeroRow(int i, Integer[] attributes_set, int lastAttributeIndex) {
        if (attributes_set != null)
            for (int index = 0; index <= lastAttributeIndex; index++) {
                int k = attributes_set[index];
                if (bm[i][k])
                    return false;
            }
        return true;
    }
    /**
     * @return the kernel of the information system
     */
    public AttrSubsetCandidate getKernel() {
        AttrSubsetCandidate kernel = new AttrSubsetCandidate(numbColsBM, numbRowsBM);
        for (int j = 0; j < numbRowsBM; j++) {
            int counter=0;
            int rowIndex=0;
            for (int attr = 0; counter<=1 && attr < numbColsBM; attr++) {
                if (bm[j][attr]){
                    counter++;
                    rowIndex=attr;
                }
            }
            if (counter==1){
                kernel=addAttribute(kernel, rowIndex);
            }
        }
        return kernel;
    }

    /**
     *
     * @param prevCandidates the set of previous attributes that contributed to the previous candidate subset
     * @param numbOnesByRow contains the contribution of each attribute in @prevCandidates
     * @param row the current row
     * @return the number of ones by rows also update the contribution of each attribute based in the number of ones in @row
     */
    private static int updateAttrContribution(ArrayList<Integer> prevCandidates, int[] numbOnesByRow, int row) {
        int numbOnes=0;
        for (int attrPosition : prevCandidates) {
            if (bm[row][attrPosition]) {
                numbOnesByRow[attrPosition]++;
                numbOnes++;
            }
        }
        return numbOnes;
    }


    /**
     *
     * @param candidate the set of attributes candidates to construct
     * @param prevCandidates the set of previous attributes that contributed to the previous candidate subset
     * @return the contribution of each attribute in @prevCandidates as the numbers of zero_rows removed if this
     * attribute is included in the current candidate subset
     */
    private int[] getAttrContribution(AttrSubsetCandidate candidate, ArrayList<Integer> prevCandidates) {
        int[]numbOnesByRow = new int[numbColsBM];
        int minOnesByRow= numbRowsBM;
        int shortestRowIndex=-1;

        for (int i = 0; i<candidate.zeroRowsSize(); i++) {
            int row = candidate.getZeroRows()[i];
            int numbOnes = updateAttrContribution(prevCandidates, numbOnesByRow, row);
            if (numbOnes==0)
                return null;
            if (minOnesByRow>numbOnes){
                minOnesByRow=numbOnes;
                shortestRowIndex=row;
            }
        }
        if (shortestRowIndex==-1)
            return null;
        for (Integer prevCandidate : prevCandidates) {
            if (bm[shortestRowIndex][prevCandidate])
                numbOnesByRow[prevCandidate]=numbOnesByRow[prevCandidate]+MAX;
        }
        return numbOnesByRow;
    }

    /**
     * @param candidate      contains the subset of attributes candidate to construct,
     *                       also holds information about the indexes of the zero-rows.
     * @param listOfAttributes the previous list of attributes
     * @return the Minimal Subset of Attributes (minSubset) and a list of attributes that contributes to candidate (restAttributes)
     */
    private Object[] getCandidates(AttrSubsetCandidate candidate, ArrayList<Integer> listOfAttributes) {
        ArrayList<Integer> minSubset = new ArrayList<>();
        ArrayList<Integer> restAttributes = new ArrayList<>();
        int[] attrImportance = getAttrContribution(candidate, listOfAttributes);
        if (attrImportance==null)
            return null;
        for (int currentAttr:listOfAttributes) {
            if (attrImportance[currentAttr]>0)
                if (attrImportance[currentAttr]>MAX)
                    minSubset.add(currentAttr);
                else
                    restAttributes.add(currentAttr);
        }
        minSubset.sort((o1, o2) -> {if (attrImportance[o1]==attrImportance[o2]) return 0; else return  (attrImportance[o1]>attrImportance[o2])?1:-1;});
        restAttributes.sort((o1, o2) -> {if (attrImportance[o1]==attrImportance[o2]) return 0; else return  (attrImportance[o1]>attrImportance[o2])?1:-1;});
        return new Object[]{minSubset,restAttributes};
    }

    /**
     *
     * @param candidate_subset the subset of attributes candidate to construct
     * @param candidateAttr the new attribute to added
     * @return True is after including the new attribute the resulting subset is going to be minimal, False otherwise
     */
    private boolean isMinimal(AttrSubsetCandidate candidate_subset, int candidateAttr) {
        for (int i = 0; i <= candidate_subset.getLastAttrIndex(); i++) {
            int currentAttr = candidate_subset.getAttribute(i);
            boolean isMinimalSubset = isMinimal_v1(candidate_subset, candidateAttr, currentAttr);
            if (!isMinimalSubset)
                return false;
        }
        return true;
    }

    /**
     * this method is used to verify the minimal condition
     */
    private boolean isMinimal_v1(AttrSubsetCandidate candidate_subset, int candidateAttr, int currentAttr) {
        boolean isMinimalSubset = false;
        for (Integer index : candidate_subset.getPosRegIndex()[currentAttr]) {
            if (index == null)
                break;
            if (!bm[index][candidateAttr]) {
                isMinimalSubset = true;
                break;
            }
        }
        return isMinimalSubset;
    }

    private void findConstruct(AttrSubsetCandidate currentCandidate, ArrayList<Integer> c_attributes) {
        Object[]results = getCandidates(currentCandidate, c_attributes);
        if (results==null)
            return;
        ArrayList<Integer> minSubsetAttr=(ArrayList<Integer>)results[0];
        ArrayList<Integer> restOfAttr=(ArrayList<Integer>)results[1];

        while (!minSubsetAttr.isEmpty()) {
//            ArrayList<Integer> copyMSA= (ArrayList<Integer>) minSubsetAttr.clone(); //comment this line is no necessary
            int newAttrCandidate = minSubsetAttr.remove(minSubsetAttr.size()-1);
            numbCandidates++;
            if (isMinimal(currentCandidate, newAttrCandidate)) {
                if (isConstruct(currentCandidate, newAttrCandidate)) {
                    //comment this line and uncomment the nex line if you want only the number of constructs
                    numbConstructs+=currentCandidate.setConstructs(newAttrCandidate);
//                    numbConstructs+=currentCandidate.getRepetitions(newAttrCandidate,indexMap,repIndex);
                    //these two lines is just for saving the run of the algorithm please comment before run the algorithm
//                    AttrSubsetCandidate tmpCopy= addAttribute(currentCandidate, newAttrCandidate);
//                    saveExecutionTrace(tmpCopy, c_attributes, copyMSA, restOfAttr,"Construct "+ Arrays.stream(currentCandidate.getConstruct()).map(c -> "c_"+c).toList());
                    //----------------------------------------------------------------------------------------------
                    continue;
                }
                AttrSubsetCandidate newC = addAttribute(currentCandidate, newAttrCandidate);
                c_attributes = new ArrayList<>(minSubsetAttr);
                c_attributes.addAll(restOfAttr);
                //this line is just for saving the run of the algorithm please comment before run the algorithm
//                saveExecutionTrace(newC, c_attributes, copyMSA, restOfAttr, "attribute: $c_"+newAttrCandidate+"$ added");
                //----------------------------------------------------------------------------------------------
                findConstruct(newC, c_attributes);
            }//else{
            //these two lines is just for saving the run of the algorithm please comment before run the algorithm
//                AttrSubsetCandidate tmpCopy= addAttribute(currentCandidate, newAttrCandidate);
//                saveExecutionTrace(tmpCopy, c_attributes, copyMSA, restOfAttr, "Is not a minimal subset");
            //----------------------------------------------------------------------------------------------
//            }
        }
    }

    private static void saveExecutionTrace(@NotNull AttrSubsetCandidate currentCandidate, @NotNull ArrayList<Integer>
            c_attributes, @NotNull ArrayList<Integer> minSubsetAttr, @NotNull ArrayList<Integer> restOfAttr, String comments)
    {
        String[] line = new String[7];
        line[0]= c_attributes.stream().map(c -> "c_"+c).toList().toString();
        line[1]= minSubsetAttr.stream().map(c -> "c_"+c).toList().toString();
        line[2]=restOfAttr.stream().map(c -> "c_"+c).toList().toString();
        line[3]=Arrays.stream(currentCandidate.getCandidateSubset()).map(c -> "c_"+c).toList().toString();
        line[4]=Arrays.stream(currentCandidate.getZeroRows()).map(r -> "r_"+r).toList().toString();
        for (int i=0; i<5; i++){
            line[i]="$"+line[i].replace("[","{").replace("]","}")+"$";
        }
        line[5]=currentCandidate.getRI();
        line[6]=comments;
        execution.add(line);
    }

    private boolean isConstruct(AttrSubsetCandidate candidate, int newAttr) {
        if (candidate.getLastAttrIndex() == -1)//is an empty subset
            return false;
        for (int i = 0; i<candidate.zeroRowsSize(); i++) {
            int rowIndex= candidate.getZeroRows()[i];
            if (!bm[rowIndex][newAttr])//if exist a zero-row is not a construct
                return false;
        }
        return true;//if there are no zero-rows is a construct
    }


    private AttrSubsetCandidate addAttribute(AttrSubsetCandidate candidate, int newAttr) {
        AttrSubsetCandidate newCandidate = new AttrSubsetCandidate(numbColsBM, numbRowsBM);
        LinkedList<Integer> indexes = new LinkedList<>();
        newCandidate.setAttrSubset(candidate.getAttrSubset());
        newCandidate.setLastAttrIndex(candidate.getLastAttrIndex());

        Integer[] zeroRows = new Integer[candidate.zeroRowsSize()];
        int c = 0;
        for (int i = 0; i<candidate.zeroRowsSize(); i++) {
            int index= candidate.getZeroRows()[i];
            if (bm[index][newAttr]) {
                indexes.add(index);
            } else {
                zeroRows[c++] = index;
            }
        }
        newCandidate.setLastIndexZeroRows(c-1);
        newCandidate.setZeroRows(zeroRows);

        newCandidate.updatePosInstances(newAttr, indexes);
        updateAttributesPosRelation(candidate, newCandidate, newAttr);
        newCandidate.addAttr(newAttr);
        return newCandidate;
    }

    private void updateAttributesPosRelation(@NotNull AttrSubsetCandidate candidate, AttrSubsetCandidate newCandidate, int newAttr) {
        for (int i = 0; i < candidate.size(); i++) {
            int attr = candidate.getAttribute(i);
            LinkedList<Integer> indexes = new LinkedList<>();
            for (Integer index : candidate.getPosRegIndex()[attr]) {
                if (index == null)
                    break;
                if (!bm[index][newAttr])
                    indexes.add(index);
            }
            newCandidate.updatePosInstances(attr, indexes);
        }
    }

    public LinkedList<Integer[]> getAllConstructs() {
        MAX = numbRowsBM;
        execution = new LinkedList<>();
        constructs = new LinkedList<>();
        AttrSubsetCandidate attrSubsetCandidate = getKernel();
        if (attrSubsetCandidate.isConstruct()) {
            numbConstructs++;
            numbCandidates++;
            attrSubsetCandidate.setConstructs();
            return constructs;
        }
        int size = numbColsBM - attrSubsetCandidate.size();

        ArrayList<Integer> candidatesAttributes= new ArrayList<>(size);

        int k_index = 0;
        for (int i = 0; i < numbColsBM; i++) {
            int nextKernelAttr = attrSubsetCandidate.getAttribute(k_index);
            if (nextKernelAttr != -1 && nextKernelAttr == i) {
                k_index++;
            } else {
                candidatesAttributes.add(i);
            }
        }

        attrSubsetCandidate.setKernel(attrSubsetCandidate.getCandidateSubset());//the kernel is set for all the candidates
        attrSubsetCandidate.clear();//the kernel is removed from the candidates attributes

        findConstruct(attrSubsetCandidate, candidatesAttributes);
        return constructs;
    }

    public void printAllConstructs() {
        System.out.println("Printing all constructs in the DS...");
        if (constructs == null)
            getAllConstructs();
        for (Integer[] construct : constructs)
            System.out.println(Arrays.toString((Object[])construct));
        System.out.println("Number of Constructs: " + constructs.size());
    }

    public void write(String filename, long time) throws IOException {
        BufferedWriter outputWriter = null;
        outputWriter = new BufferedWriter(new FileWriter(filename+".txt"));
        outputWriter.write(filename);
        outputWriter.newLine();
        outputWriter.write("Total execution time in millis: " + time);
        outputWriter.newLine();
        outputWriter.write("Candidates: " + numbCandidates);
        outputWriter.newLine();
        outputWriter.write("Number of Constructs: " + numbConstructs);
        outputWriter.newLine();
        for (Integer[] construct : constructs) {
            outputWriter.write(Arrays.toString((Object[])construct));
            outputWriter.newLine();
        }
        outputWriter.flush();
        outputWriter.close();
    }

    private AttrSubsetCandidate getConstruct(Integer [] attributeSubset) {
        AttrSubsetCandidate newConstruct = new AttrSubsetCandidate(numbColsBM, numbRowsBM);
        for (Integer attribute : attributeSubset)
            addAttribute(newConstruct, attribute);
        return newConstruct;
    }


    public static void writeRunningLog(String filePath)
    {
        // first create file object for file placed at location
        // specified by filepath
        File file = new File(filePath);
        try {
            // create FileWriter object with file as parameter
            FileWriter outputfile = new FileWriter(file);

            // create CSVWriter object filewriter object as parameter
            CSVWriter writer = new CSVWriter(outputfile);

            // adding header to csv
            String[] header = {"CA", "MSA",	"AttrList", "Candidate (A)",	"ZR", "RI", "Comments"};
            writer.writeNext(header);
            for (String[] line:execution) {
                writer.writeNext(line);
            }
            // closing writer connection
            writer.close();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
