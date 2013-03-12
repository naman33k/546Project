package evaluation;

import java.util.ArrayList;
import java.util.HashMap;

import edu.illinois.cs.cogcomp.illinoisRE.data.DataSentence;
import edu.illinois.cs.cogcomp.illinoisRE.data.GlobalDoc;
import edu.illinois.cs.cogcomp.illinoisRE.data.SemanticRelation;

public class StatisticsRelation {
	
	//tp tn fp fn
	public static int[][] stats = new int[24][4];
	public static ArrayList<Relation> foundRelations = new ArrayList<Relation>();
	
	//calculates statistics
	public static void evaluate(GlobalDoc d, String rel, String mid1, String mid2, String sid) {
		int sID = Integer.parseInt(sid);
		DataSentence s = d.sentences.get(sID);
		if(s.sentence.getSentenceId() != sID) {
			for(DataSentence sent: d.sentences)
				if(sent.sentence.getSentenceId() == sID)
					s=sent;
		}
		ArrayList<SemanticRelation> relations = s.relations;
		
		boolean notfound = true;
		for(SemanticRelation r: relations) {
			String rid = getRid(mid1, mid2, s);
			if(r.getId().equals(rid)) {
				foundRelations.add(new Relation(Integer.parseInt(sid), rid, d));
				String gold = r.getFineLabel();
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
					
					notfound = false;
				}
				else {
					System.out.println("E:"+d.cdoc.getId()+" "+sid+" "+rel+" "+mid1+" "+mid2+"\n\n\n\n\n\n");
					stats[gi][3]++;
					stats[ri][2]++;
				}
			}
		}
		
