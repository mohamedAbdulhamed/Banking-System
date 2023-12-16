# Banking System

## Requirements
OOP - Functional Programming - MySQL (حفظ واسترجاع وتعديل البيانات) - Actor System (Akka)

## Classes
- Customer
- Account
- Transaction
  
| CLASS         | **C** | **R** | **U** | **D** |
|---------------|:-----:|:-----:|:-----:|:-----:|
| _Customer_    | ✓     | ✓     | ✓     | ✓     |
| _Account_     | ✓     | ✓     | ✓     | ✓     |
| _Transaction_ | ✓     | ✓     |       |       |

## Features
- Creating and managing accounts, customers and transactions.
- Deposit and withdraw money from accounts.
- Transfer money between accounts.
- Allow customers to view their accounts information => (account balance, resent transactions, other details).

## Data
* Customer
  * ID (Int)
  * Name (String)
  * Address (String)
* Account
  * ID (Int)
  * Balance (Double)
  * Customer (Customer)
* Transaction
  * ID (Int)
  * Amount (Double)
  * From (Account)
  * To (Account)
  * Type (String)
  * Date (Date)
