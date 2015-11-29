package org.skaggs.ec.operators;

import org.skaggs.ec.multiobjective.population.FrontedPopulation;
import org.skaggs.ec.population.Population;
import org.skaggs.ec.properties.HasPropertyRequirements;
import org.skaggs.ec.properties.Properties;

/**
 * Created by Mitchell on 11/25/2015.
 */
public interface Operator<E> extends HasPropertyRequirements {

    /**
     * This method applies the operation to the entire population and returns a new collection of individuals.
     *
     * @param population the population to be operated on
     * @return a new population with the changes applied
     */
    Population<E> apply(FrontedPopulation<E> population, Properties properties);
}
