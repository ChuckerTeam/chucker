package com.chuckerteam.chucker.api.internal.support

import java.util.stream.Stream
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

/**
 * @author Olivier Perez
 */
internal class IOUtilsTest {

    private val ioUtils = IOUtils(null)

    @ParameterizedTest(name = "{0} must be supported? {1}")
    @MethodSource("supportedEncodingSource")
    @DisplayName("Check if body encoding is supported")
    fun bodyHasSupportedEncoding(encoding: String?, supported: Boolean) {
        // When
        val result = ioUtils.bodyHasSupportedEncoding(encoding)

        // Then
        assertEquals(supported, result)
    }

    companion object {
        @JvmStatic
        fun supportedEncodingSource(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(null, true),
                Arguments.of("", true),
                Arguments.of("identity", true),
                Arguments.of("gzip", true),
                Arguments.of("other", false)
            )
        }
    }
}