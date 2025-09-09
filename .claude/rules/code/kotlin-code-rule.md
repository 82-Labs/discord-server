# Kotlin Code 규칙

## 📋 개요

이 문서는 Kotlin Code Rule을 정의합니다.

## Code Rules

### Null 활용 규칙

#### Empty Object Pattern은 신중하게
```kotlin
// ✅ Empty Object가 적절한 경우
interface Logger {
    fun log(message: String)
}

object NoOpLogger : Logger {
    override fun log(message: String) { /* 아무것도 안함 */ }
}

class Service(
    private val logger: Logger = NoOpLogger  // null 체크 불필요
) {
    fun process() {
        logger.log("Processing...")  // null-safe
    }
}

// ❌ Empty Object가 부적절한 경우
data class UserProfile(
    val nickname: String,
    val bio: String
) {
    companion object {
        val EMPTY = UserProfile("", "")  // 빈 문자열이 의미가 있나?
    }
}

// ✅ 개선 - null로 명확히 구분
data class User(
    val id: String,
    val email: String,
    val profile: UserProfile? = null  // 프로필이 없음을 명확히 표현
)

data class UserProfile(
    val nickname: String,  // 프로필이 있다면 닉네임은 필수
    val bio: String? = null  // bio는 선택적
)
```

#### NULL이 의미를 가질 때는 명확히 구분
```kotlin
// ✅ GOOD - null이 명확한 의미를 가짐
data class User(
    val id: String,
    val email: String,
    val phoneNumber: String? = null,  // 선택적 필드는 nullable
    val deletedAt: Instant? = null    // 삭제되지 않은 경우 null
)

// ❌ BAD - 빈 객체로 null을 대체 (의미가 모호함)
data class User(
    val id: String,
    val email: String,
    val profile: UserProfile = UserProfile.EMPTY  // "없음"인지 "빈 값"인지 불명확
)
```


#### Safe Call과 Elvis 연산자 활용
```kotlin
// ✅ 좋은 예시
fun getUserDisplayName(user: User?): String {
    return user?.profile?.fullName ?: "Unknown User"
}

fun processUsers(users: List<User>?) {
    users?.filter { it.isActive }
        ?.forEach { processUser(it) }
}

// ❌ 나쁜 예시 - 불필요한 null 체크
fun getUserDisplayName(user: User?): String {
    if (user != null) {
        if (user.profile != null) {
            if (user.profile.fullName != null) {
                return user.profile.fullName
            }
        }
    }
    return "Unknown User"
}
```

#### 3. Null 예외처리
```kotlin
// ✅ 좋은 예시 - ApiException 으로 일관된 처리
class InsuranceScrapingService {
    fun scrapZ5007(userId: Long, bizNo: String) {
        val data = z5007HarleqPort.scrapZ5007(userId, targetYear, bizNo)
            ?: throw ApiErrorException.of(
                ApiResultCode.HOMETAX_SCRAPING_ERROR,
                "스크래핑 실패: userId=$userId, year=$targetYear"
            )
        
        val parsed = dataParser.parseZ5007A0051(data, userId, bizNo, data.requestId)
            ?: throw ApiWarnException.of(
                ApiResultCode.EXTERNAL_API_ERROR,
                "파싱 실패: requestId=${data.requestId}"
            )
    }
}


```

### Null 활용 규칙 확인 목록
- [ ] 적절하게 null을 사용했는지 확인
- [ ] Safe call (`?.`)과 Elvis 연산자 (`?:`)를 적절히 사용했는지 확인
- [ ] `!!` 연산자 사용을 필요한 곳에서만 사용했는지 확인 "예시) Jpa Entity id"
- [ ] ApiException으로 일관된 예외 처리 했는지 확인

## 가독성 (Readability)

### 직관적인 코드 작성

#### 1. 타입 추론 활용
```kotlin
// ✅ 좋은 예시 - 타입 추론 활용
val users = listOf(user1, user2, user3)  // List<User> 추론
val userMap = users.associateBy { it.id }  // Map<String, User> 추론
val activeUsers = users.filter { it.isActive }  // List<User> 추론

// ❌ 나쁜 예시 - 불필요한 타입 선언
val users: List<User> = listOf<User>(user1, user2, user3)
val userMap: Map<String, User> = users.associateBy { it.id }

// ✅ 타입 명시가 필요한 경우
val emptyUsers: List<User> = emptyList()  // 빈 컬렉션
val userCache: MutableMap<String, User> = mutableMapOf()  // 가변성 명시
```

