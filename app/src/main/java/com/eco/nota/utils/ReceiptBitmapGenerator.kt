package com.eco.nota.utils

import android.content.Context
import android.graphics.*
import android.net.Uri
import com.eco.nota.models.BusinessProfile
import com.eco.nota.models.Nota
import java.io.InputStream
import java.text.NumberFormat
import java.util.Locale

object ReceiptBitmapGenerator {

    private fun formatCurrency(amount: Long): String {
        return try {
            NumberFormat.getInstance(Locale("id", "ID")).format(amount)
        } catch (e: Exception) {
            amount.toString()
        }
    }

    fun generateReceiptBitmap(context: Context, nota: Nota, businessProfile: BusinessProfile, isThermal: Boolean = true): Bitmap {
        val width = 1000
        val padding = 54f
        val contentWidth = width - (padding * 2)

        val isLunas = nota.status.equals("Lunas", ignoreCase = true)
        val storeName = businessProfile.name.ifEmpty { "ECO TOBACCO" }
        val storeAddress = businessProfile.address.ifEmpty { "Jaten" }
        val storeNotes = businessProfile.notes
        
        // Decode QRIS Bitmap if needed for unpaid/TAGIHAN status
        var originalQris: Bitmap? = null
        if (nota.paymentMethod.equals("QRIS", ignoreCase = true) && !businessProfile.qrisUri.isNullOrEmpty() && !isLunas) {
            try {
                val uri = Uri.parse(businessProfile.qrisUri)
                val stream: InputStream? = if (businessProfile.qrisUri!!.startsWith("/")) {
                    java.io.File(businessProfile.qrisUri!!).inputStream()
                } else {
                    context.contentResolver.openInputStream(uri)
                }
                stream?.use {
                    val options = BitmapFactory.Options().apply {
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                        inScaled = false
                    }
                    originalQris = BitmapFactory.decodeStream(it, null, options)
                }
            } catch (e: Exception) {
                // ignore
            }
        }

        // Calculate proportional width and height for QR code if showing
        var qrisRenderWidth = 0f
        var qrisRenderHeight = 0f
        if (originalQris != null && !isLunas) {
            val origW = originalQris!!.width.toFloat().coerceAtLeast(1f)
            val origH = originalQris!!.height.toFloat().coerceAtLeast(1f)
            val aspect = origH / origW
            val maxAllowedWidth = (contentWidth - 60f).coerceAtMost(680f)
            qrisRenderWidth = maxAllowedWidth
            qrisRenderHeight = maxAllowedWidth * aspect
            if (qrisRenderHeight > 900f) {
                qrisRenderHeight = 900f
                qrisRenderWidth = 900f / aspect
            }
        }

        // Pre-calculate heights dynamically
        var totalHeight = 50f // top margin

        if (isLunas && nota.paymentMethod.equals("QRIS", ignoreCase = true)) {
            // Compact professional QRIS header box at the top
            totalHeight += 74f // Box height
            totalHeight += 26f // spacing
            totalHeight += 72f // Store Name
            totalHeight += 44f // Address
            if (storeNotes.isNotEmpty()) totalHeight += 38f
        } else {
            // Header
            totalHeight += 72f // Store Name
            totalHeight += 44f // Address
            if (storeNotes.isNotEmpty()) totalHeight += 38f

            when {
                nota.paymentMethod.equals("QRIS", ignoreCase = true) -> {
                    if (originalQris != null) {
                        totalHeight += qrisRenderHeight + 80f // QRIS image, quiet zone & frame
                        totalHeight += 45f // "SCAN QRIS DI ATAS UNTUK PEMBAYARAN" text
                        totalHeight += 25f
                    } else {
                        totalHeight += 85f // QRIS placeholder box
                        totalHeight += 25f
                    }
                }
                nota.paymentMethod.equals("Transfer", ignoreCase = true) -> {
                    totalHeight += 140f // Bank Card Box
                    totalHeight += 25f
                }
                nota.paymentMethod.equals("E-Wallet", ignoreCase = true) -> {
                    totalHeight += 115f // E-Wallet Box
                    totalHeight += 25f
                }
                else -> {
                    // Tunai
                    totalHeight += 70f
                    totalHeight += 20f
                }
            }
        }

        totalHeight += 45f // Divider

        // Metadata grid (No. Nota, Tanggal, Pelanggan, Alamat, Metode, Status)
        totalHeight += 48f // No. Nota / Metode
        totalHeight += 46f // Tanggal / Status
        if (nota.customerName.isNotEmpty()) totalHeight += 48f // Pelanggan / HP
        if (nota.address.isNotEmpty()) totalHeight += 46f // Alamat
        totalHeight += 45f // Divider

        // Table Header
        totalHeight += 50f
        totalHeight += 25f // dashed divider

        // Items
        for (item in nota.items) {
            totalHeight += 48f // Item Name
            totalHeight += 42f // Qty x Price = Subtotal
            totalHeight += 16f // Spacing
        }
        totalHeight += 35f // Divider

        // Financial Totals
        totalHeight += 48f // Subtotal
        if (nota.discount > 0) totalHeight += 46f // Diskon
        if (nota.shippingCost > 0) totalHeight += 46f // Ongkir
        totalHeight += 95f // Grand Total Box
        totalHeight += 40f // Divider

        // Circular Stamp: ONLY when !isLunas (TAGIHAN condition)
        if (!isLunas) {
            totalHeight += 230f
        }

        if (nota.notes.isNotEmpty()) {
            totalHeight += 50f // Catatan
        }

        // Footer
        totalHeight += 65f // Thank you note
        totalHeight += 50f // Bottom margin

        val finalHeight = totalHeight.toInt().coerceAtLeast(1000)
        val bitmap = Bitmap.createBitmap(width, finalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        val bgPaint = Paint().apply {
            color = if (isThermal) Color.WHITE else Color.parseColor("#0F172A")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), finalHeight.toFloat(), bgPaint)

        // Typography paints
        val textColorPrimary = if (isThermal) Color.parseColor("#0F172A") else Color.WHITE
        val textColorSecondary = if (isThermal) Color.parseColor("#475569") else Color.parseColor("#94A3B8")
        val dividerColor = if (isThermal) Color.parseColor("#CBD5E1") else Color.parseColor("#334155")

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isThermal) Color.BLACK else Color.WHITE
            textSize = 42f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColorSecondary
            textSize = 25f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }

        val regularTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColorPrimary
            textSize = 27f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val boldTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColorPrimary
            textSize = 27f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val rightAlignBoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColorPrimary
            textSize = 27f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }

        val rightAlignRegularPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColorSecondary
            textSize = 25f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.RIGHT
        }

        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = dividerColor
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }

        val dashedDividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = dividerColor
            strokeWidth = 2f
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(12f, 8f), 0f)
        }

        var y = 60f

        // 1. TAMPILAN ATAS (Header & QR Info)
        if (isLunas && nota.paymentMethod.equals("QRIS", ignoreCase = true)) {
            // Kondisi LUNAS: Physical QR Code block is HIDDEN.
            // Replaced by professional & compact text box with GPN logo on far right.
            val qrisBoxH = 68f
            val qrisBoxRect = RectF(padding, y, width - padding, y + qrisBoxH)

            val qrisBoxBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (isThermal) Color.parseColor("#F8FAFC") else Color.parseColor("#1E293B")
                style = Paint.Style.FILL
            }
            val qrisBoxBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (isThermal) Color.parseColor("#E2E8F0") else Color.parseColor("#334155")
                strokeWidth = 2f
                style = Paint.Style.STROKE
            }
            canvas.drawRoundRect(qrisBoxRect, 14f, 14f, qrisBoxBgPaint)
            canvas.drawRoundRect(qrisBoxRect, 14f, 14f, qrisBoxBorderPaint)

            // GPN Badge on far right
            val gpnBadgeW = 86f
            val gpnBadgeH = 40f
            val gpnBadgeRight = width - padding - 14f
            val gpnBadgeLeft = gpnBadgeRight - gpnBadgeW
            val gpnBadgeTop = y + (qrisBoxH - gpnBadgeH) / 2f
            val gpnBadgeBottom = gpnBadgeTop + gpnBadgeH
            val gpnRect = RectF(gpnBadgeLeft, gpnBadgeTop, gpnBadgeRight, gpnBadgeBottom)

            val gpnBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#DC2626") // Indonesian national red
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(gpnRect, 8f, 8f, gpnBgPaint)

            val gpnTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.08f
            }
            canvas.drawText("GPN", gpnRect.centerX(), gpnRect.centerY() + 7f, gpnTextPaint)

            // Left Text: "Info Pembayaran QRIS: ECO TOBACCO / NMID: ID1024308636892 / A01"
            val qrisInfoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (isThermal) Color.parseColor("#334155") else Color.parseColor("#CBD5E1")
                textSize = 21f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val qrisText = "Info Pembayaran QRIS: ${storeName.uppercase()} / NMID: ID1024308636892 / A01"
            canvas.drawText(qrisText, padding + 16f, y + 42f, qrisInfoPaint)

            y += qrisBoxH + 28f

            // Below this: Store Name, Address, Notes
            canvas.drawText(storeName.uppercase(), width / 2f, y, titlePaint)
            y += 44f
            canvas.drawText(storeAddress, width / 2f, y, subtitlePaint)
            if (storeNotes.isNotEmpty()) {
                y += 38f
                canvas.drawText(storeNotes, width / 2f, y, subtitlePaint)
            }
            y += 18f
        } else {
            // Store Name & Address at top
            canvas.drawText(storeName.uppercase(), width / 2f, y, titlePaint)
            y += 44f
            canvas.drawText(storeAddress, width / 2f, y, subtitlePaint)
            if (storeNotes.isNotEmpty()) {
                y += 38f
                canvas.drawText(storeNotes, width / 2f, y, subtitlePaint)
            }
            y += 20f

            // Payment Section
            val paymentBoxPaint = Paint().apply {
                color = if (isThermal) Color.parseColor("#F8FAFC") else Color.parseColor("#1E293B")
                style = Paint.Style.FILL
            }
            val paymentBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (isThermal) Color.parseColor("#E2E8F0") else Color.parseColor("#334155")
                strokeWidth = 2f
                style = Paint.Style.STROKE
            }

            when {
                nota.paymentMethod.equals("QRIS", ignoreCase = true) -> {
                    if (originalQris != null) {
                        val framePadding = 20f
                        val qrisLeft = (width - qrisRenderWidth) / 2f
                        val qrisTop = y + 10f
                        val qrisRight = qrisLeft + qrisRenderWidth
                        val qrisBottom = qrisTop + qrisRenderHeight

                        val frameRect = RectF(qrisLeft - framePadding, qrisTop - framePadding, qrisRight + framePadding, qrisBottom + framePadding)
                        val qrisBgPaint = Paint().apply {
                            color = Color.WHITE
                            style = Paint.Style.FILL
                        }
                        val qrisBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = if (isThermal) Color.parseColor("#0F172A") else Color.parseColor("#3B82F6")
                            strokeWidth = 3f
                            style = Paint.Style.STROKE
                        }
                        canvas.drawRoundRect(frameRect, 20f, 20f, qrisBgPaint)
                        canvas.drawRoundRect(frameRect, 20f, 20f, qrisBorder)

                        val destRect = RectF(qrisLeft, qrisTop, qrisRight, qrisBottom)
                        val qrisPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                            isDither = true
                        }
                        canvas.drawBitmap(originalQris!!, null, destRect, qrisPaint)
                        y = qrisBottom + framePadding + 36f

                        val qrisCaptionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = if (isThermal) Color.parseColor("#111827") else Color.parseColor("#A5B4FC")
                            textSize = 24f
                            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            textAlign = Paint.Align.CENTER
                            letterSpacing = 0.05f
                        }
                        canvas.drawText("SCAN QRIS DI ATAS UNTUK PEMBAYARAN", width / 2f, y, qrisCaptionPaint)
                        y += 25f
                    } else {
                        val boxH = 75f
                        val boxRect = RectF(padding, y, width - padding, y + boxH)
                        canvas.drawRoundRect(boxRect, 14f, 14f, paymentBoxPaint)
                        canvas.drawRoundRect(boxRect, 14f, 14f, paymentBorderPaint)

                        val qrisNotePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = textColorSecondary
                            textSize = 25f
                            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            textAlign = Paint.Align.CENTER
                        }
                        canvas.drawText("PEMBAYARAN QRIS DIGITAL", width / 2f, y + 46f, qrisNotePaint)
                        y += boxH + 20f
                    }
                }
                nota.paymentMethod.equals("Transfer", ignoreCase = true) -> {
                    val bankTitle = businessProfile.bankName.ifEmpty { "BANK TRANSFER" }
                    val bankAcc = businessProfile.bankAccount.ifEmpty { "(Nomor rekening belum diatur di Profil)" }
                    val bankHolder = if (businessProfile.bankHolder.isNotEmpty()) "a/n ${businessProfile.bankHolder}" else ""

                    val boxH = 130f
                    val boxRect = RectF(padding, y, width - padding, y + boxH)
                    canvas.drawRoundRect(boxRect, 16f, 16f, paymentBoxPaint)
                    canvas.drawRoundRect(boxRect, 16f, 16f, paymentBorderPaint)

                    val bTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = if (isThermal) Color.parseColor("#1E3A8A") else Color.parseColor("#60A5FA")
                        textSize = 25f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        textAlign = Paint.Align.CENTER
                    }
                    val bAccPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = textColorPrimary
                        textSize = 30f
                        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                        textAlign = Paint.Align.CENTER
                        letterSpacing = 0.05f
                    }
                    canvas.drawText("TRANSFER BANK: $bankTitle", width / 2f, y + 38f, bTitlePaint)
                    canvas.drawText(bankAcc, width / 2f, y + 78f, bAccPaint)
                    if (bankHolder.isNotEmpty()) {
                        canvas.drawText(bankHolder, width / 2f, y + 112f, subtitlePaint)
                    }
                    y += boxH + 25f
                }
                nota.paymentMethod.equals("E-Wallet", ignoreCase = true) -> {
                    val eWalletStr = businessProfile.eWalletInfo.ifEmpty { "(Info E-Wallet belum diatur di Profil Toko)" }
                    val boxH = 105f
                    val boxRect = RectF(padding, y, width - padding, y + boxH)
                    canvas.drawRoundRect(boxRect, 16f, 16f, paymentBoxPaint)
                    canvas.drawRoundRect(boxRect, 16f, 16f, paymentBorderPaint)

                    val ewTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = if (isThermal) Color.parseColor("#059669") else Color.parseColor("#34D399")
                        textSize = 25f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        textAlign = Paint.Align.CENTER
                    }
                    val ewContentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = textColorPrimary
                        textSize = 27f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        textAlign = Paint.Align.CENTER
                    }
                    canvas.drawText("PEMBAYARAN E-WALLET", width / 2f, y + 38f, ewTitlePaint)
                    canvas.drawText(eWalletStr, width / 2f, y + 78f, ewContentPaint)
                    y += boxH + 25f
                }
                else -> {
                    // Tunai
                    val boxH = 65f
                    val boxRect = RectF(padding, y, width - padding, y + boxH)
                    canvas.drawRoundRect(boxRect, 12f, 12f, paymentBoxPaint)
                    canvas.drawRoundRect(boxRect, 12f, 12f, paymentBorderPaint)

                    val cashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = textColorSecondary
                        textSize = 24f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        textAlign = Paint.Align.CENTER
                    }
                    canvas.drawText("METODE PEMBAYARAN : TUNAI (CASH)", width / 2f, y + 42f, cashPaint)
                    y += boxH + 20f
                }
            }
        }

        y += 8f
        canvas.drawLine(padding, y, width - padding, y, dividerPaint)
        y += 40f

        // 2. METADATA TRANSAKSI (Precise 2-Column Grid)
        val displayMetode = when (nota.paymentMethod.lowercase()) {
            "transfer" -> "TRANSFER BANK"
            "qris" -> "QRIS DIGITAL"
            "e-wallet" -> "E-WALLET"
            else -> "TUNAI (CASH)"
        }
        val statusDisplay = if (isLunas) "LUNAS" else "TAGIHAN"
        val statusColor = if (isLunas) Color.parseColor("#059669") else Color.parseColor("#DC2626")

        // Row 1: No. Nota (left) / Metode (right)
        val refPrefix = if (nota.referenceCode.startsWith("INV", ignoreCase = true) || nota.referenceCode.startsWith("#")) "" else "#"
        canvas.drawText("NO. NOTA: $refPrefix${nota.referenceCode}", padding, y, boldTextPaint)
        canvas.drawText("METODE : $displayMetode", width - padding, y, rightAlignBoldPaint)
        y += 44f

        // Row 2: Tanggal (left) / Status (right)
        canvas.drawText("TANGGAL : ${nota.date}", padding, y, regularTextPaint)
        val statusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = statusColor
            textSize = 27f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("STATUS : $statusDisplay", width - padding, y, statusPaint)
        y += 44f

        // Row 3: Pelanggan (left) / HP (right)
        if (nota.customerName.isNotEmpty()) {
            canvas.drawText("PELANGGAN : ${nota.customerName}", padding, y, boldTextPaint)
            if (nota.phoneNumber.isNotEmpty()) {
                canvas.drawText(nota.phoneNumber, width - padding, y, rightAlignRegularPaint)
            }
            y += 44f
        }

        // Row 4: Alamat (left)
        if (nota.address.isNotEmpty()) {
            canvas.drawText("Alamat   : ${nota.address}", padding, y, regularTextPaint)
            y += 44f
        }

        y += 8f
        canvas.drawLine(padding, y, width - padding, y, dividerPaint)
        y += 36f

        // 3. RINCIAN BARANG (Itemization)
        canvas.drawText("ITEM", padding, y, boldTextPaint)
        canvas.drawText("TOTAL", width - padding, y, rightAlignBoldPaint)
        y += 20f
        canvas.drawLine(padding, y, width - padding, y, dashedDividerPaint)
        y += 42f

        for (item in nota.items) {
            val qty = item.quantity.toLongOrNull() ?: 1L
            val price = item.price.toLongOrNull() ?: 0L
            val itemTotal = qty * price
            val unitStr = if (item.unit.isNotEmpty()) " ${item.unit}" else ""

            // Line 1: Item Name
            canvas.drawText(item.name, padding, y, boldTextPaint)
            y += 36f

            // Line 2: Unit calculation neatly aligned on left, total on right
            canvas.drawText("$qty$unitStr x Rp ${formatCurrency(price)}", padding + 8f, y, regularTextPaint)
            canvas.drawText("Rp ${formatCurrency(itemTotal)}", width - padding, y, rightAlignBoldPaint)
            y += 44f
        }

        y -= 8f
        canvas.drawLine(padding, y, width - padding, y, dividerPaint)
        y += 40f

        // 4. SUMMARY & STATUS (With LUNAS OVERLAY WATERMARK)
        val summaryStartY = y - 26f

        canvas.drawText("Subtotal", padding, y, regularTextPaint)
        canvas.drawText("Rp ${formatCurrency(nota.subtotal)}", width - padding, y, rightAlignRegularPaint)
        y += 44f

        if (nota.discount > 0) {
            canvas.drawText("Diskon", padding, y, regularTextPaint)
            canvas.drawText("- Rp ${formatCurrency(nota.discount)}", width - padding, y, rightAlignRegularPaint)
            y += 44f
        }

        if (nota.shippingCost > 0) {
            canvas.drawText("Biaya Pengiriman / Ongkir", padding, y, regularTextPaint)
            canvas.drawText("+ Rp ${formatCurrency(nota.shippingCost)}", width - padding, y, rightAlignRegularPaint)
            y += 44f
        }

        y += 10f
        val totalBoxPaint = Paint().apply {
            color = if (isThermal) Color.parseColor("#EFF6FF") else Color.parseColor("#1E293B")
            style = Paint.Style.FILL
        }
        val totalBoxBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isThermal) Color.parseColor("#DBEAFE") else Color.parseColor("#3B82F6")
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        val totalRect = RectF(padding, y - 32f, width - padding, y + 54f)
        canvas.drawRoundRect(totalRect, 16f, 16f, totalBoxPaint)
        canvas.drawRoundRect(totalRect, 16f, 16f, totalBoxBorder)

        val grandTotalLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColorPrimary
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val grandTotalValPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isThermal) Color.parseColor("#1D4ED8") else Color.parseColor("#60A5FA") // Bold blue
            textSize = 38f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("TOTAL BAYAR", padding + 22f, y + 22f, grandTotalLabelPaint)
        canvas.drawText(nota.total, width - padding - 22f, y + 24f, grandTotalValPaint)

        val summaryEndY = totalRect.bottom + 6f

        // OVERLAY WATERMARK: Applied directly over the entire "Total Bayar" area, from Subtotal down to Total Bayar
        if (isLunas) {
            val watermarkDate = nota.date.uppercase()
            val watermarkText = "LUNAS $watermarkDate"
            val watermarkRect = RectF(padding, summaryStartY, width - padding, summaryEndY)

            canvas.save()
            canvas.clipRect(watermarkRect)
            canvas.rotate(-18f, watermarkRect.centerX(), watermarkRect.centerY())

            val watermarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#64748B") // Subtle, translucent faint gray
                alpha = 34 // ~13% opacity: clearly visible but does not obscure numbers
                textSize = 32f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                letterSpacing = 0.08f
            }

            val textWidth = watermarkPaint.measureText(watermarkText)
            val stepX = textWidth + 80f
            val stepY = 62f

            val startX = watermarkRect.centerX() - 700f
            val endX = watermarkRect.centerX() + 700f
            val startY = watermarkRect.centerY() - 400f
            val endY = watermarkRect.centerY() + 400f

            var curY = startY
            var row = 0
            while (curY <= endY) {
                val offsetX = if (row % 2 == 0) 0f else stepX / 2f
                var curX = startX + offsetX
                while (curX <= endX) {
                    canvas.drawText(watermarkText, curX, curY, watermarkPaint)
                    curX += stepX
                }
                curY += stepY
                row++
            }
            canvas.restore()
        }

        y = summaryEndY + 20f

        // 5. STATUS STAMP: ONLY for TAGIHAN (when !isLunas). The old physical stempel sticker is REMOVED when LUNAS!
        if (!isLunas) {
            val stampColorVal = Color.parseColor("#DC2626")
            val statusText = "TAGIHAN"
            val stampDateText = nota.date.uppercase()

            val stampCenterX = width / 2f
            val stampCenterY = y + 80f
            val stampOuterRadius = 90f
            val stampInnerRadius = 80f

            canvas.save()
            canvas.rotate(-9f, stampCenterX, stampCenterY)

            val stampBgPaint = Paint().apply {
                color = stampColorVal
                alpha = if (isThermal) 14 else 25
                style = Paint.Style.FILL
            }
            canvas.drawCircle(stampCenterX, stampCenterY, stampOuterRadius, stampBgPaint)

            val stampOuterBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = stampColorVal
                strokeWidth = 5f
                style = Paint.Style.STROKE
            }
            canvas.drawCircle(stampCenterX, stampCenterY, stampOuterRadius, stampOuterBorderPaint)

            val stampInnerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = stampColorVal
                strokeWidth = 2f
                style = Paint.Style.STROKE
            }
            canvas.drawCircle(stampCenterX, stampCenterY, stampInnerRadius, stampInnerBorderPaint)

            val stampTopPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = stampColorVal
                textSize = 16f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.06f
            }
            canvas.drawText("★ $statusText ★", stampCenterX, stampCenterY - 35f, stampTopPaint)

            val stampLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = stampColorVal
                strokeWidth = 2f
                style = Paint.Style.STROKE
            }
            canvas.drawLine(stampCenterX - 55f, stampCenterY - 26f, stampCenterX + 55f, stampCenterY - 26f, stampLinePaint)

            val stampCenterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = stampColorVal
                textSize = 34f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.08f
            }
            canvas.drawText(statusText, stampCenterX, stampCenterY + 11f, stampCenterPaint)

            canvas.drawLine(stampCenterX - 55f, stampCenterY + 20f, stampCenterX + 55f, stampCenterY + 20f, stampLinePaint)

            val stampDatePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = stampColorVal
                textSize = 15f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(stampDateText, stampCenterX, stampCenterY + 44f, stampDatePaint)

            canvas.restore()
            y += 195f
        }

        if (nota.notes.isNotEmpty()) {
            val notesPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textColorSecondary
                textSize = 24f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            }
            canvas.drawText("Catatan: ${nota.notes}", padding, y, notesPaint)
            y += 42f
        }

        // 6. FOOTER
        y += 15f
        canvas.drawLine(padding, y, width - padding, y, dashedDividerPaint)
        y += 44f

        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColorSecondary
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("- Terima kasih atas kunjungan & kerjasamanya -", width / 2f, y, footerPaint)
        y += 32f
        val footerSubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColorSecondary
            textSize = 23f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Nota Kasir Pro Edition", width / 2f, y, footerSubPaint)

        return bitmap
    }
}
