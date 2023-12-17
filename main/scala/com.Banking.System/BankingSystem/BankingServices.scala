package `com.Banking.System`.BankingSystem

import akka.actor.{Actor, Props}
import DatabaseConnection._
import java.util.Date


// Banking Services class
class BankingServices extends Actor {

    def receive: Receive = {
        // Akka actors process messages asynchronously
        case CreateCustomer(name, address) =>
            val customer = createCustomer(name, address)
            
            sender() ! customer  // sender() is a reference to the actor ActorRef that sent the message to this actor

        case CreateAccount(customer, initialBalance) =>
            var account = createAccount(customer, initialBalance)
            if (account == null) {
                sender() ! "Error creating account!"
            } else {
                sender() ! account
            }
             
        case UpdateCustomer(customer) =>
            val updatedCustomer = updateCustomer(customer)

            sender() ! updatedCustomer

        case Deposit(accountId, amount) =>
            if (validateAccountId(accountId)) {
                val account = getAccountById(accountId).get
                if (validateAmount(amount)) {
                    val newBalance = account.balance + amount
                    val isSuccess = updateBalance(accountId, newBalance)
                    if (isSuccess) {
                        val transaction = createTransaction(Transaction(None, None, Some(accountId), amount, new Date(), Deposit))
                        if (transaction == null) {
                            sender() ! "Error creating transaction!"
                        } else {
                            sender() ! transaction
                        }
                    } else {
                        sender() ! "Error depositing funds!"
                    }                
                } else {
                    sender() ! "Invalid amount!"
                }
            } else {
                sender() ! "Invalid account id!"
            }
            
        case Withdraw(accountId, amount) =>
            if (validateAccountId(accountId)) {
                val account = getAccountById(accountId).get
                if (validateAmount(amount) && account.balance >= amount) {
                    val newBalance = account.balance - amount
                    val isSuccess = updateBalance(accountId, newBalance)
                    if (isSuccess) {
                        val transaction = createTransaction(Transaction(None, Some(accountId), None, amount, new Date(), Withdrawal))
                        if (transaction == null) {
                            sender() ! "Error creating transaction!"
                        } else {
                            sender() ! transaction
                        }
                    } else {
                        sender() ! "Error withdrawing funds!"
                    }
                } else {
                    sender() ! "Insufficient funds for withdrawal or invalid amount!"
                }
            } else {
                sender() ! "Invalid account id!"
            }

        case Transfer(sourceAccountId, destinationAccountId, amount) =>
            if (validateAccountId(sourceAccountId) && validateAccountId(destinationAccountId)) {
                val sourceAccount = getAccountById(sourceAccountId).get
                val destinationAccount = getAccountById(destinationAccountId).get

                if (validateAmount(amount) && sourceAccount.balance >= amount) {
                    val sourceNewBalance = sourceAccount.balance - amount
                    val destinationNewBalance = destinationAccount.balance + amount
                    val isSuccess = updateBalance(sourceAccountId, sourceNewBalance) && updateBalance(destinationAccountId, destinationNewBalance)
                    if (isSuccess) {
                        val transaction = createTransaction(Transaction(None, Some(sourceAccountId), Some(destinationAccountId), amount, new Date(), Transfer))
                        if (transaction == null) {
                            sender() ! "Error creating transaction!"
                        } else {
                            sender() ! transaction
                        }
                    } else {
                        sender() ! "Error transferring funds!"
                    }
                } else {
                    sender() ! "Insufficient funds for transfer or invalid amount!"
                }
            } else {
                sender() ! "Invalid account id!"
            }

        case GetAccountDetailsById(accountId) =>
            val account = getAccountById(accountId)
            if (account.isEmpty) {
                sender() ! s"Account $accountId not found or error retrieving account details."
            } else {
                sender() ! account.get
            }

        case GetAccountDetailsByCustomer(customer) =>
            val account = getAccountByCustomer(customer)
            if (account.isEmpty) {
                sender() ! s"Account for customer $customer not found or error retrieving account details."
            } else {
                sender() ! account.get
            }
            
            sender() ! account
        
        case GetRecentTransactions(accountId, count:Int) =>
            val transactions: List[Transaction] = getTransactions(accountId, count)
            if (!transactions.isEmpty) {
                sender() ! transactions
            } else {
                sender() ! s"Account $accountId not found or no transactions found for that account."
            }
    }

    private val validateAmount = new Function1[Double, Boolean] {
        override def apply(amount: Double): Boolean = {
            amount > 0 && amount < 10000
        }
    }

    private val validateAccountId: Int => Boolean = (accountId: Int) => accountId > 0 // private val validateAccountId = (accountId: Int) => accountId > 0 (syntax sugar)
}