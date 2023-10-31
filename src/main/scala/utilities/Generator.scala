package editor.utilities

trait Generator[A]:
  def apply(): A

class IntGenerator(var lastValue: Int = 0) extends Generator[Int]:
  def apply(): Int = 
    lastValue += 1
    lastValue
