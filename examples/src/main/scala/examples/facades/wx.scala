package examples.facades

import snipy._
import snipy.dynamic._

object wx {
  private val wx = module("wx")(PyZone.leakingZone)

  private[wx] trait _App
  type App = PyObject with _App

  private[wx] trait _Point
  type Point = PyObject with _Point

  private[wx] trait _Size
  type Size = PyObject with _Size

  private[wx] trait _Frame
  type Frame = PyObject with _Frame

  def App()(implicit z: PyZone): App = wx.App().as[App]

  def Frame(title: String)(implicit z: PyZone): Frame =
    Dyn(wx).Frame(title = title).as[Frame]

  implicit class AppOps(val app: App) extends AnyVal {
    def MainLoop(): Unit = PyZone { implicit z =>
      Dyn(app).MainLoop()
    }
  }

  implicit class FrameOps(val f: Frame) extends AnyVal {
    def Show(): Unit = PyZone { implicit z =>
      Dyn(f).Show()
    }
  }

}
