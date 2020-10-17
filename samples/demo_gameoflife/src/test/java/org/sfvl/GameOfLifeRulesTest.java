package org.sfvl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sfvl.doctesting.junitinheritance.ApprovalsBase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life
 */
@DisplayName("Game of life rules")
public class GameOfLifeRulesTest extends ApprovalsBase {


    @Test
    public void rules() throws IOException {

        write("At each step in time, the following transitions occur: \n\n");
        writeNextStateForALivingCell(Cell.alive(), "{state_verb}, as if by underpopulation", Arrays.asList(0, 1));
        writeNextStateForALivingCell(Cell.alive(),"{state_verb} on to the next generation", Arrays.asList(2, 3));
        writeNextStateForALivingCell(Cell.alive(),"{state_verb}, as if by overpopulation", Arrays.asList(4, 5, 6, 9));

        writeNextStateForALivingCell(Cell.dead(),", becomes a {state_name} cell as if by reproduction", Arrays.asList(3));
        writeNextStateForALivingCell(Cell.dead()," stayed a {state_name} cell", Arrays.asList(2, 4));

    }


    final Function<Boolean, String> state_name = state -> state ? "live" : "dead";
    final Function<Boolean, String> state_verb = state -> state ? "lives" : "dies";

    private void writeNextStateForALivingCell(Cell cell, String reason, List<Integer> neighbours) {
        String values = neighbours.stream().map(n -> String.valueOf(n)).collect(Collectors.joining(", "));
        String resultsVerb = neighbours.stream()
                .map(cell::nextState)
                .map(state_verb)
                .distinct()
                .collect(Collectors.joining(", "));

        String resultsName = neighbours.stream()
                .map(cell::nextState)
                .map(state_name)
                .distinct()
                .collect(Collectors.joining(", "));

        final String currentState = state_name.apply(cell.isAlive());
        write(". Any " + currentState + " cell with *" + values + " live neighbours* "
                + reason.replace("{state_verb}", resultsVerb).replace("{state_name}", resultsName) + ". \n\n");
    }

}
