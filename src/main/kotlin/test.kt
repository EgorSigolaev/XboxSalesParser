import org.jsoup.Jsoup

fun main() {
    val gameDoc = Jsoup.connect("https://xbdeals.net/ar-store/game/891396/blast-brawl-2").get()
    val elem = gameDoc.getElementsByClass("game-title-info-name").first()
    val name = elem?.text() ?: "Unknown"
    println("Name $name")
}