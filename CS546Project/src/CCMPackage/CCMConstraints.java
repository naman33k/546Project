package CCMPackage;
import java.util.Vector;

import edu.illinois.cs.cogcomp.illinoisRE.data.SemanticRelation;
public class CCMConstraints {
	public static int NUM_HARD_CONSTRAINTS_RELATION = 5;
	public double[] getSoftConstraintsVector(int numClasses, SemanticRelation sr){
		double[] sc = initZerosArray(numClasses);
		return sc;
	}
	public double[] initZerosArray(int size){
		double a[] = new double[size];
		for(int i=0;i<size;i++){
			a[i]=0.0;
		}
		return a;
	}
	public double[][] getHardConstraintsRelation(int numClasses, SemanticRelation sr){
		double[][] HardConstraints = new double[NUM_HARD_CONSTRAINTS_RELATION][numClasses];
		
		Vector<double[]> hc = new Vector<double[]>();
		/// Basic Hard Constraints
		
		/// all labels should sum to 1. Equality Constraint. 2 Constraints
		double hc1[] = initZerosArray(numClasses);				
		double hc2[] = initZerosArray(numClasses);
		for(int i=0;i<numClasses;i++){
			hc1[i] = 1.0;
			hc2[i] = -1.0;
		}
		hc1[numClasses] = -1.0;
		hc2[numClasses] = 1.0;
		
		hc.add(hc1);
		hc.add(hc2);
		
		/// all labels should be less than 1. numClasses constraints
		for(int i=0;i<numClasses;i++){
			double hct[] = initZerosArray(numClasses);
			hct[i] = 1.0;
			hct[numClasses] = -1.0;
			hc.add(hct);
		}
		/////////////////////////////////////////////////////////////////////////////////////////
	    
	   /// all labels should be greater than 0. numClasses constraints
	    for(int i=0;i<numClasses;i++){
			double hct[] = initZerosArray(numClasses);
			hct[i] = -1.0;
			hc.add(hct);
		}
	    /////////////////////////////////////////////////////////////////////////////////////////
	    
	    
	    /// Specific Hard Constraints - These will be tight constraints on relation - entity types
	    
	    
	    
	    ///////////////////////////////////////////////////////////////////////////////////////////
	    
	    double[][] ConstMatrix = new double[hc.size()][numClasses];
	    for(int i=0;i<hc.size();i++){
	    	for(int j=0;j<numClasses;j++){
	    		ConstMatrix[i][j] = hc.get(i)[j];
	    	}
	    }
	    return ConstMatrix;
	}
	
	
}
