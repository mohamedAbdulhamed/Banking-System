package `com.Banking.System`.BankingSystem.components
import scalafx.scene.text.Text
import scalafx.scene.paint.Color
import scalafx.scene.control.{TextField, Button}
import scalafx.beans.property.ObjectProperty
import javafx.event.{ActionEvent, EventHandler}
import scalafx.scene.layout.VBox

import `com.Banking.System`.BankingSystem.DatabaseConnection._
import `com.Banking.System`.BankingSystem.{Account, Customer}

class AccountTab {
    val bgColor = Color.rgb(38, 38, 38)
    val fgColor = Color.web("#F4EAE0")

    val resultBox = new VBox {
        spacing = 10
        style = "-fx-padding: 10px;"
    }

    // Create
    val customerIDTF =  new TextField {
        promptText = "Customer ID"
        style = s"-fx-font-size: 10pt; -fx-pref-width: 200px;"
        id = "customerID"
    }

    val accountBalanceTF = new TextField {
        promptText = "Initial Balance"
        style = s"-fx-font-size: 10pt; -fx-pref-width: 200px;"
        id = "accountBalance"
    }

    // Read
    val accountIDTF = new TextField {
        promptText = "Account ID"
        style = s"-fx-font-size: 10pt; -fx-pref-width: 200px;"
        id = "accountID"
    }

    // Delete
    val deleteAccountIDTF = new TextField {
        promptText = "Account ID"
        style = s"-fx-font-size: 10pt; -fx-pref-width: 200px;"
        id = "deleteAccountID"
    }
    
    val accountTabChildren = Seq(
        resultBox,
        new Text {
            text = "Manage Accounts"
            style = s"-fx-font-size: 20pt; -fx-font-weight: bold;"
            fill = fgColor
        },
        // create
        new Text {
            text = "- Create account -"
            style = s"-fx-font-size: 10pt;"
            fill = fgColor
        },
        customerIDTF,
        accountBalanceTF,
        new Button {
            text = "Create Account"
            style = s"-fx-font-size: 10pt;"
            onAction = new EventHandler[ActionEvent] {
                override def handle(event: ActionEvent): Unit = {
                    if (customerIDTF.text.value != "" && accountBalanceTF.text.value != "") {
                        val customerID = customerIDTF.text.value.toInt
                        val accountBalance = accountBalanceTF.text.value.toDouble

                        val customer = getCustomer(customerID)
                        val account = createAccount(customer, accountBalance)

                        println(customer, account)

                        resultBox.children.clear()
                        resultBox.children.add(new Text {
                            text = s"Created account ${account.id}."
                            style = s"-fx-font-size: 10pt;"
                            fill = Color.Green
                        })
                        
                    } else {
                        resultBox.children.clear()
                        resultBox.children.add(new Text {
                            text = "Please enter a customer ID and an initial balance."
                            style = s"-fx-font-size: 10pt;"
                            fill = Color.Red
                        })
                    }
                }
            }
        },
        // read
        new Text {
            text = "- Get Account Details -"
            style = s"-fx-font-size: 10pt;"
            fill = fgColor
        },
        accountIDTF,
        new Button {
            text = "View Details"
            style = s"-fx-font-size: 10pt;"
            onAction = new EventHandler[ActionEvent] {
                override def handle(event: ActionEvent): Unit = {
                    if (accountIDTF.text.value != "") {
                        val accountID = accountIDTF.text.value.toInt
                        val account = getAccountById(accountID)
                        if (account.isEmpty) {
                            resultBox.children.clear()
                            resultBox.children.add(new Text {
                                text = s"Account ${accountID} does not exist."
                                style = s"-fx-font-size: 10pt;"
                                fill = Color.Red
                            })
                        } else {
                            resultBox.children.clear()
                            resultBox.children.add(new Text {
                                text = s"Account ${accountID} details:"
                                style = s"-fx-font-size: 10pt;"
                                fill = Color.Green
                            })
                            resultBox.children.add(new Text {
                                text = s"Customer ID: ${account.get.customer.id}"
                                style = s"-fx-font-size: 10pt;"
                                fill = Color.Green
                            })
                            resultBox.children.add(new Text {
                                text = s"Balance: ${account.get.balance} $$"
                                style = s"-fx-font-size: 10pt;"
                                fill = Color.Green
                            })
                        }
                    } else {
                        resultBox.children.clear()
                        resultBox.children.add(new Text {
                            text = "Please enter an account ID."
                            style = s"-fx-font-size: 10pt;"
                            fill = Color.Red
                        })
                    }
                }
            }
        },
        // delete
        new Text {
            text = "- Delete account -"
            style = s"-fx-font-size: 10pt;"
            fill = fgColor
        },
        deleteAccountIDTF,
        new Button {
            text = "Delete Account"
            style = s"-fx-font-size: 10pt;"
            onAction = new EventHandler[ActionEvent] {
                override def handle(event: ActionEvent): Unit = {
                    if (deleteAccountIDTF.text.value != "") {
                        val accountID = deleteAccountIDTF.text.value.toInt
                        val isDeleted = deleteAccount(accountID)
                        if (isDeleted) {
                            resultBox.children.clear()
                            resultBox.children.add(new Text {
                                text = s"Account ${accountID} deleted."
                                style = s"-fx-font-size: 10pt;"
                                fill = Color.Green
                            })
                        } else {
                            resultBox.children.clear()
                            resultBox.children.add(new Text {
                                text = s"Account ${accountID} does not exist."
                                style = s"-fx-font-size: 10pt;"
                                fill = Color.Red
                            })
                        }
                    } else {
                        resultBox.children.clear()
                        resultBox.children.add(new Text {
                            text = "Please enter an account ID."
                            style = s"-fx-font-size: 10pt;"
                            fill = Color.Red
                        })
                    }
                }
            }
        },
    )
}

object AccountTab {
    def apply(): AccountTab = new AccountTab()
}