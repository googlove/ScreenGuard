# ScreenGuard - Android Security Application

Сучасний додаток для Android, який захищає вашу приватність від небажаних спостерігачів. Коли хтось дивиться на ваш телефон без дозволу, селфі-камера активується і видає попередження!

## 🎯 Основні функції

### 1. **Детекція осіб у реальному часі**
   - Використовує ML Kit Face Detection від Google
   - Аналізує зображення з селфі-камери постійно
   - Миттєво виявляє наявність осіб на екрані

### 2. **Система сповіщень**
   - Яскраве виконавче сповіщення
   - Вібрація та звукові сигнали
   - Повноекранне попередження про спробу доступу
   - Автоматична фіксація часу і дати інциденту

### 3. **Білий список контактів**
   - Додавайте дозволені контакти
   - Автоматично пропускаються сповіщення для них
   - Легко керуйте списком дозволених осіб

### 4. **Історія спроб**
   - Детальна історія всіх спроб доступу
   - Час та дата кожної спроби
   - Легко видалити або переглянути записи

### 5. **Налаштування**
   - Вмикання/вимикання сповіщень
   - Вібрація та звук
   - Дані про додаток

## 📋 Вимоги

### Мінімальні вимоги:
- Android SDK 21+ (Android 5.0 Lollipop)
- Камера (передня селфі-камера)
- Інтернет (для спочатку завантаження ML Kit моделей)

### Залежності:
- AndroidX
- Google ML Kit Face Detection
- CameraX
- Room Database
- DataStore
- Material Design Components
- Kotlin Coroutines

## 🚀 Встановлення та Настройка

### Крок 1: Клонування проекту
```bash
git clone https://github.com/yourusername/ScreenGuard.git
cd ScreenGuard
```

### Крок 2: Відкриття в Android Studio
1. Відкрийте Android Studio
2. File → Open → Виберіть папку ScreenGuard
3. Дайте часу на синхронізацію Gradle

### Крок 3: Установка залежностей
Gradle автоматично завантажить всі залежності під час синхронізації.

### Крок 4: Запуск на пристрої
1. Підключіть Android пристрій через USB або використовуйте emulator
2. Click Run або натисніть Shift + F10
3. Виберіть пристрій для встановлення

## 📁 Структура проекту

```
ScreenGuard/
├── src/main/
│   ├── java/com/screenguard/protector/
│   │   ├── MainActivity.kt              # Головний екран
│   │   ├── service/
│   │   │   └── ScreenGuardService.kt    # Основний сервіс детекції
│   │   ├── ui/
│   │   │   ├── AlertActivity.kt         # Екран попередження
│   │   │   ├── SettingsActivity.kt      # Налаштування
│   │   │   ├── WhitelistActivity.kt     # Білий список
│   │   │   └── HistoryActivity.kt       # Історія
│   │   ├── data/
│   │   │   ├── Models.kt                # Data classes
│   │   │   ├── Database.kt              # Room Database
│   │   │   └── Repositories.kt          # Data access layer
│   │   ├── adapter/
│   │   │   └── Adapters.kt              # RecyclerView adapters
│   │   ├── utils/
│   │   │   └── PreferencesManager.kt    # Settings management
│   │   └── receiver/
│   │       └── BootReceiver.kt          # Auto-start on reboot
│   ├── res/
│   │   ├── layout/                      # XML layouts
│   │   ├── drawable/                    # Icons and backgrounds
│   │   ├── values/
│   │   │   ├── colors.xml              # Color definitions
│   │   │   ├── strings.xml             # String resources
│   │   │   └── themes.xml              # Theme definitions
│   └── AndroidManifest.xml
├── build.gradle                        # Project configuration
└── proguard-rules.pro                  # Obfuscation rules
```

## 🔐 Дозволи

Додаток вимагає наступних дозволів:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```

## 💡 Як використовувати

### 1. Запуск захисту
- Відкрийте додаток
- Натисніть на перемикач "Активувати захист"
- Надайте дозвіл на доступ до камери
- Додаток почне моніторити передню камеру

### 2. Додавання контактів у білий список
- Перейдіть до "Дозволені контакти"
- Натисніть "Додати контакт"
- Введіть ім'я контакту
- Натисніть "Додати"

### 3. Перегляд історії
- Перейдіть до "Історія спроб"
- Дивіться всі спроби доступу
- Натисніть "Видалити історію" для очищення

### 4. Налаштування
- Вмикайте/вимикайте сповіщення
- Керуйте вібрацією та звуком
- Ознайомтесь з інформацією про додаток

## 🛠️ Розробка

### Компіляція для Release
```bash
./gradlew assembleRelease
```

### Запуск тестів
```bash
./gradlew test
```

### Код-стиль
Проект слідує Kotlin Coding Conventions.

## 🐛 Вирішення проблем

### Проблема: Камера не активується
- Переконайтесь, що дозвіл на камеру надано
- Перезапустіть додаток
- Перезавантажте пристрій

### Проблема: Сповіщення не приходять
- Перевірте налаштування сповіщень в системі
- Переконайтесь, що додаток має дозвіл на сповіщення
- Вімкніть сповіщення у налаштуваннях додатку

### Проблема: Повільна детекція
- Це нормально при першому запуску (завантаження ML Kit модели)
- Детекція прискорюється після першого використання
- Збільште освітлення в кімнаті для точнішої детекції

## 📊 Архітектура

### Design Pattern: MVVM (Model-View-ViewModel)
- **Model**: Room Database, DataStore
- **View**: Activities, Fragments, Layouts
- **ViewModel**: Управління даними та логікою

### Основні компоненти:
- **Service**: Фоновий сервіс для детекції осіб
- **Repository**: Абстракція доступу до даних
- **Database**: Room для локального зберігання
- **UI**: Material Design 3 интерфейс

## 🔄 Життєвий цикл

1. **Запуск**: Service стартує і активує CameraX
2. **Анаіз**: ML Kit постійно аналізує кадри
3. **Детекція**: Якщо виявлена особа - спрацьовує алерт
4. **Запис**: Інцидент зберігається в базі даних
5. **Зупинка**: Service завершується при деактивації

## 📱 Сумісність

- ✅ Android 5.0+ (API 21+)
- ✅ Android 6.0+ (з Runtime Permissions)
- ✅ Android 12+ (з Foreground Service)
- ✅ Material Design 3
- ✅ Dark Mode

## 🔐 Приватність та Безпека

- Всі дані зберігаються локально на пристрої
- Камера використовується тільки для детекції осіб
- Зображення не записуються або передаються
- Не потрібна реєстрація або облікові записи

## 📄 Ліцензія

MIT License - дивіться LICENSE файл

## 👨‍💻 Розробниці

- **ScreenGuard Team** - Основна розробка
- **Google ML Kit** - Технологія детекції
- **AndroidX** - Бібліотеки

## 💬 Контакти та Зворотній Зв'язок

- 🐛 Обіжки баги: Створіть Issue
- 💡 Пропозиції: Обговоріть у Discussions
- 📧 Контакти: screenguard@example.com

## 🚀 Планові функції

- [ ] Розпізнавання облич
- [ ] Захист від скрінів
- [ ] Звіти та статистика
- [ ] Cloud синхронізація
- [ ] Widget для швидкого доступу
- [ ] Інтеграція з мессенджерами

## ⭐ Подякуйте проекту

Якщо вам подобається ScreenGuard, не забудьте поставити ⭐ на GitHub!

---

**Версія**: 1.0.0  
**Останнє оновлення**: 2024  
**Статус**: 🟢 Активна розробка
