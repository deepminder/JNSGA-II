package org.jnsgaii.operators;

import org.jnsgaii.population.individual.Individual;
import org.jnsgaii.properties.AspectUser;
import org.jnsgaii.properties.HasAspectRequirements;
import org.jnsgaii.properties.HasPropertyRequirements;
import org.jnsgaii.properties.LateUpdatingProperties;

import java.util.function.BiFunction;

/**
 * Created by skaggsm on 2/3/16.
 */
public abstract class Speciator<E> extends AspectUser<E> implements BiFunction<Individual<E>, Individual<E>, Boolean>, HasPropertyRequirements, LateUpdatingProperties, HasAspectRequirements {
    @Override
    public Boolean apply(Individual<E> individual, Individual<E> individual2) {
        return getDistance(individual, individual2) < getMaxDistance(individual, individual2);
    }

    public abstract double getDistance(Individual<E> individual, Individual<E> individual2);

    protected abstract double getMaxDistance(Individual<E> individual, Individual<E> individual2);
}
