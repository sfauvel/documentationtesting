package org.sfvl.demo;

import org.junit.jupiter.api.Test;
import org.sfvl.doctesting.ApprovalsBase;

/**
 * On veut afficher un score de tennis.
 */
public class TennisTest extends ApprovalsBase {

    /**
     * Au début du jeu, le score est de 0 / 0.
     */
    @Test
    public void debut_d_un_jeu() {
        final Tennis tennis = new Tennis();
        write("[%autowidth]\n|===\n");
        write("|A | B\n");
        write("|===\n");

        displayScore(tennis);
    }

    /**
     * Le joueur A marque une fois, le score est de 15 / 0.
     */
    @Test
    public void le_joueur_A_marque_un_point() {
        final Tennis tennis = new Tennis();
        write("[%autowidth]\n|===\n");
        write("|A | B\n");
        joueurAMarque(tennis);
        write("|===\n");

        displayScore(tennis);
    }

    /**
     * Le joueur B marque une fois, le score est de 0 / 15.
     */
    @Test
    public void le_joueur_B_marque_un_point() {
        final Tennis tennis = new Tennis();
        write("[%autowidth]\n|===\n");
        write("|A | B\n");
        joueurBMarque(tennis);
        write("|===\n");
        displayScore(tennis);
    }

    /**
     * Le joueur A marque deux fois, le score est de 30 / 0.
     */
    @Test
    public void le_joueur_A_marque_deux_points() {
        final Tennis tennis = new Tennis();
        write("[%autowidth]\n|===\n");
        write("|A | B\n");
        joueurAMarque(tennis);
        joueurAMarque(tennis);
        write("|===\n");
        displayScore(tennis);
    }

    /**
     * Le joueur B marque deux fois, le score est de 0 / 30.
     */
    @Test
    public void le_joueur_B_marque_deux_points() {
        final Tennis tennis = new Tennis();
        write("[%autowidth]\n|===\n");
        write("|A | B\n");
        joueurBMarque(tennis);
        joueurBMarque(tennis);
        write("|===\n");
        displayScore(tennis);
    }

    @Test
    public void cas_complexe() {
        final Tennis tennis = new Tennis();
        write("[%autowidth]\n|===\n");
        write("|A | B\n");
        joueurBMarque(tennis);
        joueurAMarque(tennis);
        joueurAMarque(tennis);
        joueurBMarque(tennis);
        joueurBMarque(tennis);
        write("|===\n");
        displayScore(tennis);
    }

    private void joueurAMarque(Tennis tennis) {
        write("| * |\n");
        tennis.joueurAMarque();
    }

    private void joueurBMarque(Tennis tennis) {
        write("| | *\n");
        tennis.joueurBMarque();
    }

    private void displayScore(Tennis tennis) {
        Score score = tennis.getScore();
        write("Score: " + score.joueurA() + " / " + score.joueurB() + "\n\n");
    }
}

