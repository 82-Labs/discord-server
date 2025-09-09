# 단위 테스트 작성 규칙

## 📋 개요

이 문서는 멀티 모듈 프로젝트의 단위 테스트 작성 규칙을 정의합니다. 핵심 비즈니스 로직에 대한 선택적이고 고가치의 테스트 코드를 작성하여 살아있는 문서 역할을 하도록 합니다.

## 🎯 테스트 작성 원칙

### 핵심 원칙
- **선택적 커버리지**: 모든 것이 아닌 비즈니스 중요 로직만 테스트
- **패턴 일관성**: 기존 테스트 패턴 유지
- **비즈니스 중심**: 기술적 커버리지보다 비즈니스 시나리오 우선
- **살아있는 문서**: 테스트는 비즈니스 요구사항을 명확히 설명
- **모듈 인식**: 모듈 경계와 책임 이해 및 존중
- **한국어 테스트 이름**: 비즈니스 명확성을 위한 설명적인 한국어 사용

## 🛠️ 테스팅 프레임워크

### 필수 사용 도구
- **테스트 프레임워크**: Kotest
- **모킹 프레임워크**: MockK
- **테스트 픽스처**: 도메인 모듈의 testFixtures 활용

## 🏗️ 테스트 품질 기준 (Test Quality Standards)

### ✅ 고품질 테스트 구조

#### 1. Given-When-Then 패턴 준수
```kotlin
// ✅ 좋은 예시 - 명확한 3단계 구조
class MedicalScrapingServiceTest : StringSpec({
    
    val scrapingPort = mockk<ScrapingPort>()
    val validator = mockk<ScrapingValidator>()
    val service = MedicalScrapingService(scrapingPort, validator)
    
    beforeTest {
        clearMocks(scrapingPort, validator)
    }
    
    "scrapMedicalData - 유효한 스크래핑 요청 시 성공적으로 데이터 반환" {
        // Given - 테스트 조건 명확히 설정
        val request = ScrapingRequestFixture.createValid(
            userId = 1L, 
            businessNumber = "123-45-67890",
            targetYear = "2024"
        )
        val expectedData = ScrapingDataFixture.createSuccessful(
            totalAmount = 500000,
            itemCount = 5
        )
        
        every { validator.validate(request) } returns ValidationResult.success()
        every { scrapingPort.scrap(any()) } returns expectedData
        
        // When - 실제 테스트 실행
        val result = service.scrapMedicalData(request)
        
        // Then - 결과 검증
        result shouldBe expectedData
        result.totalAmount shouldBe 500000
        result.items should haveSize(5)
        
        verify(exactly = 1) { validator.validate(request) }
        verify(exactly = 1) { scrapingPort.scrap(match { it.userId == 1L }) }
    }
})

// ❌ 나쁜 예시 - 불명확한 구조
class BadServiceTest : StringSpec({
    "test" {
        val service = Service()
        val result = service.method()  // Given/When 구분 없음
        result shouldNotBe null        // 의미 없는 검증
    }
})
```

#### 2. 의미있는 테스트 케이스 명명
```kotlin
// ✅ 좋은 예시 - 비즈니스 시나리오 명확
"calculateInsurancePremium - 35세 남성 흡연자일 때 기본 요율의 150% 적용" {
    // 구체적인 비즈니스 조건과 결과 명시
}

"validateBusinessNumber - 휴업 상태 사업자번호 입력 시 ValidationException 발생" {
    // 예외 상황의 비즈니스 의미 설명
}

"processRefund - 환불 요청 기간 초과 시 거부 상태로 처리" {
    // 비즈니스 규칙과 결과 행동 설명
}

// ❌ 나쁜 예시 - 의미 불명확
"test1" { }
"should work" { }
"when input is valid" { }  // 무엇이 valid한지 불명확
"throws exception" { }     // 어떤 상황에서 어떤 예외인지 불명확
```

