package edu.illinois.cs.cogcomp.indsup.mc;

import edu.illinois.cs.cogcomp.indsup.inference.IInstance;
import edu.illinois.cs.cogcomp.indsup.learning.FeatureVector;


public class MultiClassInstance implements IInstance {
	public final FeatureVector base_fv;
	public final int base_n_fea;
	public final int number_of_class;
	
	public MultiClassInstance(int total_n_fea,int total_number_class,FeatureVector base_fv){
		this.base_fv = base_fv;
		this.base_n_fea = total_n_fea; 
		this.number_of_class = total_number_class;
	}

	@Override
	public double size() {
		return 1;
	}

}
