package CCMPackage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.illinois.cs.cogcomp.edison.sentences.Sentence;
import edu.illinois.cs.cogcomp.illinoisRE.data.Mention;
import edu.illinois.cs.cogcomp.illinoisRE.data.SemanticRelation;
import edu.illinois.cs.cogcomp.indsup.learning.FeatureVector;
import edu.illinois.cs.cogcomp.indsup.learning.LexManager;

public class FeatureExtractor {
	
	public static char FEATURE_TYPE_DELIMITER = '_';
	public static char VALUE_DELIMITER = '_';
	
	
	public static Set<String> getBagOfWords(List<String> listTokens)
	{
		
		Set<String> bagOfWords = new HashSet<String>(listTokens) ;
		/*ArrayList<String> bagOfWordsAsList = new ArrayList<String>(bagOfWords);
		Collections.sort(bagOfWordsAsList);
		return bagOfWordsAsList.toString();*/
		return bagOfWords;
	}
	
	/**
	 ** WM2: bag-of-words in M2
		HM2: head word of M2
		HM12: combination of HM1 and HM2
		WBNULL: when no word in between
		WBFL: the only word in between when only
		one word in between
		WBF: first word in between when at least two
		words in between
		WBL: last word in between when at least two
		words in between
		WBO: other words in between except first and
		last words when at least three words in between
		BM1F: first word before M1
		BM1L: second word before M1
		AM2F: first word after M2
		AM2L: second word after M2
        WBON: number of words in between
	 ** 
	 */
	
	public static void addWordFeatures(Sentence sentence, Mention M1, Mention M2,Map<String,Double> featureMap)	{
		/* Handle the case where the first argument comes after second argument */
		int M1_end, M2_start, M2_end, M1_start ;
		if(M1earlier(M1,M2)) {
			M1_start = M1.getStartTokenOffset();
			M1_end = M1.getEndTokenOffset();
			M2_start = M2.getStartTokenOffset();
			M2_end = M2.getEndTokenOffset();
		}
		else {
			M1_start = M2.getStartTokenOffset();
			M1_end = M2.getEndTokenOffset();			
			M2_start = M1.getStartTokenOffset();
			M2_end = M1.getEndTokenOffset();
		}
		
		int startSpan = sentence.getStartSpan();
		int endSpan = sentence.getEndSpan();
		
		/*
		System.out.println("Testing");
		System.out.println(sentence.getText());
		for(int i=0;i<endSpan-startSpan;i++){
			System.out.println(sentence.getToken(endSpan-startSpan-1-i));
			
		}
		System.out.println("Finished Testing");
		*/
		
		for(String word : getBagOfWords(M1.getSurfaceStringTokens()))
			featureMap.put("WM1" + FEATURE_TYPE_DELIMITER + word, 1.0);
		
		featureMap.put("HM1" + FEATURE_TYPE_DELIMITER + M1.getHeadSurfaceString(), 1.0);
		
		for(String word : getBagOfWords(M2.getSurfaceStringTokens()))
			featureMap.put("WM2" + FEATURE_TYPE_DELIMITER + word, 1.0);
		
		featureMap.put("HM2" + FEATURE_TYPE_DELIMITER + M2.getHeadSurfaceString(), 1.0);
		
		featureMap.put("HM12" + FEATURE_TYPE_DELIMITER + M1.getHeadSurfaceString()+ 
				VALUE_DELIMITER + M2.getHeadSurfaceString(), 1.0);
		
		
		
		/* NO WORD in between */
		if(M2_start-M1_end <= 1){
			featureMap.put("WBNULL",1.0);
			featureMap.put("WBON"+ FEATURE_TYPE_DELIMITER +"0",1.0);
		}
		/* One WORD in between */
		else if(M2_start-M1_end == 2){						
			featureMap.put("WBFL" + FEATURE_TYPE_DELIMITER + 
					sentence.getToken(M1_end + 1 - startSpan),1.0);			
			featureMap.put("WBON"+ FEATURE_TYPE_DELIMITER +"1",1.0);
		}
		
		/* Atleast 2 words in between */
		else if(M2_start-M1_end >= 3) {			
			/*System.out.println("Hello");
			System.out.println(sentence.getText());
			System.out.println(startSpan);
			System.out.println(M1_end);
			System.out.println(sentence.getToken(endSpan-startSpan-1));
			System.out.println(Arrays.asList(sentence.getTokensInSpan(startSpan-startSpan, endSpan-startSpan)));			
			System.out.println(M2.getSurfaceString());
			System.out.println(M1.getSurfaceString());
			System.out.println(sentence.getToken(M2.getEndTokenOffset() - startSpan));
			System.out.println(sentence.getToken(M1.getEndTokenOffset() - startSpan));*/
			
			featureMap.put("WBF" + FEATURE_TYPE_DELIMITER + 
					sentence.getToken(M1_end + 1 - startSpan),1.0);
			
			featureMap.put("WBL" + FEATURE_TYPE_DELIMITER + 
					sentence.getToken(M2_start - 1 - startSpan),1.0);
			featureMap.put("WBON"+ FEATURE_TYPE_DELIMITER +"2",1.0);
		}
		
		/* Atleast 3 words in between */
		if(M2_start-M1_end >= 4) {			
			
			String[] tokensInBtwn = sentence.getTokensInSpan(M1_end + 2 - startSpan, M2_start - 1 - startSpan);
			List<String> listofTokens = Arrays.asList(tokensInBtwn);
			for(String word : getBagOfWords(listofTokens))
				featureMap.put("WBO" + FEATURE_TYPE_DELIMITER + word,1.0);			
			featureMap.remove("WBON"+ FEATURE_TYPE_DELIMITER +"2");
			int size = 2 + listofTokens.size();
			featureMap.put("WBON"+ FEATURE_TYPE_DELIMITER +size,1.0);
		}
		
		if (M1_start - startSpan >=1)
			featureMap.put("BM1F" + FEATURE_TYPE_DELIMITER + 
					sentence.getToken(M1_start -1 - startSpan),1.0);
		
		if (M1_start - startSpan >=2)
			featureMap.put("BM1L" + FEATURE_TYPE_DELIMITER + 
					sentence.getToken(M1_start -2 - startSpan),1.0);
		
		if(endSpan - M2_end >= 2){
			/*System.out.println("Hello");
			System.out.println(sentence.getText());
			System.out.println(sentence.getToken(endSpan-startSpan-1));
			System.out.println(Arrays.asList(sentence.getTokensInSpan(startSpan-startSpan, endSpan-startSpan)));			
			System.out.println(M2.getSurfaceString());
			System.out.println(M1.getSurfaceString());
			System.out.println(sentence.getToken(M2.getEndTokenOffset() - startSpan));
			System.out.println(sentence.getToken(M1.getEndTokenOffset() - startSpan));*/
			featureMap.put("AM2F" + FEATURE_TYPE_DELIMITER + 
					sentence.getToken(M2_end - startSpan + 1),1.0);}
		
		if(endSpan - M2_end >= 3)
			featureMap.put("AM2L" + FEATURE_TYPE_DELIMITER + 
					sentence.getToken(M2_end - startSpan + 2),1.0);
	}
	
