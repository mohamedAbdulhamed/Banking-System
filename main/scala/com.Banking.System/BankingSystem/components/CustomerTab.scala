package `com.Banking.System`.BankingSystem.components
import scalafx.scene.text.Text
import scalafx.scene.paint.Color
import scalafx.scene.control.{TextField, Button}
import scalafx.beans.property.ObjectProperty
import javafx.event.{ActionEvent, EventHandler}
import scalafx.scene.layout.VBox

import `com.Banking.System`.BankingSystem.DatabaseConnection._
import `com.Banking.System`.BankingSystem.Customer

class CustomerTab {
    val bgColor = Color.rgb(38, 38, 38)
    val fgColor = Color.web("#F4EAE0")

    val resultBox = new VBox {
        spacing = 10
        style = "-fx-padding: 10px;"
    }

    // CREATE
    val createCustomerNameField = new TextField {
        promptText = "Customer Name"
        style = s"-fx-font-size: 10pt; -fx-pref-width: 200px;"
        id = "createCustomerName"
    }

    val createCustomerAddressField = new TextField {
        promptText = "Customer Address"
        style = s"-fx-font-size: 10pt; -fx-pref-width: 200px;"
        id = "createCustomerAddress"
    }

    // UPDATE
    val updateCustomerIDField = new TextField {
        promptText = "Customer ID"
        style = s"-fx-font-size: 10pt; -fx-pref-width: 200px;"
        id = "updateCustomerID"
    }

    val updateCustomerNameField = new TextField {
        promptText = "Customer Name"
        style = s"-fx-font-size: 10pt; -fx-pref-width: 200px;"
        id = "updateCustomerName"
    }

    val updateCustomerAddressField = new TextField {
        promptText = "Customer Address"
        style = s"-fx-font-size: 10pt; -fx-pref-width: 200px;"
        id = "updateCustomerAddress"
    }

    // DELETE
    val deleteCustomerIDField = new TextField {
        promptText = "Customer ID"
        style = s"-fx-font-size: 10pt; -fx-pref-width: 200px;"
        id = "deleteCustomerID"
    }

    // READ
    val readCustomerIDField = new TextField {
        promptText = "Customer ID"
        style = s"-fx-font-size: 10pt; -fx-pref-width: 200px;"
        id = "readCustomerID"
    
    }



    val customerTabChildren = Seq(
        resultBox,
        new Text {
            text = "Manage Customers"
            style = s"-fx-font-size: 20pt; -fx-font-weight: bold;"
            fill = fgColor
        },
        // CREATE
        new Text {
            text = "- Create customer -"
            style = s"-fx-font-size: 10pt;"
            fill = fgColor
        },
        createCustomerNameField,
        createCustomerAddressField,
        new Button {
            text = "Create Customer"
            style = s"-fx-font-size: 10pt;"
            onAction = new EventHandler[ActionEvent] {
                override def handle(event: ActionEvent): Unit = {
                    val createdCustomer = createCustomer(createCustomerNameField.text.value, createCustomerAddressField.text.value)
                    println(createdCustomer)
                    resultBox.children.clear()
                    val result = new Text {
                        text = s"Created customer with ID ${createdCustomer.id}"
                        style = s"-fx-font-size: 10pt;"
                        fill = fgColor
                    }
                    resultBox.children.add(result)
                }
            }
        },
        // UPDATE
        new Text {
            text = "- Update customer -"
            style = s"-fx-font-size: 10pt;"
            fill = fgColor
        },
        updateCustomerIDField,
        updateCustomerNameField,
        updateCustomerAddressField,
        new Button {
            text = "Update Customer"
            style = s"-fx-font-size: 10pt;"
            onAction = new EventHandler[ActionEvent] {
                override def handle(event: ActionEvent): Unit = {
                    if (updateCustomerIDField.text.value == "" || updateCustomerNameField.text.value == "" || updateCustomerAddressField.text.value == "") {
                        resultBox.children.clear()
                        val result = new Text {
                            text = s"Please fill out all fields."
                            style = s"-fx-font-size: 10pt;"
                            fill = Color.Red
                        }
                        resultBox.children.add(result)
                    } else {
                        val customer = Customer(updateCustomerIDField.text.value.toInt, updateCustomerNameField.text.value, updateCustomerAddressField.text.value)
                        val updatedCustomer = updateCustomer(customer)
                        println(updatedCustomer)
                        resultBox.children.clear()
                        val result = new Text {
                            text = s"Updated customer with ID ${updatedCustomer.id}"
                            style = s"-fx-font-size: 10pt;"
                            fill = fgColor
                        }
                        resultBox.children.add(result)
                    }
                }
            }
        },
        // DELETE
        new Text {
            text = "- Delete customer -"
            style = s"-fx-font-size: 10pt;"
            fill = fgColor
        },
        deleteCustomerIDField,
        new Button {
            text = "Delete Customer"
            style = s"-fx-font-size: 10pt;"
            onAction = new EventHandler[ActionEvent] {
                override def handle(event: ActionEvent): Unit = {
                    if (deleteCustomerIDField.text.value == "") {
                        resultBox.children.clear()
                        val result = new Text {
                            text = s"Please fill out all fields."
                            style = s"-fx-font-size: 10pt;"
                            fill = Color.Red
                        }
                        resultBox.children.add(result)
                    } else {
                        val deleted = deleteCustomer(deleteCustomerIDField.text.value.toInt)
                        if (deleted) {
                            println(deleted)
                            resultBox.children.clear()
                            val result = new Text {
                                text = s"Deleted customer with ID ${deleteCustomerIDField.text.value}"
                                style = s"-fx-font-size: 10pt;"
                                fill = fgColor
                            }
                            resultBox.children.add(result)
                        } else {
                            resultBox.children.clear()
                            val result = new Text {
                                text = s"Customer with ID ${deleteCustomerIDField.text.value} does not exist."
                                style = s"-fx-font-size: 10pt;"
                                fill = Color.Red
                            }
                            resultBox.children.add(result)
                        }
                    }
                }
            }
        },
        // read
        new Text {
            text = "- Get customer details -"
            style = s"-fx-font-size: 10pt;"
            fill = fgColor
        },
        readCustomerIDField,
        new Button {
            text = "View Details"
            style = s"-fx-font-size: 10pt;"
            onAction = new EventHandler[ActionEvent] {
                override def handle(event: ActionEvent): Unit = {
                    if (readCustomerIDField.text.value == "") {
                        resultBox.children.clear()
                        val result = new Text {
                            text = s"Please fill out all fields."
                            style = s"-fx-font-size: 10pt;"
                            fill = Color.Red
                        }
                        resultBox.children.add(result)
                    } else {
                        val customer = getCustomer(readCustomerIDField.text.value.toInt)
                        if (customer != null) {
                            println(customer)
                            resultBox.children.clear()
                            val result = new Text {
                                text = s"Customer with ID ${readCustomerIDField.text.value} is ${customer.name} at ${customer.address}"
                                style = s"-fx-font-size: 10pt;"
                                fill = fgColor
                            }
                            resultBox.children.add(result)
                        } else {
                            resultBox.children.clear()
                            val result = new Text {
                                text = s"Customer with ID ${readCustomerIDField.text.value} does not exist."
                                style = s"-fx-font-size: 10pt;"
                                fill = Color.Red
                            }
                            resultBox.children.add(result)
                        }
                    }
                }
            }
        },
    )

}

object CustomerTab {
    def apply(): CustomerTab = new CustomerTab()
}