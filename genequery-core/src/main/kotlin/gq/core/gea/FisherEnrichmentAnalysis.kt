package gq.core.gea

import gq.core.data.*
import gq.core.math.FisherExact

data class EnrichmentResultItem(val datasetId: String,
                                val pvalue: Double,
                                val logPvalue: Double,
                                val adjPvalue: Double,
                                val logAdjPvalue: Double,
                                val intersectionSize: Int,
                                val moduleSize: Int) : Comparable<EnrichmentResultItem> {

    companion object {
        const val MIN_LOG_P_VALUE = -325.0
    }

    constructor(dataset: GQDataset, pvalue: Double, adjPvalue: Double, intersectionSize: Int) : this(
            dataset.datasetId,
            pvalue,
            if (pvalue > 0) Math.log10(pvalue) else MIN_LOG_P_VALUE,
            adjPvalue,
            if (adjPvalue > 0) Math.log10(adjPvalue) else MIN_LOG_P_VALUE,
            intersectionSize,
            dataset.size)

    override fun compareTo(other: EnrichmentResultItem) = logPvalue.compareTo(other.logPvalue)
}

data class SpecifiedEntrezGenes(val species: Species, val entrezIds: List<Long>)


fun findBonferroniSignificant(
        moduleCollection: GQModuleCollection,
        query: SpecifiedEntrezGenes,
        bonferroniMaxPvalue: Double = 0.01): List<EnrichmentResultItem> {
    val dataSetsForThisSpecies = moduleCollection.speciesToDataSets[query.species] ?: return emptyList()
    val queryEntrezIds = if (query.entrezIds.isNotEmpty()) query.entrezIds.toSortedSet().toLongArray() else return emptyList()
    val moduleCount = moduleCollection.speciesToDataSets.getValue(query.species).filter { !it.isUniverse }.size

    return dataSetsForThisSpecies.mapNotNull(
            fun(it: GQDataset): EnrichmentResultItem? {

                val universe = moduleCollection.fullNameToGQDataset.getValue(it.universeId)
                val universeSize = universe.size
                val queryUniverseOverlap = universe.sortedEntrezIds.sizeOfIntersectionWithSorted(queryEntrezIds);
                if (queryUniverseOverlap == 0) return null


                val moduleIntersectionSize = it.sortedEntrezIds.sizeOfIntersectionWithSorted(queryEntrezIds)
                if (moduleIntersectionSize == 0) return null

                val moduleAndNotQuery = it.size - moduleIntersectionSize
                val queryAndNotModule = queryUniverseOverlap - moduleIntersectionSize
                val restOfGenes = universeSize - moduleIntersectionSize - queryAndNotModule - moduleAndNotQuery

                val pvalue = FisherExact.instance.rightTailPvalue(
                        moduleIntersectionSize, moduleAndNotQuery, queryAndNotModule, restOfGenes)
                val adjustedPvalue = pvalue * moduleCount
                return if (adjustedPvalue <= bonferroniMaxPvalue) {
                    EnrichmentResultItem(it, pvalue, adjustedPvalue, moduleIntersectionSize)
                } else {
                    null
                }
            }).sorted()
}

