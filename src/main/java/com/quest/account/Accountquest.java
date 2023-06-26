package main.java.com.quest.account.accountquest.java;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Random;

@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accountNumber;

    private String userId;

    private BigDecimal balance;

    private LocalDateTime createdDate;

    private String status;

    private LocalDateTime terminationDate;

    public Account() {
        this.createdDate = LocalDateTime.now();
    }

    public void setStatus(String string) {
    }

    public BigDecimal getBalance() {
        return null;
    }

    public void setTerminationDate(LocalDateTime now) {
    }

    public String getTerminationDate() {
        return null;
    }

    public void setAccountNumber(String accountNumber2) {
    }

    public void setUserId(Object userId2) {
    }

    public void setBalance(Object initialBalance) {
    }

    public Object getStatus() {
        return null;
    }

    public Object getId() {
        return null;
    }

    public Object getAccountNumber() {
        return null;
    }

    public Object getCreatedDate() {
        return null;
    }

}

public class CreateAccountRequest {
    private String userId;

    private BigDecimal initialBalance;

    public Object getUserId() {
        return null;
    }

    public Object getInitialBalance() {
        return null;
    }

}

public class AccountResponse {
    private Long id;

    private String accountNumber;

    private BigDecimal balance;

    private LocalDateTime createdDate;

    private String status;

    private LocalDateTime terminationDate;

    public void setId(Object id2) {
    }

    public void setAccountNumber(Object accountNumber2) {
    }

    public void setBalance(BigDecimal balance2) {
    }

    public void setStatus(Object status2) {
    }

    public void setTerminationDate(String terminationDate2) {
    }

}

public class TerminateAccountRequest {
    private String userId;

    private String accountNumber;

    public String getUserId() {
        return null;
    }

    public String getAccountNumber() {
        return null;
    }

}

@Service
public class AccountService {
    private static final int ACCOUNT_NUMBER_LENGTH = 10;

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public AccountResponse createAccount(CreateAccountRequest request) {
        // 사용자 확인
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        // 계좌 생성
        Account account = new Account();
        account.setUserId(request.getUserId());
        account.setBalance(request.getInitialBalance());
        account.setStatus("Active");

        String accountNumber = generateAccountNumber();
        while (accountRepository.existsByAccountNumber(accountNumber)) {
            accountNumber = generateAccountNumber();
        }
        account.setAccountNumber(accountNumber);

        Account savedAccount = accountRepository.save(account);

        return convertToAccountResponse(savedAccount);
    }

    public String terminateAccount(TerminateAccountRequest request) {

        if (request.getUserId() == null) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        Account account = accountRepository.findByUserIdAndAccountNumber(request.getUserId(),
                request.getAccountNumber());

        if (account == null) {
            return "실패: 해당 사용자의 계좌가 존재하지 않습니다.";
        }

        if (account.getStatus().equals("Terminated")) {
            return "실패: 이미 계좌가 해지된 상태입니다.";
        }

        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            return "실패: 계좌에 잔액이 남아있어 해지할 수 없습니다.";
        }

        account.setStatus("Terminated");
        account.setTerminationDate(LocalDateTime.now());
        accountRepository.save(account);

        return "성공: " + request.getUserId() + ", " + request.getAccountNumber() + ", " + account.getTerminationDate();
    }

    public AccountResponse getAccount(Long id) {
        Optional<Account> account = accountRepository.findById(id);

        if (account.isPresent()) {
            return convertToAccountResponse(account.get());
        }

        throw new NoSuchElementException("Account not found");
    }

    private AccountResponse convertToAccountResponse(Account account) {
        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setBalance(account.getBalance());
        response.setCreatedDate(account.getCreatedDate());
        response.setStatus(account.getStatus());
        response.setTerminationDate(account.getTerminationDate());
        return response;
    }

    private String generateAccountNumber() {
        StringBuilder sb = new StringBuilder(ACCOUNT_NUMBER_LENGTH);
        Random random = new Random();
        for (int i = 0; i < ACCOUNT_NUMBER_LENGTH; i++) {
            int digit = random.nextInt(10); // Generate random number from 0 to 9
            sb.append(digit);
        }
        return sb.toString();
    }
}

