package evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.illinois.cs.cogcomp.illinoisRE.data.DataLoader;
import edu.illinois.cs.cogcomp.illinoisRE.data.GlobalDoc;

public class Task2Eval {

	private String fname;
	private String path = "";
	
    public static void main(String[] args) {
		if(args.length < 1 || args.length > 2) {
            System.out.println("Must enter at least 1 argument. See README"); System.exit(0);
        }
        
        String path = "";
		String fname = args[0];
        if(args.length > 1) {
            path = args[1];
        }

        Task2Eval eval = new Task2Eval(fname, path);
        eval.evaluate();
    }

	public Task2Eval(String filename) {
		fname = filename;
	}

	public Task2Eval(String filename, String path) {
		fname = filename;
		this.path = path;
	}

	//reads in output file and prints statistics
	//output file contains filename, mentionLabel, mID, sID
	//or filename, relationLabel, rID, sID
	public void evaluate() {
		
		ArrayList<String> lines = Common.readLines(fname);
		HashMap<String, GlobalDoc> hash = new HashMap<String, GlobalDoc>();
		
		String prevfile = "";
		GlobalDoc d = null;
		
		for(int i = 0; i < lines.size(); i++) {
			String[] l = lines.get(i).split(",");
			String filename = path + l[0].trim();
			d = hash.get(filename);
			if(d == null) {
				d = DataLoader.readACEData2(filename);
				d.process();
				hash.put(filename, d);
			}
			
			if(l.length == 4)		
				StatisticsMention.evaluate(d, l[1].trim(), l[2].trim(), l[3].trim());
			else
				StatisticsRelation.evaluate(d, l[1].trim(), l[2].trim(), l[3].trim(), l[4].trim());
		}

		StatisticsRelation.outputStats(hash);
		StatisticsMention.outputStats();

	}
}
