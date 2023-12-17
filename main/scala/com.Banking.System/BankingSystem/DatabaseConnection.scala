package `com.Banking.System`.BankingSystem

import `com.Banking.System`.BankingSystem.Customer
import `com.Banking.System`.BankingSystem.Account
import java.sql.{Connection, DriverManager}
import java.sql.Statement
import java.sql.SQLException
import java.sql.ResultSet

import scala.util.Try
import _root_.com.typesafe.scalalogging.Logger

object DatabaseConnection {
    
    def getConnection: Connection = {
        val url = "jdbc:mysql://localhost:3306/banking_system"
        val username = "root" // sys.env("DB_USERNAME")
        val password = ""

        try {
            DriverManager.getConnection(url, username, password)
        } catch {
            case e: Exception => throw new Exception(s"Error connecting to database: ${e.getMessage}")
        }
    }

    def closeConnection(connection: Connection): Unit = {
        Try(connection.close())
        .recover {
            case e: Exception => throw new Exception(s"Error closing connection: ${e.getMessage}")
        }
    }

    // Get
    def getAccountById(accountId: Int): Option[Account] = {
        val connection = getConnection
        val query = "SELECT * FROM accounts WHERE id = ?"
        val preparedStatement = connection.prepareStatement(query)
        preparedStatement.setInt(1, accountId)
        val resultSet = preparedStatement.executeQuery()
        var account: Option[Account] = None
        while (resultSet.next) {
            val accountNumber = resultSet.getInt("id")
            val customerId = resultSet.getInt("customer_id")
            val balance = resultSet.getDouble("balance")
            val customer = getCustomer(customerId)
            account = Some(Account(accountNumber, customer, balance))
        }

        closeConnection(connection)
        account
    }

    def getAccountByCustomer(customer: Customer): Option[Account] = {
        val connection = getConnection
        val query = "SELECT * FROM accounts WHERE customer_id = ?"
        val preparedStatement = connection.prepareStatement(query)
        preparedStatement.setInt(1, customer.id)
        val resultSet = preparedStatement.executeQuery()
        var account: Option[Account] = None
        while (resultSet.next) {
            val accountNumber = resultSet.getInt("id")
            val customerId = resultSet.getInt("customer_id")
            val balance = resultSet.getDouble("balance")
            val customer = getCustomer(customerId)
            account = Some(Account(accountNumber, customer, balance))
        }

        closeConnection(connection)
        account
    }

    def getCustomer(customerId: Int): Customer = {
        val connection = getConnection
        val query = "SELECT * FROM customers WHERE id = ?"
        val preparedStatement = connection.prepareStatement(query)
        preparedStatement.setInt(1, customerId)
        val resultSet = preparedStatement.executeQuery()
        var customer: Customer = null
        while (resultSet.next) {
            val id = resultSet.getInt("id")
            val name = resultSet.getString("name")
            val address = resultSet.getString("address")
            customer = Customer(id, name, address)
        }
        
        closeConnection(connection)
        customer
    }

    def getTransactions(accountId: Int, count: Int): List[Transaction] = {
        val connection = getConnection
        val query = s"SELECT * FROM transactions WHERE source_account_id = ? OR destination_account_id = ? ORDER BY date DESC LIMIT $count"
        val preparedStatement = connection.prepareStatement(query)
        preparedStatement.setInt(1, accountId)
        preparedStatement.setInt(2, accountId)
        val resultSet = preparedStatement.executeQuery()

        var transactions: List[Transaction] = List.empty

        while (resultSet.next) {
            val id = resultSet.getInt("id")
            val sourceAccountId = resultSet.getInt("source_account_id")
            val destinationAccountId = resultSet.getInt("destination_account_id")
            val amount = resultSet.getDouble("amount")
            val date = resultSet.getDate("date")
            val transactionTypeString = resultSet.getString("transaction_type")

            val transaction = Transaction(Some(id), Some(sourceAccountId), Some(destinationAccountId), amount, date, TransactionType.fromString(transactionTypeString))
            transactions ::= transaction
        }

        closeConnection(connection)
        transactions
    }

