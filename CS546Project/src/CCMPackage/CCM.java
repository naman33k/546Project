package CCMPackage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;



import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.illinoisRE.common.Constants;
import edu.illinois.cs.cogcomp.illinoisRE.data.DataLoader;
import edu.illinois.cs.cogcomp.illinoisRE.data.DataSentence;
import edu.illinois.cs.cogcomp.illinoisRE.data.GlobalDoc;
import edu.illinois.cs.cogcomp.illinoisRE.data.SemanticRelation;
import edu.illinois.cs.cogcomp.indsup.learning.FeatureVector;
import edu.illinois.cs.cogcomp.indsup.learning.JLISModelIOManager;
import edu.illinois.cs.cogcomp.indsup.learning.LexManager;
import edu.illinois.cs.cogcomp.indsup.learning.WeightVector;
import edu.illinois.cs.cogcomp.indsup.mc.LabeledMulticlassData;
import edu.illinois.cs.cogcomp.indsup.mc.MultiClassSparseLabeledDataReader;
import edu.illinois.cs.cogcomp.indsup.mc.MultiClassTrainer;
import edu.illinois.cs.cogcomp.indsup.mc.MulticlassModel;
import edu.illinois.cs.cogcomp.indsup.mc.main.AllTest;
import edu.illinois.cs.cogcomp.indsup.util.JLISUtils;

public class CCM {
	public LexManager m;
	public static String defaultLexDump = "defaultLexDump";
    public static String SNOWDIR = "/home/shalmoli/Desktop/CS546/Snow_v3.2";
	
	public static String FEATURE_FILE_SUFFIX =  "TrainResults" + "/" +"features"; 
	public static String SNOW_MODEL_FILE_SUFFIX =  "TrainResults" + "/" +"snowModel.net";
	public static String SNOW_SCORE_FILE_SUFFIX =  "TrainResults" + "/" +"snowScore.out";
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
	
