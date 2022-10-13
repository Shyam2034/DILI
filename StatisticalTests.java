
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class StatisticalTests {

	public static double getChi(double trial[][], int nComp) {
		// Chi-squared value for y and independent Xi

		double exp00 = (trial[0][0] + trial[0][1]) * (trial[0][0] + trial[1][0]) / nComp;
		double chi = Math.pow((exp00 - trial[0][0]), 2) / exp00;
		double exp01 = (trial[0][0] + trial[0][1]) * (trial[0][1] + trial[1][1]) / nComp;
		chi += Math.pow((exp01 - trial[0][1]), 2) / exp01;
		double exp10 = (trial[0][0] + trial[1][0]) * (trial[1][0] + trial[1][1]) / nComp;
		chi += Math.pow((exp10 - trial[1][0]), 2) / exp10;
		double exp11 = (trial[0][1] + trial[1][1]) * (trial[1][0] + trial[1][1]) / nComp;
		chi += Math.pow((exp11 - trial[1][1]), 2) / exp11;
		return chi;
	}

	public static double getFisherExact(double trail[][]) {

		double row0 = trail[0][0] + trail[0][1];
		double row1 = trail[1][0] + trail[1][1];

		double col0 = trail[0][0] + trail[1][0];
		double col1 = trail[0][1] + trail[1][1];

		ArrayList<Double[][]> allComb = new ArrayList<Double[][]>();

		ArrayList<Double> allProbs = new ArrayList<Double>();

		Double[][] observedMatrix = new Double[2][2];

		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				observedMatrix[i][j] = trail[i][j];
			}
		}

		double threshProb = getFisherProbability(observedMatrix);

		for (int i = 0; i <= col0; i++) {
			double a = i;
			double b = row0 - a;
			double c = col0 - a;
			double d = col1 - b;

			if ((c + d) == row1 && a >= 0 && b >= 0 && c >= 0 && d >= 0) {
				// condition satisfied
				Double[][] temp = new Double[2][2];
				temp[0][0] = a;
				temp[0][1] = b;
				temp[1][0] = c;
				temp[1][1] = d;
				allComb.add(temp);
				allProbs.add(getFisherProbability(temp));
			}

		}

		double totalProbability = 0;
		for (int i = 0; i < allProbs.size(); i++) {
			if (allProbs.get(i) <= threshProb) {
				totalProbability += allProbs.get(i);
			}
		}
		return totalProbability;

	}

	private static double getFisherProbability(Double[][] trail) {

		double a = trail[0][0];
		double b = trail[0][1];
		double c = trail[1][0];
		double d = trail[1][1];

		double binomialAB = binomialCoeff((a + b), a);
		double binomialCD = binomialCoeff((c + d), c);
		double binomialN = binomialCoeff((a + b + c + d), (a + c));

		Double p = (binomialAB * binomialCD) / binomialN;

		if (p.isNaN() || p.isInfinite()) {
			p = naturalLogfact(a + b) + naturalLogfact(c + d) + naturalLogfact(a + c) + naturalLogfact(b + d)
					- naturalLogfact(a) - naturalLogfact(b) - naturalLogfact(c) - naturalLogfact(d)
					- naturalLogfact(a + b + c + d);
			p = Math.pow(Math.E, p);
		}
		// System.out.println(p+" Array: "+a+" "+b+" "+c+" "+" "+d);
		return p;
	}

	private static double binomialCoeff(double n, double k) {
		double res = 1;
		// Since C(n, k) = C(n, n-k)
		if (k > n - k)
			k = n - k;
		// Calculate value of [n * (n-1) *---* (n-k+1)] / [k * (k-1) *----* 1]
		for (int i = 0; i < k; ++i) {
			res *= (n - i);
			res /= (i + 1);
		}
		return res;
	}

	private static double naturalLogfact(double n) {
		double lnN = 0;
		if (n > 0)
			lnN = (n * Math.log(n) - n + 0.5 * Math.log(2 * Math.PI * n));
		else
			lnN = 0;
		return lnN;
	}

	/*
	 * public static ArrayList<Double> compare(int[] predictedY, int [] observedY){
	 * ArrayList<Integer> nonPredict = new ArrayList<Integer> (); ArrayList<Double>
	 * results=new ArrayList<Double>(); double[][] confusion_matrix = new
	 * double[2][2];
	 * 
	 * double predictedToxic=0; double predictedNonToxic=0;
	 * 
	 * double accurateToxic=0; double accurateNonToxic=0;
	 * 
	 * for(int i = 0; i < 2; i++){ for(int j = 0; j < 2; j++){
	 * confusion_matrix[i][j] = 0; } } double nonPredicted = 0, accurate = 0,
	 * inaccurate = 0; for(int nC = 0; nC < observedY.length; nC++){
	 * if(predictedY[nC] == -1){ nonPredicted++; nonPredict.add(nC); continue; }else
	 * if(predictedY[nC] == observedY[nC] ){ accurate++; if(predictedY[nC]==1)
	 * accurateToxic++; else if(predictedY[nC]==0) accurateNonToxic++; }else{
	 * //System.out.println(predictedY[nC]+"\t"+observedY[nC]); inaccurate++; }
	 * 
	 * if(predictedY[nC]==1) { predictedToxic++; }else if(predictedY[nC]==0) {
	 * predictedNonToxic++; }
	 * 
	 * confusion_matrix[observedY[nC]][predictedY[nC]] += 1; }
	 * System.out.print("\nNon Predicted: "+nonPredicted + ", Accurate: "+accurate +
	 * " Inaccurate: " + inaccurate + " Non Predicted(%): " + (100 * nonPredicted
	 * /observedY.length) + " Accuracy: " + (100 * accurate / (accurate +
	 * inaccurate)) + ""); double sensitivity = confusion_matrix[1][1] /
	 * (confusion_matrix[1][1] + confusion_matrix[1][0]); double specificity =
	 * confusion_matrix[0][0] / (confusion_matrix[0][0] + confusion_matrix[0][1]);
	 * 
	 * //System.out.print(" Sensitivity: " + sensitivity + " Specificity: " +
	 * specificity + "\n\n");
	 * 
	 * //System.out.println("TP: "+confusion_matrix[1][1]+", TN: "+confusion_matrix[
	 * 0][0]+", FN: "+confusion_matrix[1][0]+", FP: "+confusion_matrix[0][1]);
	 * double accuracy=(100 * accurate / (accurate + inaccurate)); double
	 * nonPredictedPercent=(100 * nonPredicted /observedY.length);
	 * 
	 * 
	 * results.add(predictedY.length-nonPredicted);//Predicted
	 * results.add(nonPredicted);//Non Predicted results.add(accurate);//accurate
	 * results.add(inaccurate);// inaccurate results.add(accuracy);//accuracy
	 * results.add(100-nonPredictedPercent);// Predicted Percentage
	 * results.add(sensitivity);//sensitivity results.add(specificity);//specificity
	 * results.add(predictedToxic);//PredictedToxic
	 * results.add(predictedNonToxic);//Predicted Non toxic
	 * results.add(accurateToxic);// Accurate Toxic
	 * results.add(accurateNonToxic);//Accureate Non Toxic
	 * System.out.println(nonPredict.toString()); return results; }
	 */

	/*
	 * public static void writePredctns2File(int[][] dataX, int[] dataY, String
	 * fileName) throws Exception { PrintWriter output = new PrintWriter(fileName);
	 * String delimiter = ","; for (int i = 0; i < dataX.length; i++) {
	 * output.print(dataY[i]); for (int j = 0; j < dataX[0].length; j++) {
	 * output.print(delimiter + dataX[i][j]); } output.print("\n"); }
	 * output.flush(); output.close(); }
	 * 
	 * public static void shuffleArray(int[] ar) { // If running on Java 6 or older,
	 * use `new Random()` on RHS here Random rnd = ThreadLocalRandom.current(); for
	 * (int i = ar.length - 1; i > 0; i--) { int index = rnd.nextInt(i + 1); //
	 * Simple swap int a = ar[index]; ar[index] = ar[i]; ar[i] = a; } }
	 */

}
