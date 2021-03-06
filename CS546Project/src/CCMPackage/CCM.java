package CCMPackage;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;



import edu.illinois.cs.cogcomp.illinoisRE.common.Constants;
import edu.illinois.cs.cogcomp.illinoisRE.data.DataLoader;
import edu.illinois.cs.cogcomp.illinoisRE.data.DataSentence;
import edu.illinois.cs.cogcomp.illinoisRE.data.GlobalDoc;
import edu.illinois.cs.cogcomp.illinoisRE.data.Mention;
import edu.illinois.cs.cogcomp.illinoisRE.data.SemanticRelation;
import edu.illinois.cs.cogcomp.indsup.learning.FeatureVector;
import edu.illinois.cs.cogcomp.indsup.learning.LexManager;
import evaluation.StatisticsRelation;

public class CCM {
	public LexManager m;
	public static String defaultLexDump = "defaultLexDump";
    public static String SNOWDIR = "/home/shalmoli/Desktop/CS546/Snow_v3.2";
	
	public static String FEATURE_FILE_PREFIX =  "IO_files/features_"; 
	public static String SNOW_MODEL_FILE_NAME =  "IO_files" + "/" +"snowModel.net";
	public static String SNOW_SCORE_FILE_NAME =  "IO_files" + "/" +"snowScore.out";
	public static String OUTPUT_PATH =  "IO_files" + "/" ;
	
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
	
	public void generateFeaturesFileTrain(String CorpusFolder) throws IOException{
		String outputfile = FEATURE_FILE_PREFIX + "train";
		Set<String> fileNamesWOExtension = Utils.removeFileExtensions(CorpusFolder);
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputfile));
		for (String f :fileNamesWOExtension){			
			GlobalDoc d = DataLoader.readACEData2(CorpusFolder + "/" + f);
			d.process();
			
			String relationFeatures = d.convertToRelationFeatures(m);
			bw.write(relationFeatures);
			
		}
		bw.close();
		
	}	
	
	public void generateFeaturesFileTrainWithNULL(String CorpusFolder) throws IOException{
		String outputfile = FEATURE_FILE_PREFIX + "train";
		Set<String> fileNamesWOExtension = Utils.removeFileExtensions(CorpusFolder);
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputfile));
		for (String f :fileNamesWOExtension){			
			GlobalDoc d = DataLoader.readACEData2(CorpusFolder + "/" + f);
			d.process();
			for(DataSentence s: d.sentences) {
				ArrayList<Mention> mentionsInSent = s.mentions;
				int numMentions = mentionsInSent.size();
				/*System.out.println("Total Mentions" + numMentions);
				System.out.println("Total Relations" + s.relations.size());
				// Get the set of all mentions in a sentence and get list of 2 * NC2 relations
				int count = 0;*/
				for(int i= 0; i < numMentions; i++)
					for(int j = i+1 ; j < numMentions; j++) {
						Mention M1 = mentionsInSent.get(i);
						Mention M2 = mentionsInSent.get(j);
						SemanticRelation r = Utils.getRelation(M1.getId(), M2.getId(), s);
						
						if(r==null){
							r = new SemanticRelation(M1, M2, s.sentence, s.sentence.getSentenceId());
							//count++;
						}
						bw.write(r.convertToFeatureString(m));
						SemanticRelation r1 = Utils.getRelation(M2.getId(), M1.getId(), s);
						if(r1==null){
							r1 = new SemanticRelation(M2, M1, s.sentence, s.sentence.getSentenceId());						
							//count++;
						}
						bw.write(r1.convertToFeatureString(m));
					}
				
				//System.out.println("Total NULL Relations" + count);
			}															
		}
		bw.close();
		
	}
	
	
	public void trainLISnow(String CorpusFolder) throws IOException, InterruptedException
	{				
		System.out.println("Start : Train Features File Generation .... ");
		generateFeaturesFileTrainWithNULL(CorpusFolder);
		System.out.println("End : Train Features File Generation .... ");
		
		String trainDataFileName = FEATURE_FILE_PREFIX + "train";
		
		System.out.println("Start : Learn with SNOW .... ");
		SNOWInterface.learnSNOW(trainDataFileName, SNOW_MODEL_FILE_NAME);
		System.out.println("END : Learn with SNOW .... ");
	}
	
	
	public List<CustomPair<SemanticRelation, String>> getTestRelations(String testFolder) throws IOException
	{
		Set<String> fileNamesWOExtension = Utils.removeFileExtensions(testFolder);
		
		List<CustomPair<SemanticRelation, String>> testRelations = new ArrayList<CustomPair<SemanticRelation, String>>() ;
		
		for (String f :fileNamesWOExtension){			
			GlobalDoc d = DataLoader.readACEData2(testFolder + "/" + f);
			d.process();
			
			for(DataSentence s: d.sentences) {
				ArrayList<Mention> mentionsInSent = s.mentions;
				int numMentions = mentionsInSent.size();
				
				// Get the set of all mentions in a sentence and get list of 2 * NC2 relations
				for(int i= 0; i < numMentions; i++)
					for(int j = i+1 ; j < numMentions; j++) {
						Mention M1 = mentionsInSent.get(i);
						Mention M2 = mentionsInSent.get(j);
						SemanticRelation sr1 = new SemanticRelation(M1, M2, s.sentence, s.sentence.getSentenceId());
						SemanticRelation sr2 = new SemanticRelation(M2, M1, s.sentence, s.sentence.getSentenceId());
						
						testRelations.add(new CustomPair<SemanticRelation, String>(sr1,f));
						testRelations.add(new CustomPair<SemanticRelation, String>(sr2,f));
					}
			}
			
		}
			
		
		return testRelations;
	}
	
	public void generateFeaturesFileTest(List<CustomPair<SemanticRelation, String>> sr) throws IOException{
		String testDataFileName = FEATURE_FILE_PREFIX + "test";
		BufferedWriter bw = new BufferedWriter(new FileWriter(testDataFileName));
		for(CustomPair<SemanticRelation, String> pair : sr){
			bw.write(pair.getL().convertToFeatureString(m));
		}
		bw.close();
	}
	
	public void testRelationsSNOW(String testFolder, String testOutputFileName) throws Exception
	{
		String testDataFileName = FEATURE_FILE_PREFIX + "test";
		BufferedWriter bw = new BufferedWriter(new FileWriter(OUTPUT_PATH+testOutputFileName));
		
		System.out.println("Start : Get Test Relations ....");
		List<CustomPair<SemanticRelation, String>> sr = getTestRelations(testFolder);
		System.out.println("End : Get Test Relations ....");
		
		System.out.println("Start : Generate Test Features File ....");
		generateFeaturesFileTest(sr);
		System.out.println("End : Generate Test Features File ....");
		
		System.out.println("Start : Get SNOW scores for TEST ....");
		double[][] scores = SNOWInterface.getSNOWScores(testDataFileName, SNOW_SCORE_FILE_NAME, 
				SNOW_MODEL_FILE_NAME, sr.size());
		System.out.println("END : Get SNOW scores for TEST ....");
		
		int numClasses = scores[0].length;
		
		int i=0;
		
		System.out.println("Start : Predict Labels ....");
		
		//List<String> fineRelations = Constants.fineRelationList;
		for(CustomPair<SemanticRelation, String> pair : sr){
			SemanticRelation rel = pair.getL();
			TestInstanceRelations ti = new TestInstanceRelations(rel,scores[i]);
			int resultIndex = ti.test(numClasses);
			String line = pair.getR() + ", " + Labels.mapRelationItoS(resultIndex) + ", " + 
						rel.getM1().getId() + ", "+ rel.getM2().getId() + ", " + rel.getSentenceId();
			line += "\n";
			
			bw.write(line);
			
			i++;
			System.out.println("Predicting for " + i + "/" + sr.size());
		}
		System.out.println("End : Predict Labels ....");
		bw.close();
	}
}