#### 3. 적절한 모킹 전략
```kotlin
// ✅ 좋은 예시 - 핵심 의존성만 모킹
class TaxCalculationServiceTest : StringSpec({
    
    // 핵심 외부 의존성만 모킹
    val taxRateProvider = mockk<TaxRateProvider>()
    val incomeValidator = mockk<IncomeValidator>()
    
    val service = TaxCalculationService(taxRateProvider, incomeValidator)
    
    "calculateTax - 연소득 5천만원 일반 납세자의 소득세 계산" {
        // Given
        val income = Money.krw(50_000_000)
        val taxpayer = TaxpayerFixture.createGeneral(age = 35)
        
        every { incomeValidator.validate(income) } returns ValidationResult.success()
        every { taxRateProvider.getTaxRate(income, taxpayer.type) } returns TaxRate(0.24)
        
        // When
        val result = service.calculateTax(income, taxpayer)
        
        // Then
        result.taxAmount shouldBe Money.krw(12_000_000)
        result.effectiveRate shouldBe 0.24
        
        verify { taxRateProvider.getTaxRate(income, TaxpayerType.GENERAL) }
    }
})

// ❌ 나쁜 예시 - 과도한 모킹
class OverMockedServiceTest : StringSpec({
    val mock1 = mockk<Service1>()
    val mock2 = mockk<Service2>()
    val mock3 = mockk<Service3>()
    val mock4 = mockk<Service4>()
    val mock5 = mockk<Service5>()  // 너무 많은 의존성
    
    "test" {
        // 모킹 설정만으로 테스트가 복잡해짐
        every { mock1.method() } returns "value1"
        every { mock2.method() } returns "value2"
        every { mock3.method() } returns "value3"
        every { mock4.method() } returns "value4"
        every { mock5.method() } returns "value5"
        
        // 실제 테스트하려는 로직이 묻힘
    }
})
```

## 📊 테스트 대상 선별 기준 (Test Target Selection)

### ✅ 고우선순위 테스트 대상

#### 1. 복잡한 비즈니스 로직
```kotlin
// ✅ 반드시 테스트해야 할 비즈니스 로직
class InsurancePremiumCalculator {
    fun calculatePremium(
        age: Int,
        gender: Gender,
        smokingStatus: SmokingStatus,
        healthConditions: List<HealthCondition>,
        coverageAmount: Money
    ): InsurancePremium {
        // 복잡한 계산 로직 - 반드시 테스트 필요
        val baseRate = getBaseRate(age, gender)
        val smokingMultiplier = getSmoking(smokingStatus)
        val healthMultiplier = calculateHealthRisk(healthConditions)
        
        return InsurancePremium(
            monthlyPremium = coverageAmount * baseRate * smokingMultiplier * healthMultiplier,
            riskFactors = analyzeRiskFactors(age, smokingStatus, healthConditions)
        )
    }
}

// ✅ 테스트 예시
"calculatePremium - 45세 남성 흡연자 고혈압 환자의 보험료 계산" {
    // Given
    val calculator = InsurancePremiumCalculator()
    val healthConditions = listOf(HealthCondition.HIGH_BLOOD_PRESSURE)
    
    // When
    val premium = calculator.calculatePremium(
        age = 45,
        gender = Gender.MALE,
        smokingStatus = SmokingStatus.SMOKER,
        healthConditions = healthConditions,
        coverageAmount = Money.krw(100_000_000)
    )
    
    // Then
    premium.monthlyPremium shouldBe Money.krw(450_000)  // 기본 + 흡연 + 고혈압 할증
    premium.riskFactors should contain(RiskFactor.SMOKING)
    premium.riskFactors should contain(RiskFactor.CHRONIC_DISEASE)
}
```

