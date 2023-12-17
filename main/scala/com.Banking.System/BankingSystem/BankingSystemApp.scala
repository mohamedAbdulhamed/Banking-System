package `com.Banking.System`.BankingSystem

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask

import akka.util.Timeout
import scala.util.{Try, Success, Failure}
import scala.concurrent.duration._
import scala.concurrent.{Await, Future, ExecutionContext}
import scala.language.postfixOps // disable warnings about postfix ops 5.seconds
import _root_.com.typesafe.scalalogging.Logger
import akka.actor.ActorRef

object BankingSystemApp extends App {
    private val logger = Logger(getClass.getName())

    private val system = ActorSystem("BankingSystem")
    private val bankingServices = system.actorOf(Props[BankingServices], "bankingServices") // bankingServicesActor

    implicit val timeout: Timeout = 5.seconds

    def processRequest(actorRef: ActorRef, requestMsg: Any): Try[Any] = {
        val futureResult: Future[Any] = ask(actorRef, requestMsg)  // Awaitable[Any]
        val resultTry: Try[Any] = Try(Await.result(futureResult, timeout.duration))

        resultTry
    }

    // Create customer
    val createCustomerMsg = CreateCustomer("John Doe", "123 Main St")
    val createdCustomerTry: Try[Any] = processRequest(bankingServices, createCustomerMsg)
    var createdCustomer = Customer(0, "", "")

    createdCustomerTry match {
        case Success(createdCustomerResult) =>
            createdCustomerResult match {
            case customer: Customer =>
                createdCustomer = customer.copy()
                logger.info(s"Created Customer: $customer")
            case _ =>
                logger.error(s"Unexpected result type: ${createdCustomerResult.getClass}")
            }

        case Failure(exception) =>
            logger.error(s"Timeout while waiting for the result: ${exception.getMessage}")
    }

    // Update customer
    val updateCustomerMsg = UpdateCustomer(createdCustomer.copy(address = "456 Main St")) // address updated \ .copy() returns a new Customer object (immutable)
    val updatedCustomerTry: Try[Any] = processRequest(bankingServices, updateCustomerMsg)

    updatedCustomerTry match {
        case Success(updatedCustomerResult) =>
            updatedCustomerResult match {
            case customer: Customer =>
                createdCustomer = customer.copy() // update createdCustomer
                logger.info(s"Updated Customer: $customer")
            case errorMsg: String =>
                logger.error(s"Error updating customer: $errorMsg")
            case _ =>
                logger.error(s"Unexpected result type: ${updatedCustomerResult.getClass}")
            }

        case Failure(exception) =>
            logger.error(s"Timeout while waiting for the result: ${exception.getMessage}")
    }


    // Create account
    val createAccountMsg = CreateAccount(createdCustomer, 1000.0)
    val createdAccountTry: Try[Any] = processRequest(bankingServices ,createAccountMsg)
    var createdAccount = Account(0, Customer(0, "", ""), 0.0)

    createdAccountTry match {
        case Success(createdAccountResult) =>
            createdAccountResult match {
            case account: Account =>
                createdAccount = account.copy()
                logger.info(s"Created Account: $account")
            case errorMsg: String =>
                logger.error(s"Error creating account: $errorMsg")
            case _ =>
                logger.error(s"Unexpected result type: ${createdAccountResult.getClass}")
            }

        case Failure(exception) =>
            logger.error(s"Timeout while waiting for the result: ${exception.getMessage}")
    }

    // Make a deposit
    val depositMsg = Deposit(createdAccount.id, 500.0)
    val depositResultTry: Try[Any] = processRequest(bankingServices ,depositMsg)

    depositResultTry match {
        case Success(withdrawResult) => 
            withdrawResult match {
                case transaction: Transaction =>
                    // deposit => from user to account (destinationAccountId)
                    processRequest(bankingServices, GetAccountDetailsById(transaction.destinationAccountId.get)) match {
                        case Success(result) =>
                            result match {
                                case account: Account =>
                                    logger.info(s"Account ${account.id} Balance after deposit: ${account.balance}")
                                case somthingElse =>
                                    logger.error(s"Unexpected result type: ${somthingElse} is not an instance of ${Account.getClass}")
                            }
                        case Failure(exception) =>
                            logger.error(s"Timeout while waiting for the result: ${exception.getMessage}")
                    }
            }

        case Failure(exception) =>
            logger.error(s"Timeout while waiting for the result: ${exception.getMessage}")
    }

    // Make a withdrawal
    val withdrawMsg = Withdraw(createdAccount.id, 200.0)
    val withdrawTry: Try[Any] = processRequest(bankingServices ,withdrawMsg)

    withdrawTry match {
        case Success(withdrawResult) => 
            withdrawResult match {
                case transaction: Transaction =>
                    // withdraw => from account (sourceAccountId) to user
                    processRequest(bankingServices, GetAccountDetailsById(transaction.sourceAccountId.get)) match {
                        case Success(result) =>
                            result match {
                                case account: Account =>
                                    logger.info(s"Account ${account.id} Balance after withdrawal: ${account.balance}")
                                case somthingElse =>
                                    logger.error(s"Unexpected result type: ${somthingElse} is not an instance of ${Account.getClass}")
                            }
                        case Failure(exception) =>
                            logger.error(s"Timeout while waiting for the result: ${exception.getMessage}")
                    }
            }

        case Failure(exception) =>
            logger.error(s"Timeout while waiting for the result: ${exception.getMessage}")
    }

