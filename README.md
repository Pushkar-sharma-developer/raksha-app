# R.A.K.S.H.A — Tumhara Personal Voice Assistant

Ye ek Android Studio project hai. Iske andar sara code hai jo:
- "Raksha" naam bolne par khud activate ho jaata hai (bina button dabaye)
- Website/browser voice se open karta hai
- Phone mein file naam se search karta hai
- Diye gaye link se file download karta hai
- Female voice mein wapas bolta hai

---

## STEP 1: Android Studio install karo
Agar nahi hai to [developer.android.com/studio](https://developer.android.com/studio) se download karo (free hai).

## STEP 2: Ye project kholo
1. Android Studio kholo → **Open** → is `JarvisApp` folder ko select karo
2. Gradle sync hone do (pehli baar internet chahiye, dependencies download hongi)

## STEP 3: Wake-word ("Raksha") setup — YE ZAROORI HAI
Android ka normal mic hamesha ek hi word ke liye "sunta" nahi reh sakta — iske liye ek chhota free tool use kar rahe hain jise **Picovoice** kehte hain.

1. [console.picovoice.ai](https://console.picovoice.ai) par free account banao
2. Login karne ke baad **"Porcupine"** section mein jao
3. **Create Wake Word** par click karo, likho: `Raksha`
4. Language: Hindi ya English (jaisa tum bolna chahte ho) select karo
5. Platform: **Android** select karo
6. "Train" par click karo — 1-2 minute lagega
7. `.ppn` file download hogi (jaise `Raksha_en_android.ppn`)
8. Us file ko rename karo `raksha.ppn` aur copy karo yahan:
   `app/src/main/assets/raksha.ppn`
   (jo placeholder `.txt` file wahan hai use delete kar dena)
9. Console mein **"Access Key"** bhi milegi (top pe dikhegi) — usko copy karo

## STEP 4: Access Key code mein daalo
`app/src/main/java/com/jarvis/assistant/WakeWordService.kt` file kholo, ye line dhundo:

```kotlin
private const val PICOVOICE_ACCESS_KEY = "YOUR_PICOVOICE_ACCESS_KEY"
```

Apni Access Key yahan paste kar do.

## STEP 5: Phone connect karo aur build karo
1. Phone mein **Developer Options → USB Debugging** ON karo
2. Phone ko USB se laptop se connect karo
3. Android Studio mein upar **Run (▶️) button** dabao
4. App phone mein install ho jaayegi

## STEP 6: Permissions allow karo (pehli baar app kholne par)
- Microphone → Allow
- Notifications → Allow
- File access ke liye ek settings page khulega — usme **"Allow access to manage all files"** ON karo

## STEP 7: Battery optimization band karo (warna Android Raksha ko so dega)
Phone Settings → Apps → Raksha → Battery → **"Unrestricted"** select karo

## STEP 8: Use karo
1. App kholo → **"Raksha Shuru Karein"** button dabao
2. Ab app background mein bhi rahe to bhi "Raksha" bolo
3. Wo "Jī bataiye" bolegi, phir apna command bolo:
   - "Raksha, open youtube.com"
   - "Raksha, search file resume"
   - "Raksha, download https://example.com/file.pdf"

---

## Limitations (honestly bata raha hoon)
- Voice se URL bolna kabhi-kabhi galat samjha ja sakta hai (jaise "dot com" sahi se na pakde) — abhi ke liye clear bolo
- Kuch Android phones (Xiaomi, Oppo, Vivo) apni taraf se background apps ko aggressively band kar dete hain — agar Raksha rukti hai to phone ki "Autostart" settings mein bhi Raksha ko allow karna padega
- Ye APK sirf tumhare phone ke liye hai — Play Store pe publish karne ke liye Google ki extra policies follow karni padengi (especially MANAGE_EXTERNAL_STORAGE permission ke liye)

## Aage badhana ho to
- Naye commands add karne ke liye `CommandProcessor.kt` mein naya `when` case daalo
- Reminder/alarm, WhatsApp message bhejna, aur commands add ho sakte hain — bolo to wo bhi bana dunga
