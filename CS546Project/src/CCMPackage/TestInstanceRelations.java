package CCMPackage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import edu.illinois.cs.cogcomp.illinoisRE.data.SemanticRelation;

public class TestInstanceRelations {
	private SemanticRelation sr;
	private double[] costs;
	private double[] softCons;
	private double[][] HardCons;
	
	public static String PATH_LP_SOLVER = "/home/shalmoli/Desktop/CS546/LPSolver/nice_ilp.py";
	public static String PATH_TO_PROJECT = "/home/shalmoli/Desktop/CS546/java_code/546Project/CS546Project/";
			
	
	public TestInstanceRelations(SemanticRelation sr,double[] costs){
		this.sr = sr;
		this.costs = costs;
	}
	
	private void generateLP(int numClasses){
		CCMConstraints c = new CCMConstraints();
		softCons = c.getSoftConstraintsVector(numClasses, sr);
		HardCons = c.getHardConstraintsRelation(numClasses, sr);
		//Utils.print2DArray(HardCons);
	}
	
	private double[] optimize(int numClasses) throws IOException, InterruptedException{
		/// need to implement
		String scString = Utils.doubleArrayToCSString(Utils.sumDoubleArray(softCons,costs));
		//String scString = null;
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
		
		BufferedWriter bw = new BufferedWriter(new FileWriter("IO_files/lpFileTemp"));
		bw.write(scString+"\n\n"+AString+"\n\n"+BString);
		bw.close();
		String command = "python " + PATH_LP_SOLVER + " --input " + PATH_TO_PROJECT + "IO_files/lpFileTemp --output "
				+ PATH_TO_PROJECT + "IO_files/lpSolverOutput";
		//System.out.println(command);
		
		Process p = Runtime.getRuntime().exec(command);
		int exit_code = p.waitFor();
		//System.out.println("nice_ilp.py : " + exit_code);
		
		BufferedReader r = new BufferedReader(new FileReader("IO_files/lpSolverOutput"));
		String line = r.readLine().trim();
		r.close();
		String arr[] = line.split(";")[1].split(",");
		double toRet[] = new double[arr.length];
		for(int i=0;i<arr.length;i++) toRet[i] = Double.parseDouble(arr[i]); 
		//System.out.println(Utils.doubleArrayToCSString(toRet));
		//// now call BFR's function
		return toRet;
	}
	private int readLabel(double[] a){
		int ind = 0;
		double max = a[0];
		for(int i=1;i<a.length;i++){
			if(max<a[i]){
				ind = i;
				max = a[i];
			}
		}
		return ind;
	}
	
	public int test(int numClasses) throws IOException, InterruptedException{
		generateLP(numClasses);
		double[] a = optimize(numClasses);
		int label = readLabel(a);		
		return label;
	}
	
}
