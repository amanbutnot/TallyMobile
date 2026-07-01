package org.prime.easykarobar.ui.printing

import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.ui.shared.globalShared.CompanyName
import org.prime.easykarobar.ui.shared.globalShared.Tdate
import org.prime.easykarobar.ui.shared.globalShared.getProductImage
import org.prime.easykarobar.ui.shared.reportsShared.CurrentDate
import org.tally.GetProductsForDis
import org.prime.easykarobar.BuildKonfig

fun productShareHtml(
    productName: String,
    price: Double,
    mrp: Double,
    discount: Double?,
    imageUrl: String,
    description: String?
): String {
    val cleanImageUrl = imageUrl.substringAfter("://").substringBefore("?")
    val compatibleImageUrl = "https://images.weserv.nl/?url=$cleanImageUrl&output=png&w=600"
    val primaryColor = BuildKonfig.SPLASH_TOP_COLOR
    val storeName = BuildKonfig.STORE_NAME.ifBlank { CompanyName() }

    return """
        <!DOCTYPE html>
        <html>
        <head>
        <meta charset="UTF-8">
        <style>
            @page { size: A4; margin: 0; }
            body { 
                font-family: 'Helvetica Neue', Arial, sans-serif; 
                margin: 0;
                padding: 0;
                background-color: #ffffff;
                color: #1a1c1e;
            }
            .header {
                background: linear-gradient(135deg, $primaryColor, ${BuildKonfig.SPLASH_BOTTOM_COLOR});
                padding: 40px 20px;
                text-align: center;
                color: white;
            }
            .brand-name {
                font-size: 24pt;
                font-weight: 900;
                margin: 0;
                letter-spacing: -0.5px;
                text-transform: uppercase;
            }
            .main-content {
                padding: 40px;
                max-width: 800px;
                margin: 0 auto;
            }
            .image-section {
                text-align: center;
                margin-bottom: 40px;
            }
            .product-img {
                width: 100%;
                max-width: 450px;
                height: auto;
                border-radius: 24px;
                box-shadow: 0 20px 40px rgba(0,0,0,0.1);
            }
            .details-section {
                text-align: left;
            }
            .name {
                font-size: 28pt;
                font-weight: 900;
                margin: 0 0 20px 0;
                color: #000;
                line-height: 1.1;
            }
            .price-container {
                background: #f8fafc;
                padding: 30px;
                border-radius: 24px;
                margin-bottom: 30px;
                border: 1px solid #f1f5f9;
                display: flex;
                align-items: center;
                justify-content: space-between;
            }
            .price-info {
                display: flex;
                flex-direction: column;
            }
            .label {
                font-size: 10pt;
                color: #64748b;
                text-transform: uppercase;
                font-weight: 700;
                margin-bottom: 5px;
            }
            .current-price {
                font-size: 36pt;
                font-weight: 900;
                color: $primaryColor;
            }
            .mrp-section {
                display: flex;
                align-items: center;
                gap: 15px;
                margin-top: 5px;
            }
            .old-price {
                font-size: 16pt;
                color: #94a3b8;
                text-decoration: line-through;
            }
            .discount-tag {
                background: #ef4444;
                color: white;
                padding: 6px 14px;
                border-radius: 12px;
                font-weight: 800;
                font-size: 11pt;
            }
            .description {
                font-size: 14pt;
                color: #334155;
                line-height: 1.6;
                padding: 30px;
                background: #fdfdfd;
                border-radius: 24px;
                border-left: 6px solid $primaryColor;
            }
            .footer {
                margin-top: 60px;
                padding: 40px 20px;
                text-align: center;
                border-top: 1px solid #f1f5f9;
                color: #94a3b8;
                font-size: 10pt;
            }
            a { text-decoration: none; }
        </style>
        </head>
        <body>
            <header class="header">
                <h1 class="brand-name">$storeName</h1>
            </header>
            
            <main class="main-content">
                <div class="image-section">
                    <a href="$imageUrl">
                        <img src="$compatibleImageUrl" class="product-img" />
                    </a>
                </div>
                
                <div class="details-section">
                    <h2 class="name">$productName</h2>
                    
                    <div class="price-container">
                        <div class="price-info">
                            <span class="label">Special Offer Price</span>
                            <div style="display: flex; align-items: baseline;">
                                <span class="current-price">₹${price.formatToAmtDec()}</span>
                            </div>
                            <div class="mrp-section">
                                ${if (mrp > 0 && mrp != price) """
                                    <span class="old-price">MRP ₹${mrp.formatToAmtDec()}</span>
                                """ else ""}
                                ${if (discount != null && discount > 0) """
                                    <span class="discount-tag">${discount.toInt()}% OFF</span>
                                """ else ""}
                            </div>
                        </div>
                    </div>
                    
                    ${if (!description.isNullOrBlank()) """
                        <div class="description">
                            $description
                        </div>
                    """ else ""}
                </div>
            </main>
            
            <footer class="footer">
                Exclusive Deal from $storeName • Professional Quote
            </footer>
        </body>
        </html>
    """.trimIndent()
}

