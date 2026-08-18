# Auto Data Toggle

Ye chota sa Android app project hai: jab phone WiFi se connect ho, ye automatically
phone ki Settings khol kar Mobile Data ka switch off tap kar deta hai. WiFi disconnect
hone par kuch nahi hota — data hamesha manual on karna padega, jaisa aap chahte the.

## Kaise build karein (APK banayein)

1. **Android Studio** install karein (free): https://developer.android.com/studio
2. Android Studio kholein → "Open" → is poori `AutoDataToggle` folder ko select karein.
3. Studio khud gradle sync kar lega (internet chahiye hoga pehli dafa).
4. Upar menu se: **Build → Build Bundle(s)/APK(s) → Build APK(s)**
5. Build complete hone par "locate" link pe click karein — APK file mil jayegi
   (`app/build/outputs/apk/debug/app-debug.apk`).
6. Ye APK file apne phone mein bhej kar install kar lein (Settings mein "Install
   unknown apps" allow karna parega — normal cheez hai apni banayi hui app ke liye).

## Phone pe use kaise karein

1. App kholein.
2. **"Step 1: Enable Accessibility Permission"** button dabayein → Settings khulegi
   → list mein "Auto Data Toggle" dhoond kar ON kar dein.
3. Wapis app mein aayein, **"Step 2: Start Monitoring"** dabayein.
4. Bas — ab jab bhi WiFi connect hoga, app khud Settings khol kar Mobile Data
   band kar degi aur wapis home screen pe aa jayegi.

## Zaroori note

Har phone brand (Samsung, Xiaomi, Oppo, stock Android) Settings screen thora
alag dikhata hai. Agar switch automatically na dabay, to:
`DataToggleAccessibilityService.kt` file mein `TARGET_LABELS` list mein apne
phone ki Settings mein dikhne wala exact switch label (jaise "Mobile data")
add kar dein.

Ye app koi data collect ya kahin bhejti nahi — sirf apni Settings screen pe
click simulate karti hai. Accessibility permission isi ek kaam ke liye
istemal hoti hai.
