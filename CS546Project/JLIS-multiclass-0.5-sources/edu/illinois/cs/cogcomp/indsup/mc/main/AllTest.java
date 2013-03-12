/**
 * 
 */
package edu.illinois.cs.cogcomp.indsup.mc.main;

import java.io.IOException;
import java.util.ArrayList;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.commands.CommandDescription;
import edu.illinois.cs.cogcomp.indsup.learning.JLISModelIOManager;
import edu.illinois.cs.cogcomp.indsup.mc.LabeledMulticlassData;
import edu.illinois.cs.cogcomp.indsup.mc.MultiClassSparseLabeledDataReader;
import edu.illinois.cs.cogcomp.indsup.mc.MultiClassTrainer;
import edu.illinois.cs.cogcomp.indsup.mc.MulticlassModel;
import edu.illinois.cs.cogcomp.indsup.util.JLISUtils;

public class AllTest {

	@CommandDescription(description = "<DESCRIPTION>\n"
			+ "\tThis procedure is used to train a standard multiclass classification model using ssvm \n"
			+ "<INPUT>\n"
			+ "\tIt receives 3 different arguments. \n"
			+ "\t1) train_file (a string), the file name of the training data \n"
			+ "\t2) C (a real number > 0), a regularization parameter. \n"
			+ "\t3) n_thread (an integer), which indicates how many thread you want to use (you do not want to use more threads than the number of cores you have in your computer). \n"
			+ "<OUTPUT>\n"
			+ "\tThe trained model file will be saved to ${train_file}.ssvm.model in the current working directory.")
	public static void trainMultiClass(String train_name, String C_st_str,
			String n_thread_str) throws Exception {
		LabeledMulticlassData train = MultiClassSparseLabeledDataReader
				.readTrainingData(train_name);
		MulticlassModel model = MultiClassTrainer.trainMultiClassModel(
				Double.parseDouble(C_st_str), Integer.parseInt(n_thread_str),
				train);

		String model_name = JLISUtils.getFileNameWithoutDir(train_name)
				+ ".ssvm.model";
		JLISModelIOManager io = new JLISModelIOManager();
		io.saveModel(model, model_name);
	}

	@CommandDescription(description = "<DESCRIPTION>\n"
			+ "\tThis procedure is used to extract the weight vector from a saved model \n"
			+ "<INPUT>\n"
			+ "\tIt receives 2 different arguments. \n"
			+ "\t1) model_file (a string), the file name of a trained model \n"
			+ "\t2) output_file (a string), the file name that will be used to put the contain of the weight vector. \n"
			+ "<OUTPUT>\n"
			+ "\tThe weight vector will be output in the ${output_file}.")
	public static void outputWeightVector(String model_name, String output_file)
			throws IOException, ClassNotFoundException {
		JLISModelIOManager io = new JLISModelIOManager();
		MulticlassModel model = (MulticlassModel) io.loadModel(model_name);
		String[] reverse = model.getReverseMapping();

		ArrayList<String> out = new ArrayList<String>();
		int start = 0;
		double[] w = model.wv.getInternalArray();
		for (int i = 0; i < reverse.length; i++) {
			out.add("Label:" + reverse[i]);
			for (int t = 0; t < model.n_base_feature_in_train; t++) {
				if (t == model.n_base_feature_in_train - 1)
					out.add(t + ":" + w[start + t] + " (bias)");
				else
					out.add(t + ":" + w[start + t]);
			}
			start += model.n_base_feature_in_train;
		}
		LineIO.write(output_file, out);
		System.out.println("Finish putting the weight vector at " + output_file);
	}

