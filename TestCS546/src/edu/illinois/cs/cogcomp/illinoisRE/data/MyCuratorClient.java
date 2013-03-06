/**
 * 
 */
package edu.illinois.cs.cogcomp.illinoisRE.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.protocol.TProtocol;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.edison.data.curator.CuratorClient;
import edu.illinois.cs.cogcomp.edison.data.curator.TokenAligner;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.Sentence;
import edu.illinois.cs.cogcomp.edison.sentences.SpanLabelView;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.TreeView;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.edison.utilities.ParseUtils;
import edu.illinois.cs.cogcomp.illinoisRE.common.Constants;
import edu.illinois.cs.cogcomp.illinoisRE.common.Util;
import edu.illinois.cs.cogcomp.thrift.base.AnnotationFailedException;
import edu.illinois.cs.cogcomp.thrift.base.ServiceUnavailableException;
import edu.illinois.cs.cogcomp.thrift.curator.Curator;
import edu.illinois.cs.cogcomp.thrift.curator.Record;

/**
 * @author dxquang Mar 21, 2010
 */
public class MyCuratorClient extends CuratorClient {

	public static final int MAX_TOKENS = 8;

	/**
	 * @param curatorHost
	 * @param curatorPort
	 */
	public MyCuratorClient(String curatorHost, int curatorPort) {
		super(curatorHost, curatorPort);
	}

