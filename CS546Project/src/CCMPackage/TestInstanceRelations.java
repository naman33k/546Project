package CCMPackage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import edu.illinois.cs.cogcomp.illinoisRE.data.SemanticRelation;

public class TestInstanceRelations {
	private SemanticRelation sr;
	private double[] costs;
	private double[] softCons;
	private double[][] HardCons;
	public TestInstanceRelations(SemanticRelation sr,double[] costs){
		this.sr = sr;
		this.costs = costs;
	}
	
	private void generateLP(int numClasses){
		CCMConstraints c = new CCMConstraints();
		softCons = c.getSoftConstraintsVector(numClasses, sr);
		HardCons = c.getHardConstraintsRelation(numClasses, sr);
		Utils.print2DArray(HardCons);
	}
	
	private double[] optimize(int numClasses) throws IOException{
		/// need to implement
		String scString = Utils.doubleArrayToCSString(softCons);
		double[][] A = new double[HardCons.length][numClasses];
		for(int i=0;i<HardCons.length;i++){
			for(int j=0;j<numClasses;j++){
				A[i][j] = HardCons[i][j];
			}
		}
		double[] B = new double[HardCons.length];
		for(int i=0;i<HardCons.length;i++){
			B[i] = -1*HardCons[i][numClasses];
		}
		String AString = Utils.double2DArrayToCSString(A);
		String BString = Utils.doubleArrayToCSString(B);
		
		BufferedWriter bw = new BufferedWriter(new FileWriter("lpFileTemp"));
		bw.write(scString+"\n"+AString+"\n"+BString);
		bw.close();
		
		//// now call BFR's function
		return null;
	}
	private int readLabel(double[] a){
		int ind = 0;
		double min = a[0];
		for(int i=1;i<a.length;i++){
			if(min>a[i]){
				ind = i;
				min = a[i];
			}
		}
		return ind;
	}
	
	public int test(int numClasses) throws IOException{
		generateLP(numClasses);
		double[] a = optimize(numClasses);
		int label = readLabel(a);		
		return label;
	}
	
}
