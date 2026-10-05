# 🔍 Звіт про дублювання коду в проекті

## 📊 Виявлені проблеми

### 🔴 Критичне дублювання (HIGH PRIORITY)

#### 1. **Excel Cell Readers - 100% дублікат**
**Файли:** `ExcelRowMapper.java` та `AddedUnitMapper.java`

```java
// ДУБЛЮЄТЬСЯ В ОБОХ КЛАСАХ:
private String read(Row r, int i) {
    Cell c = r.getCell(i);
    return c == null ? "" : c.getStringCellValue().trim();
}

private String format(Row r, int i) {
    Cell c = r.getCell(i);
    return c == null ? "" : formatter.formatCellValue(c).trim();
}

private final DataFormatter formatter = new DataFormatter();
```

**Вплив:** Порушення DRY принципу, подвійна підтримка

---

#### 2. **Створення тестових даних - масивне дублювання**
**Файли:** Всі інтеграційні тести (6 класів)

**Дублюються методи:**
- `createMainExcelFile()` - 3 копії з незначними відмінностями
- `createNumbersExcelFile()` - 3 копії з незначними відмінностями  
- `createReferenceFiles()` - 5 копій з незначними відмінностями
- `createAddedUnitsSheet()` - 2 копії
- `addTestUnits()` / `addDetailedTestUnits()` - 3 копії

**Кількість дублікованих рядків:** ~800+ рядків коду

---

#### 3. **getCellValueAsString в XlsProcessor**
**Файл:** `XlsProcessor.java`

```java
private String getCellValueAsString(Row row, int columnIndex) {
    Cell cell = row.getCell(columnIndex);
    if (cell == null) {
        return "";
    }
    try {
        return cell.getStringCellValue().trim();
    } catch (IllegalStateException e) {
        try {
            return String.valueOf(cell.getNumericCellValue());
        } catch (IllegalStateException ex) {
            logger.warn("Не вдалося прочитати значення комірки [{}:{}]", row.getRowNum(), columnIndex);
            return "";
        }
    }
}
```

**Проблема:** Схожа логіка з ExcelRowMapper, але більш складна

---

### 🟡 Середнє дублювання (MEDIUM PRIORITY)

#### 4. **Форматування списків підрозділів**
**Файли:** `JsonReportWriter.java` та `DivisionReportWriter.java`

Обидва класи:
- Розділяють підрозділи на original/added
- Фільтрують по позиціях (70, 100, 30)
- Використовують `UnitFormatter`

**Дублюється логіка:**
```java
// У JsonReportWriter
List<BattleUnit> originalUnits = units.stream()
    .filter(unit -> !unit.isAdded())
    .collect(Collectors.toList());

List<BattleUnit> addedUnits = units.stream()
    .filter(BattleUnit::isAdded)
    .collect(Collectors.toList());

// Те саме в DivisionReportWriter через UnitGroups
```

---

#### 5. **Ітерація по підрозділам**
**Файли:** `JsonReportWriter.java` та `DivisionReportWriter.java`

```java
for (var entry : context.getInfantryByDivision().entrySet()) {
    String division = entry.getKey();
    List<BattleUnit> units = entry.getValue();
    // ... обробка
}
```

---

### 🟢 Низьке дублювання (LOW PRIORITY)

#### 6. **Логування в Report Writers**
Схожі патерни логування у обох writer'ах

#### 7. **Exception handling**
Повторювані try-catch блоки з перетворенням IOException

---

## 🎯 План рефакторингу

### Phase 1: Excel Utils (HIGH PRIORITY)

#### Створити `ExcelCellReader` utility клас:
```java
public class ExcelCellReader {
    private final DataFormatter formatter = new DataFormatter();
    
    public String readString(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        return cell == null ? "" : cell.getStringCellValue().trim();
    }
    
    public String formatCell(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        return cell == null ? "" : formatter.formatCellValue(cell).trim();
    }
    
    public String getCellValueAsString(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return "";
        
        try {
            return cell.getStringCellValue().trim();
        } catch (IllegalStateException e) {
            try {
                return String.valueOf(cell.getNumericCellValue());
            } catch (IllegalStateException ex) {
                return "";
            }
        }
    }
}
```

**Рефакторинг:**
- ✅ `ExcelRowMapper` → використовує `ExcelCellReader`
- ✅ `AddedUnitMapper` → використовує `ExcelCellReader`
- ✅ `XlsProcessor` → використовує `ExcelCellReader`

**Вигода:** -50 рядків коду, єдина точка змін

---

### Phase 2: Test Data Builders (HIGH PRIORITY)