#### 2. 상태 변경 로직
```kotlin
// ✅ 상태 변경을 수반하는 중요 로직
class OrderProcessingService {
    fun processPayment(order: Order, payment: Payment): OrderResult {
        // 여러 상태 변경이 일어나는 중요한 로직
        order.validatePaymentAmount(payment.amount)
        
        val paymentResult = paymentGateway.process(payment)
        if (paymentResult.isSuccess()) {
            order.markAsPaid()
            inventory.reserve(order.items)
            eventPublisher.publish(OrderPaidEvent(order.id))
            return OrderResult.success(order)
        } else {
            order.markAsPaymentFailed(paymentResult.reason)
            return OrderResult.failed(paymentResult.reason)
        }
    }
}

// ✅ 테스트 예시  
"processPayment - 결제 성공 시 주문 상태 변경 및 이벤트 발생" {
    // Given
    val order = OrderFixture.createPending(amount = Money.krw(100_000))
    val payment = PaymentFixture.createValid(amount = Money.krw(100_000))
    
    every { paymentGateway.process(payment) } returns PaymentResult.success()
    every { inventory.reserve(any()) } returns ReservationResult.success()
    every { eventPublisher.publish(any()) } just Runs
    
    // When
    val result = service.processPayment(order, payment)
    
    // Then
    result.isSuccess shouldBe true
    order.status shouldBe OrderStatus.PAID
    
    verify { inventory.reserve(order.items) }
    verify { eventPublisher.publish(ofType<OrderPaidEvent>()) }
}
```

### ❌ 저우선순위 또는 제외 대상

#### 1. 단순 위임 로직
```kotlin
// ❌ 테스트 불필요 - 단순 위임
class UserController(private val userService: UserService) {
    @GetMapping("/users/{id}")
    fun getUser(@PathVariable id: Long): UserResponse {
        return userService.getUser(id).toResponse()  // 단순 위임
    }
}

// ❌ 테스트 불필요 - 단순 데이터 변환
fun User.toResponse(): UserResponse = UserResponse(
    id = this.id,
    name = this.name,
    email = this.email
)
```

#### 2. 설정 및 인프라 코드
```kotlin
// ❌ 테스트 불필요 - 프레임워크 설정
@Configuration
class DatabaseConfig {
    @Bean
    fun dataSource(): DataSource = HikariDataSource()
}

// ❌ 테스트 불필요 - JPA Entity
@Entity
@Table(name = "users")
class UserEntity(
    @Id val id: Long,
    @Column val name: String,
    @Column val email: String
)
```

## 🎨 테스트 구현 패턴 (Test Implementation Patterns)

### ✅ 효과적인 픽스처 활용

#### 1. 도메인별 픽스처 패턴
```kotlin
// ✅ 좋은 예시 - 의미있는 픽스처
object InsuranceFixture {
    fun createHealthInsurance(
        premium: Money = Money.krw(50_000),
        coverage: Money = Money.krw(10_000_000),
        deductible: Money = Money.krw(200_000)
    ): HealthInsurance = HealthInsurance(
        id = InsuranceId.generate(),
        type = InsuranceType.HEALTH,
        monthlyPremium = premium,
        coverageAmount = coverage,
        deductible = deductible,
        effectiveDate = LocalDate.now(),
        expirationDate = LocalDate.now().plusYears(1)
    )
    
    fun createExpiredInsurance(): HealthInsurance = createHealthInsurance(
        premium = Money.krw(30_000)
    ).copy(expirationDate = LocalDate.now().minusDays(1))
    
    fun createHighRiskInsurance(): HealthInsurance = createHealthInsurance(
        premium = Money.krw(100_000),  // 높은 보험료
        deductible = Money.krw(500_000)  // 높은 공제액
    )
}

// ❌ 나쁜 예시 - 의미없는 픽스처
object BadFixture {
    fun create(): Insurance = Insurance(1L, "name", 1000)  // 의미 불명확
    fun create2(): Insurance = Insurance(2L, "name2", 2000)  // 차이점 불명확
}
```

