package org.skaggs.ec.examples.deb;

import org.apache.commons.math3.util.FastMath;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.skaggs.ec.OptimizationFunction;
import org.skaggs.ec.examples.numarical.DoubleArrayPopulationGenerator;
import org.skaggs.ec.examples.numarical.SimpleDoubleArrayMutationOperator;
import org.skaggs.ec.multiobjective.NSGA_II;
import org.skaggs.ec.multiobjective.population.Front;
import org.skaggs.ec.multiobjective.population.FrontedIndividual;
import org.skaggs.ec.operators.Operator;
import org.skaggs.ec.population.PopulationGenerator;
import org.skaggs.ec.properties.Key;
import org.skaggs.ec.properties.Properties;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.function.ToDoubleFunction;

/**
 * Created by skaggsm on 12/27/15.
 */
public final class POL {
    private POL() {
    }

    public static void main(String[] args) throws InterruptedException, InvocationTargetException {
        //Thread.sleep(7000);
        XYSeriesCollection collection = new XYSeriesCollection();
        JFreeChart chart = ChartFactory.createScatterPlot("POL", "Function 1", "Function 2", collection, PlotOrientation.VERTICAL, true, true, false);
        chart.getXYPlot().setRenderer(new XYLineAndShapeRenderer(true, true));
        //noinspection MagicNumber
        chart.getXYPlot().getDomainAxis().setRange(0, 20);
        //noinspection MagicNumber
        chart.getXYPlot().getRangeAxis().setRange(0, 25);
        ChartPanel panel = new ChartPanel(chart);
        JFrame frame = new JFrame();
        frame.add(panel);
        //noinspection MagicNumber
        frame.setSize(800, 800);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //noinspection MagicNumber
        Properties properties = new Properties()
                .setBoolean(Key.BooleanKey.THREADED, true)
                .setInt(Key.IntKey.POPULATION_SIZE, 500)
                .setDouble(Key.DoubleKey.RANDOM_DOUBLE_GENERATION_MINIMUM, -FastMath.PI)
                .setDouble(Key.DoubleKey.RANDOM_DOUBLE_GENERATION_MAXIMUM, FastMath.PI)
                .setInt(Key.IntKey.DOUBLE_ARRAY_GENERATION_LENGTH, 2)

                .setDouble(Key.DoubleKey.INITIAL_MUTATION_STRENGTH, .125 / 256)
                .setDouble(Key.DoubleKey.INITIAL_MUTATION_PROBABILITY, .5)

                .setDouble(Key.DoubleKey.MUTATION_STRENGTH_MUTATION_STRENGTH, .0625)
                .setDouble(Key.DoubleKey.MUTATION_STRENGTH_MUTATION_PROBABILITY, .125)

                .setDouble(Key.DoubleKey.MUTATION_PROBABILITY_MUTATION_STRENGTH, .0625)
                .setDouble(Key.DoubleKey.MUTATION_PROBABILITY_MUTATION_PROBABILITY, .125);

        Operator<double[]> operator = new SimpleDoubleArrayMutationOperator();
        OptimizationFunction<double[]> function1 = new Function1();
        OptimizationFunction<double[]> function2 = new Function2();
        List<OptimizationFunction<double[]>> optimizationFunctions = Arrays.asList(function1, function2);
        PopulationGenerator<double[]> populationGenerator = new DoubleArrayPopulationGenerator();

        NSGA_II<double[]> nsga_ii = new NSGA_II<>(properties, operator, optimizationFunctions, populationGenerator);

        nsga_ii.addObserver(populationData -> {
            //System.out.println("\rElapsed time in generation " + populationData.getCurrentGeneration() + ": " + (populationData.getElapsedTime() / 1000000f) + "ms");
            System.out.println(((float) populationData.getTruncatedPopulation().getPopulation().parallelStream().mapToDouble(new ToDoubleFunction<FrontedIndividual<double[]>>() {
                @Override
                public double applyAsDouble(FrontedIndividual<double[]> value) {
                    return value.mutationProbability;
                }
            }).average().orElse(-1)));

            collection.removeAllSeries();
            for (Front<double[]> front : populationData.getFrontedPopulation().getFronts()) {
                    XYSeries frontSeries = new XYSeries(front.toString());
                    for (FrontedIndividual<double[]> individual : front.getMembers()) {
                        frontSeries.add(individual.getScore(function1), individual.getScore(function2));
                    }
                    collection.addSeries(frontSeries);
            }
        });


        //noinspection MagicNumber
        for (int i = 0; i < 1000000; i++) {
            SwingUtilities.invokeAndWait(nsga_ii::runGeneration);
            //noinspection MagicNumber
            //Thread.sleep(1000);
        }
    }

    static class Function1 implements OptimizationFunction<double[]> {
        @SuppressWarnings("MagicNumber")
        @Override
        public double evaluate(double[] vector, Properties properties) {
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
        public Key[] requestProperties() {
            return new Key[0];
        }

        @Override
        public double min(Properties properties) {
            return 0;
        }


        @SuppressWarnings("MagicNumber")
        @Override
        public double max(Properties properties) {
            return 30;
        }


    }

    static class Function2 implements OptimizationFunction<double[]> {
        @Override
        public double evaluate(double[] object, Properties properties) {
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
}
