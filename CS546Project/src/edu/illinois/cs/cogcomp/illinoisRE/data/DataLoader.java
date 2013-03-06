package edu.illinois.cs.cogcomp.illinoisRE.data;

import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.thrift.TException;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.edison.data.curator.TokenAligner;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.PredicateArgumentView;
import edu.illinois.cs.cogcomp.edison.sentences.Relation;
import edu.illinois.cs.cogcomp.edison.sentences.SpanLabelView;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.illinoisRE.common.Constants;
import edu.illinois.cs.cogcomp.illinoisRE.common.Document;
import edu.illinois.cs.cogcomp.illinoisRE.common.IOManager;
import edu.illinois.cs.cogcomp.illinoisRE.common.ResourceManager;
import edu.illinois.cs.cogcomp.illinoisRE.common.Util;
import edu.illinois.cs.cogcomp.illinoisRE.data.ace.ACEDataHandler;
import edu.illinois.cs.cogcomp.illinoisRE.data.CleanDoc;
import edu.illinois.cs.cogcomp.illinoisRE.data.ic.ICDataHandler;
import edu.illinois.cs.cogcomp.thrift.base.AnnotationFailedException;
import edu.illinois.cs.cogcomp.thrift.base.Labeling;
import edu.illinois.cs.cogcomp.thrift.base.ServiceUnavailableException;
import edu.illinois.cs.cogcomp.thrift.base.Span;

public class DataLoader {

	protected static MyCuratorClient curator = new MyCuratorClient(ResourceManager.getCuratorHost(), ResourceManager.getCuratorPort());
	
	public static List<CleanDoc> readACEDocumentsAnnotations(String masterfilename) throws FileNotFoundException {
		List<CleanDoc> docs = new ArrayList<CleanDoc>();
		ArrayList<String> files = IOManager.readLines(masterfilename);
		for (String file : files) {
			CleanDoc doc = readACEData(ResourceManager.getDataPath()+file);
			if (doc != null) { docs.add(doc); }
		}
		return docs;
	}

	public static List<Document> getICDocuments(String filename, String xmlDir, String srcDir) {
		List<Document> docs = new ArrayList<Document>();
		ArrayList<String> files = IOManager.readLines(filename);
		for (String file : files) {
			CleanDoc doc = readICData(file, xmlDir, srcDir);
			Document myDoc = annotateSourceText(doc);
			if (myDoc != null) { docs.add(myDoc); }
		}
		return docs;
	}
	public static List<Document> getACEDocuments(String masterfilename) throws FileNotFoundException {
		List<Document> docs = new ArrayList<Document>();
		ArrayList<String> files = IOManager.readLines(masterfilename);
		for (String file : files) {
			CleanDoc doc = readACEData(ResourceManager.getDataPath()+file);
			Document myDoc = annotateSourceText(doc);
			
			if (myDoc != null) { docs.add(myDoc); }
		}
		return docs;
	}
	public static Document getACEDocument(String filename) throws FileNotFoundException {
		CleanDoc doc = readACEData(filename);
		Document myDoc = annotateSourceText(doc);
		return myDoc;
	}
	
	public static CleanDoc getACEData(String filename) throws FileNotFoundException {
		CleanDoc doc = readACEData(filename);
		Document myDoc = annotateSourceText(doc);
		return doc;
	}

	private static CleanDoc readICData(String file, String xmlDir, String srcDir) {
		String xmlFile = xmlDir + "/" + file + ".gui.xml";
		String srcFile = srcDir + "/" + file + ".src.xml";
		CleanDoc doc = new CleanDoc();
		doc.setId(file);
		doc.setCorpusId("IC");

		ICDataHandler.readMentionRelationAnnotationInfo(xmlFile, doc);	// read relations and mentions from IC xml file
		ICDataHandler.addAnnotationsToFile(srcFile, doc);				// add the mention annotations (start, end tags) to the src text

		return doc;
	}
	public static CleanDoc readACEData(String file) {
		String xmlFile = file + ".apf.xml";
		String sgmFile = file + ".sgm";
		
		System.out.println("xmlFile:"+xmlFile);
		System.out.println("sgmFile:"+sgmFile);
		
		CleanDoc doc = new CleanDoc();
		doc.setId(file);
		if(file.indexOf("nwire")!=-1) {
			doc.setCorpusId("ace2004nwire");
		}
		else if(file.indexOf("bnews")!=-1) {
			doc.setCorpusId("ace2004bnews");
		}
		else if(file.indexOf("arabic_treebank")!=-1) {
			doc.setCorpusId("ace2004atb");
		}
		else if(file.indexOf("chinese_treebank")!=-1) {
			doc.setCorpusId("ace2004ctb");
		}
		else{
			doc.setCorpusId("default");			
		}

		
		ACEDataHandler.readMentionRelationAnnotationInfo(xmlFile, doc);	// read relations and mentions from ACE xml file
		ACEDataHandler.addAnnotationsToFile(sgmFile, doc);				// add the mention annotations (start, end tags) to the sgm text
		
		return doc;
	}
	
