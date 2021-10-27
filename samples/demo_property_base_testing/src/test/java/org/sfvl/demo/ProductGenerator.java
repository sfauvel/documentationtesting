package org.sfvl.demo;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class ProductGenerator {
    static <T> List<Product> generate(BiConsumer<Product, T> setter, T... values) {
        return generate(setter, Arrays.asList(values));
    }

    static <T> List<Product> generate(BiConsumer<Product, T> setter, List<T> values) {
        return new org.sfv.demo.DataGenerator<Product>(Product::new) {{
            with(Product::setPrice, generateInts(10));
            with(setter, values);
        }}.build();
    }

    private static List<Integer> generateInts(int nb) {

        final Random random = new Random();
        return IntStream.range(0, nb)
                .mapToObj(__ -> random.nextInt(10000))
                .collect(Collectors.toList());
    }
}
