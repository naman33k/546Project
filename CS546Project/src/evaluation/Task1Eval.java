package evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.illinois.cs.cogcomp.illinoisRE.data.DataLoader;
import edu.illinois.cs.cogcomp.illinoisRE.data.GlobalDoc;

public class Task1Eval {
	
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

        Task1Eval eval = new Task1Eval(fname, path);
        eval.evaluate();
    }
	
	public Task1Eval(String filename) {
		fname = filename;
	}

	public Task1Eval(String filename, String path) {
		fname = filename;
		this.path = path;
	}

	//reads in output file and prints statistics
	//output file contains filename, relationLabel, rID, SID
	public void evaluate() {
		
		ArrayList<String> lines = Common.readLines(fname);
		//HashSet<String> hash = new HashSet<String>();
		HashMap<String, GlobalDoc> hash = new HashMap<String, GlobalDoc>();
		
		String prevfile = "";
		GlobalDoc d = null;
		
		for(int i = 0; i < lines.size(); i++) {
			String[] l = lines.get(i).split(",");
			String filename = path + l[0].trim();
			String rel = l[1].trim();
			String m1 = l[2].trim();
			String m2 = l[3].trim();
			String sID = l[4].trim();
			
			d = hash.get(filename);
			if(d == null) {
				d = DataLoader.readACEData2(filename);
				d.process();
				hash.put(filename, d);
			}
			
			StatisticsRelation.evaluate(d, rel, m1, m2, sID);
		}

		StatisticsRelation.outputStats(hash);
		//ArrayList<String> list = new ArrayList<String>(hash);
		//for(String s: list) {
		//	System.out.println(s);
		//}
	}
}
