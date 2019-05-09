package com.github.fertkir.moneytransfer.servlet;

import com.github.fertkir.moneytransfer.ApplicationMain;
import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static io.restassured.RestAssured.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.empty;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AccountsIntegrationTest {

    @BeforeClass
    public static void runApp() throws Exception {
        ApplicationMain.main(new String[]{});
    }

    @Before
    public void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
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
        post("/accounts")
                .then()
                .statusCode(201)
                .assertThat()
                .body("id", equalTo(1))
                .body("balance", equalTo(0));
    }

    @Test
    public void _03_shouldCreateAnotherAccountAndReturnItsData() {
        post("/accounts")
                .then()
                .statusCode(201)
                .assertThat()
                .body("id", equalTo(2))
                .body("balance", equalTo(0));
    }

    @Test
    public void _04_shouldReturnFirstAccount() {
        get("/accounts?accountId=1")
                .then()
                .statusCode(200)
                .assertThat()
                .body("id", equalTo(1))
                .body("balance", equalTo(0));
    }

    @Test
    public void _05_shouldTopUpFirstAccount() {
        put("/accounts/topup?accountId=1&amount=1000")
                .then()
                .statusCode(200)
                .assertThat()
                .body("id", equalTo(1))
                .body("balance", equalTo(1000));
    }

    @Test
    public void _06_shouldReturnAllAccounts() {
        get("/accounts")
                .then()
                .statusCode(200)
                .assertThat()
                .body("[0].id", equalTo(1))
                .body("[0].balance", equalTo(1000))
                .body("[1].id", equalTo(2))
                .body("[1].balance", equalTo(0));
    }

    @Test
    public void _07_shouldWithdrawFromFirstAccount() {
        put("/accounts/withdraw?accountId=1&amount=100")
                .then()
                .statusCode(200)
                .assertThat()
                .body("id", equalTo(1))
                .body("balance", equalTo(900));
    }

    @Test
    public void _08_shouldTransferMoneyFromFirstAccountToSecondOne() {
        put("/accounts/transfer?from=1&to=2&amount=200")
                .then()
                .statusCode(200)
                .assertThat()
                .body("source.id", equalTo(1))
                .body("source.balance", equalTo(700))
                .body("target.id", equalTo(2))
                .body("target.balance", equalTo(200));
    }

    @Test
    public void _09_shouldNotTransferWhenNotEnoughMoney() {
        put("/accounts/transfer?from=1&to=2&amount=1000")
                .then()
                .statusCode(409)
                .assertThat()
                .body("message", equalTo("Cannot transfer 1000. Not enough money"));
    }

    @Test
    public void _10_shouldNotWithdrawWhenNotEnoughMoney() {
        put("/accounts/withdraw?accountId=2&amount=201")
                .then()
                .statusCode(409)
                .assertThat()
                .body("message", equalTo("Cannot withdraw 201. Not enough money"));
    }
}
