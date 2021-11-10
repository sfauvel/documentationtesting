package org.sfvl.demo;

import org.junit.jupiter.api.Test;
import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.sfvl.doctesting.junitinheritance.ApprovalsBase;
import org.sfvl.docextraction.CodeExtractor;
import org.sfvl.docextraction.MethodReference;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Demo of a property base testing generating a documentation.
 *
 * We generate a list of objects with some random values.
 * We group objects by the invariant, the value to check or by the result of a property.
 * When there is only one value, we can display it and approved the document (like in chapter below).
 * When there is more than one value, we write them so the test fail, and we have all information to investigate.
 */
public class DemoTest extends ApprovalsBase {

    @Test
    public void should_ignore_taxes_if_type_is_TAX_FREE() {
        final TaxType taxType = TaxType.TAX_FREE;
        List<Product> products = ProductGenerator.generate(Product::setTaxType, taxType);

        final Map<Integer, List<Product>> productsByTax = products.stream()
                .collect(Collectors.groupingBy(Product::getTaxes));

        write(String.format("With %s, ", taxType.name()));
        if (productsByTax.size() == 1) {
            write(String.format("tax is always %s",
                    productsByTax.keySet().toArray()[0]),
                    "", "");
        } else {
            write("tax is not always the same values",
                    valuesToString(productsByTax),
                    "", "");
        }

        write(String.format("This property has been verified using %s products.", products.size()));
    }

    @Test
    public void code_description() {
        final AsciidocFormatter formatter = new AsciidocFormatter();
        final String code = CodeExtractor.extractMethodBody(MethodReference.getMethod(DemoTest::should_ignore_taxes_if_type_is_TAX_FREE));

        write("We show here the code used to create the above chapter.",
                formatter.sourceCodeBuilder("java")
                        .content(code)
                        .build());
    }

    private String valuesToString(Map<Integer, List<Product>> productsByTax) {
        return productsByTax.entrySet().stream()
                .map(e -> String.format("- Tax is %s with price %s", e.getKey(),
                        e.getValue().stream()
                                .map(Product::getPrice)
                                .map(Object::toString)
                                .collect(Collectors.joining(", "))
                ))
                .collect(Collectors.joining("\n"));
    }


}