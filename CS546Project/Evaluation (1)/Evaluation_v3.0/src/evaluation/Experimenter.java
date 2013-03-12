package evaluation;

import java.util.ArrayList;

import edu.illinois.cs.cogcomp.illinoisRE.data.DataSentence;
import edu.illinois.cs.cogcomp.illinoisRE.data.GlobalDoc;
import edu.illinois.cs.cogcomp.illinoisRE.data.Mention;
import edu.illinois.cs.cogcomp.illinoisRE.data.SemanticRelation;

// ns 7095
// nr 5777
// nm 27644

//nok 5 - mentions in a relation that are not in mention list -- could be elsewhere? Or just not included...
//ok 5772
//nr 5777

//after fixes
//7091
//5771
//27621
//0
//5771
//5771
//0
//27621

//2 mentions with exact names that made it through the process
public class Experimenter {

	public static ArrayList<GlobalDoc> list = new ArrayList<GlobalDoc>();
	
	public static void add(GlobalDoc d) {
		list.add(d);
	}
	
	public static void experiment() {
		
		int nsent = 0;
		int nr = 0;
		int nm = 0;
		for(int i = 0; i < list.size(); i++) {
			nsent += list.get(i).sentences.size();
			nr += list.get(i).relations.size();
			nm += list.get(i).mentions.size();
			for(DataSentence s: list.get(i).sentences) {
				//nr += s.relations.size();
				//nm += s.mentions.size();
			}
		}
		System.out.println(nsent);
		System.out.println(nr);
		System.out.println(nm);
		
		int nok = 0;
		int ok = 0;
		for(int i = 0; i < list.size(); i++) {
			for(DataSentence s: list.get(i).sentences) {
				for(SemanticRelation sr : s.relations) {
					String m1 = sr.getM1().getId();
					String m2 = sr.getM2().getId();
					boolean ok1 = false;
					boolean ok2 = false;
					for(Mention m : s.mentions) {
						if(m.getId().equals(m1))
							ok1= true;
						if(m.getId().equals(m2))
							ok2= true;
					}
					if(ok1 && ok2)
						ok++;
					else {
						nok++;
						investigate(list.get(i), s, sr);
					}
				}
			}
		}
		
		System.out.println(nok);
		System.out.println(ok);
		System.out.println(nok+ok);
		
		int bad = 0;
		int nbad = 0;
		for(int i = 0; i < list.size(); i++) {
			for(DataSentence s: list.get(i).sentences) {
				for(int m = 0; m < s.mentions.size(); m++) {
					int oldbad = bad;
					for(int l = 0; l < s.mentions.size(); l++) {
						if(l != m && s.mentions.get(m).getSurfaceString().equals(s.mentions.get(l).getSurfaceString())) {
							if(s.mentions.get(m).getEndTokenOffset() == s.mentions.get(l).getEndTokenOffset())
								if(s.mentions.get(m).getStartTokenOffset() == s.mentions.get(l).getStartTokenOffset()) {
									bad++;
									System.out.println(list.get(i).cdoc.getId() + " " + s.sentence.getSentenceId());
									System.out.println(s.mentions.get(m).toString());
									System.out.println(s.mentions.get(l).toString());
								}

						}
					}
					if(bad == oldbad)
						nbad++;
				}
			}
		}
		
		System.out.println(bad);
		System.out.println(nbad);
	}

	private static void investigate(GlobalDoc globalDoc, DataSentence s,
			SemanticRelation sr) {
		
		System.out.println(globalDoc.cdoc.getId() + " " + s.sentence.getSentenceId());
		
		String m1 = sr.getM1().getId();
		String m2 = sr.getM2().getId();
		boolean ok1 = false;
		boolean ok2 = false;
		
		ArrayList<Mention> ml = globalDoc.mentions;
		
		for(Mention m: ml) {
			if(m.getId().equals(m1))
				ok1 = true;
			if(m.getId().equals(m2))
				ok2 = true;
		}
		
		if(ok1 && ok2)
			System.out.println("FOUND ELSEWHERE");
		else {
			System.out.println(sr.getM1().toString());
			System.out.println(sr.getM2().toString());
		}
		
	}
	
}
