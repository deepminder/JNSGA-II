package org.skaggs.ec.examples.deb;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.FastMath;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;
import org.skaggs.ec.DefaultOptimizationFunction;
import org.skaggs.ec.OptimizationFunction;
import org.skaggs.ec.examples.defaultoperatorframework.DoubleArrayAverageRecombiner;
import org.skaggs.ec.examples.defaultoperatorframework.DoubleArraySpeciator;
import org.skaggs.ec.examples.defaultoperatorframework.RouletteWheelLinearSelection;
import org.skaggs.ec.examples.numarical.DoubleArrayPopulationGenerator;
import org.skaggs.ec.multiobjective.NSGA_II;
import org.skaggs.ec.multiobjective.population.Front;
import org.skaggs.ec.multiobjective.population.FrontedIndividual;
import org.skaggs.ec.operators.DefaultOperator;
import org.skaggs.ec.operators.Mutator;
import org.skaggs.ec.operators.Operator;
import org.skaggs.ec.population.PopulationGenerator;
import org.skaggs.ec.properties.Key;
import org.skaggs.ec.properties.Properties;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.text.AttributedString;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by skaggsm on 12/27/15.
 */
public final class POL {
    private POL() {
    }

    public static void main(String[] args) throws InterruptedException, InvocationTargetException {
        //Thread.sleep(5000);

        XYErrorRenderer averagePlotRenderer = new XYErrorRenderer();

        XYSeriesCollection currentGenerationCollection = new XYSeriesCollection();
        JFreeChart currentGenerationChart = ChartFactory.createScatterPlot("Functions", "Function 1", "Function 2", currentGenerationCollection, PlotOrientation.VERTICAL, true, false, false);
        currentGenerationChart.getXYPlot().setRenderer(new XYLineAndShapeRenderer(true, true));
        ChartPanel currentGenerationPanel = new ChartPanel(currentGenerationChart);

        XYSeriesCollection currentPopulationCollection = new XYSeriesCollection();
        JFreeChart currentPopulationChart = ChartFactory.createScatterPlot("Individuals", "", "", currentPopulationCollection, PlotOrientation.VERTICAL, true, false, false);
        currentPopulationChart.getXYPlot().getDomainAxis().setAttributedLabel(new AttributedString("X\u2081"));
        currentPopulationChart.getXYPlot().getRangeAxis().setAttributedLabel(new AttributedString("X\u2082"));
        ChartPanel currentPopulationPanel = new ChartPanel(currentPopulationChart);

        YIntervalSeriesCollection averageAspectStrengthCollection = new YIntervalSeriesCollection();
        JFreeChart averageMutationStrengthChart = ChartFactory.createScatterPlot("Average Aspect Strengths", "Generation", "Y", averageAspectStrengthCollection, PlotOrientation.VERTICAL, true, false, false);
        averageMutationStrengthChart.getXYPlot().setRenderer(averagePlotRenderer);
        ChartPanel averageMutationStrengthPanel = new ChartPanel(averageMutationStrengthChart);

        YIntervalSeriesCollection averageAspectProbabilityCollection = new YIntervalSeriesCollection();
        JFreeChart averageMutationProbabilityChart = ChartFactory.createScatterPlot("Average Aspect Probabilities", "Generation", "Y", averageAspectProbabilityCollection, PlotOrientation.VERTICAL, true, false, false);
        averageMutationProbabilityChart.getXYPlot().setRenderer(averagePlotRenderer);
        ChartPanel averageMutationProbabilityPanel = new ChartPanel(averageMutationProbabilityChart);

        JFrame windowFrame = new JFrame("Evolutionary Algorithm");
        JPanel mainPanel = new JPanel();
        windowFrame.setLayout(new BorderLayout());
        windowFrame.add(mainPanel, BorderLayout.CENTER);

        GroupLayout groupLayout = new GroupLayout(mainPanel);
        mainPanel.setLayout(groupLayout);
        groupLayout.setAutoCreateGaps(true);
        groupLayout.setAutoCreateContainerGaps(true);

        groupLayout.setHorizontalGroup(groupLayout.createSequentialGroup()
                        .addGroup(groupLayout.createParallelGroup()
                                .addComponent(currentGenerationPanel)
                                .addComponent(currentPopulationPanel))
                        .addGroup(groupLayout.createParallelGroup()
                                .addComponent(averageMutationStrengthPanel)
                                .addComponent(averageMutationProbabilityPanel))
        );
        groupLayout.setVerticalGroup(groupLayout.createSequentialGroup()
                .addGroup(groupLayout.createParallelGroup()
                        .addComponent(currentGenerationPanel)
                        .addComponent(averageMutationStrengthPanel))
                        .addGroup(groupLayout.createParallelGroup()
                                .addComponent(currentPopulationPanel)
                                .addComponent(averageMutationProbabilityPanel))
        );

        //noinspection MagicNumber
        windowFrame.setSize(1400, 1000);
        //noinspection MagicNumber
        windowFrame.setLocation(0, 0);
        windowFrame.setVisible(true);
        windowFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //noinspection MagicNumber
        Properties properties = new Properties()
                .setBoolean(Key.BooleanKey.DefaultBooleanKey.THREADED, true)
                .setInt(Key.IntKey.DefaultIntKey.POPULATION_SIZE, 1000)
                .setInt(Key.IntKey.DefaultIntKey.ASPECT_COUNT, 3)
                .setDouble(Key.DoubleKey.DefaultDoubleKey.RANDOM_DOUBLE_GENERATION_MINIMUM, -10)//-FastMath.PI)
                .setDouble(Key.DoubleKey.DefaultDoubleKey.RANDOM_DOUBLE_GENERATION_MAXIMUM, 10)//FastMath.PI)
                .setInt(Key.IntKey.DefaultIntKey.DOUBLE_ARRAY_GENERATION_LENGTH, 2)
                .setDouble(Key.DoubleKey.DefaultDoubleKey.DOUBLE_SPECIATION_MAX_DISTANCE, .5)

                .setValue(Key.DoubleKey.DefaultDoubleKey.INITIAL_ASPECT_ARRAY, new double[]{0, 1, 0, 1, 0, 1})

                .setDouble(Key.DoubleKey.DefaultDoubleKey.MUTATION_STRENGTH_MUTATION_STRENGTH, .125 / 16)
                .setDouble(Key.DoubleKey.DefaultDoubleKey.MUTATION_STRENGTH_MUTATION_PROBABILITY, 1)

                .setDouble(Key.DoubleKey.DefaultDoubleKey.MUTATION_PROBABILITY_MUTATION_STRENGTH, .125 / 16)
                .setDouble(Key.DoubleKey.DefaultDoubleKey.MUTATION_PROBABILITY_MUTATION_PROBABILITY, 1)

                .setDouble(Key.DoubleKey.DefaultDoubleKey.CROSSOVER_STRENGTH_MUTATION_STRENGTH, .125 / 16)
                .setDouble(Key.DoubleKey.DefaultDoubleKey.CROSSOVER_STRENGTH_MUTATION_PROBABILITY, 1)

                .setDouble(Key.DoubleKey.DefaultDoubleKey.CROSSOVER_PROBABILITY_MUTATION_STRENGTH, .125 / 16)
                .setDouble(Key.DoubleKey.DefaultDoubleKey.CROSSOVER_PROBABILITY_MUTATION_PROBABILITY, 1);

        Operator<double[]> operator = new DefaultOperator<>(Arrays.asList(
                new Mutator<double[]>() {
                    @Override
                    protected double[] mutate(double[] object, double mutationStrength, double mutationProbability) {
                        Random r = ThreadLocalRandom.current();
                        double[] newObject = new double[object.length];
                        for (int i = 0; i < newObject.length; i++) {
                            newObject[i] = (r.nextGaussian() * mutationStrength) + object[i];
                            //newObject[i] = Mutator.mutate(object[i], r, mutationStrength);
                        }
                        return newObject;
                    }
                },
                new Mutator<double[]>() {
                    @Override
                    protected double[] mutate(double[] object, double mutationStrength, double mutationProbability) {
                        Random r = ThreadLocalRandom.current();
                        double[] newObject = new double[object.length];
                        for (int i = 0; i < newObject.length; i++) {
                            newObject[i] = Mutator.mutate(object[i], r, mutationStrength);
                        }
                        return newObject;
                    }
                }
        ), new DoubleArrayAverageRecombiner(), new RouletteWheelLinearSelection<>(), new DoubleArraySpeciator());
        OptimizationFunction<double[]> function1 = new Function1();
        OptimizationFunction<double[]> function2 = new Function2();
        OptimizationFunction<double[]> function3 = new Function3();
        OptimizationFunction<double[]> function4 = new Function4();
        @SuppressWarnings("unchecked")
        OptimizationFunction<double[]>[] optimizationFunctions = new OptimizationFunction[]{function1, function2};
        PopulationGenerator<double[]> populationGenerator = new DoubleArrayPopulationGenerator();

        NSGA_II<double[]> nsga_ii = new NSGA_II<>(properties, operator, optimizationFunctions, populationGenerator);

        nsga_ii.addObserver(populationData -> {
            currentGenerationChart.setNotify(false);
            currentGenerationCollection.removeAllSeries();
            for (Front<double[]> front : populationData.getTruncatedPopulation().getFronts()) {
                XYSeries frontSeries = new XYSeries(front.toString());
                for (FrontedIndividual<double[]> individual : front.getMembers()) {
                    frontSeries.add(individual.getScore(0), individual.getScore(1));
                }
                currentGenerationCollection.addSeries(frontSeries);
            }
            currentGenerationChart.setNotify(true);
        });

        nsga_ii.addObserver(populationData -> {
            currentPopulationChart.setNotify(false);
            currentPopulationCollection.removeAllSeries();
            for (Front<double[]> front : populationData.getTruncatedPopulation().getFronts()) {
                XYSeries frontSeries = new XYSeries(front.toString());
                for (FrontedIndividual<double[]> individual : front.getMembers()) {
                    frontSeries.add(individual.getIndividual()[0], individual.getIndividual()[1]);
                }
                currentPopulationCollection.addSeries(frontSeries);
            }
            currentPopulationChart.setNotify(true);
        });

        nsga_ii.addObserver(populationData -> {
            double elapsedTimeMS = (populationData.getElapsedTime() / 1000000d);
            double observationTimeMS = (populationData.getPreviousObservationTime() / 1000000d);
            System.out.print("Elapsed time in generation " + populationData.getCurrentGeneration() + ": " + String.format("%.4f", elapsedTimeMS) + "ms, with " + String.format("%.4f", observationTimeMS) + "ms observation time");

            for (int i = 0; i < properties.getInt(Key.IntKey.DefaultIntKey.ASPECT_COUNT); i++) {
                final int finalI = i;
                YIntervalSeries aspectStrengthSeries, aspectProbabilitySeries;
                DescriptiveStatistics aspectStrengthSummary, aspectProbabilitySummary;

                try {
                    aspectStrengthSeries = averageAspectStrengthCollection.getSeries(i);
                } catch (IllegalArgumentException e) {
                    aspectStrengthSeries = new YIntervalSeries("Median Aspect " + i + " Strength");
                    averageAspectStrengthCollection.addSeries(aspectStrengthSeries);
                }
                try {
                    aspectProbabilitySeries = averageAspectProbabilityCollection.getSeries(i);
                } catch (IllegalArgumentException e) {
                    aspectProbabilitySeries = new YIntervalSeries("Median Aspect " + i + " Probability");
                    averageAspectProbabilityCollection.addSeries(aspectProbabilitySeries);
                }

                aspectStrengthSummary = new DescriptiveStatistics(populationData.getTruncatedPopulation().getPopulation().parallelStream().mapToDouble(value -> value.aspects[(finalI * 2)]).toArray());
                aspectProbabilitySummary = new DescriptiveStatistics(populationData.getTruncatedPopulation().getPopulation().parallelStream().mapToDouble(value -> value.aspects[(finalI * 2) + 1]).toArray());

                //double aspectStrengthMedian = aspectStrengthSummary.getPercentile(50);
                //double aspectProbabilityMedian = aspectProbabilitySummary.getPercentile(50);
                aspectStrengthSeries.add(populationData.getCurrentGeneration(), aspectStrengthSummary.getPercentile(50), aspectStrengthSummary.getPercentile(25), aspectStrengthSummary.getPercentile(75));
                aspectProbabilitySeries.add(populationData.getCurrentGeneration(), aspectProbabilitySummary.getPercentile(50), aspectProbabilitySummary.getPercentile(25), aspectProbabilitySummary.getPercentile(75));
            }
        });


        //noinspection MagicNumber
        for (int i = 0; i < 1000000; i++) {
            long startTime = System.nanoTime();
            EventQueue.invokeAndWait(nsga_ii::runGeneration);
            long elapsedTime = System.nanoTime() - startTime;
            System.out.println("; Total elapsed time: " + String.format("%.4f", elapsedTime / 1000000d) + "ms");
            //noinspection MagicNumber
            //Thread.sleep(200);
        }
    }

