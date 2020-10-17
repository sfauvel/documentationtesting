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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 */
public class InnTest extends ApprovalsBase {


    @Test
    public void example_of_evolution_by_item() throws Exception {
        for (int i = 0; i < new Inn().getItems().size(); i++) {
            Inn inn = new Inn();

            check_item_update_line(inn, i);
        }
    }

    private void check_item_update(Inn inn, int index) throws IOException {
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
                    .filter(__ -> __.getName().equals(name))
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

        write("\n\n== Item " + name + "\n\n");


        write("\n|====\n");

        write("| iteration" + IntStream.range(0, sellIns.size()).mapToObj(Integer::toString).collect(Collectors.joining(" | ", " | ", "\n")));
        write("| sellIn" + sellIns.stream().map(Object::toString).collect(Collectors.joining(" | ", " | ", "\n")));
        write("| qualities" + qualities.stream().map(Object::toString).collect(Collectors.joining(" | ", " | ", "\n")));

        write("|====");
        write("\n\n");

        final String imageFile = generateGraph(name, sellIns, qualities);
        write("image::" + imageFile.replace("\\", "/") + "[]\n\n");
    }

    private Item getItem(Inn inn, String name) {
        return inn.getItems().stream()
                .filter(__ -> __.getName().equals(name))
                .findFirst()
                .get();
    }

}
