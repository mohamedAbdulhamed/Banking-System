package `com.Banking.System`.BankingSystem

import java.util.Date

// Customer case class
case class Customer(id: Int, name: String, address: String)

// Account case class
case class Account(id: Int, customer: Customer, balance: Double)

// TransactionType sealed trait
sealed trait TransactionType

case object Deposit extends TransactionType
case object Withdrawal extends TransactionType
case object Transfer extends TransactionType

// companion
object TransactionType {
    def fromString(str: String): TransactionType = str match {
        case "Deposit" => Deposit
        case "Withdrawal" => Withdrawal
        case "Transfer" => Transfer
        case _ => throw new IllegalArgumentException(s"Unknown transaction type: ${str}")
    }
}

// Transaction case class
case class Transaction(id: Option[Int], sourceAccountId: Option[Int], destinationAccountId: Option[Int], amount: Double, date: Date = new Date(), transactionType: TransactionType)


// Messages (used to communicate between actors)
// BankingServices messages
case class CreateCustomer(name: String, address: String)
case class CreateAccount(customer: Customer, initialBalance: Double)
case class UpdateCustomer(customer: Customer)
case class Deposit(accountId: Int, amount: Double)
case class Withdraw(accountId: Int, amount: Double)
case class Transfer(sourceAccountId: Int, destinationAccountId: Int, amount: Double)
case class GetAccountDetailsById(accountId: Int)
case class GetAccountDetailsByCustomer(customer: Customer)
case class GetRecentTransactions(accountId: Int, count: Int)
