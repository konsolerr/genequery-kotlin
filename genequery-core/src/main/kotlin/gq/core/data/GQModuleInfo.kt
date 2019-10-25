package gq.core.data
import java.io.File
import org.json.JSONObject


data class GQModuleInfo(val datasetId: String, val jsonString: String)


class GQModuleInfoCollection(gqModuleInfoItems: Iterable<GQModuleInfo>) {
    constructor(gqModuleInfoInit: () -> Iterable<GQModuleInfo>) : this(gqModuleInfoInit())
    val idToInfo = gqModuleInfoItems.associateBy { it.datasetId }
    operator fun get(datasetId: String) = idToInfo[datasetId]
    fun size() = idToInfo.size
}


fun readModuleInfoFromFile(path: String): List<GQModuleInfo> {
    val infoList = mutableListOf<GQModuleInfo>()
    val jsonString = File(path).readText()
    val jsonObject = JSONObject(jsonString)

    for (datasetId in jsonObject.keySet()) {
        val jsonContent = jsonObject.getJSONObject(datasetId).toString();
        infoList.add(GQModuleInfo(datasetId, jsonContent))
    }
    return infoList
}