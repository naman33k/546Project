package CCMPackage;

import edu.illinois.cs.cogcomp.indsup.learning.FeatureVector;
import edu.illinois.cs.cogcomp.indsup.learning.LexManager;
import edu.illinois.cs.cogcomp.indsup.learning.WeightVector;

public class Learner {
	public static MultiClassModel trainPercepetronMultiClassOvA(FeatureVector[] fv,Integer[] labels, double regParam, double stop_param, LexManager m, int numClasses ){
		WeightVector[] model = new WeightVector[numClasses];
		for(int i=0;i<numClasses;i++){
			model[i] = trainPerceptronSingleClass(fv, regParam, stop_param, m, numClasses,i,labels);			
		}
		return new MultiClassModel(model);
		
	}
	public static WeightVector trainPerceptronSingleClass(FeatureVector[] fv, double regParam, double stop_param, LexManager m, int numClasses,int classLabel, Integer labels[] ){
		WeightVector wv = new WeightVector(m.totalNumofFeature());
		Boolean classified = false;
		int[] y = new int[labels.length];
		for(int i=0;i<y.length;i++){
			y[i] = (classLabel==labels[i]) ? 1 : -1;
		}
		int iter=0;
		while((!classified) && iter<stop_param ){
			int updated = 0;
			for(int i=0;i<fv.length;i++){		
				double valPredicted = wv.dotProduct(fv[i]);
				int labelPredicted = (valPredicted >=0) ? 1 : -1;
				if(labelPredicted != y[i]) {
					wv.addSparseFeatureVector(fv[i], regParam); 
					updated++;
				}
				
			}
			System.out.println(updated);
			classified = updated == 0;
			iter++;
		}		
		return wv;
	}
}
