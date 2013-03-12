package edu.illinois.cs.cogcomp.indsup.mc;

import java.util.Map;

import edu.illinois.cs.cogcomp.indsup.inference.AbstractLossSensitiveStructureFinder;
import edu.illinois.cs.cogcomp.indsup.learning.IJLISModel;
import edu.illinois.cs.cogcomp.indsup.learning.JLISParameters;
import edu.illinois.cs.cogcomp.indsup.learning.WeightVector;

public class MulticlassModel implements IJLISModel{
	private static final long serialVersionUID = -2919690450966535216L;
	
	public MulticlassModel() {		
	}

	public WeightVector wv = null;
	public JLISParameters para = null;
	
	public int n_base_feature_in_train; // number of features appeared in the training time
	public Map<String, Integer> lab_mapping;
		
	public AbstractLossSensitiveStructureFinder s_finder = null;
	/**
	 * cost matrix: first dimension is gold lab, the second dimension is prediction lab
	 * cost_matrix[i][i] = 0
	 * cost_matrix[i][j] represents the cost of predicting j while the gold lab is i
	 */	
	public double[][] cost_matrix = null;
		
	public String[] getReverseMapping(){
		String[] reverse = new String[lab_mapping.size()];
		for(int i=0; i < reverse.length; i ++){
			reverse[i] = "";
		}
		for(String key:lab_mapping.keySet()){
			reverse[lab_mapping.get(key)]= key;
		}
		
		for(int i=0; i < reverse.length; i ++){
			assert !reverse[i].equals("");
		}
		return reverse;
	}
}
