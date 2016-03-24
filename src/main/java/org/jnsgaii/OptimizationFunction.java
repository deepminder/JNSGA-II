package org.jnsgaii;

import org.jnsgaii.population.individual.Individual;
import org.jnsgaii.properties.HasPropertyRequirements;
import org.jnsgaii.properties.Properties;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Mitchell on 11/25/2015.
 * <p>
 * This class is a comparator on its own function. It decides which score bests which other score, which affects the algorithm's choices.
 * <p>
 * NOTE: An implementor must also implement the <code>equals</code> method.
 *
 * @see java.util.Comparator
 */
public interface OptimizationFunction<E> extends Comparator<Double>, HasPropertyRequirements, Serializable {

    double[] evaluate(List<Individual<E>> individuals, Properties properties);

    /**
     * @param properties the evolutionary algorithm's properties
     * @return the minimum value this function can return
     */
    double min(Properties properties);

    /**
     * @param properties the evolutionary algorithm's properties
     * @return the maximum value this function can return
     */
    double max(Properties properties);
}
