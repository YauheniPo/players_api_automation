# Players API Automation Framework

REST API test automation framework built with **Kotlin + RestAssured + TestNG + Allure**.

CI pipeline: https://github.com/YauheniPo/players_api_automation/actions
Issues/Tickets: https://github.com/YauheniPo/players_api_automation/issues

---

## Tech Stack

| Layer            | Library / Tool                        | Version |
|------------------|---------------------------------------|---------|
| Language         | Kotlin                                | 1.9.23  |
| Build            | Gradle (Kotlin DSL)                   | 8.7     |
| HTTP client      | REST Assured                          | 5.4.0   |
| JSON             | Jackson + jackson-module-kotlin       | 2.17.0  |
| Test runner      | TestNG                                | 7.12.0  |
| Assertions       | AssertJ                               | 3.25.3  |
| Reporting        | Allure TestNG                         | 2.27.0  |
| AOP              | AspectJ Weaver                        | 1.9.22  |
| Test data        | DataFaker                             | 2.2.2   |
| Configuration    | Owner                                 | 1.0.12  |
| Code style       | ktlint                                | 1.2.1   |
| Logging          | SLF4J + Logback                       | 2.0.13  |

---

## Tech Stack & Why

### Kotlin
Основной язык фреймворка. Выбран вместо Java по ряду причин:
- **`data class`** — DTO без Lombok: `equals`, `hashCode`, `copy`, `toString` из коробки
- **`object`** — singleton без паттерна, потокобезопасный lazy init (TestContext, DataGenerator, ConfigProvider)
- **Named parameters** — `PlayerRequestDTO(email = "...", username = "...")` читается как документация
- **Sealed classes** — `ApiResponse<T>` с exhaustive `when` вместо if/else на статус-кодах
- **Extension functions** — `Response.toApiResponse<T>()` добавляет метод к чужому классу без наследования
- **Null safety** — компилятор запрещает NPE: `token.accessToken!!` явно сигнализирует о риске
- **Kotlin Multiplatform (KMP/KMM)** — DTO-модели (`PlayerRequestDTO`, `PlayerResponseDTO`, `TokenDTO`) написаны на чистом Kotlin без JVM-зависимостей. При необходимости модуль `dto/` можно вынести в `commonMain` и переиспользовать те же классы в iOS/Android/Web тестах — без дублирования контрактов между платформами

---

### REST Assured
HTTP-клиент для тестирования API. Стандарт де-факто в JVM-экосистеме:
- **Fluent DSL** — `given().spec(...).body(...).when().post(...).then().statusCode(201)` — цепочка читается как тест-сценарий
- **RequestSpecification** — переиспользуемая конфигурация (baseUrl, headers, ContentType) в `BaseApiClient`, не дублируется в каждом запросе
- **Встроенная проверка статус-кода** — `.then().statusCode(200)` с информативным сообщением об ошибке (URL, headers, тело ответа)
- **Jackson интеграция** — автоматическая сериализация/десериализация через `ObjectMapper`, настроенный с `KotlinModule`

---

### TestNG
Test runner. Выбран вместо JUnit за счёт возможностей для сложных сценариев:
- **`@DataProvider`** — генерирует 12 наборов данных и запускает `createPlayer` 12 раз, каждый как отдельный тест
- **Groups + `dependsOnGroups`** — явная цепочка `create → read → delete → verify` с автоматическим `SKIP` зависимых тестов при падении
- **`parallel="methods"` + `thread-count`** — параллельный запуск без единой строки кода, только декларативный XML-конфиг; в JUnit 5 для того же нужны аннотации, кастомные extensions и дополнительная настройка
- **`@BeforeSuite`** — однократная аутентификация до старта всех тестов
- **Allure out-of-the-box** — `allure-testng` интегрируется через стандартный TestNG listener-механизм: одна зависимость в `build.gradle.kts` и AspectJ агент — и весь жизненный цикл тестов (pass/fail/skip, шаги, вложения) попадает в отчёт автоматически, без кастомных репортеров и ручной регистрации событий как в JUnit

---

