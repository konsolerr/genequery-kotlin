package gq.core.genes

import gq.core.data.Species
import java.io.File

data class GeneMapping(val species: Species, val entrezId: Long, val otherId: String)

class GeneConverter(speciesEntrezOther: Iterable<GeneMapping> = emptyList(),
                    speciesEntrezSymbol: Iterable<GeneMapping> = emptyList()) {
    private val speciesToEntrezToSymbol = hashMapOf<Species, Map<Long, String>>()
    private val speciesToOtherToEntrez = hashMapOf<Species, Map<String, Long>>()

    init {
        populateOtherToEntrez(speciesEntrezOther)
        populateEntrezToSymbol(speciesEntrezSymbol)
    }

    fun populateOtherToEntrez(speciesEntrezOtherInit: () -> Iterable<GeneMapping>) =
            populateOtherToEntrez(speciesEntrezOtherInit())

    fun populateOtherToEntrez(speciesEntrezOther: Iterable<GeneMapping>): GeneConverter {
        val newSpeciesToOtherToEntrez = speciesEntrezOther
                .groupBy { it.species }
                .mapValues { it.value
                        .groupBy({ it.otherId }, { it.entrezId })
                        .mapValues { it.value.min()!! } }
        newSpeciesToOtherToEntrez
                .forEach { speciesToOtherToEntrez.merge(it.key, it.value, { existing, new -> existing.plus(new) }) }
        return this
    }

    fun populateEntrezToSymbol(speciesEntrezSymbolInit: () -> Iterable<GeneMapping>) =
            populateEntrezToSymbol(speciesEntrezSymbolInit())

    fun populateEntrezToSymbol(speciesEntrezSymbol: Iterable<GeneMapping>): GeneConverter {
        val newSpeciesToEntrezToSymbol = speciesEntrezSymbol
                .groupBy { it.species }
                .mapValues { it.value
                        .groupBy({ it.entrezId }, { it.otherId })
                        .mapValues { it.value.first() } }
        newSpeciesToEntrezToSymbol
                .forEach { speciesToEntrezToSymbol.merge(it.key, it.value, { existing, new -> existing.plus(new) }) }
        return this
    }

    operator fun get(species: Species, entrezId: Long): String? {
        return speciesToEntrezToSymbol[species]!![entrezId]
    }

    operator fun get(species: Species, otherId: String): Long? {
        return speciesToOtherToEntrez[species]!![otherId]
    }

}


internal fun File.readGeneMappings(): Iterable<GeneMapping> = readLines().mapNotNull {
    if (it.isNotEmpty()) {
        val (species, entrez, other) = it.split("\t")
        GeneMapping(Species.fromOriginal(species), entrez.toLong(), other)
    } else {
        null
    }
}