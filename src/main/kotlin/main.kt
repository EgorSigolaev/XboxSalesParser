import org.jsoup.Jsoup
import kotlin.math.roundToInt

fun main(args: Array<String>) {
    val pessoInUah = 0.28
    val pessoInRub = 1.20
    val percent = 100
    val games = mutableListOf<Game>()
    for (x in 1..2){
        val generalDoc = Jsoup.connect("https://xbdeals.net/ar-store/discounts/$x?type=games").get()
        generalDoc.getElementsByClass("game-collection-item-link").forEach {
            val gameDoc = Jsoup.connect("https://xbdeals.net${it.attr("href")}").get()
            val name = gameDoc.getElementsByClass("game-title-info-name").first()?.text() ?: "Unknown"
            val oldPrice = gameDoc.selectXpath("//*[@id=\"game-details-right\"]/div[4]/div/div[2]/div[1]/p[2]/span").first()?.text() ?: "Unknown"
            val newPrice = gameDoc.selectXpath("/html/body/div[1]/div[2]/div/div[2]/div[4]/div[1]/div[1]/div/a/div/div[3]/p/meta[1]").first()?.attr("content")?.toDouble() ?: 0.0
            val discountPercent = gameDoc.getElementsByClass("game-collection-item-save-regular").first()?.text() ?: ""
            val discountEndDate = gameDoc.getElementsByClass("game-cover-bottom-small").first()?.text() ?: ""
            val game = Game(
                name = name,
                oldPriceArs = oldPrice,
                newPriceArs = newPrice,
                discountPercent = discountPercent,
                discountEndDate = discountEndDate
            )
            games.add(game)
        }
        println("Страница $x спарсена")
    }
    games.sortBy { it.name }
    games.forEach { game ->
        val totalUah = ((game.newPriceArs * pessoInUah) * (1 + percent / 100.0)).roundToInt()
        val totalRub = ((game.newPriceArs * pessoInRub) * (1 + percent / 100.0)).roundToInt()
        println("${game.name} - $totalUah ₴ / $totalRub ₽")
    }


}