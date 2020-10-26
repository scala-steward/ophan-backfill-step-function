package com.gu.ophan.backfill

import org.slf4j.{ Logger, LoggerFactory }

import com.amazonaws.services.lambda.runtime.{ Context, RequestStreamHandler }
import java.io.InputStream
import java.io.OutputStream
import upickle.default.{ Reader, Writer, ReadWriter }

/**
 * A trait that handles some of the housekeeping of deserialising
 * scala case classes in and out, in particular: it is configured to
 * use the lambda handler which doesn't do any automatic
 * (de)serialisation, instead it just provides us with an InputStream
 * and an OutputStream, and we do the serialisation ourselves. The
 * result is that we can use Scala-based automatic case class
 * serialisation.
 */

case class Env(app: String, stack: String, stage: String) {
  override def toString: String = s"App: $app, Stack: $stack, Stage: $stage"
}

object Env {
  def apply(): Env = Env(
    Option(System.getenv("App")).getOrElse("DEV"),
    Option(System.getenv("Stack")).getOrElse("DEV"),
    Option(System.getenv("Stage")).getOrElse("DEV"))
}

abstract class JsonHandler[Input: Reader, Output: Writer] extends RequestStreamHandler {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def process(input: Input)(implicit env: Env): Output

  // this is the interface that AWS expects to see
  override def handleRequest(
    inputStream: InputStream,
    outputStream: OutputStream,
    context: Context): Unit = {
    val input: Input = upickle.default.read[Input](inputStream)
    implicit val env = Env()
    val res = process(input)
    outputStream.write(upickle.default.write(res).getBytes)
  }

}

// same input and output type
abstract class SimpleHandler[T: ReadWriter] extends JsonHandler[T, T]
