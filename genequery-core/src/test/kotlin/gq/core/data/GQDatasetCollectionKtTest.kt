package gq.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GQDatasetCollectionKtTest {

    companion object {
        var dataset = GQModuleCollection(readModulesFromFiles(
                Species.HUMAN to Thread.currentThread().contextClassLoader.getResource("collection/hs.modules.gmt").path,
                Species.MOUSE to Thread.currentThread().contextClassLoader.getResource("collection/mm.modules.gmt").path))

        var invalidDataset = GQModuleCollection(readModulesFromFiles(
                Species.HUMAN to Thread.currentThread().contextClassLoader.getResource("collection/hs.modules.invalid.gmt").path))

        var validDataset = GQModuleCollection(readModulesFromFiles(
                Species.HUMAN to Thread.currentThread().contextClassLoader.getResource("gea/mm.modules.gmt").path))
    }

    @Test
    fun testDatasetValidity() {
        assertEquals(false, invalidDataset.valid)
        assertEquals(true, dataset.valid)
        assertEquals(true, validDataset.valid)
    }

    @Test
    fun testGetFullNameToGQModule() {
        assertEquals(25, dataset.fullNameToGQDataset.size)
        assertEquals(13, dataset.fullNameToGQDataset["GSE1000_GPL96#0"]!!.size)
        assertTrue("GSE1000_GPL96#4" in dataset.fullNameToGQDataset)
    }

    @Test
    fun testGetSpeciesToModules() {
        assertTrue(Species.MOUSE in dataset.speciesToDataSets)
        assertTrue(Species.HUMAN in dataset.speciesToDataSets)
        assertEquals(2, dataset.speciesToDataSets.size)
        assertEquals(12, dataset.speciesToDataSets[Species.HUMAN]!!.size)
        assertEquals(13, dataset.speciesToDataSets[Species.MOUSE]!!.size)
    }


    @Test
    fun testGetSpeciesToGseGpl() {
        assertEquals(listOf("UNIVERSE_HS", "GSE1000_GPL96#0", "GSE1000_GPL96#1",
                "GSE1000_GPL96#2", "GSE1000_GPL96#3", "GSE1000_GPL96#4", "GSE1001_GPL96#0",
                "GSE1001_GPL96#1", "GSE1001_GPL96#2", "GSE1001_GPL96#3", "GSE1001_GPL96#4", "GSE1001_GPL96#5"),
                dataset.speciesToDataSets[Species.HUMAN]?.map { it.datasetId })
    }
}