fun productListHtml(
    categoryName: String,
    products: List<GetProductsForDis>,
    storeId: String
): String {
    val primaryColor = BuildKonfig.SPLASH_TOP_COLOR
    val storeName = BuildKonfig.STORE_NAME.ifBlank { CompanyName() }
    val html = StringBuilder()
    html.append("""
        <!DOCTYPE html>
        <html>
        <head>
        <meta charset="UTF-8">
        <style>
            @page { size: A4; margin: 15mm; }
            body { 
                font-family: 'Helvetica Neue', Arial, sans-serif; 
                margin: 0; 
                padding: 0;
                background-color: white;
                color: #1e293b;
            }
            .main-header {
                padding-bottom: 20px;
                border-bottom: 2px solid #f1f5f9;
                margin-bottom: 20px;
            }
            .company-title { 
                color: #0f172a; 
                font-size: 22pt; 
                font-weight: 900; 
                margin: 0;
                text-transform: uppercase;
                letter-spacing: -0.5px;
            }
            .category-tag { 
                display: inline-block;
                color: $primaryColor;
                font-size: 12pt;
                font-weight: 700;
                margin-top: 5px;
            }
            
            table {
                width: 100%;
                border-collapse: collapse;
            }
            th {
                text-align: left;
                padding: 12px;
                color: #64748b;
                font-size: 9pt;
                text-transform: uppercase;
                font-weight: 700;
                border-bottom: 1px solid #e2e8f0;
            }
            td {
                padding: 12px;
                border-bottom: 1px solid #f1f5f9;
                vertical-align: middle;
            }

            .img-container {
                width: 60px;
                height: 60px;
                border-radius: 12px;
                border: 1px solid #f1f5f9;
                display: flex;
                align-items: center;
                justify-content: center;
                background: #f8fafc;
            }
            .product-img {
                max-width: 50px;
                max-height: 50px;
                object-fit: contain;
                border-radius: 8px;
            }
            .p-name { font-size: 11pt; font-weight: 700; color: #0f172a; display: block; }
            .p-desc { font-size: 8.5pt; color: #64748b; margin-top: 2px; display: block; }
            
            .price-col { text-align: right; }
            .p-price { font-size: 12pt; font-weight: 800; color: $primaryColor; }
            .p-mrp { font-size: 8pt; color: #94a3b8; text-decoration: line-through; display: block; }
            .p-save { color: #ef4444; font-weight: 800; font-size: 8pt; }
            
            .footer {
                margin-top: 30px;
                text-align: center;
                font-size: 8pt;
                color: #94a3b8;
                padding-top: 20px;
            }
            a { text-decoration: none; }
        </style>
        </head>
        <body>
            <div class="main-header">
                <h1 class="company-title">$storeName</h1>
                <div class="category-tag">$categoryName Catalogue</div>
            </div>
            <table>
                <thead>
                    <tr>
                        <th style="width: 70px;">Item</th>
                        <th>Product Details</th>
                        <th style="text-align: right; width: 100px;">Price</th>
                    </tr>
                </thead>
                <tbody>
    """.trimIndent())

    products.forEach { product ->
        val imageUrl = getProductImage(storeId, product.product_id.toString())
        val cleanImageUrl = imageUrl.substringAfter("://").substringBefore("?")
        val compatibleImageUrl = "https://images.weserv.nl/?url=$cleanImageUrl&output=png&w=120"
        val per = product.MRP?.takeIf { it != 0.0 }?.let { mrp ->
            ((mrp - (product.sales_price ?: 0.0)) / mrp) * 100
        }

        html.append("""
            <tr>
                <td>
                    <a href="$imageUrl">
                        <div class="img-container">
                            <img src="$compatibleImageUrl" class="product-img" />
                        </div>
                    </a>
                </td>
                <td>
                    <span class="p-name">${product.product_name}</span>
                    <span class="p-desc">${product.product_description ?: ""}</span>
                </td>
                <td class="price-col">
                    <span class="p-price">₹${product.sales_price?.formatToAmtDec()}</span>
                    ${if (product.MRP != 0.0 && product.MRP != product.sales_price) """
                        <span class="p-mrp">₹${product.MRP?.formatToAmtDec()}</span>
                        <span class="p-save">${per?.toInt()}% OFF</span>
                    """ else ""}
                </td>
            </tr>
        """.trimIndent())
    }

    html.append("""
                </tbody>
            </table>
            <div class="footer">
                © $storeName • Professional Product Catalogue
            </div>
        </body>
        </html>
    """.trimIndent())

    return html.toString()
}
