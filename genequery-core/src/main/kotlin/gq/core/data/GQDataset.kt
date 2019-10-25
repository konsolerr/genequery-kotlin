package gq.core.data


infix fun LongArray.intersectWithSorted(other: LongArray): LongArray {
    val result = mutableListOf<Long>()
    var thisIndex = 0
    var otherIndex = 0

    while (thisIndex < size && otherIndex < other.size) {
        if (this[thisIndex] == other[otherIndex]) {
            result.add(this[thisIndex]);
            thisIndex++;
            otherIndex++;
        } else if (this[thisIndex] > other[otherIndex]) {
            otherIndex++;
        } else {
            thisIndex++;
        }
    }
    return result.toLongArray()
}


fun LongArray.sizeOfIntersectionWithSorted(other: LongArray): Int {
    var result = 0
    var thisIndex = 0
    var otherIndex = 0

    while (thisIndex < size && otherIndex < other.size) {
        when {
            this[thisIndex] == other[otherIndex] -> {
                result++
                thisIndex++;
                otherIndex++;
            }
            this[thisIndex] > other[otherIndex] -> otherIndex++
            else -> thisIndex++
        }
    }
    return result
}


fun LongArray.sizeOfIntersectionWithSorted(other: List<Long>) = sizeOfIntersectionWithSorted(other.toLongArray())


open class GQDataset(
        val datasetId: String,
        val universeId: String,
        val species: Species,
        entrezIds: LongArray) {
    init {
        require(entrezIds.isNotEmpty()) {"Empty entrezIds array"}
    }

    val sortedEntrezIds = entrezIds.sorted().toLongArray()
    val size = sortedEntrezIds.size
    val isUniverse = datasetId == universeId

    companion object {
        fun buildByFullName(datasetId: String, universeId: String, species: Species, entrezIds: LongArray): GQDataset {
            return GQDataset(datasetId, universeId, species, entrezIds)
        }

        fun joinFullName(datasetId: String) = "$datasetId"
    }

    fun joinFullName() = joinFullName(datasetId)
    fun fullName() = datasetId

    override fun toString() = "${joinFullName()}, $species, $size genes)"
}