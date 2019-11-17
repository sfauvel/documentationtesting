package org.sfvl.application;

import org.junit.jupiter.api.Test;
import org.sfvl.doctesting.ApprovalsBase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GameOfLifeTest extends ApprovalsBase {

    /**
     * With less than 2 neighbours, cell die on the next generation.
     * @throws IOException
     */
    @Test
    public void die_when_less_than_2_neighbours() throws IOException {
        nextGenerationOf(
                "000",
                "010",
                "000"
        );
    }

    @Test
    public void stay_alive_when_2_neighbours() throws IOException {
        nextGenerationOf(
                "000",
                "111",
                "000"
        );
    }


    private void nextGenerationOf(String... lines) throws IOException {

        GameOfLife gameOfLife = textToGameOfLife(lines);

        gameOfLife.nexGeneration();

        List<String> linesNextGeneration = gameOfLifeToText(gameOfLife, lines);


        writeNextGeneration(lines, linesNextGeneration);
    }

    private void writeNextGeneration(String[] lines, List<String> linesNextGeneration) throws IOException {
        write(String.join("\n",
                //"[.gameOfLife]",
                "[cols=\"1a,1a,1a\", width=4em, frame=none, grid=none]",
                "|====",
                "| " + drawGameOfLife(lines).replaceAll("\\|", "!"),
                "^.^| =>",
                "",
                "| " + drawGameOfLife(linesNextGeneration).replaceAll("\\|", "!"),
                "|====",
                "",
                ""));

        write(Files.lines(Paths.get("src", "test", "resources", "style.css"))
                .collect(Collectors.joining("\n")));
    }

    private GameOfLife textToGameOfLife(String[] lines) {


        GameOfLife gameOfLife = new GameOfLife();

        for (int line = 0; line < lines.length; line++) {
            char[] chars = lines[line].toCharArray();
            for (int column = 0; column < chars.length; column++) {
                if (chars[column]=='1') gameOfLife.setAlive(line, column);
            }
        }
        return gameOfLife;
    }

    private List<String> gameOfLifeToText(GameOfLife gameOfLife, String[] lines) {
        List<String> linesNextGenerationList = new ArrayList<>();
        for (int line = 0; line < lines.length; line++) {
            StringBuffer linesNextGeneration = new StringBuffer();
            char[] chars = lines[line].toCharArray();
            for (int column = 0; column < chars.length; column++) {
                linesNextGeneration.append(gameOfLife.getAlive(line, column)?"1":"0");
            }
            linesNextGenerationList.add(linesNextGeneration.toString());
        }
        return linesNextGenerationList;
    }

    private String drawGameOfLife(List<String> lines) {
        return drawGameOfLife(lines.toArray(new String[0]));
    }

    private String drawGameOfLife(String[] lines) {
        return String.join("\n",
                "[.gameOfLife]",
                "[cols=\"3*a\"]",
                "|====",
                Arrays.stream(lines).map(this::line).collect(Collectors.joining("\n")),
                "|====");
    }

    private String line(String s) {
        String cells = "";
        for (char c : s.toCharArray()) {
            cells += "| " + ((c == '0') ? "[.dead]" : "[.alive]") + "\n*\n";
        }
        return cells;
    }
}
