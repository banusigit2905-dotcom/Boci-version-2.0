# OKTSHOP17 Reseller Portal (Android Native App)

A native Android application built with **Kotlin**, **Jetpack Compose**, and **Room Database**, rewritten from the original OKTSHOP17 web portal.

## Features

### Reseller Module
- **Reseller Dashboard**: Displays stat cards for total items ordered, total spending, and current point balance.
- **Product Ordering**: Browse catalog by category, add items to cart, select customer details & payment method, and send order directly via WhatsApp.
- **Point Redemption**: Multi-step point exchange system with status tracking.
- **Leaderboard**: Top 10 active resellers ranked by spending and points.
- **Returns & Complaints**: Submit product returns or complaint reports and track resolution status.
- **Account Profile**: Manage name and WhatsApp contact details.

### Admin Module
- **Admin Dashboard**: Overview of orders, omset revenue, and issued points.
- **Account Activation**: Review pending reseller signups with auto-generated custom IDs and activate accounts.
- **Point Redemptions**: Manage incoming point redemption requests.
- **Catalog Management**: Add, edit, or remove products and manage prices/categories.
- **Global Rankings**: Full leaderboard of all registered resellers.
- **Returns & Complaints Management**: Process and resolve incoming returns and complaints.

## Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose & Material Design 3
- **Local Persistence**: Room Database (SQLite) + Coroutines Flow
- **Architecture**: MVVM with Repository Pattern