#### 2. 테스트 시나리오별 데이터 준비
```kotlin
// ✅ 좋은 예시 - 시나리오 맞춤 데이터
class TaxDeductionCalculatorTest : StringSpec({
    
    "calculateMedicalDeduction - 연소득 5천만원 4인가족의 의료비 공제 계산" {
        // Given - 구체적인 시나리오 데이터
        val taxpayer = TaxpayerFixture.createMiddleIncome(
            annualIncome = Money.krw(50_000_000),
            dependents = 3  // 4인가족
        )
        val medicalExpenses = listOf(
            MedicalExpenseFixture.createHospitalVisit(amount = Money.krw(800_000)),
            MedicalExpenseFixture.createPharmacy(amount = Money.krw(200_000)),
            MedicalExpenseFixture.createDentalCare(amount = Money.krw(500_000))
        )
        
        // When
        val deduction = calculator.calculateMedicalDeduction(taxpayer, medicalExpenses)
        
        // Then
        // 총 의료비 150만원 - 소득의 3%(150만원) = 0원 (공제 한도 미달)
        deduction.deductibleAmount shouldBe Money.ZERO
        deduction.reason shouldBe DeductionResult.BELOW_MINIMUM_THRESHOLD
    }
    
    "calculateMedicalDeduction - 고소득자의 고액 의료비 공제 계산" {
        // Given - 다른 시나리오 데이터
        val taxpayer = TaxpayerFixture.createHighIncome(
            annualIncome = Money.krw(100_000_000),
            dependents = 1
        )
        val medicalExpenses = listOf(
            MedicalExpenseFixture.createSurgery(amount = Money.krw(10_000_000)),
            MedicalExpenseFixture.createRecovery(amount = Money.krw(2_000_000))
        )
        
        // When
        val deduction = calculator.calculateMedicalDeduction(taxpayer, medicalExpenses)
        
        // Then
        // 총 의료비 1200만원 - 소득의 3%(300만원) = 900만원 공제
        deduction.deductibleAmount shouldBe Money.krw(9_000_000)
        deduction.appliedExpenses should haveSize(2)
    }
})
```

### ✅ 예외 처리 테스트 패턴

#### 1. 비즈니스 예외 시나리오
```kotlin
// ✅ 좋은 예시 - 의미있는 예외 테스트
class LoanApplicationServiceTest : StringSpec({
    
    "applyForLoan - 신용등급 7등급 이하 시 LoanApplicationException 발생" {
        // Given
        val applicant = ApplicantFixture.createWithLowCredit(
            creditGrade = CreditGrade.GRADE_8,
            annualIncome = Money.krw(30_000_000)
        )
        val loanRequest = LoanRequestFixture.create(amount = Money.krw(10_000_000))
        
        // When & Then
        val exception = shouldThrow<LoanApplicationException> {
            service.applyForLoan(applicant, loanRequest)
        }
        
        exception.reason shouldBe LoanRejectionReason.INSUFFICIENT_CREDIT_RATING
        exception.minimumRequiredGrade shouldBe CreditGrade.GRADE_6
        exception.applicantGrade shouldBe CreditGrade.GRADE_8
    }
    
    "applyForLoan - 소득 대비 과도한 대출 신청 시 적절한 오류 메시지 포함" {
        // Given
        val applicant = ApplicantFixture.createWithGoodCredit(
            annualIncome = Money.krw(20_000_000)
        )
        val loanRequest = LoanRequestFixture.create(
            amount = Money.krw(100_000_000)  // 소득의 5배
        )
        
        // When & Then
        val exception = shouldThrow<LoanApplicationException> {
            service.applyForLoan(applicant, loanRequest)
        }
        
        exception.reason shouldBe LoanRejectionReason.EXCESSIVE_DEBT_TO_INCOME_RATIO
        exception.message should include("소득 대비 과도한 대출 금액")
        exception.maxAllowedAmount shouldBe Money.krw(60_000_000)  // 소득의 3배
    }
})
```

## 📋 테스트 품질 검증 기준

### 테스트 코드 품질 확인 목록

#### 테스트 구조
- [ ] Given-When-Then 패턴을 명확히 구분했는가? - High
- [ ] 각 테스트가 단일 시나리오만 검증하는가? - High
- [ ] 테스트 간 독립성을 보장했는가? - Mid

#### 테스트 명명
- [ ] 테스트 이름이 비즈니스 시나리오를 명확히 설명하는가? - High
- [ ] 한국어 명명 규칙을 일관되게 적용했는가? - High
- [ ] "[메서드명] - [상황] 시 [결과]" 패턴을 따랐는가? - Mid

#### 모킹 전략
- [ ] 필요한 의존성만 모킹했는가? - High
- [ ] 모킹 설정이 현실적이고 의미있는가? - High
- [ ] verify를 통해 상호작용을 적절히 검증했는가? - Mid
- [ ] 과도한 모킹(5개 이상)을 피했는가? - Low

