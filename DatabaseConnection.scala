package `com.Banking.System`.BankingSystem

import java.sql.{Connection, DriverManager}
import `com.Banking.System`.BankingSystem.Customer
import `com.Banking.System`.BankingSystem.Account
import java.sql.Statement
import java.sql.SQLException
import java.sql.ResultSet

object DatabaseConnection { 
    def getConnection: Connection = {
        val url = "jdbc:mysql://localhost:3306/banking_system"
        val username = "root"
        val password = ""

        try {
            val connection = DriverManager.getConnection(url, username, password)
            if (connection != null) {
                println("Connection successful")
            } else {
                println("Connection failed")
            }
            connection
        } catch {
            case e: Exception => throw e
        }
    }

    def closeConnection(connection: Connection): Unit = {
        try {
            connection.close()
        } catch {
            case e: Exception => throw e
        }
    }

    def getAccounts(): Map[Int, Account] = {
        val connection = getConnection
        val statement = connection.createStatement
        val query = "SELECT * FROM accounts"
        val resultSet = statement.executeQuery(query)
        var accounts: Map[Int, Account] = Map.empty
        while (resultSet.next) {
            val accountNumber = resultSet.getInt("id")
            val customerId = resultSet.getInt("customer_id")
            val balance = resultSet.getDouble("balance")
            val customer = getCustomer(customerId)
            val account = Account(accountNumber, customer, balance)
            accounts += (accountNumber -> account)
        }

        closeConnection(connection)
        accounts
    }

    def getAccount(accountId: Int): Option[Account] = {
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
        preparedStatement.setInt(1, transaction.sourceAccount.id)
        preparedStatement.setInt(2, transaction.destinationAccount.id)
        preparedStatement.setDouble(3, transaction.amount)
        preparedStatement.setString(4, transaction.transactionType)
        preparedStatement.executeUpdate()

        val generatedKeys = preparedStatement.getGeneratedKeys
        val transactionId = if (generatedKeys.next()) generatedKeys.getInt(1) else -1

        closeConnection(connection)

        Transaction(Some(transactionId), transaction.sourceAccount, transaction.destinationAccount, transaction.amount, transaction.date, transaction.transactionType)
    }

    def deposit(accountId: Int, amount: Double): Boolean = {
        val connection = getConnection
        val query = "UPDATE accounts SET balance = balance + ? WHERE id = ?"
        val preparedStatement = connection.prepareStatement(query)
        preparedStatement.setDouble(1, amount)
        preparedStatement.setInt(2, accountId)
        
        val rowsUpdated = preparedStatement.executeUpdate()

        closeConnection(connection)

        rowsUpdated > 0
    }

    def withdraw(accountId: Int, amount: Double): Boolean = {
        val connection = getConnection
        val query = s"UPDATE accounts SET balance = balance - ? WHERE id = ?"
        val preparedStatement = connection.prepareStatement(query)
        preparedStatement.setDouble(1, amount)
        preparedStatement.setInt(2, accountId)

        val rowsUpdated = preparedStatement.executeUpdate()

        closeConnection(connection)

        rowsUpdated > 0
    }

    def transfer(sourceAccountId: Int, destinationAccountId: Int, amount: Double): Boolean = {
        val connection = getConnection

        val query = s"UPDATE accounts SET balance = balance - ? WHERE id = ?"
        val preparedStatement = connection.prepareStatement(query)
        preparedStatement.setDouble(1, amount)
        preparedStatement.setInt(2, sourceAccountId)

        val rowsUpdated1 = preparedStatement.executeUpdate()

        val query2 = s"UPDATE accounts SET balance = balance + ? WHERE id = ?"
        val preparedStatement2 = connection.prepareStatement(query2)
        preparedStatement2.setDouble(1, amount)
        preparedStatement2.setInt(2, destinationAccountId)

        val rowsUpdated2 = preparedStatement2.executeUpdate()

        closeConnection(connection)

        rowsUpdated1 > 0 && rowsUpdated2 > 0
    }

    def getRecentTransactions(accountId: Int, count: Int): List[Transaction] = {
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
            val transactionType = resultSet.getString("transaction_type")

            val sourceAccount = getAccount(sourceAccountId).get
            val destinationAccount = getAccount(destinationAccountId).get

            val transaction = Transaction(Some(id), sourceAccount, destinationAccount, amount, date, transactionType)
            transactions ::= transaction
        }

        closeConnection(connection)
        transactions
    }
}