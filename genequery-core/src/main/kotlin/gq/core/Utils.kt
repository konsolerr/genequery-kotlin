package gq.core


fun <T1, T2, T3> crossLinkMap(firstMap: Map<T1, T2?>, secondMap: Map<T2, T3?>) =
        firstMap.mapValues {
            val value = it.value;
            if (value != null) secondMap[value] else null
        }
