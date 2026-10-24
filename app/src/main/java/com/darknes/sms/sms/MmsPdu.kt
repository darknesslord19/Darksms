package com.darknes.sms.sms

import java.io.ByteArrayOutputStream

/**
 * Minimal MMS M-Send.req PDU kodlayicisi (OMA/WAP-209 MMS encapsulation).
 * Bir metin parcasi ve bir medya parcasi iceren multipart/mixed govde uretir.
 * SmsManager.sendMultimediaMessage ile gonderilecek ham PDU dondurur.
 *
 * Not: Gercek MMS teslimi operatore, APN'e ve veri baglantisina baglidir.
 */
object MmsPdu {

    // WSP/MMS iyi bilinen alan kodlari (kisa form = kod | 0x80)
    private const val FROM = 0x89
    private const val MESSAGE_TYPE = 0x8C
    private const val MMS_VERSION = 0x8D
    private const val TO = 0x97
    private const val TRANSACTION_ID = 0x98
    private const val CONTENT_TYPE = 0x84

    private const val MSG_TYPE_SEND_REQ = 0x80
    private const val INSERT_ADDRESS_TOKEN = 0x81

    /** application/vnd.wap.multipart.mixed kisa-tamsayi degeri */
    private const val CT_MULTIPART_MIXED = 0xA3

    fun buildSendReq(to: String, text: String?, media: ByteArray?, mediaMime: String?): ByteArray {
        val out = ByteArrayOutputStream()

        // X-Mms-Message-Type: m-send-req
        out.write(MESSAGE_TYPE); out.write(MSG_TYPE_SEND_REQ)
        // X-Mms-Transaction-Id
        out.write(TRANSACTION_ID); writeTextString(out, "T" + System.currentTimeMillis())
        // MMS-Version 1.0
        out.write(MMS_VERSION); out.write(0x90)
        // From: insert-address-token (MMSC gondereni ekler)
        out.write(FROM); out.write(0x01); out.write(INSERT_ADDRESS_TOKEN)
        // To: alici adresi
        out.write(TO); writeEncodedString(out, "$to/TYPE=PLMN")
        // Content-Type: multipart/mixed (govdeden hemen once son baslik)
        out.write(CONTENT_TYPE); out.write(CT_MULTIPART_MIXED)

        // Govde: parca sayisi + parcalar
        val parts = ArrayList<ByteArray>()
        if (!text.isNullOrEmpty()) parts.add(buildTextPart(text))
        if (media != null && media.isNotEmpty()) parts.add(buildMediaPart(media, mediaMime ?: "image/jpeg"))

        writeUintvar(out, parts.size.toLong())
        for (p in parts) out.write(p)

        return out.toByteArray()
    }

    private fun buildTextPart(text: String): ByteArray {
        val data = text.toByteArray(Charsets.UTF_8)
        // Content-Type: text/plain; charset=utf-8
        // [value-length][0x83 text/plain][0x81 charset param][0xEA utf-8(106) short-integer]
        val ct = byteArrayOf(0x03.toByte(), 0x83.toByte(), 0x81.toByte(), 0xEA.toByte())
        return assemblePart(ct, data)
    }

    private fun buildMediaPart(data: ByteArray, mime: String): ByteArray {
        val ct = encodeContentType(mime)
        return assemblePart(ct, data)
    }

    /** Tek parca: [HeadersLen][DataLen][ContentType+Headers][Data] */
    private fun assemblePart(contentTypeBytes: ByteArray, data: ByteArray): ByteArray {
        val out = ByteArrayOutputStream()
        writeUintvar(out, contentTypeBytes.size.toLong())  // HeadersLen (= content-type uzunlugu)
        writeUintvar(out, data.size.toLong())              // DataLen
        out.write(contentTypeBytes)
        out.write(data)
        return out.toByteArray()
    }

    /** Medya icin Content-Type kodlar; bilinen tipler kisa-tamsayi, digerleri metin. */
    private fun encodeContentType(mime: String): ByteArray {
        val wellKnown = when (mime.lowercase()) {
            "image/gif" -> 0x9D
            "image/jpeg", "image/jpg" -> 0x9E
            "image/tiff" -> 0x9F
            "image/png" -> 0xA0
            "image/vnd.wap.wbmp" -> 0xA1
            else -> null
        }
        return if (wellKnown != null) {
            byteArrayOf(wellKnown.toByte())
        } else {
            // Genel form: [value-length][text-string mime + null]
            val mimeBytes = mime.toByteArray(Charsets.US_ASCII)
            val b = ByteArrayOutputStream()
            writeValueLength(b, (mimeBytes.size + 1).toLong())
            b.write(mimeBytes); b.write(0x00)
            b.toByteArray()
        }
    }

    // ---- WSP ilkel kodlamalari ----

    private fun writeTextString(out: ByteArrayOutputStream, s: String) {
        val bytes = s.toByteArray(Charsets.US_ASCII)
        if (bytes.isNotEmpty() && (bytes[0].toInt() and 0xFF) >= 0x80) out.write(0x7F) // quote
        out.write(bytes); out.write(0x00)
    }

    private fun writeEncodedString(out: ByteArrayOutputStream, s: String) {
        // Basit encoded-string-value: metin + null (ASCII)
        out.write(s.toByteArray(Charsets.UTF_8)); out.write(0x00)
    }

    private fun writeValueLength(out: ByteArrayOutputStream, len: Long) {
        if (len < 31) {
            out.write(len.toInt())
        } else {
            out.write(31) // length-quote
            writeUintvar(out, len)
        }
    }

    /** Degiskenlenebilir uzunluklu tamsayi (uintvar). */
    private fun writeUintvar(out: ByteArrayOutputStream, value: Long) {
        var v = value
        if (v == 0L) { out.write(0); return }
        val tmp = ArrayList<Int>()
        while (v > 0) { tmp.add((v and 0x7F).toInt()); v = v shr 7 }
        for (i in tmp.indices.reversed()) {
            var b = tmp[i]
            if (i != 0) b = b or 0x80 // son bayt disindakilerde devam biti
            out.write(b)
        }
    }
}