#### 2. 문자열 템플릿 활용
```kotlin
// ✅ 좋은 예시
val message = "User ${user.name} has ${user.orders.size} orders"
val query = """sql
    SELECT * FROM users 
    WHERE age > $minAge 
    AND status = '${UserStatus.ACTIVE}'
""".trimIndent()

// ❌ 나쁜 예시
val message = "User " + user.name + " has " + user.orders.size + " orders"
val query = "SELECT * FROM users WHERE age > " + minAge + " AND status = '" + UserStatus.ACTIVE + "'"
```

#### 3. 구조 분해 선언 활용
```kotlin
// ✅ 좋은 예시
data class Point(val x: Int, val y: Int)

val (x, y) = point
val (user, orders) = getUserWithOrders(userId)
// Map 처리
val userScores = mapOf("alice" to 95, "bob" to 87)
for ((name, score) in userScores) {
    println("$name: $score")
}

// ❌ 나쁜 예시
val x = point.x
val y = point.y
val user = result.first
val orders = result.second
```

#### 4. when 표현식 활용
```kotlin
// ✅ 좋은 예시 - exhaustive when
sealed class Result {
    object Success : Result()
    data class Failed(val reason: String) : Result()
}

fun handlePaymentResult(result: PaymentResult): String = when (result) {
    is Result.Success -> "completed"
    is Result.Failed -> "failed: ${result.reason}"

}

// ✅ when with ranges
fun getAgeGroup(age: Int): String = when (age) {
    in 0..12 -> "Child"
    in 13..19 -> "Teenager"
    in 20..64 -> "Adult"
    else -> "Senior"
}

// ❌ 나쁜 예시 - if-else 체인
fun handlePaymentResult(age : Int): String {
    if (age in 0..12) {
        return "Child"
    } else if (age in 13..19) {
        return "Tennager"
    } else if (age in 20..64) {
        return "Adult"
    } else {
        return "Senior"
    }
}
```

### 🔍 가독성 검증 기준
- [ ] 타입 추론을 적절히 활용했는가?
- [ ] 문자열 템플릿을 사용했는가?
- [ ] 구조 분해 선언을 활용했는가?
- [ ] when 표현식을 적절히 사용했는지 확인 (조건문 2개 이하는 if 사용 권장)

## ⚡ 성능 (Performance)

### ✅ 효율적인 코드 작성

#### 1. 컬렉션과 시퀀스 적절한 선택
```kotlin
// ✅ 컬렉션 - 작은 데이터, 즉시 처리
fun getActiveUserNames(users: List<User>): List<String> {
    return users
        .filter { it.isActive }
        .map { it.name }
        .take(10)
}

// ✅ 시퀀스 - 큰 데이터, 지연 처리
fun processLargeDataset(data: List<BigData>): List<ProcessedData> {
    return data.asSequence()
        .filter { it.isValid() }
        .map { processData(it) }
        .filter { it.score > 80 }
        .take(100)
        .toList()
}

// ❌ 나쁜 예시 - 비효율적인 체이닝
fun processUsers(users: List<User>): List<String> {
    return users
        .filter { it.isActive }  // 중간 리스트 생성
        .map { it.profile }      // 중간 리스트 생성
        .filter { it.isComplete } // 중간 리스트 생성
        .map { it.displayName }  // 중간 리스트 생성
}
```

#### 2. inline 함수 활용
```kotlin
// ✅ 좋은 예시 - 고차 함수에 inline 사용
inline fun <T> measureTime(block: () -> T): Pair<T, Long> {
    val startTime = System.currentTimeMillis()
    val result = block()
    val endTime = System.currentTimeMillis()
    return result to (endTime - startTime)
}

inline fun <T> List<T>.fastFilter(predicate: (T) -> Boolean): List<T> {
    val result = mutableListOf<T>()
    for (element in this) {
        if (predicate(element)) {
            result.add(element)
        }
    }
    return result
}

// ❌ 나쁜 예시 - 불필요한 inline
inline fun simpleCalculation(a: Int, b: Int): Int {  // inline 불필요
    return a + b
}
```

### 🔍 성능 검증 기준
- [ ] 큰 컬렉션 처리 시 시퀀스를 사용했는가?
- [ ] 고차 함수에 inline을 적절히 사용했는가?

## 🏗️ 추상화 (Abstraction)

### ✅ 적절한 추상화 수준

