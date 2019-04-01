package main

import snipy._
import snipy.facades.wx

object Main {
  def main(args: Array[String]): Unit = PyZone { implicit z =>
    val app = wx.App()
    val frame = wx.Frame(title = "Simple application")
    frame.Show()
    app.MainLoop()
  }
}
