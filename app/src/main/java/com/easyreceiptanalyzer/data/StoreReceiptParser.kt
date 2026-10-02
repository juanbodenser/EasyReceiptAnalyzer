package com.easyreceiptanalyzer.data

interface StoreReceiptParser {
    fun matches(lines: List<String>): Boolean
    fun parse(lines: List<String>): ParsedReceipt
}
