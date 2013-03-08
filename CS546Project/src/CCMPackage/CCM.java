package CCMPackage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;



import edu.illinois.cs.cogcomp.illinoisRE.data.DataLoader;
import edu.illinois.cs.cogcomp.illinoisRE.data.GlobalDoc;
import edu.illinois.cs.cogcomp.indsup.learning.FeatureVector;
import edu.illinois.cs.cogcomp.indsup.learning.LexManager;
import edu.illinois.cs.cogcomp.indsup.mc.main.AllTest;

public class CCM {
	public LexManager m;
	public static String defaultLexDump = "defaultLexDump";
	public CCM() throws IOException{
		//initializeLexManager(defaultLexDump);		
		m = new LexManager();
	}
	public CCM(String lexDump) throws IOException{
		initializeLexManager(lexDump);		
	}
	
	public void initializeLexManager(String lexDump) throws IOException{
		
		///this is highly inefficient. There has to be a better way
		
		m = new LexManager();
	    BufferedReader br = new BufferedReader(new FileReader(lexDump));
	    String line = br.readLine();
	    Map<String,Double> map = new HashMap<String,Double>();
	    while(br != null){
	    	map.put(line.trim(), 1.0);	    	
	    }
	    FeatureVector s = m.convertRawFeaMap2LRFeatures(map);
	    br.close();
	}
	
	public static double[] getCostWeights(){return null;}
	public static double[][] generateConstraintMatrix(){return null;}
	public static double[] generateConstraintWeights(){return null;}
	public static void generateIP(){}
	public static void solveIP(){}
	public static void inferLabels(){
			
	}
	
	private Set<String> removeFileExtensions(String folderName) 
	{
		File folder = new File(folderName);
		String[] files = folder.list();
		
		Set<String> fileNamesWOExtension = new HashSet<String>();
		for (String filename: files){			 
			 String cleanFilename = filename.split("\\.")[0]+"."+filename.split("\\.")[1]+"."+filename.split("\\.")[2];
			 fileNamesWOExtension.add(cleanFilename.split("-")[0]);						 
		}
		return fileNamesWOExtension;
	}
	
	public void trainLI(String CorpusFolder) throws IOException{
		/// I have to write these functions :) :)		
		Set<String> fileNamesWOExtension = removeFileExtensions(CorpusFolder);
		BufferedWriter bw = new BufferedWriter(new FileWriter(CorpusFolder + "TrainResults" + "/" +"features"));
		for (String f :fileNamesWOExtension){			
			GlobalDoc d = DataLoader.readACEData2(CorpusFolder + "/" + f);
			d.process();
			String relationFeatures = d.convertToRelationFeatures(m); 
			//System.out.println(relationFeatures);
			bw.write(relationFeatures);
			//System.out.println(d.toString());
		}
		bw.close();
		/*for(int i=1;i<=m.totalNumofFeature();i++){
			System.out.println(m.getFeatureString(i));
		}*/
	}
	
	public void crossValidate(String CorpusFolder) throws Exception{
		trainLI(CorpusFolder);
		String trainDataFile = CorpusFolder + "TrainResults" + "/" +"features";
		AllTest.crossValidationMultiClass(trainDataFile, "10", "1", "5");
	}
	
	public static void test(){
		
		
	}
}
