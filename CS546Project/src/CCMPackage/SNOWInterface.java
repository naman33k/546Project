package CCMPackage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import edu.illinois.cs.cogcomp.illinoisRE.common.Constants;

public class SNOWInterface {
	public static String SNOWDIR = "/home/shalmoli/Desktop/CS546/Snow_v3.2";
		
	public static void learnSNOW(String trainDataFileName, String snowModelFileName) throws IOException
	{
		int maxRelationNum = Constants.coarseRelationList.size() - 1;
		
		String command = SNOWDIR + "/snow" + " -train -I " +  trainDataFileName 
				+ " -F " + snowModelFileName + " -P :0-" + maxRelationNum;
		Runtime.getRuntime().exec(command);
		
	}
	
	public static double[][] getSNOWScores(String testDataFileName, String snowScoreFileName, 
			String snowModelFileName, int numTestData) throws IOException
	{
		//String snowScoreFileName = "/home/shalmoli/Desktop/CS546/IO_files/output";//testFolder + SNOW_SCORE_FILE_SUFFIX;
				
		String command = SNOWDIR + "/snow" + " -test -I " +  testDataFileName 
				+ " -F " + snowModelFileName + " -o allactivations -R " + snowScoreFileName;
		Runtime.getRuntime().exec(command);
		
		int maxRelationNum = Constants.coarseRelationList.size() - 1;
		
		double SNOWScores[][] = new double[numTestData][maxRelationNum];
		/*(CLASS NUM)(:)(WHITESPACE)(DIGITS.DIGITS)(WHITESPACE)(DIGITS.DIGITS)(WHITESPACE)(DIGITS.DIGITS)(\n)*/
		String pattern = "(\\d+)(:)(\\s+)(\\d+.?\\d*)(\\s+)(\\d+.?\\d*)(\\s+)(\\d+.?\\d*)(.*)";
		
		BufferedReader br = new BufferedReader(new FileReader(snowScoreFileName));
		String line;
		int exampleNum = 0;
		while ((line = br.readLine()) != null) {
			/* Process the ouptut block of lines for each example */
			
			if(line.startsWith("Example")) {				
				for(int i = 0; i <= maxRelationNum; i++) {
					line = br.readLine();
					
					line=line.replaceAll(pattern, "$1 $6");
					String[] class_score= line.split(" ");
					
					SNOWScores[exampleNum][Integer.parseInt(class_score[0])] = Double.parseDouble(class_score[1]);
		
				}
				exampleNum++;
			}
			
		}
		
		return SNOWScores;
	}	

}