public class TransactionManager {
    public static void main(String[] args) {
        // 사용자 아이디, 계좌 번호, 거래 금액
        String userId = "user123";
        String accountNumber = "1234567890";
        double transactionAmount = 10000;
        TransactionResult result = useBalance(userId, accountNumber, transactionAmount);

        // 결과 출력
        if (result.isSuccessful()) {
            System.out.println("거래 성공");
            System.out.println("계좌번호: " + result.getAccountNumber());
            System.out.println("거래 결과: " + result.getTransactionResult());
            System.out.println("거래 ID: " + result.getTransactionId());
            System.out.println("거래금액: " + result.getTransactionAmount());
            System.out.println("거래일시: " + result.getTransactionDate());
        } else {
            System.out.println("거래 실패");
        }
    }

    public static TransactionResult useBalance(String userId, String accountNumber, double transactionAmount) {
        // 사용자가 존재
        boolean userExist = checkUserExist(userId);
        if (!userExist) {
            return new TransactionResult(false, "사용자가 존재하지 않습니다.", accountNumber, accountNumber, transactionAmount,
                    accountNumber);
        }

        // 계좌 소유주가 사용자와 일치 여부 체크
        boolean accountOwnerMatch = checkAccountOwner(userId, accountNumber);
        if (!accountOwnerMatch) {
            return new TransactionResult(false, "계좌 소유주와 사용자가 일치하지 않습니다.");
        }

        // 계좌 해지 여부 확인
        boolean accountActive = checkAccountActive(accountNumber);
        if (!accountActive) {
            return new TransactionResult(false, "해지된 계좌입니다.", accountNumber, accountNumber, transactionAmount,
                    accountNumber);
        }

        // 잔액 확인
        double currentBalance = getCurrentBalance(accountNumber);
        if (transactionAmount < 0 || transactionAmount > currentBalance) {
            return new TransactionResult(false, "거래 금액이 유효하지 않습니다.", accountNumber, accountNumber, currentBalance,
                    accountNumber);
        }

        // 거래 수행
        performTransaction(userId, accountNumber, transactionAmount);

        // 거래 결과
        return new TransactionResult(true, accountNumber, "성공", "transaction123", transactionAmount,
                "2022-01-01 10:00:00");
    }

    // 사용자 존재 확인 함수
    public static boolean checkUserExist(String userId) {

        return true;
    }

    // 소유주 일치 확인 함수
    public static boolean checkAccountOwner(String userId, String accountNumber) {

        return true;
    }

    // 계좌 해지 여부 확인
    public static boolean checkAccountActive(String accountNumber) {

        return true;
    }

    // 잔액 반환 함수
    public static double getCurrentBalance(String accountNumber) {

        return 50000;
    }

    public static void performTransaction(String userId, String accountNumber, double transactionAmount) {

    }
}

public class TransactionResult {
    private boolean successful;
    private String accountNumber;
    private String transactionResult;
    private String transactionId;
    private double transactionAmount;
    private String transactionDate;

    public TransactionResult(boolean successful, String accountNumber, String transactionResult, String transactionId,
            double transactionAmount, String transactionDate) {
        this.successful = successful;
        this.accountNumber = accountNumber;
        this.transactionResult = transactionResult;
        this.transactionId = transactionId;
        this.transactionAmount = transactionAmount;
        this.transactionDate = transactionDate;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getTransactionResult() {
        return transactionResult;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public double getTransactionAmount() {
        return transactionAmount;
    }

    public String getTransactionDate() {
        return transactionDate;
    }
}

@RestController
@RequestMapping("/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@RequestBody CreateAccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/terminate")
    public ResponseEntity<String> terminateAccount(@RequestBody TerminateAccountRequest request) {
        String result = accountService.terminateAccount(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable Long id) {
        AccountResponse response = accountService.getAccount(id);
        return ResponseEntity.ok(response);
    }

}

@SpringBootApplication
public class AccountSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(AccountSystemApplication.class, args);
    }
}

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByAccountNumber(String accountNumber);

    Optional<Account> findById(Long id);

    Account save(Account account);

    Account findByUserIdAndAccountNumber(String userId, String accountNumber);
}
