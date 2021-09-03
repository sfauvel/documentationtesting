package org.sfvl.doctesting.sample;

// tag::ClassWithNestedClass[]
public class ClassWithNestedClass {
    public int simpleMethod() {
        return 0;
    }

    public class NestedClass {
        public int nestedMethod() { return 2; }
    }
}
// end::ClassWithNestedClass[]
