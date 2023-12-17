package `com.Banking.System`.BankingSystem

import scalafx.application.JFXApp3
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.{Tab, TabPane}
import scalafx.scene.effect.DropShadow
import scalafx.scene.image.Image
import scalafx.scene.image.ImageView
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.paint.Color._
import scalafx.scene.paint._
import scalafx.scene.text.Text
import scalafx.beans.property.ObjectProperty

import components.{CustomerTab, AccountTab, TransactionTab, ServicesTab}

object BankApp extends JFXApp3 {
    val bgColor = rgb(38, 38, 38)
    val fgColor = Color.web("#F4EAE0")
    
    override def start(): Unit = {
        stage = new JFXApp3.PrimaryStage {
            title = "NM Bank"
            icons.add(new Image("../resources/bank_icon.png"))

            scene = new Scene (1280, 720) {
                fill = bgColor

                content = new HBox {
                    alignment = scalafx.geometry.Pos.Center

                    val tabPane = new TabPane {
                        tabClosingPolicy = TabPane.TabClosingPolicy.Unavailable
                        
                        val customersTab = new Tab() {
                            text = "Customers"

                            content = new VBox {
                                padding = Insets(10)
                                children = CustomerTab.apply().customerTabChildren
                            }
                        }

                        val accountsTab = new Tab() {
                            text = "Accounts"

                            content = new VBox {
                                // Add buttons or other components for managing accounts
                                padding = Insets(10)
                                children = AccountTab.apply().accountTabChildren
                            }
                        }

                        val transactionsTab = new Tab() {
                            text = "Transactions"

                            content = new VBox {
                                // Add buttons or other components for managing transactions
                                padding = Insets(10)
                                children = TransactionTab.apply().transactionTabChildren
                            }
                        }

                        val servicesTab = new Tab() {
                            text = "Services"

                            content = new VBox {
                                // Add buttons or other components for banking services (deposit, withdraw, transfer)
                                padding = Insets(10)
                                children = ServicesTab.apply().servicesTabChildren
                            }
                        }
                        tabs.addAll(customersTab, accountsTab, transactionsTab, servicesTab)
                    }

                    children = Seq(tabPane)
                }
            }
        }
    }
}