    // Create
    def createCustomer(name: String, address: String): Customer = {
        val connection = getConnection
        val query = "INSERT INTO customers (name, address) VALUES (?, ?)"
        val preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
        preparedStatement.setString(1, name)
        preparedStatement.setString(2, address)
        preparedStatement.executeUpdate()

        val generatedKeys = preparedStatement.getGeneratedKeys
        if (generatedKeys.next()) {
            val customerId = generatedKeys.getInt(1)
            closeConnection(connection)

            // Return the newly created Customer
            Customer(customerId, name, address)
        } else {
            closeConnection(connection)
            throw new SQLException("Failed to retrieve generated keys after customer insertion")
        }
    }

    def createAccount(customer: Customer, initialBalance: Double): Account = {
        val connection = getConnection
        val query = "INSERT INTO accounts (balance, customer_id) VALUES (?, ?)"
        val preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
        preparedStatement.setDouble(1, initialBalance)
        preparedStatement.setInt(2, customer.id)
        preparedStatement.executeUpdate()

        val generatedKeys = preparedStatement.getGeneratedKeys
        if (generatedKeys.next()) {
            val accountId = generatedKeys.getInt(1)
            closeConnection(connection)

            Account(accountId, customer, initialBalance)
        } else {
            closeConnection(connection)
            throw new SQLException("Failed to retrieve generated keys after account insertion")
        }
    }

    def createTransaction(transaction: Transaction): Transaction = {
        val connection = getConnection

        val query = "INSERT INTO transactions (source_account_id, destination_account_id, amount, transaction_type) VALUES (?, ?, ?, ?)"
        val preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)

        val sourceAccountId = transaction.sourceAccountId.getOrElse(-1)
        val destinationAccountId = transaction.destinationAccountId.getOrElse(-1)

        preparedStatement.setInt(1, sourceAccountId)
        preparedStatement.setInt(2, destinationAccountId)
        preparedStatement.setDouble(3, transaction.amount)
        preparedStatement.setString(4, transaction.transactionType.toString)
        preparedStatement.executeUpdate()

        val generatedKeys = preparedStatement.getGeneratedKeys
        val transactionId = if (generatedKeys.next()) generatedKeys.getInt(1) else -1

        closeConnection(connection)

        Transaction(Some(transactionId), transaction.sourceAccountId, transaction.destinationAccountId, transaction.amount, transaction.date, transaction.transactionType)
    }

    // Update
    def updateCustomer(customer: Customer): Customer = {
        val connection = getConnection
        val query = "UPDATE customers SET name = ?, address = ? WHERE id = ?"
        val preparedStatement = connection.prepareStatement(query)
        preparedStatement.setString(1, customer.name)
        preparedStatement.setString(2, customer.address)
        preparedStatement.setInt(3, customer.id)

        val rowsUpdated = preparedStatement.executeUpdate()

        closeConnection(connection)

        if (rowsUpdated > 0) {
            customer
        } else {
            throw new Exception(s"Error updating customer: ${customer.id}, ${customer.name}, ${customer.address}")
        }
    }

    def updateBalance(accountId: Int, newBalance: Double): Boolean = {
        val connection = getConnection
        val query = s"UPDATE accounts SET balance = ? WHERE id = ?"
        val preparedStatement = connection.prepareStatement(query)
        preparedStatement.setDouble(1, newBalance)
        preparedStatement.setInt(2, accountId)

        val rowsUpdated = preparedStatement.executeUpdate()

        closeConnection(connection)

        rowsUpdated > 0
    }

    // Delete
    def deleteCustomer(customerId: Int): Boolean = {
        val connection = getConnection

        val query = "DELETE FROM customers WHERE id = ?"
        val preparedStatement = connection.prepareStatement(query)
        preparedStatement.setInt(1, customerId)

        val rowsDeleted = preparedStatement.executeUpdate()

        closeConnection(connection)

        rowsDeleted > 0
    }

    def deleteAccount(accountId: Int): Boolean = {
        val connection = getConnection

        val query = "DELETE FROM accounts WHERE id = ?"
        val preparedStatement = connection.prepareStatement(query)
        preparedStatement.setInt(1, accountId)

        val rowsDeleted = preparedStatement.executeUpdate()

        closeConnection(connection)

        rowsDeleted > 0
    }
}