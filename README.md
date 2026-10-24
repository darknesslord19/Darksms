# Darknes SMS

Android SMS uygulaması (`com.darknes.sms`). Jetpack Compose + Kotlin + Room ile yazılmıştır.

## Özellikler

- **SMS gönder / al / ilet** — cihazın varsayılan SMS uygulaması olarak çalışır.
- **Multimedya (MMS) gönderme** — sohbette "+" ile resim seç, istersen metin ekle, gönder.
  Platformun MMS API'si (`sendMultimediaMessage`) ile yollanır; resim sohbette görünür.
- **Gizli kasa (PIN korumalı)** — WhatsApp'ın kilitli sohbetleri gibi. Seçtiğin konuşmalar
  ana listede ve bildirimlerde **hiç görünmez**; yalnızca PIN ile açılan bölümde durur.
  - Kasa, arayüzde hiçbir yerde belirgin değildir.
  - **PIN belirleme (gizli):** Ayarlar → "Sürüm 1.0" satırına arka arkaya **7 kez** dokun.
  - **Kasayı açma:** ana ekrandaki **arama alanına PIN'i yaz ve arama tuşuna bas**.
  - PIN, cihaz anahtarıyla şifrelenmiş olarak (SHA-256 özeti) saklanır; düz metin tutulmaz.
- **Mesaj düzenleme** — yalnızca **kendi gönderdiğin** mesajlar düzenlenebilir ve
  "düzenlendi" etiketi görünür (iMessage/WhatsApp mantığı). Gelen mesajlar değiştirilemez.
- **Sil / konuşmayı sil / kasaya taşı** — sohbet menüsünden.

## GitHub'da otomatik derleme

`main` dalına her push'ta **GitHub Actions** debug APK üretir:

1. Depo sayfasında **Actions** sekmesine gir.
2. En son "Build APK" çalışmasını aç.
3. APK'yi iki yerden indirebilirsin:
   - Çalışmanın altındaki **Artifacts → DarknesSMS-debug-apk**, veya
   - Depodaki **Releases → latest** sürümüne eklenen APK.

Elle tetiklemek için: Actions → "Build APK" → **Run workflow**.

## Android Studio ile derleme

Projeyi Android Studio'da aç; Gradle senkronu bağımlılıkları indirir ve
`Run` ile cihaza/emülatöre kurar. (Gerçek SMS göndermek için uygulamayı
**varsayılan SMS uygulaması** yapman gerekir; uygulama ilk açılışta bunu ister.)

## Bilinenler / sınırlar

- **MMS gönderme** uygulandı (resim + metin). Gerçek teslim operatöre, APN'e ve veri
  bağlantısına bağlıdır; bazı operatörlerde ayar gerekebilir. **Gelen MMS'in otomatik
  indirilip görüntülenmesi** bu sürümde sınırlıdır (gelen/giden SMS ve giden MMS tam çalışır).
- Uygulama yerel bir veritabanı kullanır; cihaz SMS sağlayıcısıyla tam senkron
  (eski mesajların içe aktarımı) bu iskelette minimal tutulmuştur.

## Yapı

```
app/src/main/java/com/darknes/sms/
├─ DarknesApp.kt            Application + bildirim kanalı
├─ data/                    Room (Thread/Message), DAO, DB, şifreli PIN kasası
├─ sms/                     SmsReceiver, SmsSender, MmsReceiver, Notifier, headless servis
└─ ui/                      MainActivity, ViewModel, navigasyon, Compose ekranları
```
