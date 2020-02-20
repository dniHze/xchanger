package dev.dnihze.revorate.model

/**
 * This class exists as reference model for Currencies within the app.
 * Locales of a platform could contain some of the currencies data, but
 * it depends on Android version and vendor.
 *
 * @param isoCode will be used as stable ID for adapter. Neat.
 * @param isoName name of currency from server.
 * @param digitsAfterSeparator  some currencies (like Japanese yen)
 *                              don't have cents. Will be used for formatting rules
 */
enum class Currency(
    val isoCode: Int,
    val isoName: String,
    val digitsAfterSeparator: Int = 2
) {
    AUD(isoCode = 36, isoName = "AUD"),
    BGN(isoCode = 975, isoName = "BGN"),
    BRL(isoCode = 986, isoName = "BRL"),
    CAD(isoCode = 124, isoName = "CAD"),
    CHF(isoCode = 756, isoName = "CHF"),
    CNY(isoCode = 156, isoName = "CNY"),
    CZK(isoCode = 203, isoName = "CZK"),
    DKK(isoCode = 208, isoName = "DKK"),
    EUR(isoCode = 978, isoName = "EUR"),
    GBP(isoCode = 826, isoName = "GBP"),
    HKD(isoCode = 344, isoName = "HKD"),
    HRK(isoCode = 191, isoName = "HRK"),
    HUF(isoCode = 348, isoName = "HUF"),
    IDR(isoCode = 360, isoName = "IDR"),
    ILS(isoCode = 376, isoName = "ILS"),
    INR(isoCode = 356, isoName = "INR"),
    ISK(isoCode = 352, isoName = "ISK", digitsAfterSeparator = 0),
    JPY(isoCode = 392, isoName = "JPY", digitsAfterSeparator = 0),
    KRW(isoCode = 410, isoName = "KRW", digitsAfterSeparator = 0),
    MXN(isoCode = 484, isoName = "MXN"),
    MYR(isoCode = 458, isoName = "MYR"),
    NOK(isoCode = 578, isoName = "NOK"),
    NZD(isoCode = 554, isoName = "NZD"),
    PHP(isoCode = 608, isoName = "PHP"),
    PLN(isoCode = 985, isoName = "PLN"),
    RON(isoCode = 946, isoName = "RON"),
    RUB(isoCode = 643, isoName = "RUB"),
    SEK(isoCode = 752, isoName = "SEK"),
    SGD(isoCode = 705, isoName = "SGD"),
    THB(isoCode = 764, isoName = "THB"),
    USD(isoCode = 840, isoName = "USD"),
    ZAR(isoCode = 710, isoName = "ZAR");


    override fun toString(): String {
        return isoName
    }}