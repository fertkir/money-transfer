# [Money transferring system](https://drive.google.com/file/d/19IM0HmgQO_Tggke8wh7N764l67JFwMnq/view)

## How to build

1. Make sure your default java is of 8 version.
2. Run `./gradlew clean build`

## How to run

`java -jar build/libs/money-transfer-1.0.0.jar`

## API usage

#### Create a new account
##### Request:
```
POST /accounts HTTP/1.1
Host: localhost:8080
```
##### Response:
```
HTTP/1.1 201 Created
Content-Type: application/json
Content-Length: 21

{
  "id": 1,
  "balance": 0
}
```

#### Getting account data
##### Request:
```
GET /accounts?accountId=1 HTTP/1.1
Host: localhost:8080
```
##### Response:
```
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 21

{
  "id": 1,
  "balance": 0
}
```

#### Getting all accounts data
##### Request:
```
GET /accounts HTTP/1.1
Host: localhost:8080
```
##### Response:
```
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 44

[
  {
    "id": 1,
    "balance": 0
  },
  {
    "id": 2,
    "balance": 0
  }
]
```

#### Top up the account
##### Request:
```
PUT /accounts/topup?accountId=1&amount=1000
Host: localhost:8080
```
##### Response:
```
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 24

{
  "id": 1,
  "balance": 1000
}
```

#### Withdraw from the account
##### Request:
```
PUT /accounts/withdraw?accountId=1&amount=100
Host: localhost:8080
```
##### Response:
```
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 23

{
  "id": 1,
  "balance": 900
}
```

#### Transfer money from one account to another
##### Request:
```
PUT /accounts/transfer?from=1&to=2&amount=100
Host: localhost:8080
```
##### Response:
```
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 66

{
  "source": {
    "id": 1,
    "balance": 800
  },
  "target": {
    "id": 2,
    "balance": 100
  }
}
```