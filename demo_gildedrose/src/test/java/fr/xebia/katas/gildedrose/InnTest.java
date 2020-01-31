package fr.xebia.katas.gildedrose;

import fr.xebia.katas.gildedrose.Inn;
import fr.xebia.katas.gildedrose.Item;
import org.junit.jupiter.api.Test;
import org.sfvl.doctesting.ApprovalsBase;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * include::../../../readme.adoc[leveloffset=+1]
 */
public class InnTest extends ApprovalsBase {

    private Inn inn = new Inn();

    @Test
    public void example_of_evolution_by_item() throws Exception {
        for (int i = 0; i < inn.getItems().size(); i++) {
            check_item_update_line(i);
        }
    }

    private void check_item_update(int index) {
        final Item initialItem = inn.getItems().get(index);
        final String name = initialItem.getName();

        write("\n\n== Name " + name);


        write("\n|====\n");
        write("| Iteration | SellIn | Quality \n");
        write(String.format("| %d | %d | %d\n",
                0,
                initialItem.getSellIn(),
                initialItem.getQuality()));

        for (int i = 1; i < 20; i++) {
            inn.updateQuality();

            final Item item = inn.getItems().stream()
                    .filter(__ ->  __.getName().equals(name))
                    .findFirst()
                    .get();

            write(String.format("| %d | %d | %d\n",
                    i,
                    item.getSellIn(),
                    item.getQuality()));

        }
        write("|====");
        write("\n\n");
    }

    private void check_item_update_line(int index) {
        final Item initialItem = inn.getItems().get(index);
        final String name = initialItem.getName();

        List<Integer> sellIns = new ArrayList<>();
        List<Integer> qualities = new ArrayList<>();

        sellIns.add(initialItem.getSellIn());
        qualities.add(initialItem.getQuality());

        for (int i = 1; i < 20; i++) {
            inn.updateQuality();

            final Item item = inn.getItems().stream()
                    .filter(__ ->  __.getName().equals(name))
                    .findFirst()
                    .get();

            sellIns.add(item.getSellIn());
            qualities.add(item.getQuality());

        }

        write("\n\n== Item " + name);
        write("\n|====\n");

        write("| iteration" + IntStream.range(0, sellIns.size()).mapToObj(Integer::toString).collect(Collectors.joining(" | ", " | ", "\n")));
        write("| sellIn" + sellIns.stream().map(Object::toString).collect(Collectors.joining(" | ", " | ", "\n")));
        write("| qualities" + qualities.stream().map(Object::toString).collect(Collectors.joining(" | ", " | ", "\n")));

        write("|====");
        write("\n\n");
    }

}