	public MyCuratorClient(String curatorHost, int curatorPort, boolean useCharniakForTokens) {
		super(curatorHost, curatorPort, useCharniakForTokens);
	}



/**
	public void addWikiHyperlinkedView(TextAnnotation ta,
			Map<String, Double> mapWikiHyperlinked)
			throws ServiceUnavailableException, AnnotationFailedException,
			TException {

		String[] tokens = ta.getTokens();

		List<Pair<Integer, Integer>> spans = new ArrayList<Pair<Integer, Integer>>();

		int offset = 0;
		for (int i = 0; i < tokens.length - 1; i++) {
			int start = offset;
			StringBuffer str = new StringBuffer();
			str.append(tokens[i]);
			// str.append(" ");
			offset++;
			String posStart = WordHelpers.getPOS(ta, start);
			if (!Constants.validStartPOS.contains(posStart.substring(0, 1))) {
				continue;
			}

			for (int j = i; j < tokens.length && (j - i) < MAX_TOKENS; j++) {
				if (j > i)
					str.append(tokens[j]);

				String posEnd = WordHelpers.getPOS(ta, j);
				if (!Constants.validEndPOS.contains(posEnd.substring(0, 1))) {
					str.append(" ");
					continue;
				}

				if (mapWikiHyperlinked
						.containsKey(str.toString().toLowerCase())) {
					int end = j + 1;
					Pair<Integer, Integer> pair = new Pair<Integer, Integer>(
							start, end);
					spans.add(pair);
					// System.out.println("[" + start + "," + end + "] : >>>"
					// + str.toString().toLowerCase() + "<<<");
				}

				str.append(" ");
			}
		}
		// System.out.println();

		SpanLabelView wikihyperlinked = new SpanLabelView(
				Constants.WIKI_HYPERLINKED, "Default", ta, 1.0, true);

		for (Pair<Integer, Integer> p : spans) {
			wikihyperlinked.addSpanLabel(p.getFirst(), p.getSecond(),
					Constants.WIKI_HYPERLINKED, 1.0);

			wikihyperlinked.getConstituentsCoveringSpan(p.getFirst(),
					p.getSecond()).get(0)
					.addAttribute("SPAN_ATTRIBUTE", "NULL");
		}

		ta.addView(Constants.WIKI_HYPERLINKED, wikihyperlinked);
	}
**/

/**
	public void addExternalMentionView(TextAnnotation ta,
			Map<String, Double> mapExternalMentions, String viewName)
			throws ServiceUnavailableException, AnnotationFailedException,
			TException {

		String[] tokens = ta.getTokens();

		List<Pair<Integer, Integer>> spans = new ArrayList<Pair<Integer, Integer>>();

		int offset = 0;
		for (int i = 0; i < tokens.length - 1; i++) {
			int start = offset;
			StringBuffer str = new StringBuffer();
			str.append(tokens[i]);
			str.append(" ");
			offset++;
			String posStart = WordHelpers.getPOS(ta, start);
			if (!Constants.validStartPOS.contains(posStart.substring(0, 1))) {
				continue;
			}
			for (int j = i + 1; j < tokens.length && (j - i) < MAX_TOKENS; j++) {
				str.append(tokens[j]);

				String posEnd = WordHelpers.getPOS(ta, j);
				if (!Constants.validEndPOS.contains(posEnd.substring(0, 1))) {
					str.append(" ");
					continue;
				}

				if (mapExternalMentions.containsKey(str.toString()
						.toLowerCase())) {
					int end = j + 1;
					Pair<Integer, Integer> pair = new Pair<Integer, Integer>(
							start, end);
					spans.add(pair);
					// System.out.println("[" + start + "," + end + "] : >>>"
					// + str.toString().toLowerCase() + "<<<");
				}

				str.append(" ");
			}
		}
		// System.out.println();

		SpanLabelView wikihyperlinked = new SpanLabelView(viewName, "Default",
				ta, 1.0, true);

		for (Pair<Integer, Integer> p : spans) {
			wikihyperlinked.addSpanLabel(p.getFirst(), p.getSecond(), viewName,
					1.0);

			wikihyperlinked.getConstituentsCoveringSpan(p.getFirst(),
					p.getSecond()).get(0)
					.addAttribute("SPAN_ATTRIBUTE", "NULL");
		}

		ta.addView(viewName, wikihyperlinked);
	}
**/

/**
	public void addExternalMentionView(TextAnnotation ta,
			Set<String> setExternalMentions, String viewName)
			throws ServiceUnavailableException, AnnotationFailedException,
			TException {

		String[] tokens = ta.getTokens();

		List<Pair<Integer, Integer>> spans = new ArrayList<Pair<Integer, Integer>>();

		int offset = 0;
		for (int i = 0; i < tokens.length - 1; i++) {
			int start = offset;
			StringBuffer str = new StringBuffer();
			str.append(tokens[i]);
			// str.append(" ");
			offset++;
			String posStart = WordHelpers.getPOS(ta, start);
			if (!Constants.validStartPOS.contains(posStart.substring(0, 1))) {
				continue;
			}
			for (int j = i; j < tokens.length && (j - i) < MAX_TOKENS; j++) {

				if (j > i)
					str.append(tokens[j]);

				String posEnd = WordHelpers.getPOS(ta, j);
				if (!Constants.validEndPOS.contains(posEnd.substring(0, 1))) {
					str.append(" ");
					continue;
				}

				if (setExternalMentions.contains(str.toString().toLowerCase())) {
					int end = j + 1;
					Pair<Integer, Integer> pair = new Pair<Integer, Integer>(
							start, end);
					spans.add(pair);
					// System.out.println("[" + start + "," + end + "] : >>>"
					// + str.toString().toLowerCase() + "<<<");
				}

				str.append(" ");
			}
		}
		// System.out.println();

		SpanLabelView wikihyperlinked = new SpanLabelView(viewName, "Default",
				ta, 1.0, true);

		for (Pair<Integer, Integer> p : spans) {
			wikihyperlinked.addSpanLabel(p.getFirst(), p.getSecond(), viewName,
					1.0);

			wikihyperlinked.getConstituentsCoveringSpan(p.getFirst(),
					p.getSecond()).get(0)
					.addAttribute("SPAN_ATTRIBUTE", "NULL");
		}

		ta.addView(viewName, wikihyperlinked);
	}
**/

	/**
	 * @param ta
	 * @param viewName
	 */
	public void addPhraseParseView(TextAnnotation ta, String viewName) {

		TreeView parseView = (TreeView) ta.getView(ViewNames.PARSE_STANFORD);

		int n = ta.getNumberOfSentences();

		SpanLabelView parseNPView = new SpanLabelView(viewName, "Default", ta,
				1.0, true);

		for (int i = 0; i < n; i++) {

			Tree<String> tree = parseView.getTree(i);
			Sentence sentence = ta.getSentence(i);
			int sentStart = sentence.getStartSpan();

			//Tree<Pair<String, Pair<Integer, Integer>>> spanTree = ParseUtils.getSpanLabeledTree(tree);
			Tree<Pair<String, IntPair>> spanTree = ParseUtils.getSpanLabeledTree(tree);

			List<Pair<Integer, Integer>> spans = new ArrayList<Pair<Integer, Integer>>();

			browseTree(ta, spanTree, ta.getTokens(), spans, sentStart);

			for (Pair<Integer, Integer> p : spans) {
				parseNPView.addSpanLabel(p.getFirst(), p.getSecond(), viewName,
						1.0);

				parseNPView.getConstituentsCoveringSpan(p.getFirst(),
						p.getSecond()).get(0).addAttribute("SPAN_ATTRIBUTE",
						"NULL");
			}
		}
		ta.addView(viewName, parseNPView);
	}