    static class Function1 extends DefaultOptimizationFunction<double[]> {
        @SuppressWarnings("MagicNumber")
        @Override
        public double evaluateIndividual(double[] vector, Properties properties) {
            assert vector.length == 2;
            final double A1 = (((.5 * FastMath.sin(1)) - (2 * FastMath.cos(1))) + FastMath.sin(2)) - (1.5 * FastMath.cos(2));
            final double A2 = (((1.5 * FastMath.sin(1)) - FastMath.cos(1)) + (2 * FastMath.sin(2))) - (.5 * FastMath.cos(2));
            final double B1 = (((.5 * FastMath.sin(vector[0])) - (2 * FastMath.cos(vector[0]))) + FastMath.sin(vector[1])) - (1.5 * FastMath.cos(vector[1]));
            final double B2 = (((1.5 * FastMath.sin(vector[0])) - FastMath.cos(vector[0])) + (2 * FastMath.sin(vector[1]))) - (.5 * FastMath.cos(vector[1]));
            return 1 + FastMath.pow(A1 - B1, 2) + FastMath.pow(A2 - B2, 2);
        }

        @Override
        public int compare(Double o1, Double o2) {
            return -Double.compare(o1, o2);
        }

        @Override
        public double min(Properties properties) {
            return 0;
        }

