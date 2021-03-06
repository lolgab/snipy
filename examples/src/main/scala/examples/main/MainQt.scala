package examples.main

import snipy._
import examples.facades.Qt._

object MainQt {
  def main(args: Array[String]): Unit = PyZone { implicit z =>
    val app = QApplication()
    val label = QLabel("Hello from SNiPy")
    label.show()
    app.exec_()
  }
}
