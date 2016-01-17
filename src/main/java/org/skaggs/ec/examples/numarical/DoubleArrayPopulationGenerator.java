package org.skaggs.ec.examples.numarical;

import org.skaggs.ec.population.PopulationGenerator;
import org.skaggs.ec.population.individual.Individual;
import org.skaggs.ec.properties.Key;
import org.skaggs.ec.properties.Properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by skaggsm on 12/27/15.
 */
public class DoubleArrayPopulationGenerator implements PopulationGenerator<double[]> {
    @Override
    public List<Individual<double[]>> generatePopulation(int num, Properties properties) {
        Random r = new Random();
        double min = properties.getDouble(Key.DoubleKey.RANDOM_DOUBLE_GENERATION_MINIMUM);
        double max = properties.getDouble(Key.DoubleKey.RANDOM_DOUBLE_GENERATION_MAXIMUM);
        int length = properties.getInt(Key.IntKey.DOUBLE_ARRAY_GENERATION_LENGTH);

        List<Individual<double[]>> individuals = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            individuals.add(new Individual<>(this.getIndividual(r, length, min, max)));
        }
        return individuals;
    }

    private double[] getIndividual(Random r, int length, double min, double max) {
        double[] val = new double[length];
        for (int i = 0; i < val.length; i++) {
            val[i] = this.getRandomDouble(r, min, max);
        }
        return val;
    }

    private double getRandomDouble(Random r, double min, double max) {
        return (r.nextDouble() * (max - min)) + min;
    }

    @Override
    public Key[] requestProperties() {
        return new Key[]{Key.DoubleKey.RANDOM_DOUBLE_GENERATION_MINIMUM, Key.DoubleKey.RANDOM_DOUBLE_GENERATION_MAXIMUM, Key.IntKey.DOUBLE_ARRAY_GENERATION_LENGTH};
    }
}
