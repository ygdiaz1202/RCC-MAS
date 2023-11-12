package org.example;
import lombok.*;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;

@Builder
@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class DecisionSystem {
    private String[] attrNames;
    private int[] attrTypes;
    private int attributes;

    private String fileName;
    private int numbInstances;
    private int classes;
    private int classIndex;
    private String[][] values;
    private boolean[][] bdsm;

    /**
     * @param ri row in the decision system
     * @param rj row in the decision system
     * @return a vector with the comparison between ri and rj
     */
    private boolean @Nullable [] getBinaryRow(int ri, int rj) {
        boolean[] result = new boolean[attributes - 1];
        boolean allZeros = true;
        boolean sameClass = values[ri][attributes - 1].equals(values[rj][attributes - 1]);
        if (sameClass) {
            for (int i = 0; i < attributes - 1; i++) {
                if (values[ri][i].equalsIgnoreCase("?") || values[rj][i].equalsIgnoreCase("?")) {
                    result[i] = false;
                } else {
                    result[i] = values[ri][i].equals(values[rj][i]);
                    if (result[i]) {
                        allZeros = false;
                    }
                }
            }
        } else {
            for (int i = 0; i < attributes - 1; i++) {
                if (values[ri][i].equalsIgnoreCase("?") || values[rj][i].equalsIgnoreCase("?")) {
                    result[i] = true;
                    allZeros = false;
                } else {
                    result[i] = !values[ri][i].equals(values[rj][i]);
                    if (result[i]) {
                        allZeros = false;
                    }
                }
            }
        }
        if (allZeros)
            return null;
        else
            return result;
    }

    public boolean[][] getBinaryMatrix() {
        BinaryMatrix binaryMatrix=new BinaryMatrix();
        for (int i = 0; i < values.length - 1; i++) {
            for (int j = i + 1; j < values.length; j++) {
                boolean[] newRow = getBinaryRow(i, j);
                if (newRow != null)//Zero rows is not taken in account
                    binaryMatrix.add(newRow);
            }
        }
        return binaryMatrix.getBm();
    }

    public void writeMatrix(String filename) throws IOException {
        System.out.println("Creating the BDSM..., this can take some minutes...");
        bdsm = getBinaryMatrix();
        final long[] c = {0};
        List<String> matrix = Arrays.stream(bdsm).map(value -> {
            StringBuilder line = new StringBuilder();
            for (boolean b : value) {
                if (b) {
                    line.append("1 ");
                    c[0]++;
                }else{
                    line.append("0 ");
                }
            }
            return line.toString();
        }).toList();
        float density=(float) c[0]/(bdsm.length*bdsm[0].length);
        File file = new File(filename+"_"+String.format("%.2f",density)+".bol");
        System.out.println("The SBDSM was generated successfully, and saved as: "+file.getName());
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(bdsm.length + "\n" + (attributes - 1) + "\n");
        for (String line : matrix) {
            fileWriter.write(line + "\n");
        }
        fileWriter.close();
    }

}
