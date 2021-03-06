package GeneticProgrammes;

import java.io.FileNotFoundException;
import java.util.List;

import org.jgap.InvalidConfigurationException;
import org.jgap.gp.GPProblem;
import org.jgap.gp.impl.DefaultGPFitnessEvaluator;
import org.jgap.gp.impl.GPConfiguration;
import org.jgap.gp.impl.GPGenotype;

import DAO.DataSet;
import DAO.Instance;
import Util.GPClassifier;

public class GPClassification {
    private static final String FILENAME = "breast-cancer-wisconsin.data";
    private static final String TRAINING_FILENAME = "training.txt";
    private static final String TEST_FILENAME = "test.txt";
    private static final int MAXIMUM_EVOLUTIONS = 800;
    private static final double TERMINATION_CRITERIA = 98;

    public static void main(String[] args) throws FileNotFoundException, InvalidConfigurationException {
        // Load data
        List<Instance> trainingData = new DataSet(TRAINING_FILENAME).loadDataSet();
        List<Instance> testData = new DataSet(TEST_FILENAME).loadDataSet();
        GPGenotype gp;
		try {
			gp = configureGeneticProgram(trainingData);
			evolveGeneticPopulation(gp);
			gp.outputSolution(gp.getAllTimeBest());
			classifyTestData(testData,gp);
		} catch (InvalidConfigurationException e) {
			System.err.println("Invalid configuration. Exiting program");
		}
    }

    public static GPGenotype configureGeneticProgram(List<Instance> trainingData) throws InvalidConfigurationException{
    	// Setup the algorithm's parameters.
        // ---------------------------------
        GPConfiguration config = new GPConfiguration();
        // We use a default fitness evaluator (DefaultGPFitnessEvaluator) because we compute classification accuracy, not
        // a defect rate (DeltaGPFitnessEvaluator)!
        // ----------------------------------------------------------------------
        config.setGPFitnessEvaluator(new DefaultGPFitnessEvaluator());
        config.setMaxInitDepth(6); //max depth of initial depth
        config.setPopulationSize(1000);
        config.setMaxCrossoverDepth(8); //max depth of tree generated by cross over
        config.setCrossoverProb(0.9f);
        config.setMutationProb(0.05f);
        config.setReproductionProb(0.05f);
        config.setFitnessFunction(new GPClassifier.FitnessFunction());
        config.setStrictProgramCreation(false);
        GPProblem problem = new GPClassifier(trainingData, config);
        // Create the genotype of the problem, i.e., define the GP commands and
        // terminals that can be used, and constrain the structure of the GP
        // program.
        // --------------------------------------------------------------------
        GPGenotype gp = problem.create();
        gp.setVerboseOutput(true);
        return gp;
    }
    
    private static void classifyTestData(List<Instance> testData, GPGenotype gp) {
        
        GPClassifier.instances = testData;

        double result = new GPClassifier.FitnessFunction().computeRawFitness(gp.getAllTimeBest());
        result = (double) Math.round(result * 100) / 100;

        System.out.println("Percentage of test instances correctly classified: " + result);
    }
    
    private static void evolveGeneticPopulation(GPGenotype geneticProgram) {
        for (int generations = 0; generations < MAXIMUM_EVOLUTIONS; generations += 1) {
            geneticProgram.evolve(1);
            if (geneticProgram.getAllTimeBest() != null && geneticProgram.getAllTimeBest().getFitnessValue() >= TERMINATION_CRITERIA) {
                System.out.println("============================\n"
					+ "TERMINATION CRITERIA REACHED"
					+ "IN " + generations + " generations"
					+ "\n============================\n"
    				+ "Solution with classification accuracy >= " + TERMINATION_CRITERIA + " was found.\n"
					+ "");
            break;
            }
        }
    }
}