	/**
	 * @param ta
	 * @param spanTree
	 * @param tokens
	 * @param spans
	 * @param sentStart
	 */
	private void browseTree(TextAnnotation ta,
			Tree<Pair<String, IntPair>> spanTree,
			String[] tokens, List<Pair<Integer, Integer>> spans, int sentStart) {

		//Pair<String, Pair<Integer, Integer>> treeLabel = spanTree.getLabel();
		Pair<String, IntPair> treeLabel = spanTree.getLabel();

		if (treeLabel.getFirst().equals("NP")) {
			Integer start = treeLabel.getSecond().getFirst() + sentStart;
			Integer end = treeLabel.getSecond().getSecond() + sentStart;

			// System.out.println("Word at " + end + " : "
			// + WordFeatures.getWord(ta, end - 1));

			String posStart = WordHelpers.getPOS(ta, start);
			String posEnd = WordHelpers.getPOS(ta, end - 1);

			// if (Constants.validStartPOS.contains(posStart.substring(0, 1))
			// && Constants.validEndPOS.contains(posEnd.substring(0, 1))) {
			if (!Constants.invalidEndPOS.contains(posEnd.substring(0, 1))
					&& !Constants.invalidEndPOS.contains(posEnd)
					&& !Constants.invalidStartPOS.contains(posStart.substring(
							0, 1))
					&& !Constants.invalidStartPOS.contains(posStart)
					&& !posStart.equals("-LRB-") && !posStart.equals("-RRB-")) {

				// System.out.print(treeLabel.getFirst() + " [" + start + "," +
				// end
				// + "] : ");
				// for (int i = start; i < end; i++) {
				// System.out.print(tokens[i] + " ");
				// }
				// System.out.println();
				Pair<Integer, Integer> pair = new Pair<Integer, Integer>(start,
						end);
				spans.add(pair);
			}

			// Getting everything in a noun phrase.
			for (int i = start; i < end - 2; i++) {
				String posI = WordHelpers.getPOS(ta, i);
				for (int j = i + 1; j < i + 2 + 3 && j < end; j++) {
					String posJ = WordHelpers.getPOS(ta, j);
					// String[] toks = ta.getTokensInSpan(i, j);
					// System.out.println("Start POS: " + posI + " - End POS: "
					// + posJ);
					// for (String t : toks) {
					// System.out.print(t + " ");
					// }
					if (!Constants.invalidEndPOS.contains(posJ.substring(0, 1))
							&& !Constants.invalidEndPOS.contains(posJ)
							&& !Constants.invalidStartPOS.contains(posI
									.substring(0, 1))
							&& !Constants.invalidStartPOS.contains(posI)
							&& !posJ.equals("-LRB-") && !posI.equals("-RRB-")) {
						// System.out.println(" ---> OK");
						Pair<Integer, Integer> pair = new Pair<Integer, Integer>(
								i, j + 1);
						spans.add(pair);
					} else {
						// System.out.println(" ---> NO");
					}
				}
			}
		}

		//Vector<Tree<Pair<String, Pair<Integer, Integer>>>> children = spanTree.getChildren();
		List<Tree<Pair<String, IntPair>>> children = spanTree.getChildren();
		//for (Tree<Pair<String, Pair<Integer, Integer>>> y : children) {
		for (Tree<Pair<String, IntPair>> y : children) {
			browseTree(ta, y, tokens, spans, sentStart);
		}

	}

