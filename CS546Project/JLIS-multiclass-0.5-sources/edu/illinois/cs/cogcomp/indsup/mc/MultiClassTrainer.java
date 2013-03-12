package edu.illinois.cs.cogcomp.indsup.mc;

import java.util.List;
import java.util.Random;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.indsup.inference.AbstractLossSensitiveStructureFinder;
import edu.illinois.cs.cogcomp.indsup.learning.JLISParameters;
import edu.illinois.cs.cogcomp.indsup.learning.StructuredProblem;
import edu.illinois.cs.cogcomp.indsup.learning.L2Loss.L2LossParallelJLISLearner;
import edu.illinois.cs.cogcomp.indsup.mc.MulticlassModel;

public class MultiClassTrainer {

	public static Pair<int[],int[]> getPredictionResults(MulticlassModel model,
			LabeledMulticlassData test) throws Exception {
		int[] gold = new int[test.sp.size()];
		int[] pred = new int[test.sp.size()];
		for (int i = 0; i < test.sp.size(); i++) {
			LabeledMultiClassStructure prediction = (LabeledMultiClassStructure) model.s_finder
					.getBestStructure(model.wv, test.sp.input_list.get(i));
			gold[i] = ((LabeledMultiClassStructure) test.sp.output_list
					.get(i)).output;
			pred[i] = prediction.output;			
		}
		return new Pair<int[], int[]>(gold, pred);
		
	}
	
	public static double getTestingAcc(int[] gold, int[] pred) throws Exception {
		double acc = 0.0;
		for (int i = 0; i < gold.length; i++) {
			if (gold[i]==pred[i])
				acc += 1.0;
		}
		return acc / gold.length;
	}

	public static double getTestingCost(int[] gold, int[] pred, double[][] cost_matirx)
			throws Exception {
		double cost = 0.0;
		for (int i = 0; i < gold.length; i++) {			
			cost += cost_matirx[gold[i]][pred[i]];
		}
		return cost / gold.length;
	}

	public static double crossValidation(double C, int n_thread, int n_fold,
			LabeledMulticlassData train) throws Exception {
		double cv_res = 0;
		List<Pair<StructuredProblem, StructuredProblem>> data_pair_list = train.sp
				.splitData(n_fold, new Random(0));
		for (int i = 0; i < n_fold; i++) {
			StructuredProblem cv_sp_train = data_pair_list.get(i).getFirst();
			StructuredProblem cv_sp_test = data_pair_list.get(i).getSecond();

			LabeledMulticlassData cv_train = new LabeledMulticlassData(
					train.label_mapping, train.n_base_feature_in_train,
					cv_sp_train);
			LabeledMulticlassData cv_test = new LabeledMulticlassData(
					train.label_mapping, train.n_base_feature_in_train,
					cv_sp_test);

			MulticlassModel cv_model = trainMultiClassModel(C, n_thread,
					cv_train);
			Pair<int[], int[]> gold_pred = getPredictionResults(cv_model, cv_test);
			double test_acc = getTestingAcc(gold_pred.getFirst(), gold_pred.getSecond());
			cv_res += test_acc;
			System.out.println("Fold " + i + ":" + test_acc);
		}
		return cv_res / n_fold;
	}

