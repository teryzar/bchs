# BCHS - Battalion Commander Help System

## 📦 Дистрибутив

Цей дистрибутив містить:
- `bchs-1.0-SNAPSHOT-jar-with-dependencies.jar` - виконуваний JAR файл з усіма залежностями
- `run.bat` - скрипт запуску для Windows (UTF-8)
- `run-utf8.bat` - альтернативний скрипт запуску для Windows
- `run-utf8.sh` - скрипт запуску для Linux/Mac
- `data/` - директорія для вхідних Excel файлів
- `logs/` - директорія для логів програми

## 🚀 Запуск

### Windows
Просто двічі клацніть на `run.bat` або `run-utf8.bat`

### Linux/Mac
```bash
chmod +x run-utf8.sh
./run-utf8.sh
```

### Вручну
```bash
java -jar bchs-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## 📋 Вимоги

- Java 11 або вище
- Windows 10/11, Linux або macOS

## 📁 Структура директорій

```
bchs-1.0-SNAPSHOT/
├── bchs-1.0-SNAPSHOT-jar-with-dependencies.jar
├── run.bat
├── run-utf8.bat
├── run-utf8.sh
├── data/          # Розмістіть тут ваші Excel файли
├── logs/          # Логи програми
└── README.md
```

## 🔧 Налаштування

Перед першим запуском створіть необхідні конфігураційні файли у директорії `config/`:
- `positions70.txt` - позиції типу БП 70%
- `positions100.txt` - позиції типу 100%
- `commanders.txt` - командири підрозділів

## 📝 Використання

1. Розмістіть Excel файли у директорії `data/`
2. Запустіть програму через відповідний скрипт
3. Результати з'являться у директорії `output/`

## 🐛 Проблеми

Якщо виникають проблеми з кодуванням тексту:
- На Windows використовуйте `run-utf8.bat`
- Переконайтеся що консоль підтримує UTF-8
- Перевірте логи у директорії `logs/`

## 📄 Ліцензія

Дивіться файл LICENSE для деталей.
