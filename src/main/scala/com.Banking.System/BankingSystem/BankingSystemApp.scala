package `com.Banking.System`.BankingSystem

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask

import scala.concurrent.Await
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.{Future, ExecutionContext}
import scala.language.postfixOps // disable warnings about postfix ops
import _root_.com.typesafe.scalalogging.Logger


object BankingSystemApp extends App {
    private val logger = Logger(getClass.getName())

    val system = ActorSystem("BankingSystem")
    val bankingServices = system.actorOf(Props[BankingServices], "bankingServices") //bankingServicesActor

    implicit val timeout: Timeout = 5.seconds

    // Create customer
    val createCustomerMsg = CreateCustomer("John Doe", "123 Main St")
    val createdCustomer = Await.result(ask(bankingServices, createCustomerMsg).mapTo[Customer], timeout.duration)

    logger.info(s"Created Customer: $createdCustomer")

    // Update customer
    val updateCustomerMsg = UpdateCustomer(createdCustomer.copy(address = "456 Main St")) // address updated
    val updatedCustomer = Await.result(ask(bankingServices, updateCustomerMsg).mapTo[Customer], timeout.duration)

    logger.info(s"Created Customer after his information has been updated: $updatedCustomer")

    // Create account
    val createAccountMsg = CreateAccount(createdCustomer, 1000.0)
    val createdAccount = Await.result(ask(bankingServices, createAccountMsg).mapTo[Account], timeout.duration)

    logger.info(s"Created Account: $createdAccount")

    // Make a deposit
    val depositMsg = Deposit(createdAccount.id, 500.0)
    val depositFuture: Future[Any] = ask(bankingServices, depositMsg)
    val depositResult: Any = Await.result(depositFuture, timeout.duration)

    depositResult match {
        case transaction: Transaction =>
            logger.info(s"Account Balance after deposit: ${transaction.sourceAccount.balance}")
        case errorMsg: String =>
            logger.error(s"Error making deposit: $errorMsg")
        case _ =>
            logger.error(s"Unexpected result type: ${depositResult.getClass}")
    }

    // Make a withdrawal
    val withdrawMsg = Withdraw(createdAccount.id, 200.0)
    val withdrawFuture: Future[Any] = ask(bankingServices, withdrawMsg)
    val withdrawResult: Any = Await.result(withdrawFuture, timeout.duration)

    withdrawResult match {
        case transaction: Transaction =>
            logger.info(s"Account Balance after withdrawal: ${transaction.sourceAccount.balance}")
        case errorMsg: String =>
            logger.error(s"Error making withdrawal: $errorMsg")
        case _ =>
            logger.error(s"Unexpected result type: ${withdrawResult.getClass}")
    }

    // Create destination account (new account for transfer)
    val createDestinationCustomerMsg = CreateCustomer("Jane Doe", "456 Main St")
    val destinationCustomer = Await.result(ask(bankingServices, createDestinationCustomerMsg).mapTo[Customer], timeout.duration)

    logger.info(s"Created Destination Customer: $destinationCustomer")

    val createDestinationAccountMsg = CreateAccount(destinationCustomer, 500.0)
    val destinationAccount = Await.result(ask(bankingServices, createDestinationAccountMsg).mapTo[Account], timeout.duration)

    logger.info(s"Created Destination Account: $destinationAccount")

    // Make a transfer
    val transferMsg = Transfer(createdAccount.id, destinationAccount.id, 300.0)
    val transferFuture: Future[Any] = ask(bankingServices, transferMsg)
    val transferResult: Any = Await.result(transferFuture, timeout.duration)

    transferResult match {
        case transaction: Transaction =>
            logger.info(s"Source Account Balance after transfer: ${transaction.sourceAccount.balance}")
            logger.info(s"Destination Account Balance after transfer: ${transaction.destinationAccount.balance}")
        case errorMsg: String =>
            logger.error(s"Error making transfer: $errorMsg")
        case _ =>
            logger.error(s"Unexpected result type: ${transferResult.getClass}")
    }

    // Get account details
    val accountDetailsMsg = GetAccountDetailsById(createdAccount.id)
    val accountDetailsFuture: Future[Any] = ask(bankingServices, accountDetailsMsg)
    val accountDetailsResult: Any = Await.result(accountDetailsFuture, timeout.duration)

    accountDetailsResult match {
        case account: Account =>
            logger.info(s"Account Details: $account")
        case _ =>
            logger.error(s"Unexpected result type: ${accountDetailsResult.getClass}")
    }

    // Get recent transactions
    val recentTransactionsMsg = GetRecentTransactions(createdAccount.id, 5)
    val recentTransactionsFuture: Future[Any] = ask(bankingServices, recentTransactionsMsg)
    val recentTransactionsResult: Any = Await.result(recentTransactionsFuture, timeout.duration)

    recentTransactionsResult match {
        case transactions: List[Transaction @unchecked] if transactions.nonEmpty =>
            logger.info("Recent Transactions:")
            transactions.foreach(transaction => logger.info(s"$transaction"))
        case transactions: List[Transaction @unchecked] if transactions.isEmpty =>
            logger.info(s"No recent transactions found for account ${createdAccount.id}")
        case _ =>
            logger.error(s"Unexpected result type: ${recentTransactionsResult.getClass}")
    }

    system.terminate()
}