/*
public static double[][] generateConstraintMatrix(){return null;}
public static double[] generateConstraintWeights(){return null;}
public static void generateIP(){}
public static void solveIP(){}
public static void inferLabels(){
		
}

public void crossValidate(String CorpusFolder) throws Exception{
		String out = generateFeaturesFileTrain(CorpusFolder);
		//String trainDataFile = CorpusFolder + FEATURE_FILE_SUFFIX;;
		AllTest.crossValidationMultiClass(out, "10", "1", "5");
	}
	
public void trainLI(String CorpusFolder,String C_st_str, String n_thread_str) throws Exception
	{
				
		generateFeaturesFileTrain(CorpusFolder);
		
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
		}
	}
public MultiClassModel trainLIPerceptronNative(String CorpusFolder, double regParam, double stopParam){
		Pair<FeatureVector[],Integer[]> p = fetchLabelsAndFV(CorpusFolder);
		MultiClassModel model = Learner.trainPercepetronMultiClassOvA(p.getFirst(), p.getSecond(), regParam, stopParam, m, Constants.coarseRelationList.size());
		return model;
	}
	
public Pair<FeatureVector[],Integer[]> fetchLabelsAndFV(String CorpusFolder){
		//String outputfile = CorpusFolhttps://github.com/naman33k/546Project.gitder + "TrainResults" + "/" +"features";
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
	public static double[] getCostWeighhttps://github.com/naman33k/546Project.gitts(MulticlassModel model){
		return null;	
		
		
	}	
*/