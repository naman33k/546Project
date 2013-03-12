package evaluation;

import java.util.ArrayList;
import java.util.HashSet;

import edu.illinois.cs.cogcomp.illinoisRE.data.DataLoader;
import edu.illinois.cs.cogcomp.illinoisRE.data.DataSentence;
import edu.illinois.cs.cogcomp.illinoisRE.data.GlobalDoc;
import edu.illinois.cs.cogcomp.illinoisRE.data.SemanticRelation;


public class Main1 {
	
	//tests evaluation
	public static void main(String[] args) {
		
		//creates sample output and evaluates statistics on it.
		
		if(args.length < 1 || args.length > 2) {
            System.out.println("Must enter at least 1 argument. See README"); System.exit(0);
        }
   
		String list = args[0];
        String path = "";
        if(args.length > 1) {
            path = args[1];
        }

		//String list = "/home/wieting2/filelist.txt";
		//String path = "/home/wieting2/";
		ArrayList<String> filenames = Common.readLines(list);
		ArrayList<String> output = new ArrayList<String>();
		HashSet<String> hash = new HashSet<String>();
		
		int count = 0;
		for(String s: filenames) {
			
			if(count == 80)
				break;
			
			int i = getLastChar(s);
			String filename = i==-2 ? "" : s.substring(0,i+1);
			if(!hash.contains(filename) && filename.length() > 0) {
				GlobalDoc d = DataLoader.readACEData2(path+filename);
				try {
					d.process();
					hash.add(filename);
					//Experimenter.add(d);
					output.addAll(getOutput(d.sentences, filename));
					count ++;
				}
				catch(Exception e) {}
			}
		}
		
		//Experimenter.experiment();
		
		//for(String s: output) {
		//	System.out.print(s);
		//}
		
		Common.writeLines(output, "output.txt");
		
		Task1Eval eval = new Task1Eval("output.txt", path);
		eval.evaluate();
	}
	
	public static ArrayList<String> getOutput(ArrayList<DataSentence> sentences, String filename) {
		
		ArrayList<String> output = new ArrayList<String>();
		
		for(DataSentence s: sentences) {
			for(SemanticRelation r : s.relations) {
				String line = filename + ", " + r.getFineLabel() + ", " + r.getM1().getId() + ", "+ r.getM2().getId() + ", " + s.sentence.getSentenceId();
				output.add(line+"\n");
			}
		}
		
		return output;
	}
	
	public static int getLastChar(String s) {
		int i = s.indexOf(".sgm");
		
		return i - 1;
	}

}
