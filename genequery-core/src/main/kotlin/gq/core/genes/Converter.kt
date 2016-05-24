package gq.core.genes

import gq.core.data.Species
import java.io.File

data class GeneMapping(val species: Species, val entrezId: Long, val otherId: String)

abstract class FromGeneToGeneConverter<TFrom, TTo : Any>(mappings: Iterable<GeneMapping> = emptyList()) {
    protected  val fromToMapping = hashMapOf<Species, Map<TFrom, TTo>>()

    init {
        populate(mappings)
    }

    abstract fun populate(initMappings: Iterable<GeneMapping>): FromGeneToGeneConverter<TFrom, TTo>

    inline fun populate(initMappingsFunc: () -> Iterable<GeneMapping>) = populate(initMappingsFunc())

    operator fun get(species: Species, fromId: TFrom) = fromToMapping[species]!![fromId]

    fun convert(species: Species, fromId: TFrom) = get(species, fromId)
    fun convert(species: Species, fromIds: Iterable<TFrom>) = convertDetailed(species, fromIds).mapNotNull { it.value }.toSet()

    fun convertDetailed(species: Species, entrezIds: Iterable<TFrom>): Map<TFrom, TTo?> {
        val currentMapping = fromToMapping[species]!!
        return entrezIds.associate { Pair(it, currentMapping[it]) }
    }

}

class ToEntrezConverter(entrezOtherMappings: Iterable<GeneMapping> = emptyList())
: FromGeneToGeneConverter<String, Long>(entrezOtherMappings) {
    override fun populate(initMappings: Iterable<GeneMapping>): ToEntrezConverter {
        val newSpeciesToOtherToEntrez = initMappings
                .groupBy { it.species }
                .mapValues { it.value
                        .groupBy({ it.otherId }, { it.entrezId })
                        .mapValues { it.value.min()!! } }
        newSpeciesToOtherToEntrez
                .forEach { fromToMapping.merge(it.key, it.value, { existing, new -> existing + new }) }
        return this
    }
}

class FromEntrezToSymbolConverter(entrezOtherMappings: Iterable<GeneMapping> = emptyList())
: FromGeneToGeneConverter<Long, String>(entrezOtherMappings) {
    override fun populate(initMappings: Iterable<GeneMapping>): FromEntrezToSymbolConverter {
        val newSpeciesToEntrezToSymbol = initMappings
                .groupBy { it.species }
                .mapValues { it.value
                        .groupBy({ it.entrezId }, { it.otherId })
                        .mapValues { it.value.first() } }
        newSpeciesToEntrezToSymbol
                .forEach { fromToMapping.merge(it.key, it.value, { existing, new -> existing + new }) }
        return this
    }
}


data class OrthologyMapping(val groupId: Int,
                            val species: Species,
                            val entrezId: Long,
                            val symbolId: String,
                            val refseqId: String)

class GeneOrthologyConverter(orthologyMappings: Iterable<OrthologyMapping>) {
    private val groupIdToOrthology = orthologyMappings
            .groupBy { it.groupId }
            .mapValues { it.value.groupBy { it.species }.mapValues { it.value.single() } }
    private val entrezToOrthology = orthologyMappings.associate { Pair(it.entrezId, groupIdToOrthology[it.groupId]!!) }
    private val symbolToOrthology = orthologyMappings.associate { Pair(it.symbolId, groupIdToOrthology[it.groupId]!!) }

    constructor(orthologyMappingsInit: () -> Iterable<OrthologyMapping>) : this(orthologyMappingsInit())

    operator fun get(entrezId: Long, species: Species) = entrezToOrthology[entrezId]?.get(species)
    operator fun get(symbolId: String, species: Species) = symbolToOrthology[symbolId]?.get(species)

    fun entrezToEntrezDetailed(entrezIds: Iterable<Long>, species: Species) =
            entrezIds.associate { Pair(it, this[it, species]?.entrezId) }

    fun entrezToSymbolDetailed(entrezIds: Iterable<Long>, species: Species) =
            entrezIds.associate { Pair(it, this[it, species]?.symbolId) }

    fun symbolToEntrezDetailed(symbolIds: Iterable<String>, species: Species) =
            symbolIds.associate { Pair(it, this[it, species]?.entrezId) }

    fun symbolToSymbolDetailed(symbolIds: Iterable<String>, species: Species) =
            symbolIds.associate { Pair(it, this[it, species]?.symbolId) }

    fun bulkEntrezToEntrez(entrezIds: Iterable<Long>,
                           speciesTo: Species) = entrezToEntrezDetailed(entrezIds, speciesTo).values.mapNotNull { it }
    fun bulkEntrezToSymbol(entrezIds: Iterable<Long>,
                           speciesTo: Species) = entrezToSymbolDetailed(entrezIds, speciesTo).values.mapNotNull { it }
    fun bulkSymbolToEntrez(symbolIds: Iterable<String>,
                           speciesTo: Species) = symbolToEntrezDetailed(symbolIds, speciesTo).values.mapNotNull { it }
    fun bulkSymbolToSymbol(symbolIds: Iterable<String>,
                           speciesTo: Species) = symbolToSymbolDetailed(symbolIds, speciesTo).values.mapNotNull { it }
}


fun mapGenesToNormalized(genes: List<String>,
                         format: GeneFormat = gq.core.genes.detectGeneSetFormat(genes)): Map<String, String> {
    val result = genes.associate { Pair(it, it.toUpperCase()) }
    return when(format) {
        GeneFormat.ENSEMBL, GeneFormat.REFSEQ -> result.mapValues { it.value.substringBefore(".") }
        else -> result
    }
}


fun File.readGeneOrthologyMappings(): Iterable<OrthologyMapping> = readLines().mapNotNull {
    if (it.isNotEmpty()) {
        val (groupId, species, entrez, symbol, refseq) = it.split("\t")
        OrthologyMapping(groupId.toInt(), Species.fromOriginal(species), entrez.toLong(), symbol, refseq)
    } else {
        null
    }
}


fun File.readGeneMappings(): Iterable<GeneMapping> = readLines().mapNotNull {
    if (it.isNotEmpty()) {
        val (species, entrez, other) = it.split("\t")
        GeneMapping(Species.fromOriginal(species), entrez.toLong(), other)
    } else {
        null
    }
}


fun detectGeneSetFormat(genes: List<String>): GeneFormat {
    require(genes.isNotEmpty(), { "Gene list is empty." })

    val actualGeneFormat = guessGeneFormat(genes.first())
    genes.forEachIndexed { i, gene ->
        val currentGeneFormat = guessGeneFormat(gene)
        if (currentGeneFormat != actualGeneFormat) throw IllegalArgumentException(
                "Ambiguous gene format: first gene (${genes.first()}) is $actualGeneFormat, but ${i + 1}th gene ($gene) is $currentGeneFormat")
    }
    return actualGeneFormat
}