#### 1. 제네릭 활용
```kotlin
// ✅ 좋은 예시 - 타입 안전한 제네릭
interface Repository<T, ID> {
    fun findById(id: ID): T?
    fun save(entity: T): T
    fun findAll(): List<T>
}

class UserRepository : Repository<User, UserId> {
    override fun findById(id: UserId): User? = // ...
    override fun save(entity: User): User = // ...
    override fun findAll(): List<User> // ...
}

// ❌ 나쁜 예시 - Any 사용
interface Repository {
    fun findById(id: Any): Any?
    fun save(entity: Any): Any
}
```

#### 2. 확장 함수 활용
```kotlin
// ✅ 좋은 예시 - 기존 타입 확장
fun String.isValidEmail(): Boolean {
    return this.contains("@") && this.contains(".")
}

fun LocalDateTime.toKoreanString(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분")
    return this.format(formatter)
}

fun <T> List<T>.second(): T {
    if (size < 2) throw IndexOutOfBoundsException("List has less than 2 elements")
    return this[1]
}

// ❌ 나쁜 예시 - 유틸 클래스
object StringUtils {
    fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }
}
```

#### 3. 고차 함수와 함수 타입 활용
```kotlin
// ✅ 좋은 예시 - 고차 함수 활용
fun <T> retry(
    times: Int,
    delay: Long = 1000,
    operation: () -> T
): T {
    repeat(times - 1) {
        try {
            return operation()
        } catch (e: Exception) {
            Thread.sleep(delay)
        }
    }
    return operation() // 마지막 시도
}

// 사용
val result = retry(times = 3) {
    apiService.fetchUserData(userId)
}

// ✅ 함수 타입을 매개변수로
class EventPublisher {
    private val handlers = mutableMapOf<String, MutableList<(Any) -> Unit>>()
    
    fun <T> subscribe(eventType: String, handler: (T) -> Unit) {
        handlers.getOrPut(eventType) { mutableListOf() }
            .add(handler as (Any) -> Unit)
    }
}

// ❌ 나쁜 예시 - 복잡한 인터페이스
interface RetryHandler {
    fun execute(): Any
    fun onFailure(exception: Exception)
    fun shouldRetry(attempt: Int): Boolean
}
```

#### 4. sealed class와 enum class 적절한 사용
```kotlin
// ✅ 좋은 예시 - sealed class (상태 + 데이터)
sealed class LoadingState<out T> {
    object Loading : LoadingState<Nothing>()
    data class Success<T>(val data: T) : LoadingState<T>()
    data class Error(val exception: Throwable) : LoadingState<Nothing>()
}

fun <T> handleLoadingState(state: LoadingState<T>) = when (state) {
    is LoadingState.Loading -> showSpinner()
    is LoadingState.Success -> displayData(state.data)
    is LoadingState.Error -> showError(state.exception.message)
}

// ✅ 좋은 예시 - enum class (단순 상태)
enum class OrderStatus(val displayName: String) {
    PENDING("주문 대기"),
    CONFIRMED("주문 확정"),
    SHIPPED("배송 중"),
    DELIVERED("배송 완료"),
    CANCELLED("주문 취소");
    
    fun canBeCancelled(): Boolean = this in listOf(PENDING, CONFIRMED)
}

// ❌ 나쁜 예시 - enum에 데이터 포함
enum class ApiResponse(val data: Any?) {  // 타입 안전성 부족
    SUCCESS(null),
    ERROR(null)
}
```

### 🔍 추상화 검증 기준
- [ ] 제네릭을 적절히 활용했는가?
- [ ] 확장 함수로 기존 타입을 확장했는가?
- [ ] 고차 함수를 활용해 코드 재사용성을 높였는가?
- [ ] sealed class와 enum class를 적절히 구분해서 사용했는가?

## 🎨 관용구 (Idioms)

### ✅ Kotlin 관용적 표현

#### 1. data class와 copy 활용
```kotlin
// ✅ 좋은 예시 - data class 활용
data class User(
    val id: String,
    val name: String,
    val email: String,
    val status: UserStatus = UserStatus.ACTIVE
) {
    fun deactivate(): User = copy(status = UserStatus.INACTIVE)
    fun changeName(newName: String): User = copy(name = newName)
}

// ✅ named arguments 활용
val user = User(
    id = "user123",
    name = "홍길동",
    email = "hong@example.com"
)
```

