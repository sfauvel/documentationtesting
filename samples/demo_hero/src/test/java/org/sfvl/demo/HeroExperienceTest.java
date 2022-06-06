package org.sfvl.demo;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.Test;
import org.sfvl.doctesting.junitinheritance.ApprovalsBase;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * We show different approach to test writing a documentation.
 * We start with a typical characterization test that produced a file using for non regression with `approvals`.
 * After, we explore other way to produce a readable document.
 * It gives ideas on advantages and drawbacks for each case.
 */
public class HeroExperienceTest extends ApprovalsBase {

    /**
     * Typical output provided by characterization test.
     * It's just data and it's hard to analysis when there is a difference.
     * It's not made to be read by someone.
     */
    @Test
    public void should_gainxp_approvals() {

        StringBuffer sb = new StringBuffer();
        HeroDataTest[] heroDataTests = HeroDataTest.dataGenerated();
        for (HeroDataTest data : heroDataTests) {

            sb.append(String.join("\n",
                    "input: ",
                    "\tdata.xpGained=" + data.xpGained,
                    ""));

            final HeroExperience heroExperience = initHeroExperience(data);

            Message message = new Message() {
                @Override
                public void setMessage(String message) {
                    sb.append("Message:" + message + "\n");
                }
            };

            heroExperience.gainxp(data.xpGained, () -> sb.append("Bob gains a " + HeroClass.NINJA + " level!\n"));

            sb.append(String.join("\n",
                    "output:",
                    "\tnlevel=" + heroExperience.getLevel(),
                    "\tgetNxp=" + heroExperience.getXp(),
                    "\tgetNlevelboost=" + heroExperience.getLevelBoost(),
                    ""
            ));

        }

        write("\n----\n" + sb.toString() + "\n----\n");
        Approvals.verify(sb.toString());
    }

    /**
     * We generate a lot of heros and values used to update hero during test.
     * We display the result of each combination.
     *
     * - Result is not very readable.
     * - It's difficult to see what has changed between two cases.
     * - Some values have been choosen arbitrary and limits are not explicit in final document.
     */
    @Test
    public void should_gainxp() {

        HeroDataTest[] heroDataTests = HeroDataTest.dataGenerated();
        for (HeroDataTest data : heroDataTests) {

            write("== data.classgain=" + data.classgain + " data.xpGained=" + data.xpGained + "\n\n");

            final HeroExperience heroExperience = initHeroExperience(data);

            write("[options=\"header\"]\n|====\n");
            write(" | nlevel " +
                    " | getNxp " +
                    " | getNlevelboost "
            );

            write("\n");
            writeLine(heroExperience);
            write("\n");

            heroExperience.gainxp(data.xpGained, () -> {
            });

            writeLine(heroExperience);
            write("\n");
            write("|====\n\n");

        }
    }

    /**
     * Iterate x time incrementing points each time and displaying values before and after.
     *
     * - Show almost all values
     * - Difficult to see when something change
     * - Most of the lines are useless
     */
    @Test
    public void should_gainxp_and_levelup() {
        HeroDataTest[] heroDataTests = new DataGenerator<HeroDataTest>(HeroDataTest::new)
                .with(HeroDataTest::setXpGained, IntStream.range(0, 10).mapToObj(i -> Integer.valueOf(i * 40)).collect(Collectors.toList()))
                .with(HeroDataTest::setLevel, Arrays.asList(1, 3, 10))
                .with(HeroDataTest::setLevelBoost, Arrays.asList(0, 1))
                .with(HeroDataTest::setXpInitial, Arrays.asList(3))
                .build().toArray(new HeroDataTest[0]);

        write("\n[%autowidth, options=\"header\"]\n|====\n");
        write(" | Level " +
                " | Xp " +
                " | xp gained " +
                " | Level " +
                " | Xp " +
                " | Message " +
                "\n");

        for (HeroDataTest data : heroDataTests) {
            StringBuffer sb = new StringBuffer();

            final HeroExperience heroExperience = initHeroExperience(data);

            write("\n");

            heroExperience.gainxp(data.xpGained, () -> sb.append("Bob gains a " + HeroClass.NINJA + " level!"));

            writeLineLight(data.level, data.xpInitial, data.xpGained, sb.toString(), heroExperience);

        }

        write("\n|====\n\n");

    }

