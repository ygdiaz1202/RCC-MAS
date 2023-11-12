package org.example;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class ParseFile {

    private static DecisionSystem parseArffFile(String filename){
        BufferedReader reader = null;
        DecisionSystem ds=null;
        try {
            reader = new BufferedReader(new FileReader(filename));
            ArffLoader.ArffReader arff = new ArffLoader.ArffReader(reader);
            Instances data = arff.getData();
            data.setClassIndex(data.numAttributes() - 1);
            int attributes=data.numAttributes();
            int classes=data.numClasses();
            data.sort(data.classIndex());
            int[] attrTypes =new int[attributes];
            String []attrNames=new String[attributes];
            for (int i = 0; i < attributes; i++) {
                attrTypes[i]=data.attribute(i).type();
                attrNames[i]=data.attribute(i).name();
            }
            String[][]values;
            System.out.println("Number of attributes: "+attributes+", Number of Classes: "+classes
                    +" Number of Instances: "+data.numInstances());
            values= data.stream().map(instance -> instance.toString().split(",")).toArray(String[][]::new);
            ds=DecisionSystem.builder()
                    .attrNames(attrNames)
                    .classIndex(attributes-1)
                    .attributes(attributes)
                    .classes(classes)
                    .attrTypes(attrTypes)
                    .values(values)
                    .fileName(Util.removeFileExtension(filename,false))
                    .numbInstances(data.numInstances())
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ds;
    }

    private static DecisionSystem parseCsv(String filename){
        return null;
    }

    public static DecisionSystem getDS(String filename){
        if (filename.endsWith(".arff")){
            return parseArffFile(filename);

        }else if (filename.endsWith(".csv")){
            return parseCsv(filename);
        }
        return null;
    }

    public static boolean[][] readBM(String filename) throws NumberFormatException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        int rows    = Integer.parseInt(br.readLine().trim());
        int columns = Integer.parseInt(br.readLine().trim());
        boolean[][] bm = new boolean[rows][columns];
        for(int r = 0; r < rows; r ++) {
            String line = br.readLine().trim();
            String[] lineArray = line.split(" ");
            for(int c = 0; c < columns; c ++) {
                if(lineArray[c].equals("1")) bm[r][c] = true;
            }
        }
        br.close();
        return bm;
    }

    public static  ArrayList<ArrayList<Integer>> readBMAsInt(String filename) throws NumberFormatException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        int rows    = Integer.parseInt(br.readLine().trim());
        int columns = Integer.parseInt(br.readLine().trim());
        ArrayList<ArrayList<Integer>> dataset=new ArrayList<>();
        for(int r = 0; r < rows; r ++) {
            String line = br.readLine().trim();
            ArrayList<Integer> data= new ArrayList<>(Arrays.stream(line.split(" ")).map(Integer::valueOf).toList());
            dataset.add(data);
        }
        br.close();
        return dataset;
    }


}
