package org.skaggs.ec.examples.numarical;

import org.skaggs.ec.population.PopulationGenerator;
import org.skaggs.ec.population.individual.Individual;
import org.skaggs.ec.properties.Key;
import org.skaggs.ec.properties.Properties;
import org.skaggs.ec.properties.Requirement;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by skaggsm on 12/27/15.
 */
public class DoubleArrayPopulationGenerator implements PopulationGenerator<double[]> {
    @Override
    public List<Individual<double[]>> generatePopulation(int num, Properties properties) {
        Random r = ThreadLocalRandom.current();
        double min = properties.getDouble(Key.DoubleKey.RANDOM_DOUBLE_GENERATION_MINIMUM);
        double max = properties.getDouble(Key.DoubleKey.RANDOM_DOUBLE_GENERATION_MAXIMUM);
        int length = properties.getInt(Key.IntKey.DOUBLE_ARRAY_GENERATION_LENGTH);
        double initialMutationStrength = properties.getDouble(Key.DoubleKey.INITIAL_MUTATION_STRENGTH);
        double initialMutationProbability = properties.getDouble(Key.DoubleKey.INITIAL_MUTATION_PROBABILITY);
        double initialCrossoverStrength = properties.getDouble(Key.DoubleKey.INITIAL_CROSSOVER_STRENGTH);
        double initialCrossoverProbability = properties.getDouble(Key.DoubleKey.INITIAL_CROSSOVER_PROBABILITY);

        int numAspects = properties.getInt(Key.IntKey.ASPECT_COUNT);
        double[] aspects = new double[numAspects * 2];
        aspects[0] = initialCrossoverStrength;
        aspects[1] = initialCrossoverProbability;
        aspects[2] = initialMutationStrength;
        aspects[3] = initialMutationProbability;

        List<Individual<double[]>> individuals = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            individuals.add(new Individual<>(this.getIndividual(r, length, min, max), aspects.clone()));
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
        return new Key[]{
                Key.DoubleKey.RANDOM_DOUBLE_GENERATION_MINIMUM, Key.DoubleKey.RANDOM_DOUBLE_GENERATION_MAXIMUM, Key.IntKey.DOUBLE_ARRAY_GENERATION_LENGTH,
                Key.DoubleKey.INITIAL_MUTATION_STRENGTH, Key.DoubleKey.INITIAL_MUTATION_PROBABILITY, Key.DoubleKey.INITIAL_CROSSOVER_STRENGTH, Key.DoubleKey.INITIAL_CROSSOVER_PROBABILITY,
                Key.IntKey.ASPECT_COUNT
        };
    }

    @Override
    public Requirement[] requestDetailedRequirements() {
        return new Requirement[]{
                new Requirement() {
                    @Override
                    public String describe() {
                        return "Number of aspects must be >= 2 (Array size must be >= 4)";
                    }

                    @Override
                    public boolean test(Properties properties) {
                        return properties.getInt(Key.IntKey.ASPECT_COUNT) >= 2;
                    }
                }
        };
    }
}
