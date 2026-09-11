# Система обробки звітів бойових підрозділів

## Опис
Додаток для автоматизованої обробки Excel-звітів бойових підрозділів з генерацією текстових та JSON звітів.

## Вимоги
- **Java**: 11 або вище
- **Maven**: 3.6+
- **Apache POI**: для роботи з Excel файлами

## Встановлення

### 1. Клонування репозиторію
```bash
git clone <repository-url>
cd bchs
```

### 2. Компіляція проєкту
```bash
mvn clean compile
```

### 3. Збірка jar-файлу з залежностями
```bash
mvn clean package assembly:single
```

Це створить файл `target/bchs-1.0-SNAPSHOT-jar-with-dependencies.jar`

## Конфігурація

### Структура проєкту
```
bchs/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/ombr24/mb1/
│   │   └── resources/
│   │       ├── application.properties
│   │       └── logback.xml
├── data/              # Excel файли для обробки
├── output/            # Згенеровані звіти
├── config/            # Файли конфігурації (позиції, командири)
└── logs/              # Файли логів (створюється автоматично)
```

### Файл application.properties
```properties
data.dir=data
output.dir=output
config.dir=config
```

### Файли конфігурації в папці config/
- **positions70.txt** - список позицій категорії 70
- **positions100.txt** - список позицій категорії 100
- **commanderPositions.txt** - мапінг позицій командирів (формат: ключ:значення)

## Запуск

### Через Maven
```bash
mvn exec:java -Dexec.mainClass="org.ombr24.mb1.Application"
```

### Через jar-файл
```bash
java -jar target/bchs-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### Інтерактивний режим
Після запуску програма запитає:
```
Введіть початкову дату (день місяця) (1-31):
15

Введіть кінцеву дату (день місяця) (1-31):
17
```

Програма обробить звіти з 15 по 17 число включно.

## Формат вхідних даних

### Excel файл з даними
- **Розташування**: `data/input.xlsx`
- **Листи**: кожен лист представляє окремий підрозділ
- **Формат**: 
  - Рядок 0: заголовки з датами
  - Рядок 1: інформація про командира
  - Наступні рядки: дані про особовий склад

### Excel файл з номерами звітів
- **Розташування**: `data/numbers.xlsx`
- **Формат**:
  - Колонка 0: номер звіту
  - Колонка 1: дата звіту
  - Колонка 2: назва підрозділу

### Лист "оперативне підпорядкування"
Містить інформацію про придані підрозділи:
- Звідки переданий
- Кому переданий

## Вихідні дані

Для кожного дня генеруються:

### 1. Текстовий звіт
- **Розташування**: `output/division-report-[день].txt`
- **Формат**: структурований текст по підрозділам з категоріями (70, 100, 30)

### 2. JSON звіт
- **Розташування**: `output/division-report-[день].json`
- **Формат**: JSON з детальною інформацією про всі підрозділи

## Логування

### Консоль
Виводиться інформація рівня INFO та вище:
```
11:18:20.123 [main] INFO  o.o.mb1.Application - === Запуск додатку обробки звітів ===
11:18:20.456 [main] INFO  o.o.mb1.Application - Конфігурацію завантажено успішно
11:18:21.789 [main] INFO  o.o.mb1.utils.XlsProcessor - Обробка даних за 15 число...
```

### Файли логів
- **Розташування**: `logs/application.log`
- **Рівень**: DEBUG та вище (детальна інформація)
- **Ротація**: щоденна, зберігаються останні 30 днів
- **Формат**: `yyyy-MM-dd HH:mm:ss.SSS [thread] LEVEL logger - message`

## Обробка помилок

### Валідація введення
```
Помилка: День має бути в діапазоні 1-31. Введено: 35
Помилка: Початкова дата (20) не може бути більшою за кінцеву (15)
Помилка: Невірний формат числа: abc
```

### Помилки файлів
```
Помилка: Файл не існує: data/input.xlsx
Помилка: Файл позиції 70 не доступний для читання: config/positions70.txt
```

### Помилки обробки
```
Помилка: Не знайдено лист: 1 мр
Помилка: Не вдалося прочитати дату для дня: 15
```

## Архітектура

### Пакети
```
org.ombr24.mb1/
├── Application.java              # Головний клас
├── config/                       # Конфігурація
│   ├── AppConfig.java
│   ├── AppConfigLoader.java
│   ├── ReferenceData.java
│   ├── ReferenceDataLoader.java
│   ├── Constants.java
│   └── Ambushes.java
├── domain/                       # Доменні об'єкти
│   ├── BattleUnit.java
│   ├── AddedBattleUnit.java
│   ├── ParsedRow.java
│   └── ParsedAddedUnitRow.java
├── excel/                        # Обробка Excel
│   ├── ExcelRowMapper.java
│   └── AddedUnitMapper.java
├── exception/                    # Винятки
│   ├── DataProcessingException.java
│   ├── FileOperationException.java
│   └── InvalidInputException.java
├── report/                       # Генерація звітів
│   ├── ReportWriter.java
│   ├── DivisionReportWriter.java
│   └── JsonReportWriter.java
└── utils/                        # Утиліти
    ├── XlsProcessor.java
    └── EngagementContext.java
