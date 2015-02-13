import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.util.Timeout
import scala.concurrent.Await
import scala.concurrent.duration._

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

  def run(precision: Int, parallelism: Int)(implicit timeout: Timeout = 10.minutes): Double = {
    import akka.pattern.{ask, pipe}
    import scala.concurrent.ExecutionContext.Implicits.global

    val system = ActorSystem("montecarlo-pi")
    val master = system.actorOf(MasterActor.props(parallelism), name = "master")

    val resp = Await.result(master ? Req(precision), timeout.duration).asInstanceOf[Resp]
    val pi = resp.result * 4.0 / precision
    system.shutdown()

    pi
  }
  
  def main(args: Array[String]): Unit = {
    val pi = run(
      precision = args(0).toInt,
      parallelism = args(1).toInt
    )

    println(pi)
  }
}
