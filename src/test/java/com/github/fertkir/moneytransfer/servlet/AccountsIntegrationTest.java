package com.github.fertkir.moneytransfer.servlet;

import com.github.fertkir.moneytransfer.ApplicationMain;
import com.google.inject.Injector;
import io.restassured.RestAssured;
import org.h2.tools.RunScript;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.sql.DataSource;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;

import static io.restassured.RestAssured.*;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.empty;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AccountsIntegrationTest {

    private static final Injector injector = ApplicationMain.getInjector();

    private static Integer ID_1;
    private static Integer ID_2;

    @BeforeClass
    public static void setUp() throws Exception {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        ApplicationMain.run();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        cleanDatabase();
        ApplicationMain.stopJetty();
    }

    @Test
    public void _01_shouldReturnEmptyAccountsList() {
        get("/accounts")
                .then()
                .statusCode(200)
                .assertThat()
                .body("", empty());
    }

    @Test
    public void _02_shouldCreateAnAccountAndReturnItsData() {
        ID_1 = post("/accounts")
                .then()
                .statusCode(201)
                .assertThat()
                .body("balance", equalTo(0))
                .extract().path("id");
    }

    @Test
    public void _03_shouldCreateAnotherAccountAndReturnItsData() {
        ID_2 = post("/accounts")
                .then()
                .statusCode(201)
                .assertThat()
                .body("balance", equalTo(0))
                .extract().path("id");
    }

    @Test
    public void _04_shouldReturnFirstAccount() {
        get(format("/accounts?accountId=%d", ID_1))
                .then()
                .statusCode(200)
                .assertThat()
                .body("id", equalTo(ID_1))
                .body("balance", equalTo(0));
    }

    @Test
    public void _05_shouldTopUpFirstAccount() {
        put(format("/accounts/topup?accountId=%d&amount=1000", ID_1))
                .then()
                .statusCode(200)
                .assertThat()
                .body("id", equalTo(ID_1))
                .body("balance", equalTo(1000));
    }

    @Test
    public void _06_shouldReturnAllAccounts() {
        get("/accounts")
                .then()
                .statusCode(200)
                .assertThat()
                .body("[0].id", equalTo(ID_1))
                .body("[0].balance", equalTo(1000))
                .body("[1].id", equalTo(ID_2))
                .body("[1].balance", equalTo(0));
    }

    @Test
    public void _07_shouldWithdrawFromFirstAccount() {
        put(format("/accounts/withdraw?accountId=%d&amount=100", ID_1))
                .then()
                .statusCode(200)
                .assertThat()
                .body("id", equalTo(ID_1))
                .body("balance", equalTo(900));
    }

    @Test
    public void _08_shouldTransferMoneyFromFirstAccountToSecondOne() {
        put(format("/accounts/transfer?from=%d&to=%d&amount=200", ID_1, ID_2))
                .then()
                .statusCode(200)
                .assertThat()
                .body("source.id", equalTo(ID_1))
                .body("source.balance", equalTo(700))
                .body("target.id", equalTo(ID_2))
                .body("target.balance", equalTo(200));
    }

    @Test
    public void _09_shouldNotTransferWhenNotEnoughMoney() {
        put(format("/accounts/transfer?from=%d&to=%d&amount=1000", ID_1, ID_2))
                .then()
                .statusCode(409)
                .assertThat()
                .body("message", equalTo("Cannot transfer 1000. Not enough money"));
    }

    @Test
    public void _10_shouldNotWithdrawWhenNotEnoughMoney() {
        put(format("/accounts/withdraw?accountId=%d&amount=201", ID_2))
                .then()
                .statusCode(409)
                .assertThat()
                .body("message", equalTo("Cannot withdraw 201. Not enough money"));
    }

    private static void cleanDatabase() throws SQLException {
        DataSource dataSource = injector.getInstance(DataSource.class);
        try (Connection connection = dataSource.getConnection()) {
            RunScript.execute(connection, new InputStreamReader(AccountsIntegrationTest.class.getClassLoader()
                    .getResourceAsStream("clean.sql")));
        }
    }
}
