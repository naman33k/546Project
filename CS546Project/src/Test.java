import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.illinoisRE.common.Constants;
import edu.illinois.cs.cogcomp.illinoisRE.common.Document;
import edu.illinois.cs.cogcomp.illinoisRE.data.CleanDoc;
import edu.illinois.cs.cogcomp.illinoisRE.data.DataLoader;
import edu.illinois.cs.cogcomp.illinoisRE.data.GlobalDoc;
import edu.illinois.cs.cogcomp.illinoisRE.data.Mention;
import edu.illinois.cs.cogcomp.indsup.learning.FeatureVector;
import edu.illinois.cs.cogcomp.indsup.learning.LexManager;
import edu.illinois.cs.cogcomp.indsup.mc.MulticlassModel;
import edu.illinois.cs.cogcomp.indsup.mc.main.AllTest;
import evaluation.Task1Eval;

import java.io.*;

import CCMPackage.CCM;
import CCMPackage.MultiClassModel;

public class Test {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		//String file = "/home/wieting2/TIDES-Extraction-2004-Training-Data-V1.4/English/nwire/APW20001007.1745.0371";
         /*
        if(args.length != 2) {
            System.out.println("Need to enter a the train folder and test folder as arguments");
            System.exit(0);
        }
         */
		
		String testFolder = "test1";
		String trainFolder = "train1";
		String testOutputFileName = "output";
		CCM c = new CCM();
		c.trainLISnow(trainFolder);
		c.testRelationsSNOW(testFolder, testOutputFileName);
		Task1Eval eval = new Task1Eval(testOutputFileName, testFolder);
        eval.evaluate();
	}

}