        @Override
        public Key[] requestProperties() {
            return new Key[0];
        }


        @SuppressWarnings("MagicNumber")
        @Override
        public double max(Properties properties) {
            return 30;
        }


    }

    static class Function2 extends DefaultOptimizationFunction<double[]> {
        @Override
        public double evaluateIndividual(double[] object, Properties properties) {
            assert object.length == 2;
            return FastMath.pow(object[0] + 3, 2) + FastMath.pow(object[1] + 1, 2);
        }

        @Override
        public double min(Properties properties) {
            return 0;
        }

        @Override
        public double max(Properties properties) {
            return FastMath.pow(FastMath.PI + 3, 2) + FastMath.pow(FastMath.PI + 1, 2);
        }

        @Override
        public int compare(Double o1, Double o2) {
            return -Double.compare(o1, o2);
        }

        @Override
        public Key[] requestProperties() {
            return new Key[0];
        }
    }

    static class Function3 extends DefaultOptimizationFunction<double[]> {
        @Override
        public double evaluateIndividual(double[] object, Properties properties) {
            assert object.length == 3;
            double result = 0;
            for (int i = 0; i < object.length - 1; i++) {
                result += -10 * FastMath.exp(-0.2 * FastMath.sqrt(FastMath.pow(object[i], 2) + FastMath.pow(object[i + 1], 2)));
            }
            return result;
        }

        @Override
        public double min(Properties properties) {
            return 0;
        }

        @Override
        public double max(Properties properties) {
            return 1;
        }

        @Override
        public int compare(Double o1, Double o2) {
            return -Double.compare(o1, o2);
        }

        @Override
        public Key[] requestProperties() {
            return new Key[0];
        }
    }

    static class Function4 extends DefaultOptimizationFunction<double[]> {
        @Override
        public double evaluateIndividual(double[] object, Properties properties) {
            assert object.length == 3;
            double result = 0;
            for (int i = 0; i < object.length; i++) {
                result += FastMath.pow(FastMath.abs(object[i]), 0.8) + 5 * FastMath.sin(FastMath.pow(object[i], 3));
            }
            return result;
        }

        @Override
        public double min(Properties properties) {
            return 0;
        }

        @Override
        public double max(Properties properties) {
            return 1;
        }

        @Override
        public int compare(Double o1, Double o2) {
            return -Double.compare(o1, o2);
        }

        @Override
        public Key[] requestProperties() {
            return new Key[0];
        }
    }
}