#### 2. apply, let, run, with, also 활용
```kotlin
// ✅ apply - 객체 설정
fun createDatabaseConfig(): DatabaseConfig {
    return DatabaseConfig().apply {
        host = "localhost"
        port = 5432
        database = "myapp"
        username = "admin"
        password = "secret"
    }
}

// ✅ let - null 체크와 변환
fun processUser(user: User?) {
    user?.let { u ->
        logger.info("Processing user: ${u.name}")
        processUserData(u)
        sendNotification(u)
    }
}

// ✅ run - 객체에서 코드 블록 실행
fun calculateOrderTotal(order: Order): Money {
    return order.run {
        val itemsTotal = items.sumOf { it.price * it.quantity }
        val shippingCost = calculateShipping(deliveryAddress)
        val tax = calculateTax(itemsTotal + shippingCost)
        itemsTotal + shippingCost + tax
    }
}

// ✅ with - 객체 없이 멤버 접근
fun printUserInfo(user: User) {
    with(user) {
        println("ID: $id")
        println("Name: $name")
        println("Email: $email")
        println("Status: $status")
    }
}

// ✅ also - 부수 효과
fun saveUser(user: User): User {
    return userRepository.save(user)
        .also { savedUser ->
            logger.info("User saved: ${savedUser.id}")
            eventPublisher.publish(UserSavedEvent(savedUser))
        }
}
```

#### 3. 람다와 함수 참조
```kotlin
// ✅ 좋은 예시 - 함수 참조
val activeUsers = users.filter(User::isActive)
val userNames = users.map(User::name)
val userIds = users.map { it.id }

// ✅ 람다 표현식
val processedUsers = users
    .filter { it.age >= 18 }
    .map { user ->
        ProcessedUser(
            id = user.id,
            displayName = "${user.firstName} ${user.lastName}",
            isAdult = true
        )
    }

// ❌ 나쁜 예시 - 불필요한 람다
val activeUsers = users.filter { user -> user.isActive() }  // User::isActive 사용
val userNames = users.map { user -> user.name }  // User::name 사용
```

#### 4. companion object 활용
```kotlin
// ✅ 좋은 예시 - factory 메서드
data class User private constructor(
    val id: Long?,
    val email: Email,
    val profile: UserProfile
) {
    companion object {
        fun create(email: String, firstName: String, lastName: String): User {
            return User(
                id = null,
                email = Email(email),
                profile = UserProfile(firstName, lastName)
            )
        }
    }
}

// ✅ 상수와 유틸 함수
class Money(val amount: BigDecimal, val currency: Currency) {
    companion object {
        val ZERO = Money(BigDecimal.ZERO, Currency.getInstance("KRW"))
        
        fun krw(amount: Int): Money = Money(BigDecimal(amount), Currency.getInstance("KRW"))
        fun krw(amount: String): Money = Money(BigDecimal(amount), Currency.getInstance("KRW"))
    }
}
```

### 🔍 관용구 검증 기준
- [ ] data class를 적절히 활용했는가?
- [ ] scope function(apply, let, run, with, also)을 올바르게 사용했는가?
- [ ] 람다와 함수 참조를 적절히 구분해서 사용했는가?
- [ ] companion object를 활용해 factory 패턴을 구현했는가?

## 🔐 불변성 (Immutability)

### ✅ 불변 객체 선호

#### 1. val vs var
```kotlin
// ✅ 좋은 예시 - val 우선 사용
class UserService(
    private val userRepository: UserRepository,  // val
    private val emailService: EmailService       // val
) {
    fun processUsers(): List<User> {
        val allUsers = userRepository.findAll()  // val
        val activeUsers = allUsers.filter { it.isActive }  // val
        return activeUsers.map { user ->  // val (implicit)
            processUser(user)
        }
    }
}

// ❌ 나쁜 예시 - 불필요한 var
class UserService(
    private var userRepository: UserRepository,  // var 불필요
    private var emailService: EmailService       // var 불필요
) {
    fun processUsers(): List<User> {
        var allUsers = userRepository.findAll()  // var 불필요
        var activeUsers = allUsers.filter { it.isActive }  // var 불필요
        return activeUsers
    }
}
```

