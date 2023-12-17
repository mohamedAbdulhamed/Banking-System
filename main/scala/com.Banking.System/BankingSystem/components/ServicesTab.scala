package `com.Banking.System`.BankingSystem.components
import scalafx.scene.text.Text
import scalafx.scene.paint.Color
import scalafx.scene.control.{TextField, Button}
import scalafx.beans.property.ObjectProperty
import javafx.event.{ActionEvent, EventHandler}
import scalafx.scene.layout.VBox
import java.util.Date

import `com.Banking.System`.BankingSystem.DatabaseConnection._
import `com.Banking.System`.BankingSystem.{Transaction, Deposit, Withdrawal, Transfer}

class ServicesTab {
    val bgColor = Color.rgb(38, 38, 38)
    val fgColor = Color.web("#F4EAE0")

    val resultBox = new VBox {
        spacing = 10
        style = "-fx-padding: 10px;"
    }

    // Deposit
    val depositAccountIDTF = new TextField {
        promptText = "Account ID"
        style = s"-fx-font-size: 10pt; -fx-pref-width: 200px;"
        id = "depositAccountID"
    }

    val depositAmountTF = new TextField {
        promptText = "Amount"
        style = s"-fx-font-size: 10pt; -fx-pref-width: 200px;"
        id = "depositAmount"
    }

    // Withdraw
    val withdrawAccountIDTF = new TextField {
        promptText = "Account ID"
        style = s"-fx-font-size: 10pt; -fx-pref-width: 200px;"
        id = "withdrawAccountID"
    }

    val withdrawAmountTF = new TextField {
        promptText = "Amount"
        style = s"-fx-font-size: 10pt; -fx-pref-width: 200px;"
        id = "withdrawAmount"
    }

    // Transfer
    val sourceAccountIdTF = new TextField {
        promptText = "From Account ID"
        style = s"-fx-font-size: 10pt; -fx-pref-width: 200px;"
        id = "sourceAccountId"
    }

    val destinationAccountIdTF = new TextField {
        promptText = "To Account ID"
        style = s"-fx-font-size: 10pt; -fx-pref-width: 200px;"
        id = "destinationAccountId"
    }

    val transferAmountTF = new TextField {
        promptText = "Amount"
        style = s"-fx-font-size: 10pt; -fx-pref-width: 200px;"
        id = "transferAmount"
    }

