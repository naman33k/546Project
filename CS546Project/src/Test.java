import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.illinoisRE.common.Constants;
import edu.illinois.cs.cogcomp.illinoisRE.common.Document;
import edu.illinois.cs.cogcomp.illinoisRE.data.CleanDoc;
import edu.illinois.cs.cogcomp.illinoisRE.data.DataLoader;
import edu.illinois.cs.cogcomp.illinoisRE.data.GlobalDoc;
import edu.illinois.cs.cogcomp.illinoisRE.data.Mention;
import edu.illinois.cs.cogcomp.indsup.learning.FeatureVector;
import edu.illinois.cs.cogcomp.indsup.learning.LexManager;
import edu.illinois.cs.cogcomp.indsup.mc.MulticlassModel;
import edu.illinois.cs.cogcomp.indsup.mc.main.AllTest;

import java.io.*;

import CCMPackage.CCM;
import CCMPackage.MultiClassModel;

public class Test {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		//String file = "/home/wieting2/TIDES-Extraction-2004-Training-Data-V1.4/English/nwire/APW20001007.1745.0371";
         /*
        if(args.length != 1) {
            System.out.println("Need to enter a file stem as an argument");
            System.exit(0);
        }
         */
		/*
		String testFolderString = "test1";
		File testFolder = new File(testFolderString);
		String[] files = testFolder.list();
		//System.out.println(files[0]);
		Set<String> cleanFileNames = new HashSet<String>();
		for (String filename: files){
			 //System.out.println(filename);
			 String cleanFilename = filename.split("\\.")[0]+"."+filename.split("\\.")[1]+"."+filename.split("\\.")[2];
			 cleanFileNames.add(cleanFilename.split("-")[0]);						 
		}
		BufferedWriter bw = new BufferedWriter(new FileWriter("output"));
		System.out.println(cleanFileNames.toString());
		for (String f :cleanFileNames){
			GlobalDoc d = DataLoader.readACEData2(testFolderString + "/" + f);
			d.process();
			
			
			bw.write(d.convertToRelationFeatures());
			
			//System.out.println(d.convertToRelationFeatures());			
			//System.out.println("End");
		}
		bw.close();*/
		//System.out.println(Constants.coarseEntityList.toString());
		//System.out.println(Constants.POSList.toString());
        //String file = "APW20001001.2021.0521";            
		
		
		/*LexManager l = new LexManager();
		Map<String,Double> M = new HashMap<String,Double>();
		M.put("Hello", 1.0);
		M.put("Hello2", 2.0);
		M.put("Hello3", 3.0);
		FeatureVector v = l.convertRawFeaMap2LRFeatures(M);
		System.out.println(v.toString());
		M.put("Hello4",4.0);
		v = l.convertRawFeaMap2LRFeatures(M);
		System.out.println(v.toString());
		M.remove("Hello3");
		v = l.convertRawFeaMap2LRFeatures(M);
		System.out.println(v.toString());
		*/
		
		
		
		CCM c = new CCM();
		//c.trainLI("test2");
		//c.crossValidate("test2");
		//c.trainLI("test2", "10", "1");
		MultiClassModel model = c.trainLIPerceptronNative("test2", 1.0, 5.0);
		model.toString();
		System.out.println("Ended");
		
	}

}
