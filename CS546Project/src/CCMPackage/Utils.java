package CCMPackage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import edu.illinois.cs.cogcomp.illinoisRE.data.DataSentence;
import edu.illinois.cs.cogcomp.illinoisRE.data.SemanticRelation;

public class Utils {
	public static void print2DArray(double[][] arr){
		
		for(int i=0;i<arr.length;i++){
			for(int j=0;j<arr[0].length;j++){
				System.out.print(arr[i][j] + " ");
			}
			System.out.println();
		}
	}
	
	public static String doubleArrayToCSString(double[] arr){
		String toRet = "";
		int len = arr.length;
		for(int i=0;i<len-1;i++){
			toRet += arr[i] + ",";
		}
		toRet += arr[len-1];
		return toRet;
	}
	public static String double2DArrayToCSString(double[][] arr){
		String toRet = "";
		int len = arr.length;		
		for(int i=0;i<len-1;i++){
			toRet += doubleArrayToCSString(arr[i]) + "\n";
		}
		toRet += doubleArrayToCSString(arr[len-1]);
		return toRet;
	}
	/* Function to remove the extensions from the file Names like .sgm, .xml etc */
	public static Set<String> removeFileExtensions(String folderName) 
	{
		File folder = new File(folderName);
		String[] files = folder.list();
		
		Set<String> fileNamesWOExtension = new HashSet<String>();
		for (String filename: files){			 
			 String cleanFilename = filename.split("\\.")[0]+"."+filename.split("\\.")[1]+"."+filename.split("\\.")[2];
			 fileNamesWOExtension.add(cleanFilename.split("-")[0]);						 
		}
		return fileNamesWOExtension;
	}
	public static SemanticRelation getRelation(String mid1, String mid2, DataSentence s) {
		
		ArrayList<SemanticRelation> relations = s.relations;
		
		SemanticRelation r = null;
		
		for(SemanticRelation sr : relations) {
			String m1 = sr.getM1().getId();
			String m2 = sr.getM2().getId();
			
			if(m1.equals(mid1) && m2.equals(mid2)) {
				r = sr;
			}
		}
		
		return r;
	}
	public static double[] sumDoubleArray(double[] a,double[] b){
		double[] sum = new double[a.length];
		for(int i=0;i<a.length;i++){
			sum[i]=a[i]+b[i];
		}
		return sum;
	}
}