	public String generateFeaturesFileTrain(String CorpusFolder) throws IOException{
		String outputfile = CorpusFolder + FEATURE_FILE_SUFFIX;
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
	
	public void learnSNOW(String CorpusFolder) throws IOException
	{
		String snowModelFileName = CorpusFolder + SNOW_MODEL_FILE_SUFFIX;
		String trainDataFileName = CorpusFolder + FEATURE_FILE_SUFFIX;
		int maxRelationNum = Constants.coarseEntityList.size() - 1;
		
		String command = SNOWDIR + "/snow" + " -train -I " +  trainDataFileName 
				+ " -F " + snowModelFileName + " -P :0-" + maxRelationNum;
		Runtime.getRuntime().exec(command);
		
	}
	
	public double[][] getSNOWScores(String testFolder, String snowModelFileName, int numTestData) throws IOException
	{
		//String snowScoreFileName = "/home/shalmoli/Desktop/CS546/IO_files/output";//testFolder + SNOW_SCORE_FILE_SUFFIX;
		String snowScoreFileName = testFolder + SNOW_SCORE_FILE_SUFFIX;
		String testDataFileName = testFolder + FEATURE_FILE_SUFFIX;
		
		String command = SNOWDIR + "/snow" + " -test -I " +  testDataFileName 
				+ " -F " + snowModelFileName + " -o allactivations -R " + snowScoreFileName;
		Runtime.getRuntime().exec(command);
		BufferedReader br = null;
		
		int maxRelationNum = Constants.coarseEntityList.size() - 1;
		
		double SNOWScores[][] = new double[numTestData][maxRelationNum];
		/*(CLASS NUM)(:)(WHITESPACE)(DIGITS.DIGITS)(WHITESPACE)(DIGITS.DIGITS)(WHITESPACE)(DIGITS.DIGITS)(\n)*/
		String pattern = "(\\d+)(:)(\\s+)(\\d+.?\\d*)(\\s+)(\\d+.?\\d*)(\\s+)(\\d+.?\\d*)(.*)";
		
		br = new BufferedReader(new FileReader(snowScoreFileName));
		String line;
		int exampleNum = 0;
		while ((line = br.readLine()) != null) {
			/* Process the ouptut block of lines for each example */
			
			if(line.startsWith("Example")) {
				System.out.print(exampleNum + " : ");
				for(int i = 0; i <= maxRelationNum; i++) {
					line = br.readLine();
					
					line=line.replaceAll(pattern, "$1 $6");
					String[] class_score= line.split(" ");
					
					SNOWScores[exampleNum][Integer.parseInt(class_score[0])] = Double.parseDouble(class_score[1]);
		
				}
				exampleNum++;
			}
			
		}
		
		return SNOWScores;
	}
	
	public Pair<FeatureVector[],Integer[]> fetchLabelsAndFV(String CorpusFolder){
		//String outputfile = CorpusFolder + "TrainResults" + "/" +"features";
		Set<String> fileNamesWOExtension = removeFileExtensions(CorpusFolder);
		//BufferedWriter bw = new BufferedWriter(new FileWriter(outputfile));
		List<FeatureVector> fvl = new ArrayList<FeatureVector>();
		List<Integer> labelsL = new ArrayList<Integer>();
		for (String f :fileNamesWOExtension){			
			GlobalDoc d = DataLoader.readACEData2(CorpusFolder + "/" + f);
			d.process();
			for(DataSentence ds:d.sentences){
				for(SemanticRelation r:ds.relations){
					fvl.add(FeatureExtractor.extractFeatureVectorRelation(r.getSentence(), r, m));
					labelsL.add(r.getLabel());
				}
			} 
			//System.out.println(relationFeatures);
			//bw.write(relationFeatures);
			//System.out.println(d.toString());
		}
		//bw.close();
		//return outputfile;
		FeatureVector[] fv = fvl.toArray(new FeatureVector[0]);
		Integer[] labels = labelsL.toArray(new Integer[0]);
		return new Pair<FeatureVector[], Integer[]>(fv, labels);
	}
	
	public MultiClassModel trainLIPerceptronNative(String CorpusFolder, double regParam, double stopParam){
		Pair<FeatureVector[],Integer[]> p = fetchLabelsAndFV(CorpusFolder);
		MultiClassModel model = Learner.trainPercepetronMultiClassOvA(p.getFirst(), p.getSecond(), regParam, stopParam, m, Constants.coarseRelationList.size());
		return model;
	}
	
	
	public void trainLI(String CorpusFolder,String C_st_str, String n_thread_str) throws Exception{
				
		String out = generateFeaturesFileTrain(CorpusFolder);
		
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
	
	public void trainLISnow(String CorpusFolder) throws IOException
	{				
		generateFeaturesFileTrain(CorpusFolder);
		learnSNOW(CorpusFolder);
	}
	
	public List<SemanticRelation> generateTestData(String testFolder){
			return null;
	}
	
	public void generateFeaturesFileTest(String testFolder, List<SemanticRelation> sr) throws IOException{
		String testDataFileName = testFolder + FEATURE_FILE_SUFFIX;
		BufferedWriter br = new BufferedWriter(new FileWriter(testDataFileName));
		for(SemanticRelation r:sr){
			br.write(r.convertToFeatureString(m));
		}
		br.close();
	}
	
	public void crossValidate(String CorpusFolder) throws Exception{
		String out = generateFeaturesFileTrain(CorpusFolder);
		//String trainDataFile = CorpusFolder + FEATURE_FILE_SUFFIX;;
		AllTest.crossValidationMultiClass(out, "10", "1", "5");
	}
	
	public void testRelationsSNOW(String testFolder, String snowModelFileName) throws Exception{
		List<SemanticRelation> sr = generateTestData(testFolder);
		generateFeaturesFileTest(testFolder, sr);
		double[][] scores = getSNOWScores(testFolder, snowModelFileName, sr.size());
		int numClasses = scores[0].length;
		int resultLabels[] = new int[sr.size()];
		int i=0;
		for(SemanticRelation r:sr){
			TestInstanceRelations ti = new TestInstanceRelations(r,scores[i]);
			resultLabels[i] = ti.test(numClasses);
			i++;
		}
		
		
	}
}