#### 테스트 대상 선별
- [ ] 핵심 비즈니스 로직을 우선적으로 테스트했는가? - High
- [ ] 복잡한 상태 변경 로직을 테스트했는가? - High
- [ ] 단순 위임이나 설정 코드는 제외했는가? - Mid
- [ ] 중요한 예외 처리 시나리오를 포함했는가? - Mid

#### 검증 완성도
- [ ] 비즈니스 결과를 적절히 검증했는가? - High
- [ ] 예외 상황에 대한 적절한 검증을 했는가? - High
- [ ] 테스트 픽스처를 의미있게 활용했는가? - Mid

### 자동 검증 스크립트

```bash
#!/bin/bash
# unit-test-quality-check.sh

echo "🧪 단위 테스트 품질 검증 중..."

# 1. Given-When-Then 패턴 확인
echo "📊 Given-When-Then 패턴 검증..."
TOTAL_TESTS=$(find src/test -name "*Test.kt" -exec grep -c "\".*\" {" {} + | awk '{sum+=$1} END {print sum}')
GWT_TESTS=$(find src/test -name "*Test.kt" -exec grep -l "// Given\|// When\|// Then" {} \; | wc -l)

if [ "$TOTAL_TESTS" -gt 0 ]; then
    GWT_RATIO=$(echo "scale=2; $GWT_TESTS / $TOTAL_TESTS * 100" | bc)
    if (( $(echo "$GWT_RATIO < 80" | bc -l) )); then
        echo "⚠️ Given-When-Then 패턴 사용률 낮음: ${GWT_RATIO}% (권장: 80% 이상)"
    fi
fi

# 2. 한국어 테스트 이름 검증
echo "🇰🇷 한국어 테스트 명명 검증..."
KOREAN_TESTS=$(find src/test -name "*Test.kt" -exec grep -c "\"[^\"]*[가-힣][^\"]*\" {" {} + | awk '{sum+=$1} END {print sum}')
if [ "$TOTAL_TESTS" -gt 0 ]; then
    KOREAN_RATIO=$(echo "scale=2; $KOREAN_TESTS / $TOTAL_TESTS * 100" | bc)
    if (( $(echo "$KOREAN_RATIO < 70" | bc -l) )); then
        echo "⚠️ 한국어 테스트 이름 사용률 낮음: ${KOREAN_RATIO}% (권장: 70% 이상)"
    fi
fi

# 3. 과도한 모킹 검증
echo "🎭 모킹 사용 패턴 검증..."
find src/test -name "*Test.kt" | while read file; do
    MOCK_COUNT=$(grep -c "= mockk<" "$file")
    if [ "$MOCK_COUNT" -gt 5 ]; then
        echo "⚠️ 과도한 모킹 사용: $file (${MOCK_COUNT}개 모킹)"
    fi
done

# 4. 테스트 이름 품질 검증
echo "📝 테스트 이름 품질 검증..."
find src/test -name "*Test.kt" -exec grep -Hn "\"[^\"]*test[^\"]*\" {" {} \; | while read line; do
    echo "⚠️ 불명확한 테스트 이름: $line"
done

# 5. 검증 누락 확인
echo "✅ 검증 구문 확인..."
find src/test -name "*Test.kt" | while read file; do
    TEST_COUNT=$(grep -c "\".*\" {" "$file")
    ASSERTION_COUNT=$(grep -c "should\|verify" "$file")
    
    if [ "$TEST_COUNT" -gt 0 ] && [ "$ASSERTION_COUNT" -eq 0 ]; then
        echo "⚠️ 검증이 없는 테스트 파일: $file"
    fi
done

echo "✅ 단위 테스트 품질 검증 완료"
```

## 🎯 점수 기준

### 단위 테스트 품질 점수
코드리뷰 확인사항 항목을 토대로 점수를 구하고 비율은 아래와 같음:

High - 6점
Mid - 3점
Low - 1점

- **테스트 구조**: 30점
- **테스트 명명**: 25점
- **모킹 전략**: 20점
- **테스트 대상 선별**: 15점
- **검증 완성도**: 10점

