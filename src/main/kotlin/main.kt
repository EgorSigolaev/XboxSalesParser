import RegionType.ARG
import RegionType.TR
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import kotlin.math.roundToInt

const val liraInUah = 2.1
const val pessoInUah = 0.28
const val uahInRub = 4.20
const val pagesFrom = 21
const val pagesTo = 40
val allGames = mutableListOf<Game>()

fun main(args: Array<String>) {
    parseRegionDiscounts(ARG)
    parseRegionDiscounts(TR)
    allGames.forEach { game ->
        val uahWithoutCommission = getPriceInUAH(game.newPrice, game.regionType)
        val totalUah = uahWithoutCommission + when {
            uahWithoutCommission < 40 -> {
                20
            }
            uahWithoutCommission in 40..100 -> {
                35
            }
            uahWithoutCommission in 101..129 -> {
                45
            }
            uahWithoutCommission in 130..200 -> {
                55
            }
            uahWithoutCommission in 201..249 -> {
                65
            }
            uahWithoutCommission in 250..400 -> {
                90
            }
            else -> {
                105
            }
        }
        val totalRub = (totalUah * uahInRub).roundToInt()
        val countrySign = when (game.regionType) {
            ARG -> "🇦🇷"
            TR -> "🇹🇷"
        }
        println("${game.name} ≈ $totalUah ₴ / $totalRub ₽ - $countrySign")
    }

}

fun parseRegionDiscounts(regionType: RegionType) {
    val prefix = when (regionType) {
        ARG -> "ar"
        TR -> "tr"
    }
    for (x in pagesFrom..pagesTo) {
        val html = getHtml("https://xbdeals.net/$prefix-store/discounts/$x?type=games&sort=title-desc")
        val generalDoc = Jsoup.parse(html)
        generalDoc.getElementsByClass("game-collection-item-link").forEach {
            val game = parseGame("https://xbdeals.net${it.attr("href")}", regionType)
            if (game.newPrice != 0.0) {
                allGames.find { it.name == game.name }?.let { gameWithSameName ->
                    if (getPriceInUAH(game.newPrice, regionType) < getPriceInUAH(gameWithSameName.newPrice, regionType)) {
                        allGames.remove(gameWithSameName)
                        allGames.add(game)
                    }
                } ?: run {
                    allGames.add(game)
                }
            }

        }
        println("$regionType parsed page $x")
    }
}

fun parseGame(url: String, regionType: RegionType): Game {
    val gameHtml = getHtml(url)
    val gameDoc = Jsoup.parse(gameHtml)
    val name = gameDoc.getElementsByClass("game-title-info-name").first()?.text() ?: "Unknown"
    val oldPrice =
        gameDoc.selectXpath("//*[@id=\"game-details-right\"]/div[4]/div/div[2]/div[1]/p[2]/span").first()
            ?.text() ?: "Unknown"
    val newPrice = gameDoc.getElementsByClass("game-collection-item-discount-price").first()?.text()
        ?.replace("$ ", "")?.replace(",", "")?.toDouble() ?: 0.0

    val discountPercent = gameDoc.getElementsByClass("game-collection-item-save-regular").first()?.text() ?: ""
    val discountEndDate = gameDoc.getElementsByClass("game-cover-bottom-small").first()?.text() ?: ""
    return Game(
        name = name,
        oldPrice = oldPrice,
        newPrice = newPrice,
        discountPercent = discountPercent,
        discountEndDate = discountEndDate,
        regionType = regionType
    )
}

fun getHtml(url: String): String {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(url)
        .addHeader(
            "User-Agent",
            generateRandomUserAgent()
        )
        .addHeader(
            "Cookie",
            "psdeals_session=bk6gaqg2fen1btnof9n7lh68l8; _store=597d23bbf48312591bec1cc2cd64059b4e9b239f5f27f73f0bec3c22e4ce4d03a%3A2%3A%7Bi%3A0%3Bs%3A6%3A%22_store%22%3Bi%3A1%3Bs%3A2%3A%22AR%22%3B%7D; _language=efbf6a710e263bc15314bbbb2e228808836c0b854668c2711ab03e0b3167c6d7a%3A2%3A%7Bi%3A0%3Bs%3A9%3A%22_language%22%3Bi%3A1%3Bs%3A2%3A%22ru%22%3B%7D; _ga=GA1.2.1651825172.1646825848; _gid=GA1.2.604358126.1646825848; __gads=ID=8071be2d2d6f3425-2217ac4d58cd0063:T=1646825848:RT=1646825848:S=ALNI_MZOH9cpP1RKU9Xj_UGH7ahnosNZ2w; _gat=1"
        )
        .build()
    return client.newCall(request).execute().body!!.string()
}

fun getPriceInUAH(price: Double, regionType: RegionType): Int{
    return when (regionType) {
        ARG -> ((price * pessoInUah))
        TR -> ((price * liraInUah))
    }.roundToInt()
}

