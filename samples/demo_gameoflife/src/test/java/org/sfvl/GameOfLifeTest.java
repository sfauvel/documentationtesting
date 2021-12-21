package org.sfvl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.sfvl.doctesting.junitinheritance.ApprovalsBase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life
 */
@DisplayName("Game of life illustrated by examples")
public class GameOfLifeTest extends ApprovalsBase {

    @Override
    protected String buildContent(TestInfo testInfo) {
        return super.buildContent(testInfo) +
                GameOfLifeDocumentation.include_style();
    }

    /**
     * With less than 2 neighbours, cell die on the next generation.
     *
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
                "001",
                "010",
                "100"
        );
    }

    @Test
    public void becomes_alive_when_3_neighbours() throws IOException {
        nextGenerationOf(
                "101",
                "000",
                "100"
        );
    }

    private void nextGenerationOf(String... lines) throws IOException {

        GameOfLife gameOfLife = textToGameOfLife(lines);

        gameOfLife.nexGeneration();

        List<String> linesNextGeneration = Arrays.asList("   ", "   ", "   ");
        for (int line=0; line<3; line++) {

            linesNextGeneration.set(line,
                    (gameOfLife.getAlive(line, 0) ? "1" : "0") +
                    (gameOfLife.getAlive(line, 1) ? "1" : "0") +
                    (gameOfLife.getAlive(line, 2) ? "1" : "0")
            );

        }
        writeNextGeneration(lines, linesNextGeneration);
    }

    private void writeNextGeneration(String[] lines, List<String> linesNextGeneration) throws IOException {

        write(drawGameOfLife(lines, ".gameOfLife.inline"),
                drawTextBetweenGames("=>"),
                drawGameOfLife(linesNextGeneration, ".gameOfLife.inline"),
                "");
    }

    private String drawTextBetweenGames(String text) {
        return String.join("\n",
                "[.gameOfLife.inline]",
                "[%autowidth, cols=\"1*a\", frame=none, grid=none]",
                "|====",
                "| [] ",
                "*",
                "| [.show-text]",
                text,
                "| [] ",
                "*",
                "|====");
    }

    private GameOfLife textToGameOfLife(String[] lines) {
        GameOfLife gameOfLife = new GameOfLife();

        for (int line = 0; line < lines.length; line++) {
            char[] chars = lines[line].toCharArray();
            for (int column = 0; column < chars.length; column++) {
                if (chars[column] == '1') {
                    gameOfLife.setAlive(line, column);
                }
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
                linesNextGeneration.append(gameOfLife.getAlive(line, column) ? "1" : "0");
            }
            linesNextGenerationList.add(linesNextGeneration.toString());
        }
        return linesNextGenerationList;
    }

    private String drawGameOfLife(List<String> lines, String style) {
        return drawGameOfLife(lines.toArray(new String[0]), style);
    }

    private String drawGameOfLife(String[] lines, String style) {
        return String.join("\n",
                "[" + style + "]",
                "[%autowidth,cols=\"3*a\"]",
                "|====",
                Arrays.stream(lines).map(this::line).collect(Collectors.joining("\n")),
                "|====");
    }

    private String line(String s) {
        String cells = "";
        for (char c : s.toCharArray()) {
            cells += "| " + getStyle(c) + "\n*\n";
        }
        return cells;
    }

    private String getStyle(char c) {
        if (c == '0') return "[.dead]";
        if (c == '1') return "[.alive]";
        return "[]";
    }
}