**총점 100점 만점**
- 90-100점: 우수 (Excellent Testing)
- 80-89점: 양호 (Good Testing)
- 70-79점: 보통 (Average Testing)
- 70점 미만: 개선 필요 (Needs Improvement)

### 점수별 개선 방향

#### 90-100점 (우수)
- 현재 테스트 품질 유지
- 복잡한 엣지 케이스 추가 고려
- 테스트 문서화 역할 강화

#### 80-89점 (양호)
- Given-When-Then 패턴 일관성 개선
- 테스트 이름의 비즈니스 의미 강화
- 불필요한 모킹 제거

#### 70-79점 (보통)
- 테스트 구조 개선 (3단계 명확히 구분)
- 한국어 명명 규칙 적용
- 핵심 비즈니스 로직 테스트 추가

#### 70점 미만 (개선 필요)
- 기본 테스트 작성 패턴부터 학습
- Given-When-Then 구조 적용
- 의미있는 검증 추가

## 🚀 테스트 작성 가이드라인

### 모듈별 테스트 전략

#### Application 모듈 (고우선순위)
```kotlin
// ✅ 핵심 비즈니스 유스케이스 테스트
class InsuranceCalculationServiceTest : StringSpec({
    
    val rateProvider = mockk<InsuranceRateProvider>()
    val riskAssessor = mockk<RiskAssessmentService>()
    
    val service = InsuranceCalculationService(rateProvider, riskAssessor)
    
    beforeTest {
        clearMocks(rateProvider, riskAssessor)
    }
    
    "calculatePremium - 35세 비흡연 남성의 건강보험료 계산" {
        // Given
        val applicant = ApplicantFixture.createHealthy(
            age = 35,
            gender = Gender.MALE,
            smokingStatus = SmokingStatus.NON_SMOKER
        )
        val coverage = CoverageFixture.createStandard(amount = Money.krw(50_000_000))
        
        every { riskAssessor.assess(applicant) } returns RiskLevel.LOW
        every { rateProvider.getBaseRate(RiskLevel.LOW, coverage) } returns Rate(0.012)
        
        // When
        val premium = service.calculatePremium(applicant, coverage)
        
        // Then
        premium.monthlyAmount shouldBe Money.krw(50_000)
        premium.riskLevel shouldBe RiskLevel.LOW
        premium.discounts should contain(Discount.NON_SMOKER)
        
        verify { riskAssessor.assess(applicant) }
        verify { rateProvider.getBaseRate(RiskLevel.LOW, coverage) }
    }
})
```

#### API 모듈 (중간우선순위)
```kotlin
// ✅ 복잡한 Facade 로직 테스트
class InsuranceRecommendationFacadeTest : StringSpec({
    
    val calculationService = mockk<InsuranceCalculationService>()
    val comparisonService = mockk<InsuranceComparisonService>()
    val userService = mockk<UserService>()
    
    val facade = InsuranceRecommendationFacade(
        calculationService, 
        comparisonService, 
        userService
    )
    
    "recommendInsurance - 사용자 프로필 기반 최적 보험 추천" {
        // Given
        val userId = UserId(1L)
        val userProfile = UserProfileFixture.createYoungProfessional(
            age = 28,
            income = Money.krw(40_000_000),
            hasFamily = false
        )
        val availableProducts = InsuranceProductFixture.createVariety()
        
        every { userService.getUserProfile(userId) } returns userProfile
        every { calculationService.calculateAll(userProfile, availableProducts) } returns premiumResults
        every { comparisonService.rankByValue(premiumResults) } returns rankedProducts
        
        // When
        val recommendations = facade.recommendInsurance(userId)
        
        // Then
        recommendations should haveSize(3)
        recommendations.first().rank shouldBe 1
        recommendations.first().reasonCode shouldBe RecommendationReason.BEST_VALUE
        
        verify { userService.getUserProfile(userId) }
        verify { calculationService.calculateAll(userProfile, availableProducts) }
        verify { comparisonService.rankByValue(premiumResults) }
    }
})
```

## 📚 안티패턴 및 개선사항

### 피해야 할 테스트 패턴