	/*
	 * BOVP - Boolean Feature representing overlap
	 * WOVP - Overlapping Words 
	 * 
	 * 
	 */
	
	public static Boolean M1earlier(Mention M1,Mention M2){
		if(M1.getStartTokenOffset() < M2.getStartTokenOffset()) {
			return true;
		}
		else if(M1.getEndTokenOffset() < M2.getEndTokenOffset() && M1.getStartTokenOffset() == M2.getStartTokenOffset()){
			return true;
		}
		else {
			return false;
		}
		
	}
	
	public static void addOverlapFeatures(Sentence sentence, Mention M1, Mention M2,Map<String,Double> featureMap){
		int M1_end, M2_start, M2_end, M1_start ;
		if(M1earlier(M1,M2)) {
			M1_start = M1.getStartTokenOffset();
			M1_end = M1.getEndTokenOffset();
			M2_start = M2.getStartTokenOffset();
			M2_end = M2.getEndTokenOffset();
		}
		else {
			M1_start = M2.getStartTokenOffset();
			M1_end = M2.getEndTokenOffset();			
			M2_start = M1.getStartTokenOffset();
			M2_end = M1.getEndTokenOffset();
		}
		//detect overlap
		Boolean overlap = M1_end >= M2_start;
		
		int startSpan = sentence.getStartSpan();
		
		//int overlapEnd = (M1_end - M2_end > 0) ? M2_end : M1_end; 
		if(overlap){
			featureMap.put("BOVP", 1.0);
			for(int i=M2_start;i<=M1_end;i++){
				featureMap.put("WOVP"+FEATURE_TYPE_DELIMITER+sentence.getToken(i-startSpan), 1.0);				
			}									
		}
	}
	
	public static void addEntityTypeFeatures(Mention M1, Mention M2,Map<String,Double> featureMap)
	{
		featureMap.put("ET12" +FEATURE_TYPE_DELIMITER + M1.getSC()+ VALUE_DELIMITER + M2.getSC(), 1.0);
	}
	
	public static void addMentionLevelFeatures(Mention M1, Mention M2,Map<String,Double> featureMap)
	{
		featureMap.put("ML12" +FEATURE_TYPE_DELIMITER + M1.getMentionLevel()+ VALUE_DELIMITER + M2.getMentionLevel(), 1.0);
	}
	

	
	public static String extractFeaturesRelation(Sentence sent, SemanticRelation reln,LexManager l)
	{
		
		FeatureVector v = extractFeatureVectorRelation(sent, reln, l);
		int[] indices = v.getIdx();
		String featureString = "";
		for(int i=0;i<indices.length - 1;i++) featureString += indices[i] + ", ";
		featureString+= indices[indices.length-1] + ":";
		//String featureString = extractFeatureVectorRelation(sent, reln, l).toString().trim();
		//featureString=featureString.replaceAll(":1.0", ",");
		
		return featureString;
	}
	
	public static FeatureVector extractFeatureVectorRelation(Sentence sent, SemanticRelation reln,LexManager l)
	{
		Mention M1 = reln.getM1();
		Mention M2 = reln.getM2();
		Map<String,Double> featureMap = new HashMap<String,Double>(); 
		addWordFeatures(sent, M1, M2,featureMap);
		addEntityTypeFeatures(M1, M2,featureMap);
		addMentionLevelFeatures(M1, M2,featureMap);
		addOverlapFeatures(sent, M1, M2,featureMap);
		FeatureVector v = l.convertRawFeaMap2LRFeatures(featureMap);
		return v;
	}
	
	
}