### Allure
Отчётность. Превращает результаты тестов в читаемую документацию:
- **`@Feature` / `@Story`** — группировка тестов по функциональным областям в отчёте
- **`@Step`** на методах API-клиентов — каждый HTTP-запрос виден в отчёте как отдельный шаг с request/response
- **`AllureRestAssured` filter** — автоматически логирует все HTTP-запросы и ответы в Allure без ручного кода
- **`@Description`** — описание ожидаемого результата прямо в отчёте, без открытия кода

---

### AssertJ
Библиотека assertions. Использована вместо стандартных TestNG assert по нескольким причинам:
- **Fluent chain** — `assertThat(list).isNotEmpty().hasSizeGreaterThanOrEqualTo(12)` вместо двух отдельных вызовов
- **`.as("описание")`** — контекст в сообщении ошибки: `[email] expected "x" but was "y"`
- **`SoftAssertions.assertSoftly {}`** — все assertions в блоке выполняются до конца, итоговый репорт содержит все упавшие, а не только первый

---

### DataFaker
Генерация тестовых данных:
- **Реалистичные данные** — настоящие имена, валюты, email вместо `"test1"`, `"test2"`
- **Уникальность** — `AtomicInteger` счётчик + случайный суффикс исключает коллизии при параллельном создании 12 игроков
- **Изоляция прогонов** — каждый запуск создаёт новые данные, тесты не зависят от состояния БД предыдущего запуска

---

### Owner
Конфигурация через `.properties` файлы и переменные среды:
- **Type-safe** — `config.baseUrl()` вместо `System.getProperty("base.url")` со String-ключами
- **Приоритет источников** — `env > system properties > config.local.properties > config.properties`, что позволяет переопределять значения локально или в CI без изменения файлов в репозитории
- **`config.local.properties`** — git-ignored файл для локальных кредов; если файл отсутствует (например, на CI), Owner его просто пропускает

---

### ktlint
Статический анализатор стиля Kotlin-кода. Запускается автоматически перед каждой компиляцией и блокирует сборку при нарушениях:
- **Единый стиль в команде** — все разработчики пишут код в одном стиле без ручных ревью "поставь пробел здесь"
- **Нарушение = ошибка компиляции** — `ktlintCheck` запускается как зависимость `compileKotlin`, грязный код не компилируется
- **Авто-исправление** — большинство нарушений устраняет `./gradlew ktlintFormat` без ручного вмешательства
- **Конфигурация через `.editorconfig`** — правила читаются из стандартного файла, который подхватывают и IDE (IntelliJ, Cursor/VS Code)

**Команды:**
```bash
./gradlew ktlintCheck    # проверить стиль (запускается автоматически при компиляции)
./gradlew ktlintFormat   # авто-исправить все нарушения
```

**Ключевые включённые правила:**

| Правило | Что проверяет |
|---|---|
| `no-wildcard-imports` | `import org.foo.*` запрещён - только явные импорты |
| `no-unused-imports` | удаляет мёртвые импорты |
| `trailing-comma-on-*-site` | trailing comma в declaration и call site |
| `no-consecutive-blank-lines` | не более 1 пустой строки между блоками |
| `function-naming` / `class-naming` | camelCase / PascalCase |
| `no-semi` | точки с запятой запрещены |
| `string-template` | `"${x}"` упрощается до `"$x"` |

**Намеренно отключённые правила:**

| Правило | Причина отключения |
|---|---|
| `multiline-expression-wrapping` | RestAssured-цепочки многострочные по дизайну |
| `argument-list-wrapping` | слишком агрессивно для `SoftAssertions`-блоков |

---

### AspectJ Weaver
AOP-агент, подключается к JVM через `-javaagent` в `build.gradle.kts`. В проекте используется как обязательная зависимость Allure:
- **`@Step` на методах API-клиентов** — AspectJ перехватывает каждый вызов и регистрирует его как шаг в Allure-отчёте; без агента `@Step` компилируется, но в отчёте не появляется
- **`@Feature` / `@Story` / `@Description`** — не требуют агента, читаются из метаданных класса напрямую

**Перспективы использования AOP в фреймворке:**

