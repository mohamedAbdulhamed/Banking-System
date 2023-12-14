package `com.Banking.System`.BankingSystem

import akka.actor.{Actor, Props}
import DatabaseConnection._
import java.util.Date


// Banking Services class
class BankingServices extends Actor {

    def receive: Receive = {
        case CreateCustomer(name, address) =>
            val customer = createCustomer(name, address)

            sender() ! customer

        case CreateAccount(customer, initialBalance) =>
            var account = createAccount(customer, initialBalance)
             
            sender() ! account

        case UpdateCustomer(customer) =>
            val updatedCustomer = updateCustomer(customer).get

            sender() ! updatedCustomer

        case Deposit(accountId, amount) =>
            val account = getAccountById(accountId).get
            if (validateAmount(amount)) {
                val updatedAccount = account.copy(balance = account.balance + amount)
                val isSuccess = updateBalance(accountId, amount, "+")
                if (isSuccess) {
                    val transaction = createTransaction(Transaction(None, updatedAccount, updatedAccount, amount, new Date(), Deposit))
                    sender() ! transaction
                } else {
                    sender() ! "Error depositing funds!"
                }                
            } else {
                sender() ! "Invalid amount!"
            }

        case Withdraw(accountId, amount) =>
            val account = getAccountById(accountId).get
            if (validateAmount(amount) && account.balance >= amount) {
                val updatedAccount = account.copy(balance = account.balance - amount)
                val isSuccess = updateBalance(accountId, amount, "-")
                if (isSuccess) {
                    val transaction = createTransaction(Transaction(None, updatedAccount, updatedAccount, amount, new Date(), Withdrawal))
                    sender() ! transaction
                } else {
                    sender() ! "Error withdrawing funds!"
                }
            } else {
                sender() ! "Insufficient funds for withdrawal or invalid amount!"
            }

        case Transfer(sourceAccountId, destinationAccountId, amount) =>
            val sourceAccount = getAccountById(sourceAccountId).get
            val destinationAccount = getAccountById(destinationAccountId).get

            if (validateAmount(amount) && sourceAccount.balance >= amount) {
                val isSuccess = updateBalance(sourceAccountId, amount, "-") && updateBalance(destinationAccountId, amount, "+")
                if (isSuccess) {
                    val updatedSourceAccount = sourceAccount.copy(balance = sourceAccount.balance - amount)
                    val updatedDestinationAccount = destinationAccount.copy(balance = destinationAccount.balance + amount)
                    val transaction = createTransaction(Transaction(None, updatedSourceAccount, updatedDestinationAccount, amount, new Date(), Transfer))
                    sender() ! transaction
                } else {
                    sender() ! "Error transferring funds!"
                }
            } else {
                sender() ! "Insufficient funds for transfer or invalid amount!"
            }

        case GetAccountDetailsById(accountId) =>
            val account = getAccountById(accountId).get
            
            sender() ! account

        case GetAccountDetailsByCustomer(customer) =>
            val account = getAccountByCustomer(customer).get
            
            sender() ! account
        
        case GetRecentTransactions(accountId, count:Int) =>
            val transactions: List[Transaction] = getTransactions(accountId, count)
            if (!transactions.isEmpty) {
                sender() ! transactions
            } else {
                sender() ! s"Account $accountId not found or no transactions found for that account."
            }
    }

    private def validateAmount(amount: Double): Boolean = {
        amount > 0 && amount < 10000
    }
}