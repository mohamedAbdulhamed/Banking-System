package `com.Banking.System`.BankingSystem.components

import scalafx.scene.text.Text
import scalafx.scene.paint.Color
import scalafx.scene.control.{TextField, Button, TableColumn, TableView}
import scalafx.beans.property.{ObjectProperty, StringProperty}
import javafx.event.{ActionEvent, EventHandler}
import scalafx.scene.layout.VBox
import scalafx.collections.ObservableBuffer

import `com.Banking.System`.BankingSystem.DatabaseConnection._
import `com.Banking.System`.BankingSystem.Transaction

class TransactionTab {
    val bgColor = Color.rgb(38, 38, 38)
    val fgColor = Color.web("#F4EAE0")

    val resultBox = new VBox {
        spacing = 10
        style = "-fx-padding: 10px;"
    }

    val transactionAccountIDField = new TextField {
        promptText = "Account ID"
        style = s"-fx-font-size: 10pt; -fx-pref-width: 200px;"
        id = "viewTransactionsAccountID"
    }

    val transactionCountField = new TextField {
        promptText = "How many transactions to view?"
        style = s"-fx-font-size: 10pt; -fx-pref-width: 200px;"
        id = "viewTransactionsCount"
    }

    // TableView to display transactions
    val transactionTable = new TableView[Transaction] {
        columns ++= List(
            new TableColumn[Transaction, String] {
                text = "Transaction ID"
                cellValueFactory = { data =>
                    new StringProperty(data.value.id.getOrElse("").toString)
                }
                prefWidth = 100
            },
            new TableColumn[Transaction, String] {
                text = "Source Account ID"
                cellValueFactory = { data =>
                    if (data.value.sourceAccountId.getOrElse("").toString != "-1") {
                        new StringProperty(data.value.sourceAccountId.getOrElse("").toString)
                    } else {
                        new StringProperty("User")
                    }
                }
                prefWidth = 100
            },
            new TableColumn[Transaction, String] {
                text = "Destination Account ID"
                cellValueFactory = { data =>
                    if (data.value.destinationAccountId.getOrElse("").toString != "-1") {
                        new StringProperty(data.value.destinationAccountId.getOrElse("").toString)
                    } else {
                        new StringProperty("User")
                    }
                }
                prefWidth = 100
            },
            new TableColumn[Transaction, String] {
                text = "Transaction Type"
                cellValueFactory = { data =>
                    new StringProperty(data.value.transactionType.toString())
                }
                prefWidth = 100
            },
            new TableColumn[Transaction, String] {
                text = "Amount"
                cellValueFactory = { data =>
                    new StringProperty(data.value.amount.toString)
                }
                prefWidth = 100
            },
            new TableColumn[Transaction, String] {
                text = "Date"
                cellValueFactory = { data =>
                    new StringProperty(data.value.date.toString)
                }
                prefWidth = 100
            }
        )
    }

  val transactionTabChildren = Seq(
    new Text {
      text = "Recent Transactions"
      style = s"-fx-font-size: 20pt; -fx-font-weight: bold;"
      fill = fgColor
    },
    new Text {
      text = "- View recent transactions -"
      style = s"-fx-font-size: 10pt;"
      fill = fgColor
    },
    new Text {
      text = "Account ID"
      style = s"-fx-font-size: 10pt;"
      fill = fgColor
    },
    transactionAccountIDField,
    new Text {
      text = "Count"
      style = s"-fx-font-size: 10pt;"
      fill = fgColor
    },
    transactionCountField,
    new Button {
      text = "View Transactions"
      style = s"-fx-font-size: 10pt;"
      onAction = new EventHandler[ActionEvent] {
        override def handle(event: ActionEvent): Unit = {
            if (transactionAccountIDField.text.value == "") {
                println("Please enter an account ID")
            } else if (transactionCountField.text.value == "") {
                println("Please enter a count")
            } else {
                val accountID = transactionAccountIDField.text.value.toInt
                val count = transactionCountField.text.value.toInt
                val transactions = getTransactions(accountID, count) // List[Transaction]

                // Clear the table
                transactionTable.items = ObservableBuffer[Transaction]()

                // Add transactions to the table
                transactionTable.items = ObservableBuffer[Transaction](transactions: _*)


            }
        }
      }
    },
    new Text {
      text = "Resent Transactions will be displayed below"
      style = s"-fx-font-size: 10pt;"
      fill = fgColor
    },
    transactionTable
  )
}

object TransactionTab {
  def apply(): TransactionTab = new TransactionTab()
}