AspectJ не ограничивается Allure — это полноценный инструмент для сквозной логики. В рамках этого фреймворка его можно развить:

| Сценарий | Что делает aspect |
|---|---|
| **Логирование всех запросов** | `@Before`/`@After` на методах `*ApiClient` — лог каждого вызова без изменения кода клиентов |
| **Retry при флакующих тестах** | `@Around` на `@Test`-методах — автоматический повтор при `AssertionError` или сетевом таймауте |
| **Контроль времени ответа** | `@Around` на API-клиентах — измеряет время выполнения, падает если превышен порог |
| **Маскирование чувствительных данных** | `@Before` на методах с `CredentialsDTO` — заменяет пароль на `***` в логах до того как он туда попадёт |
| **Трассировка параллельных потоков** | `@Before`/`@After` добавляют `threadName` и `testName` в MDC, логи параллельных тестов перестают перемешиваться |

Всё это реализуется в одном `aspect`-классе без единого изменения в тестовом коде.

---

### Logback / SLF4J
Логирование:
- **SLF4J** — абстракция над логгером, тесты не привязаны к конкретной реализации
- **Logback** — подавляет шум от внутренних библиотек (Apache HTTP, RestAssured) и оставляет только логи фреймворка

---

## Project Structure

```
src/
├── main/kotlin/com/automation/
│   ├── api/
│   │   ├── ApiResponse.kt         # sealed class Success/Error + toApiResponse<T>()
│   │   ├── BaseApiClient.kt       # RequestSpec factory (unauth / bearer)
│   │   ├── AuthApiClient.kt       # POST /api/tester/login → ApiResponse<TokenDTO>
│   │   └── PlayerApiClient.kt     # /api/automationTask/* → ApiResponse<T>
│   ├── config/
│   │   ├── AppConfig.kt           # Owner interface
│   │   └── ConfigProvider.kt      # object singleton
│   ├── dto/
│   │   ├── request/               # CredentialsDTO, CredentialsLoginDTO,
│   │   │                          # PlayerRequestDTO, PlayerRequestOneDTO
│   │   └── response/              # TokenDTO, PlayerResponseDTO
│   └── utils/
│       ├── DataGenerator.kt       # DataFaker + AtomicInteger для уникальности
│       └── TestContext.kt         # @Volatile token + CopyOnWriteArrayList игроков
│
└── test/kotlin/com/automation/
    ├── base/BaseTest.kt           # @BeforeSuite: Jackson config + auth
    └── tests/
        ├── AuthTest.kt            # тест эндпоинта /api/tester/login
        └── PlayerTest.kt          # полный CRUD-цикл 12 игроков
```

---

## Configuration