#### 1. 과도한 세부사항 테스트
```kotlin
// ❌ 나쁜 예시 - 내부 구현 세부사항 테스트
"calculateTax - 내부 임시 변수 값 확인" {
    // 내부 구현에 과도하게 의존
}

// ✅ 좋은 예시 - 비즈니스 결과 테스트
"calculateTax - 연소득 5천만원 일반 납세자의 세액 계산 결과" {
    // 비즈니스 관점의 결과만 검증
}
```

#### 2. 테스트 간 의존성
```kotlin
// ❌ 나쁜 예시 - 테스트 간 의존성
var globalState: String = ""

"첫 번째 테스트" {
    globalState = "modified"
}

"두 번째 테스트 - globalState 의존" {
    globalState shouldBe "modified"  // 이전 테스트에 의존
}

// ✅ 좋은 예시 - 독립적인 테스트
"각각 독립적인 테스트" {
    val localState = createTestState()
    // 테스트 내에서 완결
}
```

#### 3. 의미없는 assertion
```kotlin
// ❌ 나쁜 예시 - 의미없는 검증
"test" {
    val result = service.method()
    result shouldNotBe null  // 의미없는 검증
}

// ✅ 좋은 예시 - 의미있는 검증
"calculateDiscount - VIP 고객 10% 할인 적용" {
    val result = service.calculateDiscount(vipCustomer, order)
    result.discountRate shouldBe 0.10
    result.appliedRule shouldBe DiscountRule.VIP_CUSTOMER
}
```

## 규칙 확인사항

### 테스트 구조
- [ ] Given-When-Then 패턴을 명확히 구분했는지 확인 - High
- [ ] 각 테스트가 단일 시나리오만 검증하는지 확인 - High
- [ ] 테스트 간 독립성을 보장했는지 확인 - Mid

### 테스트 명명
- [ ] 테스트 이름이 비즈니스 시나리오를 명확히 설명하는지 확인 - High
- [ ] 한국어 명명 규칙을 일관되게 적용했는지 확인 - High
- [ ] "[메서드명] - [상황] 시 [결과]" 패턴을 따랐는지 확인 - Mid

### 모킹 전략
- [ ] 필요한 의존성만 모킹했는지 확인 - High
- [ ] 모킹 설정이 현실적이고 의미있는지 확인 - High
- [ ] verify를 통해 상호작용을 적절히 검증했는지 확인 - Mid
- [ ] 과도한 모킹(5개 이상)을 피했는지 확인 - Low

### 테스트 대상 선별
- [ ] 핵심 비즈니스 로직을 우선적으로 테스트했는지 확인 - High
- [ ] 복잡한 상태 변경 로직을 테스트했는지 확인 - High
- [ ] 단순 위임이나 설정 코드는 제외했는지 확인 - Mid
- [ ] 중요한 예외 처리 시나리오를 포함했는지 확인 - Mid

### 검증 완성도
- [ ] 비즈니스 결과를 적절히 검증했는지 확인 - High
- [ ] 예외 상황에 대한 적절한 검증을 했는지 확인 - High
- [ ] 테스트 픽스처를 의미있게 활용했는지 확인 - Mid

### 안티패턴 방지
- [ ] 과도한 세부사항 테스트를 피했는지 확인 - Mid
- [ ] 테스트 간 의존성이 없는지 확인 - Mid
- [ ] 의미없는 assertion을 피했는지 확인 - Mid

### 점수
규칙 확인사항 항목을 토대로 점수를 구하고 비율은 아래와 같음
High - 6
Mid - 3
Low - 1

- **테스트 구조**: 15점
- **테스트 명명**: 15점
- **모킹 전략**: 19점
- **테스트 대상 선별**: 18점
- **검증 완성도**: 15점
- **안티패턴 방지**: 9점

**총점 91점** (100점 기준으로 환산)
- 82-91점: 우수 (Excellent Testing) - 90-100점
- 73-81점: 양호 (Good Testing) - 80-89점
- 64-72점: 보통 (Average Testing) - 70-79점
- 64점 미만: 개선 필요 (Needs Improvement) - 70점 미만