package org.example;

import org.jetbrains.annotations.NotNull;

import java.util.BitSet;
import java.util.LinkedList;

public class Util {
    public static boolean[][] bm;//save the pairs of object in the discenibility-similarity relation

    public static int attributes;
    public static int numbConsistentTuples;
    public static DecisionSystem ds;
    public static  int start;
    public static int end;

    public static LinkedList<Integer[]> constructs;//contain all the constructs found so far

    public static String removeFileExtension(String filename, boolean removeAllExtensions) {
        if (filename == null || filename.isEmpty()) {
            return filename;
        }
        String extPattern = "(?<!^)[.]" + (removeAllExtensions ? ".*" : "[^.]*$");
        return filename.replaceAll(extPattern, "");
    }

    /**
     * Converts a boolean array into a BitSet
     * @param bits Boolean array to convert.
     * @param offset Array index to start reading from.
     * @param length Number of bits to convert.
     * @return
     */
    public static  BitSet boolToBitSet(boolean[] bits, int offset, int length) {
        BitSet bitset = new BitSet(length - offset);
        for (int i = offset; i < length; i++)
            bitset.set(i - offset, bits[i]);

        return bitset;
    }

    public static void init(String filename){
        ds=ParseFile.getDS(filename);
        assert ds != null;
        attributes=ds.getAttributes()-1;
        bm =ds.getBinaryMatrix();
        numbConsistentTuples=bm.length;
        start=0;
        end=numbConsistentTuples;
    }

    public static void init(boolean[][] otherBM){
        bm=otherBM;
        attributes=bm[0].length;
        numbConsistentTuples=bm.length;
        start=0;
        end=numbConsistentTuples;
    }

    public static boolean noZeroRow(int row, int[] attributes_set, int lastAttributeIndex) {
        if (attributes_set != null) {
            for (int index=0; index<=lastAttributeIndex; index++) {
                int k = attributes_set[index];
                if (bm[row][k]) {
                    return true;
                }
            }
        }
        return false;
    }
    public static boolean noZeroRow(int row, int attr) {
        return bm[row][attr];
    }

    public static Object  [] readCols(boolean [] [] simplifiedMatrix)
    {
        Object[] result = new Object[3];
        LinkedList<Integer>[] repIndex = new LinkedList[simplifiedMatrix[0].length];
        int[] indexMap = new int[simplifiedMatrix[0].length];

        boolean[] repeatedCols = new boolean[simplifiedMatrix[0].length];

        for (int i = 0; i < simplifiedMatrix[0].length; i++) {
            repeatedCols[i] = true;
            for (boolean[] matrix : simplifiedMatrix) {
                if (matrix[i]) {
                    repeatedCols[i] = false;
                    break;
                }

            }
        }
        for (int firstCol = 0; firstCol < simplifiedMatrix[0].length-1; firstCol++) {
            LinkedList<Integer> repeatedAttr=new LinkedList<>();
            if(!repeatedCols[firstCol])
            {
                for (int secondCol = firstCol+1; secondCol < simplifiedMatrix[0].length; secondCol++) {
                    if(!repeatedCols[secondCol]) {
                        repeatedCols[secondCol] = true;
                        for (boolean[] row : simplifiedMatrix) {
                            if (row[firstCol] != row[secondCol]) {
                                repeatedCols[secondCol] = false;
                                break;
                            }
                        }
                        if(repeatedCols[secondCol]){
                            repeatedAttr.add(secondCol);
                        }
                    }

                }
            }
            repIndex[firstCol]=repeatedAttr;
        }
        int numberOfRepeatedCols = 0;
        for (boolean repeatedCol : repeatedCols)
            if (repeatedCol)
                numberOfRepeatedCols++;

        int newColsNumber = simplifiedMatrix[0].length - numberOfRepeatedCols;
        int newRowsNumber = simplifiedMatrix.length;
        boolean[][] newMatrix = new boolean[newRowsNumber][newColsNumber];
        int cont = 0;
        for (int i = 0; i < repeatedCols.length; i++) {
            if(!repeatedCols[i])
            {
                for (int j = 0; j < newRowsNumber; j++)
                    newMatrix[j][cont] = simplifiedMatrix[j][i];
                indexMap[cont]=i;
                cont++;
            }
        }
        result[0] = newMatrix;
        result[1] = indexMap;
        result[2] = repIndex;
        return result;
    }

    public static Object @NotNull [] readColsV1(boolean [] [] simplifiedMatrix)
    {
        Object[] result = new Object[3];
        int[] repIndex = new int[simplifiedMatrix[0].length];
        int[] indexMap = new int[simplifiedMatrix[0].length];

        boolean[] repeatedCols = new boolean[simplifiedMatrix[0].length];

        for (int i = 0; i < simplifiedMatrix[0].length; i++) {
            repeatedCols[i] = true;
            for (int j = 0; j < simplifiedMatrix.length; j++) {
                if(simplifiedMatrix[j][i]){
                    repeatedCols[i] = false;
                    break;
                }

            }
        }
        for (int firstCol = 0; firstCol < simplifiedMatrix[0].length-1; firstCol++) {
            if(!repeatedCols[firstCol])
            {
                for (int secondCol = firstCol+1; secondCol < simplifiedMatrix[0].length; secondCol++) {
                    if(!repeatedCols[secondCol]) {
                        repeatedCols[secondCol] = true;
                        for (boolean[] row : simplifiedMatrix) {
                            if (row[firstCol] != row[secondCol]) {
                                repeatedCols[secondCol] = false;
                                break;
                            }
                        }
                        if(repeatedCols[secondCol])
                            repIndex[firstCol]++;
                    }

                }
            }
        }
        int numberOfRepeatedCols = 0;
        for (boolean repeatedCol : repeatedCols)
            if (repeatedCol)
                numberOfRepeatedCols++;

        int newColsNumber = simplifiedMatrix[0].length - numberOfRepeatedCols;
        int newRowsNumber = simplifiedMatrix.length;
        boolean[][] newMatrix = new boolean[newRowsNumber][newColsNumber];
        int cont = 0;
        for (int i = 0; i < repeatedCols.length; i++) {
            if(!repeatedCols[i])
            {
                for (int j = 0; j < newRowsNumber; j++)
                    newMatrix[j][cont] = simplifiedMatrix[j][i];
                indexMap[cont]=i;
                cont++;
            }
        }

        result[0] = newMatrix;
        result[1] = indexMap;
        result[2] = repIndex;
        return result;
    }
}
