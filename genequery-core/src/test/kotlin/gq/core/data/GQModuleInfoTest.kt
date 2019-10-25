package gq.core.data

import org.junit.Assert.assertEquals
import org.junit.Test

class GQModuleInfoTest {

    @Test
    fun testJsonParser() {
        val infoCollection = GQModuleInfoCollection(
                readModuleInfoFromFile(
                        Thread.currentThread().contextClassLoader.getResource("collection/annotation.json").path
                )
        )
        assertEquals(12, infoCollection.size())
    }
}