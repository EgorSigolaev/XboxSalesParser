data class Game(
    val name: String,
    val oldPrice: String,
    val newPrice: Double,
    val discountPercent: String,
    val discountEndDate: String,
    val regionType: RegionType
)

enum class RegionType{
    ARG, TR
}