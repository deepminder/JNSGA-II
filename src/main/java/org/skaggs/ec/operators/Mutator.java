package org.skaggs.ec.operators;

import org.skaggs.ec.population.individual.Individual;
import org.skaggs.ec.properties.*;
import org.skaggs.ec.util.Range;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

/**
 * Created by skaggsm on 1/22/16.
 */
public abstract class Mutator<E> implements Function<Individual<E>, Individual<E>>, HasPropertyRequirements, LateUpdatingProperties, HasAspectRequirements {

    private int startIndex;
    private double[] aspectModificationArray;

    public static double mutate(double d, Random r, double range) {
        return (d + (r.nextDouble() * 2 * range)) - range;
    }

    @Override
    public int requestAspectLocation(int startIndex) {
        this.startIndex = startIndex;
        return 2;
    }

    @Override
    public String[] getAspectDescriptions() {
        return new String[]{"Mutation Strength", "Mutation Probability"};
    }

    @Override
    public void updateProperties(Properties properties) {
        aspectModificationArray = (double[]) properties.getValue(Key.DoubleKey.DefaultDoubleKey.ASPECT_MODIFICATION_ARRAY);
    }

    @Override
    public Individual<E> apply(Individual<E> e) {
        Random r = ThreadLocalRandom.current();

        double[] newAspects = e.aspects.clone();

        if (r.nextDouble() < aspectModificationArray[startIndex * 2 + 1])
            newAspects[startIndex] = Mutator.mutate(newAspects[startIndex], r, aspectModificationArray[startIndex * 2]);
        if (r.nextDouble() < aspectModificationArray[startIndex * 2 + 3])
            newAspects[startIndex + 1] = Mutator.mutate(newAspects[startIndex + 1], r, aspectModificationArray[startIndex * 2 + 2]);

        newAspects[startIndex] = Range.clip(0, newAspects[startIndex], Double.POSITIVE_INFINITY);
        newAspects[startIndex + 1] = Range.clip(0, newAspects[startIndex + 1], 1);

        E individual = e.getIndividual();

        if (r.nextDouble() < newAspects[startIndex + 1])
            individual = mutate(e.getIndividual(), newAspects[startIndex], newAspects[startIndex + 1]);

        return new Individual<>(individual, newAspects);
    }

    protected abstract E mutate(E object, double mutationStrength, double mutationProbability);

    @Override
    public Key[] requestProperties() {
        return new Key[]{
                Key.DoubleKey.DefaultDoubleKey.ASPECT_MODIFICATION_ARRAY
        };
    }
}
