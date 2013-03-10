package CCMPackage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;



import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.illinoisRE.data.DataLoader;
import edu.illinois.cs.cogcomp.illinoisRE.data.GlobalDoc;
import edu.illinois.cs.cogcomp.indsup.learning.FeatureVector;
import edu.illinois.cs.cogcomp.indsup.learning.JLISModelIOManager;
import edu.illinois.cs.cogcomp.indsup.learning.LexManager;
import edu.illinois.cs.cogcomp.indsup.mc.LabeledMulticlassData;
import edu.illinois.cs.cogcomp.indsup.mc.MultiClassSparseLabeledDataReader;
import edu.illinois.cs.cogcomp.indsup.mc.MultiClassTrainer;
import edu.illinois.cs.cogcomp.indsup.mc.MulticlassModel;
import edu.illinois.cs.cogcomp.indsup.mc.main.AllTest;
import edu.illinois.cs.cogcomp.indsup.util.JLISUtils;

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
	    while(line != null){
	    	map.put(line.trim(), 1.0);	    	
	    	line = br.readLine();
	    }
	    FeatureVector s = m.convertRawFeaMap2LRFeatures(map);
	    br.close();
	}
	
	public static double[] getCostWeights(MulticlassModel model){
		return null;	
		
		
	}
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
	
	public String generateFeaturesFile(String CorpusFolder) throws IOException{
		String outputfile = CorpusFolder + "TrainResults" + "/" +"features";
		Set<String> fileNamesWOExtension = removeFileExtensions(CorpusFolder);
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputfile));
		for (String f :fileNamesWOExtension){			
			GlobalDoc d = DataLoader.readACEData2(CorpusFolder + "/" + f);
			d.process();
			String relationFeatures = d.convertToRelationFeatures(m); 
			//System.out.println(relationFeatures);
			bw.write(relationFeatures);
			//System.out.println(d.toString());
		}
		bw.close();
		return outputfile;
	}
	
	public void trainLI(String CorpusFolder,String C_st_str, String n_thread_str) throws Exception{
				
		String out = generateFeaturesFile(CorpusFolder);
		
		// This is model generation
		/*
		LabeledMulticlassData train = MultiClassSparseLabeledDataReader
				.readTrainingData(out);
		MulticlassModel model = MultiClassTrainer.trainMultiClassModel(
				Double.parseDouble(C_st_str), Integer.parseInt(n_thread_str),
				train);

		// This is saving the model
		
		String model_name = CorpusFolder + "TrainResults/trained"
				+ ".ssvm.model";
		JLISModelIOManager io = new JLISModelIOManager();
		io.saveModel(model, model_name);
		
		return model;
		*/
		/*for(int i=1;i<=m.totalNumofFeature();i++){
			System.out.println(m.getFeatureString(i));
		}*/
	}
	
	public void crossValidate(String CorpusFolder) throws Exception{
		String out = generateFeaturesFile(CorpusFolder);
		//String trainDataFile = CorpusFolder + "TrainResults" + "/" +"features";
		AllTest.crossValidationMultiClass(out, "10", "1", "5");
	}
	
	public void test(String test_name) throws Exception{
		//JLISModelIOManager io = new JLISModelIOManager();
		/*LabeledMulticlassData test = MultiClassSparseLabeledDataReader
				.readTestingData(test_name, model.lab_mapping,
						model.n_base_feature_in_train);
		Pair<int[], int[]> gold_pred = MultiClassTrainer.getPredictionResults(
				model, test);
		int[] pred = gold_pred.getSecond();
		int[] gold = gold_pred.getFirst();
		for(int i:pred){
			System.out.println(i);
		}
		System.out.println("\n GAP \n");
		for(int i:gold){
			System.out.println(i);
		}*/
	}
}
