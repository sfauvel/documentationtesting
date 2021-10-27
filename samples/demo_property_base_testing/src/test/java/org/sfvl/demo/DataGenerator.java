package org.sfv.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * An object that can generate a combination of values.
 *
 * To generate a list of object, create a anonymous class of DataGenerator.
 * The generic type is the one of object generated.
 * Constructor of generator take a supplier that create the object to return.
 * With an anonymous class, we can configure the class using with into a static constructor
 * (between {{ }}).
 *
 * <code>
 *   List<Person> people = new DataGenerator<Person>(Person::new) {{
 *             with((o, __) -> o.setFirstName(__), asList("John", "Bob"));
 *             with((o, __) -> o.setLastName(__), asList("Doe", "Morane"));
 *             with((o, __) -> o.setActive(__), asList(true, false));
 *         }}.build();
 * </code>
 * <code>
 *        List<Person> people = new DataGenerator<Person>(Person::new) {{
 *             with(Person::setFirstName, asList("John", "Bob"));
 *             with(Person::setLastName, asList("Doe", "Morane"));
 *             with(Person::setActive, asList(true, false));
 *         }}.build();
 * </code>
 * @param <D>
 */
public class DataGenerator<D> {
    private final Supplier<D> factory;
    private List<SetterWithValues> setters = new ArrayList<>();

    static protected class SetterWithValues<O, T> {

        private final BiConsumer<O, T> setter;
        private final List<T> values;

        public SetterWithValues(BiConsumer<O, T> setter, List<T> values) {
            this.setter = setter;
            this.values = values;
        }

        public void accept(O o, Object value) {
            setter.accept(o, (T) value);
        }
    }

    public DataGenerator(Supplier<D> factory) {
        this.factory = factory;
    }

    public <T> DataGenerator<D> with(BiConsumer<D, T> setter, List<T> values) {
        setters.add(new SetterWithValues(setter, values));
        return this;
    }

    public List<D> build() {
        return buildModuleOptim(factory, setters);
    }

    private List<D> buildModuleOptim(Supplier<D> factory, List<SetterWithValues> setters) {
        ArrayList<Integer> combinationNumber = getCombinationNumber(setters);
        List<Integer> nbValuesPerField = nbValuesPerField(setters);

        int combinationSize = combinationSize(setters);
        List<D> combinations = IntStream.range(0, combinationSize).parallel()
                .mapToObj(index -> {
                    int[] combinationForIndex = getCombinationForIndex(combinationNumber, nbValuesPerField, index);
                    return buildObject(factory, setters, combinationForIndex);
                }).collect(Collectors.toList());

        return combinations;
    }

    private int combinationSize(List<SetterWithValues> setters) {
        return setters.stream().mapToInt(s -> s.values.size()).reduce(1, (a, b) -> a * b);
    }

    private List<Integer> nbValuesPerField(List<SetterWithValues> setters) {
        return setters.stream()
                .map(s -> s.values.size())
                .collect(Collectors.toList());
    }

    private ArrayList<Integer> getCombinationNumber(List<SetterWithValues> setters) {
        ArrayList<Integer> combinationNumber = new ArrayList<>();
        combinationNumber.add(1);
        setters.stream().forEach(setter -> combinationNumber.add(combinationNumber.get(Math.max(0, combinationNumber.size() - 1)) * setter.values.size()));
        return combinationNumber;
    }

    private D buildObject(Supplier<D> factory, List<SetterWithValues> setters, int[] ints) {
        D instance = factory.get();
        for (int i = 0; i < ints.length; i++) {
            SetterWithValues setterWithValues = setters.get(i);
            setterWithValues.setter.accept(instance, setterWithValues.values.get(ints[i]));
        }
        return instance;
    }

    private int[] getCombinationForIndex(ArrayList<Integer> combinationNumber, List<Integer> nbPerField, int globalIndex) {

        int[] combination = new int[nbPerField.size()];
        for (int i = 0; i < combination.length; i++) {
            combination[i] = ((globalIndex / combinationNumber.get(i)) % nbPerField.get(i));
        }
        return combination;
    }

}