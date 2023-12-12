package `com.Banking.System`.BankingSystem

import DatabaseConnection._

import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask

import scala.concurrent.Await
import akka.util.Timeout
import scala.concurrent.duration._

import java.util.Date
import scala.concurrent.{Future, ExecutionContext}
import scala.language.postfixOps


// Customer class
case class Customer(id: Int, name: String, address: String)

// Account class
case class Account(id: Int, customer: Customer, balance: Double)

// Transaction class
case class Transaction(id: Option[Int], sourceAccount: Account, destinationAccount: Account, amount: Double, date: Date = new Date(), transactionType: String)

// Banking Services class
class BankingServices extends Actor {

    def receive: Receive = {
        case CreateCustomer(name, address) =>
            val customer = createCustomer(name, address)
            sender() ! customer

        case CreateAccount(customer, initialBalance) =>
            var account = createAccount(customer, initialBalance)
            sender() ! account

        case Deposit(accountId, amount) =>
            val account = getAccount(accountId).get
            if (validateAmount(amount)) {
                val updatedAccount = account.copy(balance = account.balance + amount)
                val isSuccess = deposit(accountId, amount)
                if (isSuccess) {
                    val transaction = createTransaction(Transaction(None, updatedAccount, updatedAccount, amount, new Date(), "Deposit"))
                    sender() ! transaction
                } else {
                    sender() ! "Error depositing funds!"
                }                
            } else {
                sender() ! "Invalid amount!"
            }

        case Withdraw(accountId, amount) =>
            val account = getAccount(accountId).get
            if (validateAmount(amount) && account.balance >= amount) {
                val updatedAccount = account.copy(balance = account.balance - amount)
                val isSuccess = withdraw(accountId, amount)
                if (isSuccess) {
                    val transaction = createTransaction(Transaction(None, updatedAccount, updatedAccount, amount, new Date(), "Withdrawal"))
                    sender() ! transaction
                } else {
                    sender() ! "Error withdrawing funds!"
                }
            } else {
                sender() ! "Insufficient funds for withdrawal or invalid amount!"
            }

        case Transfer(sourceAccountId, destinationAccountId, amount) =>
            val sourceAccount = getAccount(sourceAccountId).get
            val destinationAccount = getAccount(destinationAccountId).get

            if (validateAmount(amount) && sourceAccount.balance >= amount) {
                val isSuccess = transfer(sourceAccountId, destinationAccountId, amount)
                if (isSuccess) {
                    val updatedSourceAccount = sourceAccount.copy(balance = sourceAccount.balance - amount)
                    val updatedDestinationAccount = destinationAccount.copy(balance = destinationAccount.balance + amount)
                    val transaction = createTransaction(Transaction(None, updatedSourceAccount, updatedDestinationAccount, amount, new Date(), "Transfer"))
                    sender() ! transaction
                } else {
                    sender() ! "Error transferring funds!"
                }
            } else {
                sender() ! "Insufficient funds for transfer or invalid amount!"
            }

        case GetAccountDetails(accountId) =>
            val account = getAccount(accountId).get
            sender() ! account

        case GetRecentTransactions(accountId, count:Int) =>
            val transactions: List[Transaction] = getRecentTransactions(accountId, count)
            if (!transactions.isEmpty) {
                sender() ! transactions
            } else {
                sender() ! s"Account $accountId not found or no transactions found for that account."
            }
    }

    private def returnNotFound(accountType: String): String = {
        s"Account $accountType not found."
    }

    private def validateAmount(amount: Double): Boolean = {
        amount > 0 && amount < 10000
    }


}

// Messages
case class CreateCustomer(name: String, address: String)
case class CreateAccount(customer: Customer, initialBalance: Double)
case class Deposit(accountId: Int, amount: Double)
case class Withdraw(accountId: Int, amount: Double)
case class Transfer(sourceAccountId: Int, destinationAccountId: Int, amount: Double)
case class GetAccountDetails(accountId: Int)
case class GetRecentTransactions(accountNumber: Int, count: Int)

 
object BankingSystemApp extends App {
    val system = ActorSystem("BankingSystem")
    val bankingServices = system.actorOf(Props[BankingServices], "bankingServices")

    implicit val timeout: Timeout = 5.seconds

    // Create customer
    val createCustomerMsg = CreateCustomer("John Doe", "123 Main St")
    val createdCustomer = Await.result(ask(bankingServices, createCustomerMsg).mapTo[Customer], timeout.duration)

    println(s"Created Customer: $createdCustomer")

    // Create account
    val createAccountMsg = CreateAccount(createdCustomer, 1000.0)
    val createdAccount = Await.result(ask(bankingServices, createAccountMsg).mapTo[Account], timeout.duration)

    println(s"Created Account: $createdAccount")

    // Make a deposit
    val depositMsg = Deposit(createdAccount.id, 500.0)
    val depositResult = Await.result(ask(bankingServices, depositMsg).mapTo[Transaction], timeout.duration) // transaction

    if (depositResult.isInstanceOf[Transaction]) {
        println(s"Account Balance after deposit: ${depositResult.sourceAccount.balance}")
    } else {
        println(depositResult) // error message
    }

    // Make a withdrawal
    val withdrawMsg = Withdraw(createdAccount.id, 200.0)
    val withdrawResult = Await.result(ask(bankingServices, withdrawMsg).mapTo[Transaction], timeout.duration)

    if (withdrawResult.isInstanceOf[Transaction]) {
        println(s"Account Balance after withdrawal: ${withdrawResult.sourceAccount.balance}")
    } else {
        println(withdrawResult) // error message
    }

    // Make a transfer
    // Create destination account (new account for transfer)
    val createDestinationCustomerMsg = CreateCustomer("Jane Doe", "456 Main St")
    val destinationCustomer = Await.result(ask(bankingServices, createDestinationCustomerMsg).mapTo[Customer], timeout.duration)

    println(s"Created Destination Customer: $destinationCustomer")

    val createDestinationAccountMsg = CreateAccount(destinationCustomer, 500.0)
    val destinationAccount = Await.result(ask(bankingServices, createDestinationAccountMsg).mapTo[Account], timeout.duration)

    println(s"Created Destination Account: $destinationAccount")

    // Make transfer
    val transferMsg = Transfer(createdAccount.id, destinationAccount.id, 300.0)
    val transferResult = Await.result(ask(bankingServices, transferMsg).mapTo[Transaction], timeout.duration)

    if (transferResult.isInstanceOf[Transaction]) {
        println(s"Source Account Balance after transfer: ${transferResult.sourceAccount.balance}")
        println(s"Destination Account Balance after transfer: ${transferResult.destinationAccount.balance}")
    } else {
        println(transferResult) // error message
    }
    
    val accountDetailsMsg = GetAccountDetails(createdAccount.id)
    val accountDetailsFuture: Future[Any] = ask(bankingServices, accountDetailsMsg)
    val accountDetailsResult: Any = Await.result(accountDetailsFuture, timeout.duration)

    accountDetailsResult match {
        case account: Account =>
            println(s"Account Details: $account")
        case errorMsg: String =>
            println(s"Error getting account details: $errorMsg")
        case _ =>
            println("Unexpected result type.")
            // print what type is it
            println(accountDetailsResult.getClass)
    }

    val recentTransactionsMsg = GetRecentTransactions(createdAccount.id, 5)
    val recentTransactions = Await.result(ask(bankingServices, recentTransactionsMsg).mapTo[List[Transaction]], timeout.duration)

    println(s"Recent Transactions: $recentTransactions")

    system.terminate()
}
