package snipy

import scala.util.control.NonFatal
import java.util.concurrent.ConcurrentLinkedQueue

import scala.scalanative.meta.LinktimeInfo

class MemorySuite extends munit.FunSuite {

  Python.exec("""
def snipy_make_tracker():
    class Tracked:
        alive = 0
        def __init__(self): Tracked.alive += 1
        def __del__(self): Tracked.alive -= 1
    return Tracked
""")

  /** A fresh Python class counting its live instances. */
  private class Tracker {
    val cls: Py = Py.global(Python.eval("snipy_make_tracker")())
    def apply(): Py = cls()
    def alive: Int = cls.alive.as[Int]
  }

  private def refcount(py: Py): Long = module("sys").getrefcount(py).as[Long]

  test("dispose releases the reference immediately") {
    val tracked = new Tracker
    val obj = tracked()
    assertEquals(tracked.alive, 1)
    obj.dispose()
    assertEquals(tracked.alive, 0)
    assert(obj.isDisposed)
    intercept[IllegalStateException](obj.repr)
    assertEquals(obj.toString, "<disposed Python object>")
    obj.dispose() // idempotent
  }

  test("each Py owns exactly one reference") {
    val holder = Py(List.empty[Int])
    val before = refcount(holder)
    val other = Py.local(Python.builtins.list(Py.list(holder)).bracketAccess(0))
    assertEquals(refcount(holder), before + 1)
    other.dispose()
    assertEquals(refcount(holder), before)
  }

  test("Py.local disposes everything created inside except the result") {
    val tracked = new Tracker
    val kept = Py.local {
      (1 to 50).foreach(_ => tracked())
      assertEquals(tracked.alive, 50)
      tracked()
    }
    assertEquals(tracked.alive, 1)
    assert(!kept.isDisposed)
    kept.dispose()
    assertEquals(tracked.alive, 0)
  }

  test("Py.local blocks nest") {
    val tracked = new Tracker
    val outer = Py.local {
      val inner = Py.local(tracked())
      assertEquals(tracked.alive, 1)
      tracked()
      inner
    }
    assertEquals(tracked.alive, 1)
    outer.dispose()
    assertEquals(tracked.alive, 0)
  }

  test("Py.local releases objects when the body throws") {
    val tracked = new Tracker
    intercept[RuntimeException] {
      Py.local {
        tracked()
        throw new RuntimeException("stop")
      }
    }
    assertEquals(tracked.alive, 0)
  }

  test("Py.local keeps the exception value of a PythonException") {
    val e = intercept[PythonException](Py.local(Python.eval("1/0")))
    assertEquals(Python.builtins.`type`(e.value).__name__.as[String], "ZeroDivisionError")
  }

  test("Py.global opts out of the enclosing local block") {
    val tracked = new Tracker
    Py.local {
      Py.global(tracked())
      tracked()
      Py.global(Py.local(tracked()))
      ()
    }
    assertEquals(tracked.alive, 2)
  }

  test("library globals survive being initialized inside Py.local") {
    Py.local(Python.builtins.len(List(1)))
    assertEquals(Python.builtins.len(List(1, 2)).as[Int], 2)
  }

  test("objects are released when their Py is garbage collected") {
    assume(
      LinktimeInfo.isWeakReferenceSupported && !LinktimeInfo.gc.isBoehm,
      "objects are only released automatically with immix or commix"
    )
    val tracked = new Tracker
    def allocate(): Unit = (1 to 1000).foreach(_ => tracked())
    allocate()
    assert(tracked.alive >= 900)
    var attempts = 0
    // weak references are processed asynchronously, by a reference handler thread
    while (tracked.alive > 100 && attempts < 100) {
      System.gc()
      Thread.sleep(20)
      Python.withGil(())
      attempts += 1
    }
    assert(tracked.alive <= 100, s"${tracked.alive} objects still alive")
  }

  test("converted containers own their items exactly once") {
    val item = Python.eval("object()")
    val start = refcount(item)
    Py.local {
      Py(List(item, item))
      Py(Map("a" -> item))
      Py((item, 1))
      Py(Set(item))
      Py.list(item)
      Py.tuple(item)
      ()
    }
    assertEquals(refcount(item), start)
  }

  test("many temporaries in a loop") {
    val math = module("math")
    var sum = 0.0
    var i = 0
    while (i < 100000) {
      Py.local(sum += math.sqrt(i.toDouble).as[Double])
      i += 1
    }
    assert(sum > 0)
  }

  test("Python can be used from several threads at once") {
    assume(LinktimeInfo.isMultithreadingEnabled, "multithreading is disabled")
    // resolved at link time: threads can't even be referenced without multithreading
    if (LinktimeInfo.isMultithreadingEnabled) runThreads()
  }

  private def runThreads(): Unit = {
    val counter = Python.eval("[0]")
    val lock = module("threading").Lock()
    val failures = new ConcurrentLinkedQueue[Throwable]()
    val threads = (1 to 4).map { t =>
      new Thread(() =>
        try {
          var i = 0
          while (i < 500) {
            Py.local {
              assertEquals(Python.builtins.sum(List(t, i)).as[Int], t + i)
              lock.acquire()
              counter.bracketUpdate(0, counter.bracketAccess(0) + 1)
              lock.release()
            }
            i += 1
          }
        } catch { case NonFatal(e) => failures.add(e); () }
      )
    }
    threads.foreach(_.start())
    threads.foreach(_.join())
    assert(failures.isEmpty, failures)
    assertEquals(counter.bracketAccess(0).as[Int], 2000)
  }

  test("withGil batches operations") {
    val total = Python.withGil {
      (1 to 100).map(i => Py(i)).reduce(_ + _).as[Int]
    }
    assertEquals(total, 5050)
  }
}
