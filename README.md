# DIY_Trading_Advisor2
New DIY Trading Advisor done in Java and running in AWS Lambda this time. It will regularly scan your email inbox for stock trading notifications, parse them and then update your portfolio tracking website to constantly and automatically keep your portfolio upto date with your trades.

##Project Breakdown 
 1. Query emails from a [JMap REST API](https://jmap.io/spec-mail.html) email source (filtered by subject and from address). 
 2. If any trades are found, it will parse relevant trading data out of the email bodies (Fidelity and Schwab at this point)
 3. Trades are then persisted on your portfolio tracking website ([Simplevisor](https://simplevisor.com/) in this case) using a REST API
