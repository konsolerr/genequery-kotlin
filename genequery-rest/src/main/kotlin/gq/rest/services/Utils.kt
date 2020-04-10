package gq.rest.services

import gq.core.data.Species
import gq.core.genes.GeneFormat
import gq.rest.GQDataRepository
import gq.rest.exceptions.BadRequestException

fun convertGenesToEntrez(rawGenes: List<String>,
                         speciesFrom: Species,
                         speciesTo: Species = speciesFrom,
                         gqDataRepository: GQDataRepository): Pair<GeneFormat, Map<String, Long?>> {
    try {
        val currentGeneFormat = GeneFormat.guess(rawGenes)
        return Pair(
                currentGeneFormat,
                gqDataRepository.smartConverter.toEntrez(rawGenes, currentGeneFormat, speciesFrom, speciesTo))
    } catch (ex: IllegalArgumentException) {
        throw BadRequestException(ex)
    }
}

fun convertGenesToSymbol(rawGenes: List<String>,
                         speciesFrom: Species,
                         speciesTo: Species = speciesFrom,
                         gqDataRepository: GQDataRepository): Pair<GeneFormat, Map<String, String?>> {
    try {
        val currentGeneFormat = GeneFormat.guess(rawGenes)
        return Pair(
                currentGeneFormat,
                gqDataRepository.smartConverter.toSymbol(rawGenes, currentGeneFormat, speciesFrom, speciesTo))
    } catch (ex: IllegalArgumentException) {
        throw BadRequestException(ex)
    }
}