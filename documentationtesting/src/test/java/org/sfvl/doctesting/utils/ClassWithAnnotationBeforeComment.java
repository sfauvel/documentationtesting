package org.sfvl.doctesting.utils;

import org.sfvl.doctesting.NotIncludeToDoc;


// tag::ClassWithAnnotationBeforeComment[]

@NotIncludeToDoc
/**
 * Comment of the class.
 */
class ClassWithAnnotationBeforeComment {
    // ...
}
// end::ClassWithAnnotationBeforeComment[]