package CCMPackage;

import edu.illinois.cs.cogcomp.indsup.learning.WeightVector;

public class MultiClassModel {
	WeightVector[] wv;
	public MultiClassModel(WeightVector[] wv){
		this.wv = wv;
	}
	public String toString(){
		String sr = "";
		for (WeightVector w : wv){
			sr += w.toString() + "\n";
		}
		return sr;
	}
}
