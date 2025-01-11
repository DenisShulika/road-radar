import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.android.gms.maps.model.LatLng
import java.io.InputStream

data class Region(val name: String, val latitude: Double, val longitude: Double)

object RegionLoader {
    fun loadRegionsFromJson(context: Context): Map<String, LatLng> {
        val inputStream: InputStream = context.assets.open("regionsLatLng.json")
        val json = inputStream.bufferedReader().use { it.readText() }

        val regionListType = object : TypeToken<List<Region>>() {}.type
        val regions: List<Region> = Gson().fromJson(json, regionListType)

        return regions.associate { it.name to LatLng(it.latitude, it.longitude) }
    }
}
