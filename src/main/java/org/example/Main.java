package org.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


public class Main {
    static long startTime=0L;
    static long elapsedTime=0L;

    public static void main(String  [] args) {
        String fileName="./BM.bol";
        if (args.length == 0){
            runAlgorithm(fileName);
        }else{
            if (args[0].equals("-all")){
                // Get the current directory
                File currentDirectory = new File(System.getProperty("user.dir"));

                // List all files in the current directory
                File[] files = currentDirectory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()) {
                            String filename= file.getName();
                            if (filename.endsWith(".bol") || filename.endsWith(".arff")){
                                runAlgorithm(filename);
                            }
                        }
                    }
                } else {
                    System.err.println("Failed to list files in the current directory.");
                }
            }else{
                for (String filename:args) {
                    runAlgorithm(filename);
                }
            }
        }
    }

    private static void runAlgorithm( String filename){
        RccMasAlgorithm constructC=null;
//        ConstructC_v2 constructC=null;
        if (filename.endsWith("arff")){
            DecisionSystem ds= ParseFile.getDS(filename);
            assert ds != null;
            try {
                ds.writeMatrix(ds.getFileName());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
//            constructC=new ConstructC_v2(ds);
            constructC=new RccMasAlgorithm(ds);
        }else if (filename.endsWith(".bol")){
            try {
//                constructC=new ConstructC_v2(ParseFile.readBM(filename));
                constructC=new RccMasAlgorithm(ParseFile.readBM(filename));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (constructC!=null){
            Path path = Paths.get(filename);

            // Get the filename
            String fileName = path.getFileName().toString();
            startTime = System.nanoTime();
            constructC.getAllConstructs();
            elapsedTime=(System.nanoTime() - startTime)/1000000;
            System.out.println(fileName);
            System.out.println("time (ms): " + elapsedTime);
            System.out.println("Constructs: " + RccMasAlgorithm.numbConstructs);
            System.out.println("Candidates: " + RccMasAlgorithm.numbCandidates);
//            RccMasAlgorithm.writeRunningLog("./output.csv");
            try {
                constructC.write(fileName, elapsedTime);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
