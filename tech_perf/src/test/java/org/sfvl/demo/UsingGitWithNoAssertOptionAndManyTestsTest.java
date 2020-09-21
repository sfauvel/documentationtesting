package org.sfvl.demo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sfvl.doctesting.GitBase;

import java.io.IOException;

/**
 * Demo of a simple usage to generate documentation.
 */
@DisplayName("Use Git to check file modification but after all tests. The time shown not include this verification. 100 tests are made.")
@TestCategory(category = TestCategory.Cat.Many)
public class UsingGitWithNoAssertOptionAndManyTestsTest extends GitBase {

    @BeforeAll
    public static void begin() {
        System.setProperty("no-assert", "");
    }

    @AfterAll
    public static void end() throws IOException {
        System.clearProperty("no-assert");
    }

//    @Test
//    public void generate_tests() {
//        for (int i = 0; i < 100; i++) {
//            System.out.println("" +
//            "@Test\n" +
//            "public void should_give_person_information_xxx_" + i + "() {\n" +
//            "    BasicDocumentation.generateTestDocumentation(this);\n" +
//            "}\n" +
//                    "");
//        }
//    }


    @Test
    public void should_give_person_information_xxx_0() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_1() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_2() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_3() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_4() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_5() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_6() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_7() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_8() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_9() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_10() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_11() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_12() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_13() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_14() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_15() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_16() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_17() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_18() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_19() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_20() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_21() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_22() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_23() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_24() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_25() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_26() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_27() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_28() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_29() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_30() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_31() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_32() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_33() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_34() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_35() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_36() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_37() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_38() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_39() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_40() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_41() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_42() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_43() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_44() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_45() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_46() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_47() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_48() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_49() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_50() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_51() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_52() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_53() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_54() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_55() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_56() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_57() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_58() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_59() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_60() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_61() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_62() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_63() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_64() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_65() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_66() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_67() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_68() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_69() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_70() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_71() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_72() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_73() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_74() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_75() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_76() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_77() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_78() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_79() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_80() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_81() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_82() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_83() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_84() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_85() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_86() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_87() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_88() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_89() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_90() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_91() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_92() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_93() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_94() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_95() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_96() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_97() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_98() {
        BasicDocumentation.generateTestDocumentation(this);
    }

    @Test
    public void should_give_person_information_xxx_99() {
        BasicDocumentation.generateTestDocumentation(this);
    }

}
