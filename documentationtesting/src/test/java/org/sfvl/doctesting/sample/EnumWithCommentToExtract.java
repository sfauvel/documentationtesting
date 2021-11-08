package org.sfvl.doctesting.sample;

/**
 * Comment of the class.
 */
public class EnumWithCommentToExtract {

    /**
     * Comment of the enum.
     */
    public static enum MyEnum {
        /**
         * First enum with comment.
         */
        FirstEnum,
        /** Second enum with comment. */
        SecondEnum,
        ThirdEnum;

        /**
         * A method in enum.
         */
        public void methodInEnum() {

        }
    }

}