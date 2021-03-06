package org.jnsgaii.multiobjective.population;

import org.jnsgaii.functions.OptimizationFunction;
import org.jnsgaii.population.EvaluatedPopulation;
import org.jnsgaii.population.individual.EvaluatedIndividual;
import org.jnsgaii.properties.Key;
import org.jnsgaii.properties.Properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Mitchell on 11/28/2015.
 */
@SuppressWarnings("AssignmentToSuperclassField")
public class FrontedPopulation<E> extends EvaluatedPopulation<E> {

    protected List<Front<E>> fronts;

    @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
    protected FrontedPopulation(List<FrontedIndividual<E>> population, List<Front<E>> fronts) {
        this.fronts = fronts;
        this.population = population;
    }

    private FrontedPopulation() {
        this(null, null);
    }

    public FrontedPopulation(EvaluatedPopulation<E> population, List<OptimizationFunction<E>> optimizationFunctions, Properties properties) {
        super();

        this.fronts = new ArrayList<>();
        this.population = new ArrayList<FrontedIndividual<E>>();

        /*
        A view of the population that has been cast to a list of FrontedIndividuals
        Generics just can't handle me right now. I can't override a variable, so I have to cast the List<Individual<>> to a more precise Individual type
        */
        @SuppressWarnings("unchecked") List<FrontedIndividual<E>> castPopulationView = (List<FrontedIndividual<E>>) this.population;

        for (EvaluatedIndividual<E> individual : population.getPopulation()) {
            FrontedIndividual<E> frontedIndividual = new FrontedIndividual<>(individual);
            castPopulationView.add(frontedIndividual);
        }

        boolean threaded = properties.getBoolean(Key.BooleanKey.DefaultBooleanKey.THREADED);

        Front<E> firstFront = new Front<>(new SortedArrayList<>(), 0);
        this.fronts.add(0, firstFront);

        // Start computing the crowding distance

        //for (OptimizationFunction<E> optimizationFunction : optimizationFunctions) {
        for (int i = 0; i < optimizationFunctions.size(); i++) {
            // Sorts the population according to the comparator
            final int finalI = i; // To satisfy the lambda
            Collections.sort(castPopulationView, (o1, o2) -> -optimizationFunctions.get(finalI).compare(o1.getScore(finalI), o2.getScore(finalI))); // Lowest first
            // First and last have priority with the crowding score
            castPopulationView.get(0).crowdingScore = Double.POSITIVE_INFINITY;
            castPopulationView.get(castPopulationView.size() - 1).crowdingScore = Double.POSITIVE_INFINITY;

            for (int j = 1; j < (castPopulationView.size() - 1); j++) { // Don't check the outside ones
                if (Double.isFinite(castPopulationView.get(j).crowdingScore)) // Only add to it if it isn't an outlier on another function
                    castPopulationView.get(j).crowdingScore += (castPopulationView.get(j + 1).getScore(i) - castPopulationView.get(j - 1).getScore(i)) / (optimizationFunctions.get(i).max(properties) - optimizationFunctions.get(i).min(properties));
            }
        }

        // Start ranking individuals //TODO This takes 95% of the CPU time for each generation; optimize like crazy

        Stream<FrontedIndividual<E>> populationStream;
        if (threaded)
            populationStream = castPopulationView.parallelStream();
        else
            populationStream = castPopulationView.stream();
        populationStream.forEach(individual -> {
            for (FrontedIndividual<E> otherIndividual : castPopulationView) {
                if (otherIndividual == individual) continue;
                int domination = individual.dominates(otherIndividual);
                if (domination < 0) {
                    //System.out.println("Individual was dominated");
                    individual.dominationCount++;
                } else if (domination > 0) {
                    //System.out.println("Added individual to dominated list...");
                    individual.dominatedIndividuals.add(otherIndividual);
                }
            }
        });

        castPopulationView.stream().filter(individual -> individual.dominationCount == 0).forEach(individual -> { // Add it to the first front (Front 0). That front has RANK 0, is at POSITION 0, and the individual has RANK 0
            individual.rank = 0;
            this.fronts.get(0).members.add(individual);
        });

        // Start establishing Fronts from ranked individuals

        int currentFrontRank = 0;
        while (!this.fronts.get(currentFrontRank).members.isEmpty()) {
            SortedArrayList<FrontedIndividual<E>> nextFront = new SortedArrayList<>();
            final int finalCurrentFrontRank = currentFrontRank;

            Stream<FrontedIndividual<E>> frontStream;
            if (threaded)
                frontStream = this.fronts.get(currentFrontRank).members.parallelStream();
            else
                frontStream = this.fronts.get(currentFrontRank).members.stream();

            frontStream.forEach(individual -> {
                Stream<FrontedIndividual<E>> dominatedStream;
                if (threaded)
                    dominatedStream = individual.dominatedIndividuals.parallelStream();
                else
                    dominatedStream = individual.dominatedIndividuals.stream();

                dominatedStream.forEach(dominatedIndividual -> {
                    //noinspection SynchronizationOnLocalVariableOrMethodParameter
                    synchronized (dominatedIndividual) {
                        dominatedIndividual.dominationCount--;
                        if (dominatedIndividual.dominationCount == 0) {
                            dominatedIndividual.rank = finalCurrentFrontRank + 1; // Part of the next front
                            synchronized (nextFront) {
                                nextFront.add(dominatedIndividual);
                            }
                        }
                    }
                });
            });
            this.fronts.add(currentFrontRank + 1, new Front<>(nextFront, currentFrontRank + 1));
            currentFrontRank++;
        }
    }