#### 2. 불변 컬렉션 사용
```kotlin
// ✅ 좋은 예시 - 불변 컬렉션
class Order(
    val id: OrderId,
    val customerId: CustomerId,
    private val _items: List<OrderItem>  // 내부적으로 불변
) {
    val items: List<OrderItem> get() = _items  // 읽기 전용 접근
    
    fun addItem(item: OrderItem): Order {
        return copy(_items = _items + item)  // 새 객체 반환
    }
    
    fun removeItem(itemId: OrderItemId): Order {
        return copy(_items = _items.filterNot { it.id == itemId })
    }
}

// ❌ 나쁜 예시 - 가변 컬렉션 노출
class Order(
    val id: OrderId,
    val items: MutableList<OrderItem>  // 외부에서 수정 가능
) {
    fun addItem(item: OrderItem) {
        items.add(item)  // 객체 상태 변경
    }
}
```

#### 3. copy를 이용한 수정
```kotlin
// ✅ 좋은 예시 - 불변 객체 수정
data class UserProfile(
    val firstName: String,
    val lastName: String,
    val age: Int,
    val address: Address
) {
    fun updateAge(newAge: Int): UserProfile = copy(age = newAge)
    
    fun updateAddress(newAddress: Address): UserProfile = copy(address = newAddress)
    
    fun updateName(firstName: String, lastName: String): UserProfile = 
        copy(firstName = firstName, lastName = lastName)
}

// ❌ 나쁜 예시 - 가변 객체
class UserProfile(
    var firstName: String,
    var lastName: String,
    var age: Int,
    var address: Address
) {
    fun updateAge(newAge: Int) {
        this.age = newAge  // 객체 상태 변경
    }
}
```

### 🔍 불변성 검증 기준
- [ ] val을 var보다 우선적으로 사용했는가?
- [ ] 컬렉션을 불변으로 노출했는가?
- [ ] 객체 수정 시 새 객체를 반환하는가?
- [ ] 가변 상태를 최소화했는가?

## 🚀 코루틴 (Coroutines)

### ✅ 효율적인 비동기 처리

#### 1. suspend 함수 활용
```kotlin
// ✅ 좋은 예시 - suspend 함수


class InsuranceScrapingService(
    private val scrapingRepository: HometaxScrapingRepository,
    private val insuranceRepository: InsuranceRepository,
    private val hometaxClient: HometaxClient,
    private val parser: ScrapingDataParser
) {
    suspend fun scrapAndSave(userId: Long, bizNo: String): ScrapResult {
        // 이미 존재하는지 체크
        val currentYear = LocalDate.now().year.toString()
        if (scrapingRepository.existsByUserIdAndYear(userId, currentYear)) {
            return ScrapResult.AlreadyExists
        }
        
        // 외부 API 호출 (suspend)
        val scrapData = hometaxClient.scrapB6004(userId, currentYear)
            ?: return ScrapResult.Failed("스크래핑 실패")
        
        // 데이터 파싱
        val insurances = parser.parseInsurances(scrapData)
        
        // DB 저장
        scrapingRepository.save(scrapData)
        insuranceRepository.saveAll(insurances)
        
        return ScrapResult.Success(insurances.size)
    }
}
```

#### 2. 구조화된 동시성
```kotlin
// ✅ 좋은 예시 - 구조화된 동시성
// ✅ 실용적 - 성공 or 실패만
@Service
class InsuranceScrapingFacade(
    private val scrapingService: InsuranceScrapingService,
    private val estimateService: EstimateInsuranceService
) {
    suspend fun scrapAndEstimate(
        userId: Long, 
        bizNo: String,
        birthYear: Int,
        gender: String
    ): InsuranceEstimateCommand = coroutineScope {
        
        // 병렬 스크래핑 - 둘 다 성공해야 함
        val z5007Job = async { scrapingService.scrapZ5007(userId, bizNo) }
        val b6004Job = async { scrapingService.scrapB6004(userId) }
        
        // 하나라도 실패하면 예외 발생
        z5007Job.await()
        b6004Job.await()
        
        // 모두 성공시에만 여기 도달
        estimateService.estimate(userId, birthYear, gender)
    }
}
```

### 🔍 코루틴 검증 기준
- [ ] I/O 작업에 suspend 함수를 사용했는지 확인
- [ ] 병렬 처리가 필요한 곳에 async/await를 사용했는지 확인
- [ ] 구조화된 동시성을 통해 처리했는지 확인

## 📋 자동 검증 스크립트