	/**
	 * @param ta
	 * @param viewName
	 */
	public void addPhraseChunkView(TextAnnotation ta, String viewName) {

		// SpanLabelView chunkView = (SpanLabelView) ta
		// .getView(ViewNames.SHALLOW_PARSE);
		int numSen = ta.getNumberOfSentences();

		SpanLabelView parseNPView = new SpanLabelView(viewName, "Default", ta, 1.0, true );

		for (int idx = 0; idx < numSen; idx++) {
			Sentence sen = ta.getSentence(idx);
			SpanLabelView chunkView = (SpanLabelView) sen.getView(ViewNames.SHALLOW_PARSE);

			List<Constituent> chunks = chunkView.getConstituents();

			List<Pair<Integer, Integer>> spans = new ArrayList<Pair<Integer, Integer>>();

			for (Constituent cons : chunks) {
				if (cons.getLabel().equals("NP")) {
					int start = cons.getStartSpan();
					int end = cons.getEndSpan();
					String posStart = WordHelpers.getPOS(ta, start);
					String posEnd = WordHelpers.getPOS(ta, end - 1);

					if (Constants.validStartPOS.contains(posStart.substring(0, 1))
							&& Constants.validEndPOS.contains(posEnd.substring(0, 1))) {
						Pair<Integer, Integer> pair = new Pair<Integer, Integer>(start, end);
						spans.add(pair);
					}
				}
			}

			// Implementation of the heuristic: connecting chunks in the form
			// (NP
			// (PP NP)+).
			Util.sortConstituents(chunks);
			int n = chunks.size();
			for (int i = 0; i < n - 2; i++) {
				Constituent con = chunks.get(i);
				if (con.getLabel().equals("NP")) {
					int j = i + 1;
					Constituent next = chunks.get(j);
					while (j < n && next.getLabel().equals("PP")) {
						j++;
						if (j >= n)
							break;
						next = chunks.get(j);
						if (next.getLabel().equals("NP")) {
							Pair<Integer, Integer> pair = new Pair<Integer, Integer>(
									con.getStartSpan(), next.getEndSpan());
							spans.add(pair);
							j++;
							if (j >= n)
								break;
							next = chunks.get(j);
						} else {
							break;
						}
					}
				}
			}
			// ------------

			for (Pair<Integer, Integer> p : spans) {
				parseNPView.addSpanLabel(p.getFirst(), p.getSecond(), viewName,
						1.0);

				parseNPView.getConstituentsCoveringSpan(p.getFirst(),
						p.getSecond()).get(0).addAttribute("SPAN_ATTRIBUTE",
						"NULL");
			}
		}
		ta.addView(viewName, parseNPView);

	}

