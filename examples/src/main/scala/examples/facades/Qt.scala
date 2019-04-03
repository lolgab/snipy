package examples.facades

import snipy.dynamic._
import snipy._

object Qt {
  private[Qt] trait _QApplication
  type QApplication = PyObject with _QApplication

  private[Qt] trait _QLabel
  type QLabel = PyObject with _QLabel

  private val QWidgets = module("PyQt5.QtWidgets")(PyZone.leakingZone)

  def QApplication()(implicit z: PyZone): QApplication = {
    QWidgets.QApplication(PyList.empty).as[QApplication]
  }

  def QLabel(title: String)(implicit z: PyZone): QLabel =
    QWidgets.QLabel(title).as[QLabel]

  implicit class QLabelOps(val l: QLabel) extends AnyVal {
    def show()(implicit z: PyZone): Unit = Dyn(l).show()
  }

  implicit class QApplicationOps(val app: QApplication) extends AnyVal {
    def exec_()(implicit z: PyZone): Unit = Dyn(app).exec_()
  }
}