### Kotlin 스타일 검증
```bash
#!/bin/bash
# kotlin-style-check.sh

echo "🔍 Kotlin 스타일 검증 중..."

# 1. nullable 남용 검사
find src/main/kotlin -name "*.kt" -exec grep -Hn ".*?.*?.*?" {} \; | while read line; do
    echo "⚠️ 중첩된 nullable 타입: $line"
done

# 2. !! 연산자 남용 검사
# Entity ID 관련 !! 사용은 제외하고 카운트
FORCE_UNWRAP_COUNT=$(find src/main/kotlin -name "*.kt" -exec grep -E "!!" {} \; | \
    grep -v -E "(id!!|\.id!!|entityId!!|pk!!|primaryKey!!)" | \
    wc -l)

if [ "$FORCE_UNWRAP_COUNT" -gt 10 ]; then
    echo "⚠️ !! 연산자 과다 사용: ${FORCE_UNWRAP_COUNT}개 (Entity ID 제외)"
fi

# 3. var vs val 비율 검사
VAR_COUNT=$(find src/main/kotlin -name "*.kt" -exec grep -c "^\s*var " {} + | awk '{sum+=$1} END {print sum}')
VAL_COUNT=$(find src/main/kotlin -name "*.kt" -exec grep -c "^\s*val " {} + | awk '{sum+=$1} END {print sum}')
VAR_RATIO=$(echo "scale=2; $VAR_COUNT / ($VAR_COUNT + $VAL_COUNT) * 100" | bc)

if (( $(echo "$VAR_RATIO > 30" | bc -l) )); then
    echo "⚠️ var 사용률이 높음: ${VAR_RATIO}% (권장: 30% 이하)"
fi

# 4. 불필요한 타입 선언 검사
find src/main/kotlin -name "*.kt" -exec grep -Hn ": List<.*> = listOf<.*>" {} \; | while read line; do
    echo "⚠️ 불필요한 타입 선언: $line"
done

echo "✅ Kotlin 스타일 검증 완료"
```

### 성능 관련 검증
```bash
#!/bin/bash
# kotlin-performance-check.sh

echo "⚡ Kotlin 성능 검증 중..."

# 1. 시퀀스 사용 권장 검사
find src/main/kotlin -name "*.kt" -exec grep -Hn "\.filter.*\.map.*\.filter" {} \; | while read line; do
    echo "💡 시퀀스 사용 고려: $line"
done

# 2. 불필요한 객체 생성 검사
find src/main/kotlin -name "*.kt" -exec grep -Hn "mutableListOf<.*>()" {} \; | while read line; do
    echo "💡 immutable 컬렉션 고려: $line"
done

# 3. inline 함수 권장 검사
find src/main/kotlin -name "*.kt" -exec grep -A5 "fun.*block.*->.*{" {} \; | grep -v "inline" | while read line; do
    echo "💡 inline 함수 고려: $line"
done

# 4. if (x == null) throw 패턴
echo "🔍 requireNotNull/checkNotNull 사용 권장..."
find src/main/kotlin -name "*.kt" -exec grep -Hn "if.*==.*null.*throw" {} \; | while read line; do
    echo "💡 requireNotNull/checkNotNull 사용 권장: $line"
done

echo "✅ Kotlin 성능 검증 완료"
```

## 규칙 확인사항

### 코드 스타일
- [ ] Kotlin 스럽게 작성했는지 확인 - High
- [ ] 타입 추론, 문자열 템플릿, 구조 분해, 확장함수등을 통해 가독성을 높혔는지 확인 - High
- [ ] null에 대한 처리를 코틀린스럽게 안전하게 처리했는지 확인 - High
- [ ] scope function을 적절하게 사용하였는지 [Depth <= 2] 확인 - Mid
- [ ] val이나 immutable을 최대한 적용했는지 확인 - Mid
- [ ] 고차함수를 제대로 활용하였는지 확인 - Low
- [ ] Sealed Class나 Value Class를 제대로 활용하는지 확인 - Low

### 성능 최적화
- [ ] 컬렉션과 시퀀스 제대로 사용했는지 확인 - High
- [ ] inline 함수를 제대로 사용했는지 확인 - High

### 점수
규칙 확인사항 항목을 토대로 점수를 구하고 비율은 아래와 같음
High - 6
Mid - 3
Low - 1

- **코드 스타일**: 65점
- **성능 최적화**: 35점

**총점 100점 만점**
- 90-100점: 우수 (Excellent Kotlin)
- 80-89점: 양호 (Good Kotlin)
- 70-79점: 보통 (Average Kotlin)
- 70점 미만: 개선 필요 (Needs Improvement)