    public List<Front<E>> getFronts() {
        return Collections.unmodifiableList(fronts);
    }

    public List<Front<E>> getFronts(int limit) {
        return Collections.unmodifiableList(fronts.stream().limit(limit).collect(Collectors.toList()));
    }


    @Override
    public String toString() {
        return this.population.toString();
    }

    @Override
    public List<? extends FrontedIndividual<E>> getPopulation() {
        // This SHOULD work, since the only constructor fills the List<> with FrontedIndividual<>s
        //noinspection unchecked
        return (List<FrontedIndividual<E>>) this.population;
    }

    public FrontedPopulation<E> truncate(int limit) {
        //System.err.println("Truncating population of " + this.getPopulation().size() + " to " + limit);
        this.sort();
        List<Front<E>> newFronts = new ArrayList<>();
        List<FrontedIndividual<E>> newPopulation = new ArrayList<>(limit);

        int currentFront = 0;
        int numIndividuals = 0;

        while (currentFront < this.fronts.size() && (numIndividuals + this.fronts.get(currentFront).members.size()) <= limit) {
            //System.out.println("Adding front " + currentFront + " out of " + this.fronts.size());
            newPopulation.addAll(this.fronts.get(currentFront).members);
            newFronts.add(currentFront, this.fronts.get(currentFront));
            numIndividuals += this.fronts.get(currentFront).members.size();
            currentFront++;
        }
        assert numIndividuals == newPopulation.size();
        if (currentFront < this.fronts.size() && limit - numIndividuals > 0) {
            SortedArrayList<FrontedIndividual<E>> individuals = new SortedArrayList<>();
            Iterator<FrontedIndividual<E>> iterator = this.fronts.get(currentFront).members.iterator();
            for (int i = 0; i < limit - numIndividuals; i++) {
                FrontedIndividual<E> individual = iterator.next();
                //System.out.println("Current iterator return: " + individual);
                individuals.add(individual);
                newPopulation.add(individual);
            }
            newFronts.add(currentFront, new Front<>(individuals, currentFront));
        }

        //System.err.println("Truncated to " + newPopulation.size());
        if (newPopulation.size() != limit) {
            System.err.println("Population: " + newPopulation);
            System.err.println("Fronts: " + newFronts);
            throw new Error();
        }
        return new FrontedPopulation<>(newPopulation, newFronts);
    }

    private void sort() {
        //noinspection unchecked
        Collections.sort(((List<FrontedIndividual<E>>) this.population));
    }
}
