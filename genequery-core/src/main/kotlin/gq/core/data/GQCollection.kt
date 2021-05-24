package gq.core.data

import java.io.File
import de.jupf.staticlog.Log

fun populateModulesFromGmt(path: String, species: Species, dest: MutableList<GQDataset>) {
    File(path).forEachLine {
        if (it.isNotEmpty()) {
            try {
                val gmtParts = it.split("\t")
                when (gmtParts.size) {
                    2 -> {
                        Log.warn("Failed to parse GMT line: $it. Empty module")
                    }
                    3 -> {
                        val (datasetId, universeId, commaSepEntrezIds) = it.split("\t")
                        dest.add(GQDataset.buildByFullName(
                                datasetId,
                                universeId,
                                species,
                                commaSepEntrezIds.split(',').map { it.toLong() }.toLongArray()))
                    }
                    else -> {
                        Log.error("Failed to parse GMT line: $it")
                    }
                }
            } catch(e: Exception) {
                Log.error("Failed to parse GMT line: $it. With exception $e")
            }
        }
    }
}

fun readModulesFromFiles(speciesToPath: Iterable<Pair<Species, String>>): List<GQDataset> {
    val modules = mutableListOf<GQDataset>()
    speciesToPath.forEach { populateModulesFromGmt(it.second, it.first, modules) }
    return modules
}

fun readModulesFromFiles(vararg speciesToPath: Pair<Species, String>) = readModulesFromFiles(speciesToPath.asList())

class GQModuleCollection(modules: Iterable<GQDataset>) {

    constructor(modulesInit: () -> Iterable<GQDataset>) : this(modulesInit())

    val fullNameToGQDataset = modules.associateBy { it.fullName() }

    val speciesToDataSets = fullNameToGQDataset.values.groupBy { it.species }

    val valid = modules.none {
        val universe = fullNameToGQDataset[it.universeId]
        universe == null || it.sortedEntrezIds.sizeOfIntersectionWithSorted(universe.sortedEntrezIds) != it.size
    }


    override fun toString(): String =
            "GQCollection(${fullNameToGQDataset.size} data sets, ${speciesToDataSets.size} species)"
}