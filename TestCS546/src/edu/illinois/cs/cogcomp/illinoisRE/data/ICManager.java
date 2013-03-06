package edu.illinois.cs.cogcomp.illinoisRE.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.Relation;
import edu.illinois.cs.cogcomp.edison.sentences.SpanLabelView;
import edu.illinois.cs.cogcomp.illinoisRE.common.Constants;
import edu.illinois.cs.cogcomp.illinoisRE.common.Document;

public class ICManager {
	
	
	public static List<Constituent> getOnlyICMentions(Document doc) {
		
		SpanLabelView typedView = new SpanLabelView(Constants.IC_MENTION_VIEW, "IC_predict", doc.ta, 1.0, true);
		for(Constituent c: doc.ta.getView(Constants.PRED_MENTION_VIEW).getConstituents()) {
			String label = c.getLabel();
			
			if(label.compareTo("WEA:Exploding")==0) {
				typedView.addSpanLabel(c.getStartSpan(), c.getEndSpan(), "Bomb", c.getConstituentScore());
			}
			else if(label.compareTo("FAC:Building")==0) {
				typedView.addSpanLabel(c.getStartSpan(), c.getEndSpan(), "Building", c.getConstituentScore());
			}
			else if(label.compareTo("ORG:Commerical")==0) {
				typedView.addSpanLabel(c.getStartSpan(), c.getEndSpan(), "CommercialOrganization", c.getConstituentScore());
			}
			else if(label.compareTo("ORG:Political")==0) {
				typedView.addSpanLabel(c.getStartSpan(), c.getEndSpan(), "PoliticalParty", c.getConstituentScore());
			}
			else if(label.compareTo("ORG:Educational")==0) {
				typedView.addSpanLabel(c.getStartSpan(), c.getEndSpan(), "EducationalInstitution", c.getConstituentScore());
			}
			else if(label.compareTo("GPE:Municipality")==0) {
				typedView.addSpanLabel(c.getStartSpan(), c.getEndSpan(), "Municipality", c.getConstituentScore());
			}
			else if(label.compareTo("GPE:Nation")==0) {
				typedView.addSpanLabel(c.getStartSpan(), c.getEndSpan(), "NationState", c.getConstituentScore());
			}
			else if(label.compareTo("GPE:State-or-Province")==0) {
				typedView.addSpanLabel(c.getStartSpan(), c.getEndSpan(), "StateOrProvince", c.getConstituentScore());
			}
			else if(label.compareTo("ORG:Government")==0) {
				typedView.addSpanLabel(c.getStartSpan(), c.getEndSpan(), "GovernmentOrganization", c.getConstituentScore());
			}
			else if(label.compareTo("PER")==0) {
				typedView.addSpanLabel(c.getStartSpan(), c.getEndSpan(), "Person", c.getConstituentScore());
			}
			else if(label.compareTo("ORG:Terrorist")==0) {
				typedView.addSpanLabel(c.getStartSpan(), c.getEndSpan(), "TerroristOrganization", c.getConstituentScore());
			}
			
		}
		
		doc.ta.addView(Constants.IC_MENTION_VIEW, typedView);
		return doc.ta.getView(Constants.IC_MENTION_VIEW).getConstituents();
		
	}
	
	public static List<String> getICRelationPredictions(Document doc) {
		List<String> resultStrings = new ArrayList<String>();
		
		BinaryRelationView relationView = (BinaryRelationView)doc.getTextAnnotation().getView(Constants.PRED_RELATION_VIEW);
		List<Relation> relations = relationView.getRelations();
		HashMap<Relation, List<Pair<String, Double>>> scores = relationView.getScores();
		
		for(int i=0; i<relations.size(); i++) {
			Relation r = relations.get(i);
			
			String oneBestLabel = r.getRelationName();
			
			if(Constants.aceToIcRelationsRemap.containsKey(oneBestLabel)) {
				String icRelationLabel = Constants.aceToIcRelationsRemap.get(oneBestLabel);
				StringBuffer s = new StringBuffer();
				
				s.append("[");
				s.append(r.getSource().getSurfaceString());		// surface string of mention 1
				s.append("]");
				s.append(r.getSource().getStartSpan()+","+r.getSource().getEndSpan());	// start, end token index of mention 1
				s.append(" ");

				s.append(icRelationLabel);

				s.append(" [");
				s.append(r.getTarget().getSurfaceString());		// surface string of mention 2
				s.append("]");
				s.append(r.getTarget().getStartSpan()+","+r.getTarget().getEndSpan());	// start, end token index of mention 2

				// print out the entire probability distribution over all the possible (IC only) relation labels/classes
				if(scores.containsKey(r)) {
					List<Pair<String, Double>> relScores = scores.get(r);
					if(relScores!=null) {
						for(int j=0; j<relScores.size(); j++) {
							String label = relScores.get(j).getFirst();
							double score = relScores.get(j).getSecond().doubleValue();
							
							if(Constants.aceToIcRelationsRemap.containsKey(label)) {
								s.append(" "+ Constants.aceToIcRelationsRemap.get(label) +"|"+score);
							}
						}
					}
				}

				resultStrings.add(s.toString());
			}
		}
		
		return resultStrings;
	}
}