	public static GlobalDoc readACEData2(String file) {
		CleanDoc doc = readACEData(file);
		Document myDoc = annotateSourceText(doc);
		return new GlobalDoc(myDoc, doc);
	}
	
	protected static SpanLabelView alignLabelingToSpans(String viewName, TextAnnotation ta, Labeling spanLabeling) {
		List<Span> labels = spanLabeling.getLabels();
		double score = spanLabeling.getScore();
		String generator = spanLabeling.getSource();

		SpanLabelView view = new SpanLabelView(viewName, generator, ta, score, true);

		for (Span span : labels) {

			int tokenId = ta.getTokenIdFromCharacterOffset(span.getStart());
			//System.out.println("char>>" + spanLabeling.rawText.substring(span.getStart(), span.getEnding()));
			
			int endTokenId = ta.getTokenIdFromCharacterOffset(span.getEnding() - 1);

			view.addSpanLabel(tokenId, endTokenId + 1, span.getLabel(), span.getScore());

			if (span.isSetAttributes() && span.getAttributes().size() > 0) {

				Constituent newConstituent = view.getConstituentsCoveringSpan(tokenId, endTokenId + 1).get(0);

				if (span.isSetAttributes()) {
					for (String attribKey : span.getAttributes().keySet()) {
						newConstituent.addAttribute(attribKey, span.getAttributes().get(attribKey));
					}
				}
				//System.out.println("token>>" +newConstituent);
			}
		}
		return view;
	}

	
	private static Document annotateSourceText(CleanDoc doc) {
		
		String cleanText;
		try {
			cleanText = doc.cleanRawText();	// this is where I will generate cleanMentions
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		
		try {
			boolean forceUpdate = ResourceManager.getCuratorForceUpdate();
			//System.out.println("---------- cleanText ---------");
			//System.out.println(cleanText);
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	        Date date = new Date();
	        System.out.println( dateFormat.format(date) );
			//System.out.println("DataLoader.annotateSourceText A");
			TextAnnotation ta = curator.getTextAnnotation(doc.getCorpusId(), doc.getId(), cleanText, forceUpdate);
			System.out.println("DataLoader.annotateSourceText: Number of sentences = " + ta.getNumberOfSentences());
			for(int i=0; i<ta.getNumberOfSentences(); i++) {
				//System.out.println(ta.getSentence(i));
			}
			//System.out.println("");
			
			curator.addStanfordParse(ta, forceUpdate);
			//System.out.println("DataLoader.annotateSourceText B");
			curator.addStanfordDependencyView(ta, forceUpdate);
			//System.out.println("DataLoader.annotateSourceText C");
			curator.addPOSView(ta, forceUpdate);
			//System.out.println("DataLoader.annotateSourceText D");
			curator.addChunkView(ta, forceUpdate);
			//System.out.println("DataLoader.annotateSourceText E");
			curator.addNamedEntityView(ta, forceUpdate);
			
			// Get POS view
			SpanLabelView posView = (SpanLabelView) ta.getView(ViewNames.POS);
			List<Constituent> cons = posView.getConstituents();
			List<Pair<Integer, Integer>> charOffsetsForTokens = TokenAligner.getCharacterOffsets(ta, cons);
			if (cons.size() != charOffsetsForTokens.size()) {
				throw new Exception();
			}
			// ==========================
			/*
			 * Modifying mention offsets : adding token offsets for the mentions in relations; but note that mentionCons will store all IC gold mentions too
			 */

			List<Constituent> mentionCons = new ArrayList<Constituent>();	// these are mentions annotated by the IC annotators
																			// I will now add them to this mentionCons, where their start/end spans are tokens

			HashMap<Pair<Integer, Integer>, Mention> cleanMentions = doc.getCleanMentions();
			List<Mention> cleanMentionsList = new ArrayList<Mention>(cleanMentions.values());
			int n = cons.size();		// n = number of POS constituent
			boolean flag = false;
			
			for(int mIndex=0; mIndex<cleanMentionsList.size(); mIndex++) {	// go through each mention, to change their offsets (from char to token based)
				Mention m = cleanMentionsList.get(mIndex);
				int mStartCharOffset = m.getStartCharOffset();
				int mEndCharOffset = m.getEndCharOffset();
				int mHeadStartCharOffset = m.getHeadStartCharOffset();
				int mHeadEndCharOffset = m.getHeadEndCharOffset();
				int startTokenOffset=-1, endTokenOffset=-1, headStartTokenOffset=-1, headEndTokenOffset=-1;
				//if(m.getId().compareTo("66-119")==0) {
				//	System.out.println("**** "+mStartCharOffset+","+mEndCharOffset+" "+mHeadStartCharOffset+","+mHeadEndCharOffset); 
				//}
				flag = false;
				for (int i = 0; i < n; i++) {					// for each POS constituent
					if(mStartCharOffset == charOffsetsForTokens.get(i).getFirst().intValue()) {
						startTokenOffset = i;
						//if(m.getId().compareTo("66-119")==0) {
						//	System.out.println("**** startTokenOffset="+startTokenOffset);
						//}
						for (int j = i; j < n; j++) {
							Pair<Integer, Integer> charOffsetForToken = charOffsetsForTokens.get(j);
							int startCharOffsetForToken = charOffsetForToken.getFirst();
							int endCharOffsetForToken = charOffsetForToken.getSecond() - 1;
							
							if(mHeadStartCharOffset == startCharOffsetForToken) {
								headStartTokenOffset = j;
							}
							if(mHeadEndCharOffset == endCharOffsetForToken) {
								headEndTokenOffset = j;
							}
							if((mHeadEndCharOffset+1) == endCharOffsetForToken) {
								if(ta.getToken(j).charAt( ta.getToken(j).length()-1 )=='.') {
									headEndTokenOffset = j;
								}
							}
							
							if (mEndCharOffset == endCharOffsetForToken) {			// I have found a pos constituent whose end char offset is the same as end char offset of the clean mention
								flag = true;
								endTokenOffset = j;
								//System.out.println("**** endTokenOffset="+endTokenOffset);
								break;
							}
							if((mEndCharOffset+1) == endCharOffsetForToken) {
								if(ta.getToken(j).charAt( ta.getToken(j).length()-1 )=='.') {
									flag = true;
									endTokenOffset = j;
									break;
								}
							}
							if (flag == true) { break; }
						}
						if (flag == false) {
							System.out.println("ERROR: Unable to find the corresponding end constituent.");
							System.out.println(m.getId()+" ["+m.getSurfaceString()+"]"+m.getStartCharOffset()+","+m.getEndCharOffset());
						} else {
							// headStartTokenOffset might still be -1 (i.e. not yet set). E.g. mention id 7-94 "lower-court" where "court" is the head
							// and so the head does not match the beginning span as lower-court is still kept as one single token ; so set to startTokenOffset
							if(headStartTokenOffset==-1) { headStartTokenOffset = startTokenOffset; }
							
							Constituent newCon = createNewConstituent(startTokenOffset, endTokenOffset + 1, Constants.GOLD_MENTION_VIEW, m.getFineSC(), ta);
							newCon.addAttribute("id", m.getId());
							newCon.addAttribute("headStartTokenOffset", new Integer(headStartTokenOffset).toString());
							newCon.addAttribute("headEndTokenOffset", new Integer(headEndTokenOffset+1).toString());
							newCon.addAttribute("sc", m.getSC());
							newCon.addAttribute("fineSc", m.getFineSC());
							//System.out.println("DataLoader.AnnotateSourceText: "+m);
							mentionCons.add(newCon);

							// Quang: Adding token offset for the mentions in the relations
							String mId = m.getId();
							doc.addRelationMentionTokenOffset(mId, startTokenOffset, endTokenOffset + 1);
							doc.addRelationMentionHeadTokenOffset(mId, headStartTokenOffset, headEndTokenOffset + 1);
						}
					}
					if (flag == true)
						break;		// so that I can go on to the next clean mention
				}
			}
		
			
			// =================
			/*
			 * Add mention view
			 */
			SpanLabelView mentionView = new SpanLabelView(Constants.GOLD_MENTION_VIEW, "Default", ta, 1.0, true);
			Util.sortConstituents(mentionCons);
			for (Constituent mCon : mentionCons) {
				mentionView.addConstituent(mCon);
				// TODO: CYS: What is the purpose of the following? get(0) just returns the constituent itself, right?
				mentionView.getConstituentsCoveringSpan(mCon.getStartSpan(), mCon.getEndSpan()).get(0).addAttribute("SPAN_ATTRIBUTE", "NULL");
			}
			ta.addView(Constants.GOLD_MENTION_VIEW, mentionView);

			/*
			 * Add relation view
			 */
			BinaryRelationView pav = getBinaryRelationView(ta, doc);
			ta.addView(Constants.GOLD_RELATION_VIEW, pav);

			/*
			 * Create a Document object
			 */

			Document myDoc = new Document(ta);
			
			return myDoc;

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Unable to annotate the following text with TextAnnotation:");
			System.out.println(cleanText);
			return null;
		}
	}
	
	
	
	private static BinaryRelationView getBinaryRelationView(TextAnnotation ta, CleanDoc doc) {
		BinaryRelationView relationView = new BinaryRelationView(Constants.GOLD_RELATION_VIEW, "GoldStandard", ta, 1.0);
		for (SemanticRelation rel : doc.getRelations()) {
			TreeSet<Integer> constituentTokens = new TreeSet<Integer>();
			constituentTokens.add(rel.getM1().getStartTokenOffset());
			constituentTokens.add(rel.getM1().getEndTokenOffset());
			//Constituent m1 = new Constituent(rel.getM1().getSC(), 1.0, Constants.GOLD_RELATION_VIEW, ta, constituentTokens, true);
			Constituent m1 = new Constituent(rel.getM1().getSC(), 1.0, Constants.GOLD_RELATION_VIEW, ta, rel.getM1().getStartTokenOffset(), rel.getM1().getEndTokenOffset());
			
			constituentTokens = new TreeSet<Integer>();
			constituentTokens.add(rel.getM2().getStartTokenOffset());
			constituentTokens.add(rel.getM2().getEndTokenOffset());
			//Constituent m2 = new Constituent(rel.getM2().getSC(), 1.0, Constants.GOLD_RELATION_VIEW, ta, constituentTokens, true);
			Constituent m2 = new Constituent(rel.getM2().getSC(), 1.0, Constants.GOLD_RELATION_VIEW, ta, rel.getM2().getStartTokenOffset(), rel.getM2().getEndTokenOffset());
			
			Relation r = relationView.addRelation(rel.getFineLabel()+"|"+rel.getLexicalCondition(), m1, m2, 1.0);		// TODO  ask Vivek
			
			HashMap<String, String> rAttributes = new HashMap<String, String>();
			//if(rel.getCoarseLabel()!=null) { relationAttributes.put("coarseLabel", rel.getCoarseLabel()); }
			//if(rel.getFineLabel()!=null) { relationAttributes.put("fineLabel", rel.getFineLabel()); }
			//if(rel.getLexicalCondition()!=null) { relationAttributes.put("lexicalCondition", rel.getLexicalCondition()); }
			//if(rel.getId()!=null) { relationAttributes.put("id", rel.getId()); }
			//relationView.setRelationAttributes(r, rAttributes);
		}
		return relationView;
	}
	
	private static Constituent createNewConstituent(int start, int end, String viewName, String label, TextAnnotation ta) {
		Constituent con = new Constituent(label, viewName, ta, start, end);
		return con;
	}
}