    val servicesTabChildren = Seq(
        resultBox,
        new Text {
            text = "Services"
            style = s"-fx-font-size: 20pt; -fx-font-weight: bold;"
            fill = fgColor
        },
        new Text {
            text = "- Deposit -"
            style = s"-fx-font-size: 10pt;"
            fill = fgColor
        },
        depositAccountIDTF,
        depositAmountTF,
        new Button {
            text = "Deposit"
            style = s"-fx-font-size: 10pt;"
            onAction = new EventHandler[ActionEvent] {
                override def handle(event: ActionEvent): Unit = {
                    if (!depositAccountIDTF.getText.isEmpty() && !depositAmountTF.getText.isEmpty()) {
                        val accountId = depositAccountIDTF.getText.toInt
                        val amount = depositAmountTF.getText.toDouble

                        val account = getAccountById(accountId).get

                        val newBalance = account.balance + amount
                        val isSuccess = updateBalance(accountId, newBalance)
                        if (isSuccess) {
                            val transaction = createTransaction(Transaction(None, None, Some(accountId), amount, new Date(), Deposit))
                            if (transaction == null) {
                                resultBox.children.clear()
                                resultBox.children.add(new Text {
                                    text = "Error creating transaction!"
                                    style = s"-fx-font-size: 10pt;"
                                    fill = Color.Red
                                })
                            } else {
                                resultBox.children.clear()
                                resultBox.children.add(new Text {
                                    text = s"Successfully deposited $amount into account $accountId."
                                    style = s"-fx-font-size: 10pt;"
                                    fill = Color.Green
                                })
                            }
                        } else {
                            resultBox.children.clear()
                            resultBox.children.add(new Text {
                                text = "Error depositing funds!"
                                style = s"-fx-font-size: 10pt;"
                                fill = Color.Red
                            })
                        }
                    } else {
                        resultBox.children.clear()
                        resultBox.children.add(new Text {
                            text = "Please enter an account ID and amount."
                            style = s"-fx-font-size: 10pt;"
                            fill = Color.Red
                        })
                    }
                }
            }
        },
        new Text {
            text = "- Withdraw -"
            style = s"-fx-font-size: 10pt;"
            fill = fgColor
        },
        withdrawAccountIDTF,
        withdrawAmountTF,
        new Button {
            text = "Withdraw"
            style = s"-fx-font-size: 10pt;"
            onAction = new EventHandler[ActionEvent] {
                override def handle(event: ActionEvent): Unit = {
                    if (!withdrawAccountIDTF.getText.isEmpty() && !withdrawAmountTF.getText.isEmpty()) {
                        val accountId = withdrawAccountIDTF.getText.toInt
                        val amount = withdrawAmountTF.getText.toDouble

                        val account = getAccountById(accountId).get

                        if (account.balance >= amount) {
                            val newBalance = account.balance - amount
                            val isSuccess = updateBalance(accountId, newBalance)
                            if (isSuccess) {
                                val transaction = createTransaction(Transaction(None, Some(accountId), None, amount, new Date(), Withdrawal))
                                if (transaction == null) {
                                    resultBox.children.clear()
                                    resultBox.children.add(new Text {
                                        text = "Error creating transaction!"
                                        style = s"-fx-font-size: 10pt;"
                                        fill = Color.Red
                                    })
                                } else {
                                    resultBox.children.clear()
                                    resultBox.children.add(new Text {
                                        text = s"Successfully withdrew $amount from account $accountId."
                                        style = s"-fx-font-size: 10pt;"
                                        fill = Color.Green
                                    })
                                }
                            } else {
                                resultBox.children.clear()
                                resultBox.children.add(new Text {
                                    text = "Error withdrawing funds!"
                                    style = s"-fx-font-size: 10pt;"
                                    fill = Color.Red
                                })
                            }
                        } else {
                            resultBox.children.clear()
                            resultBox.children.add(new Text {
                                text = "Insufficient funds for withdrawal!"
                                style = s"-fx-font-size: 10pt;"
                                fill = Color.Red
                            })
                        }
                    } else {
                        resultBox.children.clear()
                        resultBox.children.add(new Text {
                            text = "Please enter an account ID and amount."
                            style = s"-fx-font-size: 10pt;"
                            fill = Color.Red
                        })
                    }
                }
            }
        },
        new Text {
            text = "- Transfer -"
            style = s"-fx-font-size: 10pt;"
            fill = fgColor
        },
        sourceAccountIdTF,
        destinationAccountIdTF,
        transferAmountTF, 
        new Button {
            text = "Transfer"
            style = s"-fx-font-size: 10pt;"
            onAction = new EventHandler[ActionEvent] {
                override def handle(event: ActionEvent): Unit = {
                    if (!sourceAccountIdTF.getText().isEmpty() && !destinationAccountIdTF.getText().isEmpty() && !transferAmountTF.getText().isEmpty()) {
                        val sourceAccountId = sourceAccountIdTF.getText().toInt
                        val destinationAccountId = destinationAccountIdTF.getText().toInt
                        val amount = transferAmountTF.getText().toDouble

                        val sourceAccount = getAccountById(sourceAccountId).get
                        val destinationAccount = getAccountById(destinationAccountId).get

                        val sourceNewBalance = sourceAccount.balance - amount
                        val destinationNewBalance = destinationAccount.balance + amount
                        val isSuccess = updateBalance(sourceAccountId, sourceNewBalance) && updateBalance(destinationAccountId, destinationNewBalance)

                        if (isSuccess) {
                            val transaction = createTransaction(Transaction(None, Some(sourceAccountId), Some(destinationAccountId), amount, new Date(), Transfer))
                            if (transaction == null) {
                                resultBox.children.clear()
                                resultBox.children.add(new Text {
                                    text = "Error creating transaction!"
                                    style = s"-fx-font-size: 10pt;"
                                    fill = Color.Red
                                })
                            } else {
                                resultBox.children.clear()
                                resultBox.children.add(new Text {
                                    text = s"Successfully transferred $amount from account $sourceAccountId to account $destinationAccountId."
                                    style = s"-fx-font-size: 10pt;"
                                    fill = Color.Green
                                })
                            }
                        } else {
                            resultBox.children.clear()
                            resultBox.children.add(new Text {
                                text = "Error transferring funds!"
                                style = s"-fx-font-size: 10pt;"
                                fill = Color.Red
                            })
                        }
                    } else {
                        resultBox.children.clear()
                        resultBox.children.add(new Text {
                            text = "Please enter a source account ID, destination account ID, and amount."
                            style = s"-fx-font-size: 10pt;"
                            fill = Color.Red
                        })
                    }
                }
            }
        },
    )
}

object ServicesTab {
    def apply(): ServicesTab = new ServicesTab()
}