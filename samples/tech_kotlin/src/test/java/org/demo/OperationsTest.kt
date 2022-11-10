package org.demo

import org.demo.Operations.square
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.sfvl.docformatter.Formatter
import org.sfvl.doctesting.junitextension.ApprovalsExtension
import org.sfvl.doctesting.junitextension.HtmlPageExtension

@ExtendWith(HtmlPageExtension::class)
@DisplayName("Operations")
class OperationsTest {
    @Test
    fun calculate_the_square_of_a_number() {
        val a = 5
        val result = square(a)

        doc.write(
            "In mathematics, a square is the result of multiplying a number by itself.",
            "",
            String.format("The square of %d is equal to %d", a, result)
        )
    }

    companion object {
        @RegisterExtension
        var doc: ApprovalsExtension<*, *> = ApprovalsExtension(KTDocWriter<Formatter>())
    }
}