    // Create destination customer (new customer for transfer)
    val createDestinationCustomerMsg = CreateCustomer("Jane Doe", "456 Main St")
    val destinationCustomerTry: Try[Any] = processRequest(bankingServices, createDestinationCustomerMsg)
    var destinationCustomer = Customer(0, "", "")

    destinationCustomerTry match {
        case Success(destinationCustomerResult) => 
            destinationCustomerResult match {
                case customer: Customer =>
                    destinationCustomer = customer.copy()
                    logger.info(s"Created Destination Customer: $customer")
                case errorMsg: String =>
                    logger.error(s"Error creating destination customer: $errorMsg")
                case _ =>
                    logger.error(s"Unexpected result type: ${destinationCustomerResult.getClass}")
            }

        case Failure(exception) =>
            logger.error(s"Timeout while waiting for the result: ${exception.getMessage}")
    }

    // Create destination account (new account for transfer)
    val createDestinationAccountMsg = CreateAccount(destinationCustomer, 500.0)
    val destinationAccountTry: Try[Any] = processRequest(bankingServices, createDestinationAccountMsg)
    var destinationAccount = Account(0, Customer(0, "", ""), 0.0)

    destinationAccountTry match {
        case Success(destinationAccountResult) =>
            destinationAccountResult match {
                case account: Account =>
                    destinationAccount = account.copy()
                    logger.info(s"Created Destination Account: $account")
                case errorMsg: String =>
                    logger.error(s"Error creating destination account: $errorMsg")
                case _ =>
                    logger.error(s"Unexpected result type: ${destinationAccountResult.getClass}")
            }

        case Failure(exception) =>
            logger.error(s"Timeout while waiting for the result: ${exception.getMessage}")
    }

    // Make a transfer
    val transferMsg = Transfer(createdAccount.id, destinationAccount.id, 300.0)
    val transferTry: Try[Any] = processRequest(bankingServices, transferMsg)

    transferTry match {
        case Success(transferResult) =>
            transferResult match {
                case transaction: Transaction =>
                    processRequest(bankingServices, GetAccountDetailsById(transaction.sourceAccountId.get)) match {
                        case Success(result) =>
                            result match {
                                case account: Account =>
                                    logger.info(s"Account ${account.id} Balance after transfer: ${account.balance}")
                                case somthingElse =>
                                    logger.error(s"Unexpected result type: ${somthingElse} is not an instance of ${Account.getClass}")
                            }
                        case Failure(exception) =>
                            logger.error(s"Timeout while waiting for the result: ${exception.getMessage}")
                    }

                    processRequest(bankingServices, GetAccountDetailsById(transaction.destinationAccountId.get)) match {
                        case Success(result) =>
                            result match {
                                case account: Account =>
                                    logger.info(s"Account ${account.id} Balance after transfer: ${account.balance}")
                                case somthingElse =>
                                    logger.error(s"Unexpected result type: ${somthingElse} is not an instance of ${Account.getClass}")
                            }
                        case Failure(exception) =>
                            logger.error(s"Timeout while waiting for the result: ${exception.getMessage}")
                    }
                case errorMsg: String =>
                    logger.error(s"Error making transfer: $errorMsg")
            }

        case Failure(exception) =>
            logger.error(s"Timeout while waiting for the result: ${exception.getMessage}")
    }

    // Get account details
    val accountDetailsMsg = GetAccountDetailsById(createdAccount.id)
    val accountDetailsTry: Try[Any] = processRequest(bankingServices, accountDetailsMsg)

    accountDetailsTry match {
        case Success(accountDetailsResult) =>
            accountDetailsResult match {
                case account: Account =>
                    logger.info(s"Account Details: $account")
                case errorMsg: String =>
                    logger.error(s"Error getting account details: $errorMsg")
                case _ =>
                    logger.error(s"Unexpected result type: ${accountDetailsResult.getClass}")
            }

        case Failure(exception) =>
            logger.error(s"Timeout while waiting for the result: ${exception.getMessage}")
    }

    // Get recent transactions
    val recentTransactionsMsg = GetRecentTransactions(createdAccount.id, 5)
    val recentTransactionsTry: Try[Any] = processRequest(bankingServices, recentTransactionsMsg)

    recentTransactionsTry match {
        case Success(recentTransactionsResult) =>
            recentTransactionsResult match {
                case transactions: List[Transaction @unchecked] =>
                    transactions match {
                        case Nil =>
                            logger.info(s"No recent transactions found for account ${createdAccount.id}")
                        case nonEmptyTransactions =>
                            logger.info("Recent Transactions:")
                            nonEmptyTransactions.foreach(transaction => logger.info(s"$transaction"))
                    }
                case errorMsg: String =>
                    logger.error(s"Error getting recent transactions: $errorMsg")
                case _ =>
                    logger.error(s"Unexpected result type: ${recentTransactionsResult.getClass}")
            }

        case Failure(exception) =>
            logger.error(s"Timeout while waiting for the result: ${exception.getMessage}")
    }

    system.terminate()
}
