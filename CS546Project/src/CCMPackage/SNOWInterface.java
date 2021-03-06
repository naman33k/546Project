package CCMPackage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import edu.illinois.cs.cogcomp.illinoisRE.common.Constants;

public class SNOWInterface {
	public static String SNOWDIR = "/home/shalmoli/Desktop/CS546/Snow_v3.2";
	public static String PATH_TO_PROJECT = "/home/shalmoli/Desktop/CS546/java_code/546Project/CS546Project/";
		
	public static void learnSNOW(String trainDataFileName, String snowModelFileName) throws IOException, InterruptedException
	{
		int maxRelationNum = Labels.numRelationFineLabels() - 1;
		
		String command = SNOWDIR + "/snow" + " -train -I " +  PATH_TO_PROJECT+trainDataFileName 
				+ " -F " + PATH_TO_PROJECT+snowModelFileName + " -P :0-" + maxRelationNum;
//		/System.out.println(command);
		Process p = Runtime.getRuntime().exec(command);
		int exit_code = p.waitFor();
		System.out.println("learnSnow : " + exit_code);
		
	}
	
	public static double[][] getSNOWScores(String testDataFileName, String snowScoreFileName, 
			String snowModelFileName, int numTestData) throws IOException, InterruptedException
	{
		//String snowScoreFileName = "/home/shalmoli/Desktop/CS546/IO_files/output";//testFolder + SNOW_SCORE_FILE_SUFFIX;
				
		String command = SNOWDIR + "/snow" + " -test -I " +  PATH_TO_PROJECT+testDataFileName 
				+ " -F " + PATH_TO_PROJECT+snowModelFileName + " -o allactivations -R " + PATH_TO_PROJECT+snowScoreFileName;
		//System.out.println(command);
		Process p = Runtime.getRuntime().exec(command);
		int exit_code = p.waitFor();
		System.out.println("getSNOWScores : "+ exit_code);
		
		int maxRelationNum = Labels.numRelationFineLabels();
		
		double SNOWScores[][] = new double[numTestData][maxRelationNum];
		/*(CLASS NUM)(:)(WHITESPACE)(DIGITS.DIGITS)(WHITESPACE)(DIGITS.DIGITS)(WHITESPACE)(DIGITS.DIGITS)(\n)*/
		//String pattern = "(\\d+)(:)(\\s+)(-?\\d+\\.?\\d*e?-?\\d*)(\\s+)(-?\\d+.?\\d*e?-?\\d*)(\\s+)(-?\\d+.?\\d*e?-?\\d*)(.*)";
		String pattern = "(\\d+)(:)(\\s+)(-?\\d+\\.?\\d*e?-?\\d*)(\\s+)(-?\\d+\\.?\\d*e?-?\\d*)(\\s+)(-?\\d+\\.?\\d*e?-?\\d*)(.*)";
		
		BufferedReader br = new BufferedReader(new FileReader(snowScoreFileName));
		String line;
		int exampleNum = 0;
		while ((line = br.readLine()) != null) {
			/* Process the ouptut block of lines for each example */
			
			if(line.startsWith("Example")) {				
				for(int i = 0; i < maxRelationNum; i++) {
					line = br.readLine();
					
					line=line.replaceAll(pattern, "$1 $8");
					String[] class_score= line.split(" ");
					System.out.println(line);
					SNOWScores[exampleNum][Integer.parseInt(class_score[0])] = Double.parseDouble(class_score[1]);
		
				}
				exampleNum++;
			}
			
		}
		
		return SNOWScores;
	}	

}
