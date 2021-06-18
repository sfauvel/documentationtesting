package fr.xebia.katas.gildedrose;

import org.junit.jupiter.api.Test;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;
import org.sfvl.doctesting.junitinheritance.ApprovalsBase;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 */
public class InnTest extends ApprovalsBase {


    @Test
    public void example_of_evolution_by_item() throws Exception {
        for (int i = 0; i < new Inn().getItems().size(); i++) {
            Inn inn = new Inn();

            check_item_update_line(inn, i);
        }
    }

    private String generateGraph(String name, List<Integer> sellIns, List<Integer> qualities) throws IOException {

        final Path docPath = getDocPath();

        // Create Chart
        final XYChart chart = new XYChartBuilder().width(600).height(300).title(name).build();

        // Customize Chart
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);

        chart.addSeries("SellIn", IntStream.range(0, sellIns.size()).toArray(), sellIns.stream().mapToInt(i -> i).toArray());
        chart.addSeries("Qualities", IntStream.range(0, qualities.size()).toArray(), qualities.stream().mapToInt(i -> i).toArray());

        // Save it
        final String fileName = name
                .replaceAll(" ", "_")
                .replaceAll(",", "_")
                .replaceAll("\\+", "");

        final Path path = Paths.get(getClass().getPackage().getName().replaceAll("\\.", "/"), fileName);

        BitmapEncoder.saveBitmap(chart, docPath.resolve(path).toFile().getPath(), BitmapEncoder.BitmapFormat.PNG);

        return path + ".png";
    }

    /**
     * Show chart.
     *
     * @param chart
     */
    private void show(XYChart chart) {
        new SwingWrapper(chart).displayChart();
    }

    private void check_item_update_line(Inn inn, int index) throws IOException {

        final Item initialItem = inn.getItems().get(index);
        final String name = initialItem.getName();

        List<Integer> sellIns = new ArrayList<>();
        List<Integer> qualities = new ArrayList<>();

        for (int i = 1; i <= 20; i++) {
            final Item item = getItem(inn, name);

            sellIns.add(item.getSellIn());
            qualities.add(item.getQuality());

            inn.updateQuality();
        }

        final String imageFile = generateGraph(name, sellIns, qualities);

        write("\n\n== Item " + name + "\n\n");

        write("[%autowidth]",
                "[cols=\"1,1\"]",
                "|====", "");

        write("<| ", generic_rules(), "", specifc_rules(name).map(rule -> "*" + rule + "*").orElse(""), "");
        write("a|", "image::{ROOT_PATH}/" + imageFile.replace("\\", "/") + "[]", "");
        write("|====", "", "");

        write(generate_values_table(sellIns, qualities));
    }

    private Optional<String> specifc_rules(String name) {
        if (name.contains("Aged Brie")) {
            return Optional.of("\"Aged Brie\" actually increases in Quality the older it gets");
        }
        if (name.contains("Sulfuras")) {
            return Optional.of("\"Sulfuras\", being a legendary item, never has to be sold or decreases in Quality");
        }
        if (name.contains("Backstage passes")) {
            return Optional.of("\"Backstage passes\", like aged brie, increases in Quality as it’s SellIn value approaches; Quality increases by 2 when there are 10 days or less and by 3 when there are 5 days or less but Quality drops to 0 after the concert");
        }
        return Optional.empty();
    }

    private String generic_rules() {
        return String.join("\n\n",
                "All items have a SellIn value which denotes the number of days we have to sell the item",
                "All items have a Quality value which denotes how valuable the item is",
                "At the end of each day our system lowers both values for every item",
                "Once the sell by date has passed, Quality degrades twice as fast",
                "The Quality of an item is never negative",
                "The Quality of an item is never more than 50");

    }

    private String generate_values_table(List<Integer> sellIns, List<Integer> qualities) {
        String result = "";
        result += "\n|====\n";

        result += "| iteration" + IntStream.range(0, sellIns.size()).mapToObj(Integer::toString).collect(Collectors.joining(" | ", " | ", "\n"));
        result += "| sellIn" + sellIns.stream().map(Object::toString).collect(Collectors.joining(" | ", " | ", "\n"));
        result += "| qualities" + qualities.stream().map(Object::toString).collect(Collectors.joining(" | ", " | ", "\n"));

        result += "|====";
        return result;
    }

    private Item getItem(Inn inn, String name) {
        return inn.getItems().stream()
                .filter(__ -> __.getName().equals(name))
                .findFirst()
                .get();
    }


}
