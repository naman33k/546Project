package CCMPackage;

import edu.illinois.cs.cogcomp.indsup.learning.LexManager;

public class CCM {
	public LexManager m;
	public static String defaultLexDump = "defaultLexDump";
	public CCM(){
		initializeLexManager(defaultLexDump);		
	}
	public CCM(String lexDump){
		initializeLexManager(lexDump);		
	}
	
	public void initializeLexManager(String lexDump){}
	
	public static double[] getCostWeights(){return null;}
	public static double[][] generateConstraintMatrix(){return null;}
	public static double[] generateConstraintWeights(){return null;}
	public static void generateIP(){}
	public static void solveIP(){}
	public static void inferLabels(){
			
	}
	public static void train(){
		
		
		
	}
	
	public static void test(){
		
		
	}
}