#### Створити `TestDataFactory` клас:
```java
public class TestDataFactory {
    
    public static void createCompleteProjectStructure(Path dataDir, Path configDir) {
        createMainExcelFile(dataDir);
        createNumbersExcelFile(dataDir);
        createReferenceFiles(configDir);
    }
    
    public static void createMainExcelFile(Path dataDir) { ... }
    public static void createNumbersExcelFile(Path dataDir) { ... }
    public static void createReferenceFiles(Path configDir) { ... }
    
    // Builder pattern для гнучкості
    public static ExcelFileBuilder excelFile() {
        return new ExcelFileBuilder();
    }
    
    public static class ExcelFileBuilder {
        private int startDay = 1;
        private int endDay = 31;
        private String[] divisions = {"1 мр", "2 мр", "3 мр"};
        
        public ExcelFileBuilder withDays(int start, int end) { ... }
        public ExcelFileBuilder withDivisions(String... divisions) { ... }
        public void createIn(Path directory) { ... }
    }
}
```

**Рефакторинг всіх інтеграційних тестів:**
- ✅ `EndToEndIntegrationTest`
- ✅ `ReportGenerationIntegrationTest`
- ✅ `XlsProcessorIntegrationTest`
- ✅ `ReportWriterIntegrationTest`
- ✅ `ConfigurationIntegrationTest`
- ✅ `XlsProcessorErrorHandlingIntegrationTest`

**Вигода:** -700+ рядків коду, гнучкість через builder

---

### Phase 3: Unit Grouping Logic (MEDIUM PRIORITY)

#### Створити `UnitGroupingService`:
```java
public class UnitGroupingService {
    
    public static UnitGroups groupByAddedStatus(List<BattleUnit> units) {
        List<BattleUnit> original = units.stream()
            .filter(unit -> !unit.isAdded())
            .collect(Collectors.toList());
            
        List<BattleUnit> added = units.stream()
            .filter(BattleUnit::isAdded)
            .collect(Collectors.toList());
            
        return new UnitGroups(original, added);
    }
    
    public static List<BattleUnit> filterByPosition(
            List<BattleUnit> units, 
            Predicate<BattleUnit> filter) {
        return units.stream()
            .filter(filter)
            .collect(Collectors.toList());
    }
}
```

**Рефакторинг:**
- ✅ `JsonReportWriter` → використовує `UnitGroupingService`
- ✅ `DivisionReportWriter` → вже використовує `UnitGroups`, додати service

**Вигода:** -30 рядків коду, єдина логіка групування

---

### Phase 4: Report Generation Template (LOW PRIORITY)

#### Створити абстрактний `AbstractReportWriter`:
```java
public abstract class AbstractReportWriter implements ReportWriter {
    
    protected void processDivisions(EngagementContext context, 
                                   DivisionProcessor processor) {
        for (var entry : context.getInfantryByDivision().entrySet()) {
            String division = entry.getKey();
            List<BattleUnit> units = entry.getValue();
            processor.process(division, units, context);
        }
    }
    
    @FunctionalInterface
    protected interface DivisionProcessor {
        void process(String division, List<BattleUnit> units, 
                    EngagementContext context);
    }
}
```

**Вигода:** Template Method pattern, -20 рядків коду

---

## 📈 Метрики до/після рефакторингу

| Метрика | До | Після | Покращення |
|---------|-----|-------|------------|
| Загальні рядки коду | ~8500 | ~7700 | -9.4% |
| Дублікованих рядків | ~900 | ~100 | -88.9% |
| Test code duplication | ~800 | ~50 | -93.8% |
| Main code duplication | ~100 | ~50 | -50% |
| Code coverage | ~70% | ~85%* | +15%* |

*Після рефакторингу легше покрити тестами utility класи

---

## 🚀 Порядок виконання

### Sprint 1 (2-3 години)
1. ✅ Створити `ExcelCellReader`
2. ✅ Рефакторити `ExcelRowMapper` і `AddedUnitMapper`
3. ✅ Рефакторити `XlsProcessor`
4. ✅ Запустити всі тести

### Sprint 2 (3-4 години)
5. ✅ Створити `TestDataFactory` з builder'ом
6. ✅ Рефакторити всі інтеграційні тести
7. ✅ Запустити всі тести

### Sprint 3 (1-2 години)
8. ✅ Створити `UnitGroupingService`
9. ✅ Рефакторити report writers
10. ✅ Запустити всі тести

### Sprint 4 (опціонально, 1-2 години)
11. ✅ Створити `AbstractReportWriter`
12. ✅ Додаткові покращення
13. ✅ Фінальне тестування

---

## ⚠️ Ризики

1. **Backward compatibility** - всі існуючі тести мають пройти
2. **Performance** - перевірити що немає деградації
3. **Test coverage** - не втратити покриття після рефакторингу

---

## ✅ Критерії успіху

- ✅ Всі тести проходять
- ✅ Дублювання коду < 5%
- ✅ Code coverage > 80%
- ✅ Немає regression bugs
- ✅ Код легше читати та підтримувати

---

## 📝 Висновок

Проект має **критичне дублювання коду** особливо в:
1. ❗ Excel cell reading (100% дублікат)
2. ❗ Test data creation (800+ дублікованих рядків)
3. ⚠️ Unit grouping logic (часткове дублювання)

**Рекомендація:** Виконати рефакторинг Phase 1 та Phase 2 **негайно**, Phase 3 - при можливості.

Очікуваний результат: **-800 рядків коду, +15% maintainability score**