	@CommandDescription(description = "<DESCRIPTION>\n"
			+ "\tThis procedure is used to train a cost-sensitive multiclass classification model using ssvm \n"
			+ "<INPUT>\n"
			+ "\tIt receives 4 different arguments. \n"
			+ "\t1) train_file (a string), the file name of the training data \n"
			+ "\t2) cost_matrix_file (a string), the file name of a cost matrix. \n"
			+ "\t3) C (a real number > 0), a regularization parameter. \n"
			+ "\t4) n_thread (a integer), which indicates how many thread you want to use (you do not want to use more threads than the number of cores you have in your computer). \n"
			+ "<OUTPUT>\n"
			+ "\tThe trained model file will be saved to ${train_file}.ssvm.model in the current working directory.")
	public static void trainCostSensitiveMultiClass(String train_name,
			String cost_matrix_file_name, String C_st_str, String n_thread_str)
			throws Exception {
		LabeledMulticlassData train = MultiClassSparseLabeledDataReader
				.readTrainingData(train_name);

		double[][] cost_matrix = MultiClassSparseLabeledDataReader
				.getCostMatrix(train.label_mapping, cost_matrix_file_name);

		MulticlassModel model = MultiClassTrainer
				.trainCostSensitiveMultiClassModel(
						Double.parseDouble(C_st_str),
						Integer.parseInt(n_thread_str), train, cost_matrix);

		String model_name = JLISUtils.getFileNameWithoutDir(train_name)
				+ ".ssvm.model";
		JLISModelIOManager io = new JLISModelIOManager();
		io.saveModel(model, model_name);
	}

	@CommandDescription(description = "<DESCRIPTION>\n"
			+ "\tThis procedure is used to perform cross-validtion for standard multiclass classification \n"
			+ "<INPUT>\n"
			+ "\tIt receives 5 different arguments. \n"
			+ "\t1) train_file (a string), the file name of the training data \n"
			+ "\t2) cost_matrix_file (a string), the file name of a cost matrix \n"
			+ "\t3) C (a real number > 0), a regularization parameter. \n"
			+ "\t4) n_thread (an integer), which indicates how many thread you want to use (you do not want to use more threads than the number of cores you have in your computer). \n"
			+ "\t5) n_fold (an integer), which indicates the number of fold you want). \n"
			+ "<OUTPUT>\n"
			+ "\t The cross validtaion accuracy and cost will be printed on the screen. To understand the meaning of \"cost\", please refer to read me.")
	public static void crossValidationCostSensitiveMultiClass(
			String train_name, String cost_matrix_name, String C_st_str,
			String n_thread_str, String n_fold_str) throws Exception {
		LabeledMulticlassData train = MultiClassSparseLabeledDataReader
				.readTrainingData(train_name);
		double[][] cost_matrix = MultiClassSparseLabeledDataReader
				.getCostMatrix(train.label_mapping, cost_matrix_name);

		Pair<Double, Double> cv_res = MultiClassTrainer.crossValidation(
				Double.parseDouble(C_st_str), Integer.parseInt(n_thread_str),
				Integer.parseInt(n_fold_str), train, cost_matrix);

		System.out
				.println("===================================================");
		System.out.println("CV Res: Acc (High is good): " + cv_res.getFirst());
		System.out.println("CV Res: Cost (Low is good): " + cv_res.getSecond());
	}

	@CommandDescription(description = "<DESCRIPTION>\n"
			+ "\tThis procedure is used to perform cross-validtion for standard multiclass classification \n"
			+ "<INPUT>\n"
			+ "\tIt receives 4 different arguments. \n"
			+ "\t1) train_file (a string), the file name of the training data \n"
			+ "\t2) C (a real number > 0), a regularization parameter. \n"
			+ "\t3) n_thread (an integer), which indicates how many thread you want to use (you do not want to use more threads than the number of cores you have in your computer). \n"
			+ "\t4) n_fold (an integer), which indicates the number of fold you want). \n"
			+ "<OUTPUT>\n"
			+ "\t The cross validtaion accuracy will be printed on the screen.")
	public static void crossValidationMultiClass(String train_name,
			String C_st_str, String n_thread_str, String n_fold_str)
			throws Exception {
		LabeledMulticlassData train = MultiClassSparseLabeledDataReader
				.readTrainingData(train_name);
		double cv_res = MultiClassTrainer.crossValidation(
				Double.parseDouble(C_st_str), Integer.parseInt(n_thread_str),
				Integer.parseInt(n_fold_str), train);

		System.out
				.println("===================================================");
		System.out.println("CV Res: Acc: " + cv_res);
	}