Конфигурация загружается через [Owner](http://owner.aeonbits.org/) с политикой `MERGE`.
Приоритет источников (от высшего к низшему):

| Приоритет | Источник | Назначение |
|-----------|----------|------------|
| 1 | Environment variables | CI/CD (GitHub Actions, GitLab CI, …) |
| 2 | JVM system properties (`-D`) | Запуск из командной строки |
| 3 | `config.local.properties` | Локальная разработка (git-ignored) |
| 4 | `config.properties` | Шаблон с пустыми значениями, хранится в репозитории |

### Локальная разработка

Создайте `src/main/resources/config.local.properties` (уже добавлен в `.gitignore`):

```properties
base.url=https://your-env.example.com
admin.login=your@email.com
admin.password=your_password
```

### CI / командная строка

Через JVM-флаги `-D`:
```bash
./gradlew test \
  -Dbase.url=https://your-env.example.com \
  -Dadmin.login=your@email.com \
  -Dadmin.password=your_password
```

---

## Running Tests

```bash
# Запуск полного сьюта (thread-count=4, parallel="tests" из testng.xml)
./gradlew test

# Открыть Allure-отчёт
allure serve build/allure-results
```

---

## Test Execution Flow

```
@BeforeSuite  →  authenticate() → TestContext.authToken
                                        │
              ┌─────────────────────────┴──────────────────────────┐
        AuthTest                                              PlayerTest
   loginReturnsValidToken()                            createPlayer ×12  ← @DataProvider parallel (group: create)
   assertSuccess()                                             │
                                                  ┌──────────┴──────────┐
                                             getOnePlayer        getAllSortedByName   (group: read)
                                                   └──────────┬──────────┘
                                              deleteAllCreatedPlayers       (group: delete)
                                                              │
                                              playerListIsEmptyAfterDeletion (group: verify)
                                                              │
                                                   @AfterClass tearDown()
```

---

## Future: Kotlin Multiplatform (KMP) с Ktor

RestAssured — JVM-only библиотека и в KMP не работает. Однако архитектура проекта намеренно выстроена так, чтобы миграция на KMP была минимально болезненной.

### Что уже готово к переносу в `commonMain`

| Модуль | Статус | Причина |
|---|---|---|
| `dto/request/*.kt` | ✅ готово | чистые Kotlin `data class`, нет JVM-зависимостей |
| `dto/response/*.kt` | ✅ готово | то же самое |
| `ApiResponse.kt` | ✅ готово | Kotlin sealed class + stdlib `AssertionError` |
| `TestContext.kt` | ✅ готово | Kotlin object + stdlib коллекции |
| `*ApiClient.kt` | 🔄 заменить | RestAssured → Ktor Client |
| `DataGenerator.kt` | 🔄 адаптировать | DataFaker → платформенные реализации |

### Целевая структура KMP-модуля

```
src/
├── commonMain/kotlin/com/automation/
│   ├── dto/                    ← переносится как есть
│   ├── api/
│   │   ├── ApiResponse.kt      ← переносится как есть
│   │   ├── BaseApiClient.kt    ← Ktor HttpClient (commonMain)
│   │   ├── AuthApiClient.kt    ← Ktor (commonMain)
│   │   └── PlayerApiClient.kt  ← Ktor (commonMain)
│   └── utils/
│       └── TestContext.kt      ← переносится как есть
│
├── jvmMain/kotlin/             ← TestNG + Allure остаются здесь
│   └── tests/
│       ├── AuthTest.kt
│       └── PlayerTest.kt
│
└── iosMain/kotlin/             ← XCTest / ios-тесты в будущем
    └── tests/
```

### Как выглядит Ktor-клиент в `commonMain`

```kotlin
// build.gradle.kts — KMP
kotlin {
    jvm()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation("io.ktor:ktor-client-core:2.3.x")
            implementation("io.ktor:ktor-client-content-negotiation:2.3.x")
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.x")
            // Jackson заменяется на kotlinx.serialization
        }
        jvmMain.dependencies {
            implementation("io.ktor:ktor-client-okhttp:2.3.x")   // JVM engine
                testImplementation("org.testng:testng:7.12.0")
        }
        iosMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:2.3.x")   // iOS engine
        }
    }
}
```

```kotlin
// PlayerApiClient.kt в commonMain — вместо RestAssured
class PlayerApiClient(private val token: String) {

    private val client = HttpClient {
        install(ContentNegotiation) { json() }
        defaultRequest {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun createPlayer(request: PlayerRequestDTO): ApiResponse<PlayerResponseDTO> =
        runCatching { client.post("/api/automationTask/create") { setBody(request) }.body<PlayerResponseDTO>() }
            .fold(
                onSuccess = { ApiResponse.Success(it) },
                onFailure = { ApiResponse.Error(it) },
            )
}
```

### Что нужно заменить при миграции

| Текущее (JVM) | KMP-альтернатива |
|---|---|
| RestAssured | **Ktor Client** (`ktor-client-core`) |
| Jackson | **kotlinx.serialization** (`@Serializable` вместо `@JsonProperty`) |
| DataFaker | платформенные `expect/actual` реализации или UUID-based генератор |
| Owner (config) | `expect/actual` или environment-specific конфиги |
| `@JsonProperty("snake_case")` | `@SerialName("snake_case")` из kotlinx.serialization |

Основной выигрыш: один набор DTO-моделей и API-контрактов используется для автоматизации на JVM (TestNG), Android (Espresso/instrumented), iOS (XCTest) и Web (KotlinJS) — без дублирования кода между платформами.