	public static MulticlassModel trainMultiClassModel(double C, int n_thread,
			LabeledMulticlassData train) throws Exception {
		MulticlassModel model = new MulticlassModel();
		model.lab_mapping = train.label_mapping; // for the bias term
		model.n_base_feature_in_train = train.n_base_feature_in_train;

		model.para = new JLISParameters();

		// para.total_number_features = train.label_mapping.size() *
		// train.n_base_feature_in_train;
		model.para.c_struct = C;
		model.para.TRAINMINI = true;

		// play with the following two parameters if you want to solve SSVM more
		// tightly
		model.para.DUAL_GAP = 0.01;
		model.para.WORKINGSETSVM_STOP = 0.01;

		System.out.println("Initializing Solvers...");
		System.out.flush();
		AbstractLossSensitiveStructureFinder[] s_finder_list = new AbstractLossSensitiveStructureFinder[n_thread];
		for (int i = 0; i < s_finder_list.length; i++) {
			s_finder_list[i] = new MultiClassStructureFinder();
		}
		System.out.println("Done!");
		System.out.flush();

		model.s_finder = s_finder_list[0];
		L2LossParallelJLISLearner learner = new L2LossParallelJLISLearner();

		// train model
		model.wv = learner.parallelTrainStructuredSVM(s_finder_list, train.sp,
				model.para);
		return model;
	}

	public static MulticlassModel trainCostSensitiveMultiClassModel(double C,
			int n_thread, LabeledMulticlassData train, double[][] cost_matrix)
			throws Exception {
		MulticlassModel model = new MulticlassModel();
		model.lab_mapping = train.label_mapping; // for the bias term
		model.n_base_feature_in_train = train.n_base_feature_in_train;

		model.para = new JLISParameters();
		//model.para.verbose_level = JLISParameters.VLEVEL_HIGH;
		model.cost_matrix = cost_matrix;

		// para.total_number_features = train.label_mapping.size() *
		// train.n_base_feature_in_train;
		model.para.c_struct = C;
		model.para.TRAINMINI = true;

		// play with the following two parameters if you want to solve SSVM more
		// tightly
		model.para.DUAL_GAP = 0.01;
		model.para.WORKINGSETSVM_STOP = 0.01;

		System.out.println("Initializing Solvers...");
		System.out.flush();
		AbstractLossSensitiveStructureFinder[] s_finder_list = new AbstractLossSensitiveStructureFinder[n_thread];
		for (int i = 0; i < s_finder_list.length; i++) {
			s_finder_list[i] = new MultiClassStructureFinder(cost_matrix);
		}
		System.out.println("Done!");
		System.out.flush();

		model.s_finder = s_finder_list[0];
		L2LossParallelJLISLearner learner = new L2LossParallelJLISLearner();

		// train model
		model.wv = learner.parallelTrainStructuredSVM(s_finder_list, train.sp,
				model.para);
		return model;
	}

	public static Pair<Double,Double> crossValidation(double C, int n_thread, int n_fold,
			LabeledMulticlassData train, double[][] cost_matrix) throws Exception {
		double cv_acc = 0;
		double cv_cost = 0;
		
		List<Pair<StructuredProblem, StructuredProblem>> data_pair_list = train.sp
				.splitData(n_fold, new Random(0));
		for (int i = 0; i < n_fold; i++) {
			StructuredProblem cv_sp_train = data_pair_list.get(i).getFirst();
			StructuredProblem cv_sp_test = data_pair_list.get(i).getSecond();

			LabeledMulticlassData cv_train = new LabeledMulticlassData(
					train.label_mapping, train.n_base_feature_in_train,
					cv_sp_train);
			LabeledMulticlassData cv_test = new LabeledMulticlassData(
					train.label_mapping, train.n_base_feature_in_train,
					cv_sp_test);

			MulticlassModel cv_model = trainCostSensitiveMultiClassModel(C, n_thread,
					cv_train,cost_matrix);
			Pair<int[], int[]> gold_pred = getPredictionResults(cv_model, cv_test);
			double test_acc = getTestingAcc(gold_pred.getFirst(), gold_pred.getSecond());
			cv_acc += test_acc;
			System.out.println("Fold " + i + " acc :" + test_acc);
						
			double test_cost = getTestingCost(gold_pred.getFirst(), gold_pred.getSecond(), cost_matrix);
			cv_cost += test_cost;
			System.out.println("Fold " + i + " cost :" + test_cost);
		}
		return new Pair<Double, Double>(cv_acc/n_fold, cv_cost/n_fold);
	}

}
