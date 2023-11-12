package org.example;

import lombok.Getter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class AttrSubsetCandidate {
    @Getter
    private Integer []attrSubset;
    @Getter
    private int lastAttrIndex=-1;
    @Getter
    private static Integer []kernel=null;
    @Getter
    public int max=0;
    @Getter
    private int lastIndexZeroRows =-1;
    @Getter
    private LinkedList<Integer>[] posRegIndex;
    private Integer[] zeroRows;
    public static void setKernel(Integer[] kernel) {
        AttrSubsetCandidate.kernel = kernel;
    }
    public String getRI(){
        String ri="${";
        for (int i=0; i<lastAttrIndex; i++){
            if (posRegIndex[attrSubset[i]].isEmpty()){
                ri+="{}, ";
            }else{
                ri+=(posRegIndex[attrSubset[i]]).stream().map(r -> "r_"+r).toList().toString().replace("[","{").replace("]","}")+",";
            }
        }
        if (posRegIndex[attrSubset[0]].isEmpty()){
            ri+="{}";
        }else {
            ri+=(posRegIndex[attrSubset[lastAttrIndex]]).stream().map(r -> "r_"+r).toList().toString().replace("[","{").replace("]","}");
        }
        ri+="}$";
        return ri;
    }
    public void setAttrSubset(Integer[] attrSubset) {
        this.attrSubset = attrSubset;
    }
    public void setLastAttrIndex(int lastAttrIndex) {
        this.lastAttrIndex = lastAttrIndex;
    }
    public void setMax(int max) {
        this.max = max;
    }
    public void setLastIndexZeroRows(int lastIndexZeroRows) {
        this.lastIndexZeroRows = lastIndexZeroRows;
    }
    public Integer[] getZeroRows() {
        Integer [] arr=new Integer[lastIndexZeroRows +1];
        System.arraycopy(zeroRows,0,arr,0, lastIndexZeroRows +1);
        return arr;
    }

    public void setZeroRows(Integer[] zeroRows) {
        this.zeroRows = zeroRows;
    }

    public void addAttr(int attr){
        if (this.lastAttrIndex < this.max - 1) {
            this.lastAttrIndex++;
            this.attrSubset[this.lastAttrIndex] = attr;
        }
    }
    public AttrSubsetCandidate(AttrSubsetCandidate that){
        this(   that.getAttrSubset().clone(),
                that.getLastAttrIndex(),
                that.getLastIndexZeroRows(),
                that.getPosRegIndex().clone(),
                that.getZeroRows().clone(),
                that.max
        );
    }
    public AttrSubsetCandidate(Integer[] attrSubset, int lastAttrIndex, int lastIndexNegReg, LinkedList<Integer>[] posRegIndex, Integer[] zeroRows, int max) {
        this.attrSubset = attrSubset;
        this.lastAttrIndex = lastAttrIndex;
        this.lastIndexZeroRows = lastIndexNegReg;
        this.posRegIndex = posRegIndex;
        this.zeroRows = zeroRows;
        this.max=max;
    }

    public Integer[] getConstruct(int attr){
        Integer []construct;
        if (kernel==null) {
            construct = new Integer[lastAttrIndex + 2];
        }else {
            construct = new Integer[lastAttrIndex + 2 + kernel.length];
            System.arraycopy(kernel, 0, construct, lastAttrIndex + 2, kernel.length);
        }
        System.arraycopy(attrSubset, 0, construct, 0, lastAttrIndex + 1);
        construct[lastAttrIndex + 1]=attr;
        return construct;
    }

    public Integer[] getConstruct(){
        Integer []construct;
        if (kernel==null) {
            construct = new Integer[lastAttrIndex + 1];
        }else {
            construct = new Integer[lastAttrIndex + 1 + kernel.length];
            System.arraycopy(kernel, 0, construct, lastAttrIndex + 1, kernel.length);
        }
        System.arraycopy(attrSubset, 0, construct, 0, lastAttrIndex + 1);
        return construct;
    }

    public static Integer[] getRealAttrIndex(Integer[] attrSubset){
        return Arrays.stream(attrSubset)
                .map(attr -> RccMasAlgorithm.indexMap[attr])
                .toArray(Integer[]::new);
    }

    public Integer[] getCandidateSubset(){
        Integer []cSubset= new Integer[lastAttrIndex + 1];
        System.arraycopy(attrSubset, 0, cSubset, 0, lastAttrIndex + 1);
        return  cSubset;
    }

    public int setConstructs(int attr){
        Integer[]construct = getRealAttrIndex(getConstruct(attr));
        LinkedList<Integer>[] substitutionValues = RccMasAlgorithm.repIndex.clone();
        LinkedList<Integer[]> solutions= generateAllConstructs(construct,0, new Integer[construct.length], substitutionValues);
        RccMasAlgorithm.constructs.addAll(solutions);
        return solutions.size();
    }

    public int setConstructs(){
        Integer[]construct = getRealAttrIndex(getConstruct());
        LinkedList<Integer>[] substitutionValues = RccMasAlgorithm.repIndex.clone();
        LinkedList<Integer[]> solutions= generateAllConstructs(construct,0, new Integer[construct.length], substitutionValues);
        return solutions.size();
    }

    public static LinkedList<Integer[]> generateAllConstructs(Integer[] originalArray, int currentIndex, Integer[] currentArray,  List<Integer>[] substitutionValues) {
        LinkedList<Integer[]> result = new LinkedList<>();
        if (currentIndex == originalArray.length) {
            result.add(currentArray.clone());
            return result;
        }
        // Continue with the next index if no substitutions are available
        if (substitutionValues[originalArray[currentIndex]] == null || substitutionValues[originalArray[currentIndex]].isEmpty()) {
            currentArray[currentIndex] = originalArray[currentIndex];
            result.addAll(generateAllConstructs(originalArray, currentIndex + 1, currentArray, substitutionValues));
        } else {
            // Include the original value in the result
            currentArray[currentIndex] = originalArray[currentIndex];
            result.addAll(generateAllConstructs(originalArray, currentIndex + 1, currentArray.clone(), substitutionValues));

            // Substitute with alternative values
            for (int newValue : substitutionValues[originalArray[currentIndex]]) {
                currentArray[currentIndex] = newValue;
                result.addAll(generateAllConstructs(originalArray, currentIndex + 1, currentArray.clone(), substitutionValues));
            }
        }
        return result;
    }

    public boolean isConstruct(){
        return !isEmpty() && lastIndexZeroRows ==-1;
    }

    public boolean isEmpty(){
        return lastAttrIndex==-1;
    }

    public int size(){
        return getLastAttrIndex()+1;
    }
    public int getAttribute(int index){
        if (index<attrSubset.length&&index<=lastAttrIndex)
            return attrSubset[index];
        else
            return -1;
    }

    public AttrSubsetCandidate(int numbAttributes, int size) {
        this.posRegIndex=new LinkedList[numbAttributes];
        this.zeroRows =new Integer[size];
        for (int i = 0; i < size; i++) {
            this.zeroRows[i]=i;
        }
        this.lastIndexZeroRows =size-1;
        this.attrSubset=new Integer[numbAttributes];
        max=numbAttributes;
    }

    public int zeroRowsSize(){
        return lastIndexZeroRows +1;
    }
    public void updatePosInstances(int attrIndex, LinkedList<Integer> tuples){
        posRegIndex[attrIndex]=tuples;
    }

    public void clear(){
        this.posRegIndex=new LinkedList[posRegIndex.length];
        this.attrSubset=new Integer[attrSubset.length];
        this.lastAttrIndex=-1;
    }

    public int getRepetitions(int [] indexMap, int[] repIndex) {
        int count = 1;
        for (int i = 0; i < lastAttrIndex + 1; i++) {
            int realIndex = indexMap[attrSubset[i]];
            int repetitions=repIndex[realIndex];
            if (repetitions > 0) {
                count *= (repetitions + 1);
            }
        }
        return count;
    }

    public int getRepetitions(int attr, int [] indexMap, int [] repIndex){
        int count=getRepetitions(indexMap, repIndex);
        int realIndex=indexMap[attr];
        int repetitions=repIndex[realIndex];
        if (repetitions > 0) {
            count *= (repetitions + 1);
        }
        return count;
    }
}