		if(notfound) { //must be null relation
			System.out.println("NF:"+d.cdoc.getId()+" "+sid+" "+rel+" "+mid1+" "+mid2+"\n\n\n\n\n\n");
			int gi = mapStoI("Null");
			int ri = mapStoI(rel);
			
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
	
	public static String getRid(String mid1, String mid2, DataSentence s) {
		
		ArrayList<SemanticRelation> relations = s.relations;
		
		String rid = "";
		
		for(SemanticRelation sr : relations) {
			String m1 = sr.getM1().getId();
			String m2 = sr.getM2().getId();
			
			if(m1.equals(mid1) && m2.equals(mid2)) {
				rid = sr.getId();
			}
		}
		
		return rid;
	}

	//prints statistics
	public static void outputStats(HashMap<String, GlobalDoc> hash) {
		
		//add false negative - inefficiently... but okay since small set
		/*ArrayList<GlobalDoc> docs = new ArrayList<GlobalDoc>(hash.values());
		for(GlobalDoc d : docs) {
			for(DataSentence s: d.sentences) {
				for(SemanticRelation sr : s.relations) {
					int i  = s.sentence.getSentenceId();
					String rID = sr.getId();
					boolean found = false;
					for(Relation r: foundRelations) {
						if(r.rId.equals(rID) && r.sId == i && r.d.equals(d))
							found = true;
					}
					if(!found) {
						String gold = sr.getFineLabel();
						int gi = mapStoI(gold);
						stats[gi][3]++;
					}
				}
			}
		}*/
		
		//acc, prec, rec, f1
		float[][] scores = new float[24][4];
		
		for(int i = 0; i < scores.length; i++) {
			scores[i][0]= getAcc(stats[i][0], stats[i][1], stats[i][2], stats[i][3]);
			scores[i][1]= getPrec(stats[i][0], stats[i][1], stats[i][2], stats[i][3]);
			scores[i][2]= getRec(stats[i][0], stats[i][1], stats[i][2], stats[i][3]);
			scores[i][3] = getF1(scores[i][1], scores[i][2]);
		}
		
		System.out.println("Results: Relations");
		System.out.printf("%s %s %s %s %s\n", "RelationType", "Acc.", "Prec.", "Rec.", "F1");
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
	EMP-ORG:Partner 1
	PHYS:Part-Whole 2
	EMP-ORG:Employ-Staff 3
	DISC 4
	PER-SOC:Family 5
	GPE-AFF:Other 6
	EMP-ORG:Member-of-Group 7
	EMP-ORG:Subsidiary 8
	PER-SOC:Other 9
	EMP-ORG:Employ-Undetermined 10
	PHYS:Near 11
	ART:Other 12+
	OTHER-AFF:Ethnic 13+
	PER-SOC:Business 14
	EMP-ORG:Employ-Executive 15
	OTHER-AFF:Ideology 16
	PHYS:Located 17
	ART:User-or-Owner 18
	EMP-ORG:Other 19
	GPE-AFF:Based-In 20
	OTHER-AFF:Other 21
	ART:Inventor-or-Manufacturer 22+
	GPE-AFF:Citizen-or-Resident 23*/

		
		switch (i) {
		case 0: output = "EMP-ORG:Partner";
			break;
		case 1: output = "PHYS:Part-Whole";
			break;
		case 2: output = "EMP-ORG:Employ-Staff";
			break;
		case 3: output = "DISC";
			break;
		case 4: output = "PER-SOC:Family";
			break;
		case 5: output = "GPE-AFF:Other";
			break;
		case 6: output = "EMP-ORG:Member-of-Group";
			break;
		case 7: output = "EMP-ORG:Subsidiary";
			break;
		case 8: output = "PER-SOC:Other";
			break;
		case 9: output = "EMP-ORG:Employ-Undetermined";
			break;
		case 10: output = "PHYS:Near";
			break;
		case 11: output = "ART:Other";
			break;
		case 12: output = "OTHER-AFF:Ethnic";
			break;
		case 13: output = "PER-SOC:Business";
			break;
		case 14: output = "EMP-ORG:Employ-Executive";
			break;
		case 15: output = "OTHER-AFF:Ideology";
			break;
		case 16: output = "PHYS:Located";
			break;
		case 17: output = "ART:User-or-Owner";
			break;
		case 18: output = "EMP-ORG:Other";
			break;
		case 19: output = "GPE-AFF:Based-In";
			break;
		case 20: output = "OTHER-AFF:Other";
			break;
		case 21: output = "ART:Inventor-or-Manufacturer";
			break;
		case 22: output = "GPE-AFF:Citizen-or-Resident";
			break;
		case 23: output = "Null";
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
		if(s.equals("EMP-ORG:Partner")) output = 0;
		else if(s.equals("PHYS:Part-Whole")) output = 1;
		else if(s.equals("EMP-ORG:Employ-Staff")) output = 2;
		else if(s.equals("DISC")) output = 3;
		else if(s.equals("PER-SOC:Family")) output = 4;
		else if(s.equals("GPE-AFF:Other")) output = 5;
		else if(s.equals("EMP-ORG:Member-of-Group")) output = 6;
		else if(s.equals("EMP-ORG:Subsidiary")) output = 7;
		else if(s.equals("PER-SOC:Other")) output = 8;
		else if(s.equals("EMP-ORG:Employ-Undetermined")) output = 9;
		else if(s.equals("PHYS:Near")) output = 10;
		else if(s.equals("ART:Other")) output = 11;
		else if(s.equals("OTHER-AFF:Ethnic")) output = 12;
		else if(s.equals("PER-SOC:Business")) output = 13;
		else if(s.equals("EMP-ORG:Employ-Executive")) output = 14;
		else if(s.equals("OTHER-AFF:Ideology")) output = 15;
		else if(s.equals("PHYS:Located")) output = 16;
		else if(s.equals("ART:User-or-Owner")) output = 17;
		else if(s.equals("EMP-ORG:Other")) output = 18;
		else if(s.equals("GPE-AFF:Based-In")) output = 19;
		else if(s.equals("OTHER-AFF:Other")) output = 20;
		else if(s.equals("ART:Inventor-or-Manufacturer")) output = 21;
		else if(s.equals("GPE-AFF:Citizen-or-Resident")) output = 22;
		else if(s.equals("Null")) output = 23;
		
		
		if(output == -1) {
			throw new IllegalArgumentException("Label must be one of the defined types");
		}
		
		return output;
	}
	
	public static class Relation {
		
		public int sId;
		public String rId;
		public GlobalDoc d;
		
		public Relation(int i, String rId, GlobalDoc d) {
			sId = i;
			this.rId = rId;
			this.d = d;
		}
	}
}
