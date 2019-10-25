package gq.rest.services

import gq.core.data.Species
import gq.core.gea.*
import gq.rest.GQDataRepository
import gq.rest.config.GQRestProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.configurationprocessor.json.JSONObject
import org.springframework.stereotype.Service

data class EnrichmentResponse(val identifiedGeneFormat: String,
                              val geneConversionMap: Map<String, Long?>,
                              val enrichmentResultItems: List<EnrichmentResultItem>,
                              val meta: Map<String, Map<String, String>>)

@Service
open class GeneSetEnrichmentService @Autowired constructor(
        private val gqRestProperties: GQRestProperties,
        private val gqDataRepository: GQDataRepository) {

    open fun findEnrichedModules(rawGenes: List<String>,
                                 speciesFrom: Species,
                                 speciesTo: Species = speciesFrom): EnrichmentResponse {
        val (identifiedGeneFormat, conversionMap) = convertGenesToEntrez(rawGenes, speciesFrom, speciesTo, gqDataRepository)

        val entrezIds = conversionMap.values.filterNotNull()
        val enrichmentItems = findBonferroniSignificant(
                gqDataRepository.moduleCollection,
                SpecifiedEntrezGenes(speciesTo, entrezIds),
                gqRestProperties.adjPvalueMin)

        val enrichedModules = enrichmentItems.map { it.datasetId }

        val modulesToInfo = enrichedModules.associateWith { it ->
            val string = gqDataRepository.moduleInfoCollection.idToInfo[it]?.jsonString ?: "{}"
            val obj = JSONObject(string)
            var keys = obj.keys().asSequence().toList()
            keys = keys.map { itt -> itt as String }
            keys.associateWith { itt -> obj.getString(itt) }
        }

        return EnrichmentResponse(identifiedGeneFormat.formatName, conversionMap, enrichmentItems, modulesToInfo)
    }
}