package org.sfvl.doctesting.sample;


public class FieldWithCommentToExtract {
    /**
     * Comment on a public field.
     */
    public String publicField;

    /**
     * Comment on a private field.
     */
    private String privateField;

    private String fieldWithoutComment;

    public static class SubClassWithFieldToExtract {
        /**
         * Comment on a subclass field.
         */
        public String subclassField;
    }

}