	@CommandDescription(description = "<DESCRIPTION>\n"
		+ "\tThis procedure is used to generate prediction results and report testing performance.\n"
		+ "<INPUT>\n"
		+ "\tIt receives 4 different arguments. \n"
		+ "\t1) model_file (a string), the file name of the trained model \n"
		+ "\t2) cost_matrix_file (a string), the file name of a cost matrix \n"		
		+ "\t3) test_name (a string), the testing file. \n"
		+ "\t4) output_file_name (a string), the location to put the testing results \n"
		+ "<OUTPUT>\n"
		+ "\t The testing acc/cost will be printed on the screen. The testing prediction results will be put in ${output_file_name}")
	public static void testCostSensitiveMultiClass(String model_name,
			String cost_matrix_file_name, String test_name, String output_name)
			throws Exception {
		JLISModelIOManager io = new JLISModelIOManager();

		MulticlassModel model = (MulticlassModel) io.loadModel(model_name);

		LabeledMulticlassData test = MultiClassSparseLabeledDataReader
				.readTestingData(test_name, model.lab_mapping,
						model.n_base_feature_in_train);

		Pair<int[], int[]> gold_pred = MultiClassTrainer.getPredictionResults(
				model, test);
		int[] pred = gold_pred.getSecond();
		int[] gold = gold_pred.getFirst();

		double[][] cost_matrix = MultiClassSparseLabeledDataReader
				.getCostMatrix(test.label_mapping, cost_matrix_file_name);

		System.out.println("===================================");
		System.out.println(" Accuracy (The higher the better)  ");
		System.out.println("===================================");
		System.out.println("Acc = "
				+ MultiClassTrainer.getTestingAcc(gold, pred));

		System.out.println("==================================");
		System.out.println("   Cost  (The lower the better)   ");
		System.out.println("==================================");
		System.out.println("Cost = "
				+ MultiClassTrainer.getTestingCost(gold, pred, cost_matrix));

		String[] reverse = model.getReverseMapping();

		ArrayList<String> out = new ArrayList<String>();
		for (int i = 0; i < pred.length; i++)
			out.add("" + reverse[pred[i]]);

		System.out.println("The prediction results have been output to ["
				+ output_name + "]");
		LineIO.write(output_name, out);
	}

	@CommandDescription(description = "<DESCRIPTION>\n"
		+ "\tThis procedure is used to generate prediction results and report testing performance.\n"
		+ "<INPUT>\n"
		+ "\tIt receives 3 different arguments. \n"
		+ "\t1) model_file (a string), the file name of the trained model \n"				
		+ "\t2) test_name (a string), the testing file. \n"
		+ "\t3) output_file_name (a string), the location to put the testing results \n"
		+ "<OUTPUT>\n"
		+ "\t The testing acc/cost will be printed on the screen. The testing prediction results will be put in ${output_file_name}")
	public static void testMultiClass(String model_name, String test_name,
			String output_name) throws Exception {
		JLISModelIOManager io = new JLISModelIOManager();
		MulticlassModel model = (MulticlassModel) io.loadModel(model_name);
		LabeledMulticlassData test = MultiClassSparseLabeledDataReader
				.readTestingData(test_name, model.lab_mapping,
						model.n_base_feature_in_train);

		Pair<int[], int[]> gold_pred = MultiClassTrainer.getPredictionResults(
				model, test);
		int[] pred = gold_pred.getSecond();
		int[] gold = gold_pred.getFirst();

		System.out.println("===================================");
		System.out.println(" Accuracy (The higher the better)  ");
		System.out.println("===================================");
		System.out.println("Acc = "
				+ MultiClassTrainer.getTestingAcc(gold, pred));

		String[] reverse = model.getReverseMapping();

		ArrayList<String> out = new ArrayList<String>();
		for (int i = 0; i < pred.length; i++)
			out.add("" + reverse[pred[i]]);
		System.out.println("The prediction results have been output to ["
				+ output_name + "]");
		LineIO.write(output_name, out);
	}

}
