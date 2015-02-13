import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.util.Timeout

object MontecarloPi {
  def sample(iters: Int): Int = {
    val rng = new java.util.Random(System.nanoTime())

    @scala.annotation.tailrec
    def rec(n: Int, inside: Int): Int =
      if (n <= 0) {
        inside
      } else {
        val x = rng.nextDouble()
        val y = rng.nextDouble()

        if (x*x+y*y < 1.0) {
          rec(n-1, inside+1)
        } else {
          rec(n-1, inside)
        }
      }

    rec(iters, 0)
  }

  case class Req(n: Int)
  case class Resp(result: Int)

  class WorkerActor extends Actor with ActorLogging {
    override def receive = {
      case Req(n) =>
        val result = MontecarloPi.sample(n)
        sender() ! Resp(result)
    }
  }

  class MasterActor(concurrency: Int = 4) extends Actor with ActorLogging {
    val workers =
      for (i <- 1 to concurrency)
        yield context.actorOf(WorkerActor.props)

    override def receive = {
      case Req(n) =>
        val iters: Int = n / concurrency

        for (w <- workers)
          w ! Req(iters)

        context become waiting(sender(), concurrency, 0)
    }

    def waiting(parent: ActorRef, remaining: Int, acc: Int): Receive = {
      case Resp(result) =>
        if (remaining == 1) {
          parent ! Resp(acc + result)
          context become receive
        } else {
          context become waiting(parent, remaining - 1, acc + result)
        }
    }
  }

  object WorkerActor {
    def props = Props[WorkerActor]
  }

  object MasterActor {
    def props(c: Int) = Props(classOf[MasterActor], c)
  }

  implicit class ExtArgs(val self: Array[String]) extends AnyVal {
    def getInt(i: Int): Option[Int] =
      if (i >= self.length) None
      else try {
        Some(java.lang.Integer.parseInt(self(i)))
      } catch {
        case _: java.lang.NumberFormatException =>
          None
      }
  }
  
  def main(args: Array[String]): Unit = {
    import akka.pattern.{ask, pipe}
    import scala.concurrent.duration._
    import scala.concurrent.ExecutionContext.Implicits.global

    implicit val timeout: Timeout = 10.minutes

    val n = args.getInt(0).getOrElse(100000000)
    val c = args.getInt(1).getOrElse(4)
    
    val system = ActorSystem("montecarlo-pi")
    val master = system.actorOf(MasterActor.props(c), name = "master")

    val respFut = master ? Req(n)
    for (resp <- respFut.mapTo[Resp]) {
      val pi = resp.result * 4.0 / n

      println(pi)

      system.shutdown()
    }
  }
}
