package evaluation;

import java.util.ArrayList;

import edu.illinois.cs.cogcomp.illinoisRE.data.DataSentence;
import edu.illinois.cs.cogcomp.illinoisRE.data.GlobalDoc;
import edu.illinois.cs.cogcomp.illinoisRE.data.Mention;
import edu.illinois.cs.cogcomp.illinoisRE.data.SemanticRelation;

public class StatisticsMention {
	
	//tp tn fp fn
	public static int[][] stats = new int[7][4];

	//calculates statistics
	public static void evaluate(GlobalDoc d, String rel, String rid, String sid) {
		int sID = Integer.parseInt(sid);
		DataSentence s = d.sentences.get(sID);
		if(s.sentence.getSentenceId() != sID) {
			for(DataSentence sent: d.sentences)
				if(sent.sentence.getSentenceId() == sID)
					s=sent;
		}
		
		ArrayList<Mention> mentions = s.mentions;
		
		for(Mention m: mentions) {
			if(m.getId().equals(rid)) {
				String gold = m.getSC();
				String pred = rel;
				int gi = mapStoI(gold);
				int ri = mapStoI(pred);
				
				if(gi == ri) {
					stats[gi][0]++;
					for(int i =0; i < stats.length; i++) {
						if(i != gi) {
							stats[i][1]++;
						}
					}
				}
				else {
					stats[gi][3]++;
					stats[ri][2]++;
				}
			}
		}
	}
	
	//prints statistics
	public static void outputStats() {
		
		//acc, prec, rec, f1
		float[][] scores = new float[7][4];
		
		for(int i = 0; i < scores.length; i++) {
			scores[i][0]= getAcc(stats[i][0], stats[i][1], stats[i][2], stats[i][3]);
			scores[i][1]= getPrec(stats[i][0], stats[i][1], stats[i][2], stats[i][3]);
			scores[i][2]= getRec(stats[i][0], stats[i][1], stats[i][2], stats[i][3]);
			scores[i][3] = getF1(scores[i][1], scores[i][2]);
		}
		
		System.out.println("Results: Mentions");
		System.out.printf("%s %s %s %s %s\n", "MentionType", "Acc.", "Prec.", "Rec.", "F1");
		for(int i = 0; i < scores.length; i++) {
			System.out.printf("%s %f %f %f %f\n", mapItoS(i), scores[i][0], scores[i][1], scores[i][2], scores[i][3]);
		}
		
		float total = 0;
		int size = 0;
		for(int i = 0; i < scores.length; i++) {
			if(!new Double(scores[i][3]).isNaN()) {
				total += scores[i][3];
				size++;
			}
		}
		
		System.out.println("Average F1: " + total/size);
	}
	
	public static float getAcc(int tp, int tn, int fp, int fn){
		return ((float) tp + tn) / (tp + tn + fp + fn);
	}
	
	public static float getPrec(int tp, int tn, int fp, int fn){
		return ((float) tp) / (tp + fp);
	}
	
	public static float getRec(int tp, int tn, int fp, int fn){
		return ((float) tp) / (tp + fn);
	}
	
	public static float getF1(float prec, float rec){
		return 2*prec*rec / (prec + rec);
	}
	
	public static String mapItoS(int i) {
		
		String output = "";
		
		/*
			GPE
			PER
			FAC
			WEA
			VEH
			LOC
			ORG
		*/

		
		switch (i) {
		case 0: output = "GPE";
			break;
		case 1: output = "PER";
			break;
		case 2: output = "FAC";
			break;
		case 3: output = "WEA";
			break;
		case 4: output = "VEH";
			break;
		case 5: output = "LOC";
			break;
		case 6: output = "ORG";
			break;
		}
		
		if(output.equals("")) {
			throw new IllegalArgumentException("Label must be one of the defined types");
		}
		
		return output;
	}
	
	public static int mapStoI(String s) {

		int output = -1;
		
		//no switch statement in java 6 :( need java 7.
		if(s.equals("GPE")) output = 0;
		else if(s.equals("PER")) output = 1;
		else if(s.equals("FAC")) output = 2;
		else if(s.equals("WEA")) output = 3;
		else if(s.equals("VEH")) output = 4;
		else if(s.equals("LOC")) output = 5;
		else if(s.equals("ORG")) output = 6;
		
		if(output == -1) {
			throw new IllegalArgumentException("Label must be one of the defined types");
		}
		
		return output;
	}
}