	public void addMentionView(TextAnnotation ta) {

		List<Constituent> mentions = new ArrayList<Constituent>();
		//Set<Pair<Integer, Integer>> setUsedSpans = new HashSet<Pair<Integer, Integer>>();
		Set<IntPair> setUsedSpans = new HashSet<IntPair>();

		List<Constituent> posCons = ta.getView(ViewNames.POS).getConstituents();
		for (Constituent con : posCons) {
			if (con.getLabel().substring(0, 1).equals("N") || con.getLabel().equals("PRP$")) {
				Constituent newCon = createNewConstituent(con.getStartSpan(), con.getEndSpan(), Constants.POS_MENTION, "POS_MENTION",ta);
				if (setUsedSpans.contains(newCon.getSpan()))
					continue;
				mentions.add(newCon);
				setUsedSpans.add(newCon.getSpan());
			}
		}

		List<Constituent> chunkCons = ta.getView(Constants.CHUNK_PHRASE).getConstituents();
		for (Constituent con : chunkCons) {
			if (setUsedSpans.contains(con.getSpan()))
				continue;
			mentions.add(con);
			setUsedSpans.add(con.getSpan());
		}

		List<Constituent> nerCons = ta.getView(ViewNames.NER).getConstituents();
		for (Constituent con : nerCons) {
			if (setUsedSpans.contains(con.getSpan()))
				continue;
			mentions.add(con);
			setUsedSpans.add(con.getSpan());
		}
/*
		if(ta.hasView(ViewNames.COREF)) {
			List<Constituent> corefCons = ta.getView(ViewNames.COREF).getConstituents();
			for(Constituent con : corefCons) {
				if (setUsedSpans.contains(con.getSpan()))
					continue;
				mentions.add(con);
				setUsedSpans.add(con.getSpan());
			}
		}
		else {
			System.out.println("**** no coref view ****");
		}
*/		
//		List<Constituent> quanCons = ta.getView(ViewNames.QUANTITIES)
//				.getConstituents();
//		for (Constituent con : quanCons) {
//			if (setUsedSpans.contains(con.getSpan()))
//				continue;
//			mentions.add(con);
//			setUsedSpans.add(con.getSpan());
//		}

		/**
		 * List<Constituent> wikiCons = ta.getView(Constants.WIKI_HYPERLINKED)
		 * .getConstituents(); for (Constituent con : wikiCons) { if
		 * (setUsedSpans.contains(con.getSpan())) continue; mentions.add(con);
		 * setUsedSpans.add(con.getSpan()); }
		 * 
		 * List<Constituent> teamCons = ta.getView(Constants.NFL_TEAM)
		 * .getConstituents(); for (Constituent con : teamCons) { if
		 * (setUsedSpans.contains(con.getSpan())) continue; mentions.add(con);
		 * setUsedSpans.add(con.getSpan()); }
		 * 
		 * List<Constituent> playerCons = ta.getView(Constants.NFL_PLAYER)
		 * .getConstituents(); for (Constituent con : playerCons) { if
		 * (setUsedSpans.contains(con.getSpan())) continue; mentions.add(con);
		 * setUsedSpans.add(con.getSpan()); }
		 * 
		 * List<Constituent> positionCons = ta.getView(Constants.NFL_POSITION)
		 * .getConstituents(); for (Constituent con : positionCons) { if
		 * (setUsedSpans.contains(con.getSpan())) continue; mentions.add(con);
		 * setUsedSpans.add(con.getSpan()); }
		 **/

		List<Constituent> parseCons = ta.getView(Constants.PARSE_PHRASE).getConstituents();
		for (Constituent con : parseCons) {
			if (setUsedSpans.contains(con.getSpan()))
				continue;
			mentions.add(con);
			setUsedSpans.add(con.getSpan());
		}

		List<Constituent> tempCons = new ArrayList<Constituent>();
		tempCons.addAll(chunkCons);
		tempCons.addAll(parseCons);

		for (Constituent con : tempCons) {
			for (Constituent nerCon : nerCons) {
				if (nerCon.isConsituentInRange(con.getStartSpan(), con.getEndSpan())) {
					if (con.getStartSpan() < nerCon.getStartSpan()) {
						Constituent newCon = createNewConstituent(con.getStartSpan(), nerCon.getStartSpan(), Constants.CHUNK_PHRASE, "ADD_MENTION", ta);
						//Pair<Integer, Integer> span = newCon.getSpan();
						IntPair span = newCon.getSpan();

						if (!setUsedSpans.contains(span)
								&& Constants.validEndPOS.contains(WordHelpers.getPOS(ta, span.getSecond() - 1).substring(0, 1))) {
							mentions.add(newCon);
							setUsedSpans.add(span);
						}
					}
				}
			}
		}

		Util.sortConstituents(mentions);

		int preStart = -1;
		int preEnd = -1;

		List<Pair<Pair<Integer, Integer>, String>> spans = new ArrayList<Pair<Pair<Integer, Integer>, String>>();

		StringBuffer label = new StringBuffer();
		for (Constituent con : mentions) {
			int curStart = con.getStartSpan();
			int curEnd = con.getEndSpan();

			if (preStart != curStart || preEnd != curEnd) {
				if (preStart != -1 && preEnd != -1) {
					Pair<Integer, Integer> pairIntegers = new Pair<Integer, Integer>(preStart, preEnd);
					Pair<Pair<Integer, Integer>, String> pair = new Pair<Pair<Integer, Integer>, String>(pairIntegers, label.toString());
					spans.add(pair);
				}

				preStart = curStart;
				preEnd = curEnd;
				label = new StringBuffer();
				label.append(con.getLabel());
			} else {
				label.append(",");
				label.append(con.getLabel());
			}
		}

		if (preStart != -1 && preEnd != -1) {
			Pair<Integer, Integer> pairIntegers = new Pair<Integer, Integer>(preStart, preEnd);
			Pair<Pair<Integer, Integer>, String> pair = new Pair<Pair<Integer, Integer>, String>(pairIntegers, label.toString());
			spans.add(pair);
		}

		SpanLabelView mentionView = new SpanLabelView(Constants.CANDIDATE_MENTION_VIEW, "Default", ta, 1.0, true);

		for (Pair<Pair<Integer, Integer>, String> p : spans) {
			mentionView.addSpanLabel(p.getFirst().getFirst(), p.getFirst().getSecond(), p.getSecond(), 1.0);
			mentionView.getConstituentsCoveringSpan(p.getFirst().getFirst(), p.getFirst().getSecond()).get(0).addAttribute("SPAN_ATTRIBUTE", "NULL");
		}

		ta.addView(Constants.CANDIDATE_MENTION_VIEW, mentionView);

	}

	/**
	 * @param start
	 * @param end
	 * @param viewName
	 * @param ta
	 * @param label
	 * @return
	 */
	private Constituent createNewConstituent(int start, int end,
			String viewName, String label, TextAnnotation ta) {
		Constituent con = new Constituent(label, viewName, ta, start, end);
		return con;
	}
}