    /**
     * Iterate x time and increment points to add each time.
     * Each line show what happened when an initial HeroExperience (with xp = 0) is incrementing by an amount of experience points.
     *
     * - Show only when level change
     *
     * - All values are present as comment. It's not visible but it's checked.
     *
     * Have some text in comment seems to be a bad idea.
     * When we approved the file, this text is not visible so we validate without seeing what we validate.
     *
     * If we really want to ensure there is no regression on any values, we may create two tests.
     * One with something readable with only some significant values and one with all values.
     */
    @Test
    public void should_gainxp_and_levelup_bounded() {
        {
            final int xpInitial = 3;
            final int getLevelBoost = 0;
            final int initialLevel = 1;
            writeExperienceChange(xpInitial, getLevelBoost, initialLevel, true);
        }
        {
            final int xpInitial = 3;
            final int getLevelBoost = 1;
            final int initialLevel = 1;
            writeExperienceChange(xpInitial, getLevelBoost, initialLevel, true);
        }
        {
            final int xpInitial = 3;
            final int getLevelBoost = 0;
            final int initialLevel = 4;
            writeExperienceChange(xpInitial, getLevelBoost, initialLevel, true);
        }

    }

    /**
     * Iterate x time and increment points to add each time.
     * Each line show what happened when an initial HeroExperience (with xp = 0) is incrementing by an amount of experience points.
     * Display all values but highlight lines when level change.
     */
    @Test
    public void should_gainxp_and_levelup_bounded_full() {
        {
            final int xpInitial = 3;
            final int getLevelBoost = 0;
            final int initialLevel = 1;
            writeExperienceChange(xpInitial, getLevelBoost, initialLevel, false);
        }
        {
            final int xpInitial = 3;
            final int getLevelBoost = 1;
            final int initialLevel = 1;
            writeExperienceChange(xpInitial, getLevelBoost, initialLevel, false);
        }
        {
            final int xpInitial = 3;
            final int getLevelBoost = 0;
            final int initialLevel = 4;
            writeExperienceChange(xpInitial, getLevelBoost, initialLevel, false);
        }

    }

    private void writeExperienceChange(int xpInitial, int getLevelBoost, int initialLevel, boolean onlySignificantLines) {
        HeroDataTest[] heroDataTests = new DataGenerator<HeroDataTest>(HeroDataTest::new)
                .with(HeroDataTest::setXpGained, IntStream.range(0, 500).mapToObj(i -> Integer.valueOf(i * 1)).collect(Collectors.toList()))
                .with(HeroDataTest::setLevel, Arrays.asList(initialLevel))
                .with(HeroDataTest::setLevelBoost, Arrays.asList(getLevelBoost))
                .with(HeroDataTest::setXpInitial, Arrays.asList(xpInitial))
                .with(HeroDataTest::setClassgain, Arrays.asList(HeroClass.NINJA))
                .build().toArray(new HeroDataTest[0]);


        write("\nWith getNlevelboost = " + getLevelBoost + "\n");
        write("\n[%autowidth, options=\"header\"]\n|====\n");
        write(" | Level " +
                " | Xp " +
                " | xp gained " +
                " | Level " +
                " | Xp " +
                " | Message " +
                "\n");

        String lastLine = "";
        int lastLevel = initialLevel;
        for (HeroDataTest data : heroDataTests) {
            StringBuffer sb = new StringBuffer();

            Message message = new Message() {
                @Override
                public void setMessage(String message) {
                    sb.append(message);
                }
            };

            final HeroExperience heroExperience = initHeroExperience(data);

            heroExperience.gainxp(data.xpGained, () -> message.setMessage("Bob gains a " + HeroClass.NINJA + " level!"));


            String newLine = getLineLight(initialLevel, data.xpInitial, data.xpGained, sb.toString(), heroExperience);
            if (lastLevel != heroExperience.getLevel() || (data.xpGained == 0)) {
                write(toBold(lastLine));
                write(toBold(newLine));
                lastLine = "";
            } else {
                if (onlySignificantLines && !lastLine.trim().isEmpty()) {
                    write("// ");
                }
                write(lastLine);
                lastLine = newLine;
            }

            lastLevel = heroExperience.getLevel();

        }

        write("\n|====\n\n");
    }

    private String toBold(String tableLine) {
        if (tableLine.trim().isEmpty()) {
            return tableLine;
        }
        return "a|" + Arrays.stream(tableLine.split("\\|"))
                .skip(1)
                .map(String::trim)
                .map(s -> (s.isEmpty()?"":" *" + s.trim() + "* "))
                .collect(Collectors.joining("|")) + "\n";
    }

    /**
     * With some experience, hero will pass a new level.
     * It needs more and more experience points to pass a level with higher level.
     *
     * Number of points required to change level : stem:[(1.55 ^ "level") + (35 * "level)" + 50]
     */
    @Test
    public void should_show_when_gainxp() {

        final HeroExperience heroExperience = initHeroExperience();
        int lastLevel = heroExperience.getLevel();
        int xpFromLastLevel = 0;

        for (int xp = 0; xp < 10000; xp++, xpFromLastLevel++) {
            final int currentLevel = heroExperience.getLevel();
            if (currentLevel != lastLevel) {
                write("\nPass to level " + currentLevel + " with " + xp + " xp (+" + xpFromLastLevel + ")\n");
                xpFromLastLevel = 0;
            }
            lastLevel = currentLevel;

            heroExperience.gainxp(1, () -> {
            });

        }

    }

