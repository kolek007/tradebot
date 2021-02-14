# Tradebot

## Project Setup
- Install Intellij IDEA Community Edition https://www.jetbrains.com/ru-ru/idea/download/
- Install JDK 8 https://www.oracle.com/ru/java/technologies/javase/javase-jdk8-downloads.html
- Install Git https://git-scm.com/downloads
- Create GitHub Account https://github.com/
- Create folder "tradebot"
- Navigate to created folder and open Git Bash
- Run command "git clone https://github.com/kolek007/tradebot.git"
- Open IDEA, create new project from existing sources (choose tradebot/build.gradle file)

## Run
To run tradebot you need to prepare config file with bot properties like in the following example
```json
{
  "broker": {
    "name": "Tinkoff",
    "ssoToken": "token",
    "sandboxSsoToken": "token",
    "sandbox": true
  },
  "strategies":[
    {
      "name": "Absorption",
      "tickers": "AAPL",
      "intervals": "MIN_1",
      "wallet": {
        "amount": 1000
      }
    }
  ]
}
```
Strategy name should correspond to name described in annotation [Strategy](https://github.com/kolek007/tradebot/blob/main/src/main/java/org/nl/bot/api/annotations/Strategy.java) near the method responsible for strategy creation in [StrategiesFactory](https://github.com/kolek007/tradebot/blob/main/src/main/java/org/nl/bot/api/strategies/StrategiesFactory.java).
Example of how to define Strategy creation method:
```java
@Strategy(name = "NameOfStrategy")
public SomeStrategy createSomeStrategy(@Nonnull String[] ticker, @Nonnull Interval[] interval, @Nonnull Wallet wallet, @Nonnull BrokerAdapter adapter) {
    return new SomeStrategy(ticker, interval, adapter, wallet);
}
```
> Signature of the method should be the same as in the example bellow 

As of now broker name means nothing - Sandbox Adapter will be used.

## Tinkoff API   
Learn about tinkoff api here https://github.com/TinkoffCreditSystems/invest-openapi-java-sdk.git