```

### Ключові покращення (вересень 2026)
- ✅ Валідація всіх вхідних даних
- ✅ Повне логування всіх операцій
- ✅ Кастомні винятки для різних типів помилок
- ✅ Константи замість magic values
- ✅ Рефакторинг великих методів
- ✅ Перевірка існування та доступності файлів
- ✅ Правильне управління ресурсами
- ✅ Детальні повідомлення про помилки
- ✅ Підтримка Java 11

## Приклад використання

```bash
# Компіляція
mvn clean compile

# Збірка
mvn clean package assembly:single

# Запуск
java -jar target/bchs-1.0-SNAPSHOT-jar-with-dependencies.jar

# Введення
Введіть початкову дату (день місяця) (1-31):
15
Введіть кінцеву дату (день місяця) (1-31):
15

# Вивід
11:18:20.835 [main] INFO  o.o.mb1.Application - === Запуск додатку обробки звітів ===
11:18:20.835 [main] INFO  o.o.mb1.Application - Робоча директорія: D:/projects/bchs
11:18:21.123 [main] INFO  o.o.mb1.Application - Конфігурацію завантажено успішно
11:18:21.456 [main] INFO  o.o.mb1.config.ReferenceDataLoader - Довідкові дані завантажено: позицій 70=15, позицій 100=42, командирів=8
11:18:21.789 [main] INFO  o.o.mb1.Application - Обробка звітів з 15 по 15 число
11:18:22.012 [main] INFO  o.o.mb1.utils.XlsProcessor - Початок обробки даних за 15 число
11:18:23.456 [main] INFO  o.o.mb1.utils.XlsProcessor - Згенеровано текстовий звіт: output/division-report-15.txt
11:18:23.789 [main] INFO  o.o.mb1.utils.XlsProcessor - Згенеровано JSON звіти: output/division-report-15.json
11:18:24.012 [main] INFO  o.o.mb1.Application - === Обробка всіх звітів завершена успішно ===
```

## Troubleshooting

### Проблема: "Файл не знайдено"
**Рішення**: Перевірте, що всі необхідні файли на місці:
- `data/input.xlsx`
- `data/numbers.xlsx`
- `config/positions70.txt`
- `config/positions100.txt`
- `config/commanderPositions.txt`

### Проблема: "Помилка читання вводу"
**Рішення**: Введіть коректне ціле число від 1 до 31

### Проблема: OutOfMemoryError
**Рішення**: Збільшіть heap memory:
```bash
java -Xmx2G -jar target/bchs-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### Проблема: "Не знайдено лист: 1 мр"
**Рішення**: Перевірте структуру Excel файлу, має існувати лист "1 мр"

## Ліцензія
[Вкажіть вашу ліцензію]

## Контакти
[Вкажіть контактну інформацію]