    /**
     * With some experience, hero will pass a new level.
     * It needs more and more experience points to pass a level with higher level.
     *
     * Number of points required to change level : stem:[(1.55^"level") + (35*"level)" + 50]
     */
    @Test
    public void experience_points_needed_to_go_to_the_next_level() {
        final HeroExperience heroExperience = initHeroExperience();

        write_when_levelup(heroExperience);
    }

    /**
     * When a level boost is set, it's easier to gain a level.
     * Level taken into account is `level` minus `level boost`.
     */
    @Test
    public void experience_points_needed_to_go_to_the_next_level_with_boost_equals_1() {
        final HeroExperience heroExperience = initHeroExperience();
        heroExperience.setLevelBoost(1);

        write_when_levelup(heroExperience);
    }


    private void write_when_levelup(HeroExperience heroExperience) {
        write("\n[%autowidth, cols=\"^.^1,^.^1,^.^1,^.^1\", options=\"header\"]\n" +
                "|====\n" +
                "| from level\n" +
                "| to level\n" +
                "| total xp +\nfrom level 0\n" +
                "| xp from +\nlast level\n");

        int lastLevel = heroExperience.getLevel();
        int xpFromLastLevel = 0;
        for (int xp = 0; xp < 10000; xp++, xpFromLastLevel++) {
            final int currentLevel = heroExperience.getLevel();

            final String row = "| " + lastLevel + " | " + currentLevel + " | " + xp + " | +" + xpFromLastLevel;
            write(((currentLevel == lastLevel) ? "//" + row : row) + "\n");

            if (currentLevel != lastLevel) {
                xpFromLastLevel = 0;
            }
            lastLevel = currentLevel;

            heroExperience.gainxp(1, () -> {});
        }
        write("\n|====\n");
    }

    private HeroExperience initHeroExperience(HeroDataTest data) {
        final HeroExperience heroExperience = initHeroExperience();
        setInitialValues(data, heroExperience);
        return heroExperience;
    }

    private HeroExperience initHeroExperience() {
        return new HeroExperience() {
                @Override
                protected void levelgainRule() {

                }
            };
    }

    private void setInitialValues(HeroDataTest data, HeroExperience heroExperience) {
        heroExperience.setLevel(data.level);
        heroExperience.setXp(data.xpInitial);
        heroExperience.setLevelBoost(data.levelBoost);
    }

    private void writeLineLight(int initialLevel, int xpInitial, int xpGained, String message, HeroExperience heroExperience) {
        write(getLineLight(initialLevel, xpInitial, xpGained, message, heroExperience));
    }

    private String getLineLight(int initialLevel, int initialXp, int xpGained, String message, HeroExperience heroExperience) {
        return " | " + initialLevel +
                " | " + initialXp +
                " | + " + xpGained +
                " | " + heroExperience.getLevel() +
                " | " + heroExperience.getXp() +
                " | " + message +
                "\n";
    }

    private void writeLine(HeroExperience heroExperience) {
        write(
                " | " + heroExperience.getLevel() +
                        " | " + heroExperience.getXp() +
                        " | " + heroExperience.getLevelBoost()
        );
    }

    public static class HeroDataTest {
        int xpGained;
        int level;
        int levelBoost;
        int xpInitial;
        HeroClass classgain;

        public void setXpGained(int xpGained) {
            this.xpGained = xpGained;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public void setLevelBoost(int levelBoost) {
            this.levelBoost = levelBoost;
        }

        public void setXpInitial(int xpInitial) {
            this.xpInitial = xpInitial;
        }

        public void setClassgain(HeroClass classgain) {
            this.classgain = classgain;
        }

        public static HeroDataTest[] dataGenerated() {
            return new DataGenerator<HeroDataTest>(HeroDataTest::new)
                    .with(HeroDataTest::setXpGained, Arrays.asList(1000, 100, 10, 0))
                    .with(HeroDataTest::setLevel, Arrays.asList(1, 14))
                    .with(HeroDataTest::setLevelBoost, Arrays.asList(0, 1, 5))
                    .with(HeroDataTest::setXpInitial, Arrays.asList(3, 30, 60))
                    .with(HeroDataTest::setClassgain, Arrays.asList(HeroClass.NINJA))
                    .build().toArray(new HeroDataTest[0]);
        }

    }
}
