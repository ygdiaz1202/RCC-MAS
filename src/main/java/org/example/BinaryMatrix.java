package org.example;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BinaryMatrix {
    LinkedList<boolean[]> bdsm;

    public boolean[][] getBm() {
        boolean[][]mb=new boolean[bdsm.size()][bdsm.getLast().length];
        mb = bdsm.toArray(mb);
        return mb;
    }

    public int size() {
        return bdsm.size();
    }

    public BinaryMatrix() {
        //    private boolean[][] bm;
        this.bdsm = new LinkedList<>();
    }

    /**
     * @param row is the new binary row to add to the basic matrix
     */
    public void add(boolean[]row) {
        boolean basicRow = true;
        Iterator<boolean[]> iterator = bdsm.iterator();
        while (iterator.hasNext()){
            boolean[] currentRow = iterator.next();
            int comparison = compareRows(row, currentRow);
            if (comparison!=-1)
                if (comparison==0)
                    iterator.remove();
                else{
                    if (comparison==1){
                        basicRow = false;
                        break;
                    }
                }
        }
        if (basicRow)
            bdsm.add(row);
    }

    /**
     * @param new_row is the new binary row to add to the basic matrix
     * @param row_j   is a pseudo basic row in the temporal basic matrix
     * @return 0 if ri is sub row of rj, 1 if ri is super row of rj, and -1 if ri!=rf
     */
    public int compareRows(boolean @NotNull [] new_row, boolean[] row_j) {
        boolean sub_row = true;
        boolean super_row = true;
        for (int i = 0; i < new_row.length; i++) {
            if (new_row[i]) {
                sub_row = row_j[i] & sub_row;
            } else {
                super_row = (!row_j[i]) & super_row;
            }
            if (!sub_row && !super_row) {
                return -1;
            }
        }
        return sub_row ? 0 : 1;
    }
}

