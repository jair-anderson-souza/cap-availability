package tests

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._

class PersonSimulation extends Simulation {

  // =========================
  // FEEDERS
  // =========================

  val persons =
    csv("persons.csv").random

  val ids =
    csv("ids.csv").random

  val createPersons =
    csv("create-persons.csv").random

  val pagination =
    csv("pagination.csv").random


  // =========================
  // HTTP
  // =========================

  val httpProtocol =
    http
      .baseUrl("http://localhost:8080")
      .acceptHeader("application/json")
      .contentTypeHeader("application/json")


  // =========================
  // LIST
  // =========================

  val listPersons =
    exec(
      feed(pagination)
    )
      .exec(
        http("LIST - /person")
          .get("/person?page=${page}&size=${size}")
          .check(status.is(200))
      )


  // =========================
  // FIND
  // =========================

  val findPerson =
    exec(
      feed(ids)
    )
      .exec(
        http("FIND - /person/{id}")
          .get("/person/${id}")
          .check(status.is(200))
      )


  // =========================
  // UPDATE
  // =========================

  val updatePerson =
    exec(
      feed(persons)
    )
      .exec(
        http("UPDATE - /person")
          .put("/person")
          .body(
            StringBody(
              """{"id":"${id}","name":"${name}"}"""
            )
          )
          .check(status.is(200))
      )


  // =========================
  // CREATE
  // =========================

  val createPerson =
    exec(
      feed(createPersons)
    )
      .exec(
        http("CREATE - /person")
          .post("/person")
          .body(
            StringBody(
              """{"name":"${name}"}"""
            )
          )
          .check(status.in(200, 201))
      )


  // =========================
  // MIXED CRUD
  // =========================

  val crud =
    scenario("Person CRUD")

      .randomSwitch(
        70.0 -> exec(listPersons),
        15.0 -> exec(findPerson),
        10.0 -> exec(updatePerson),
        5.0 -> exec(createPerson)
      )


  // =========================
  // LOAD PROFILE
  // =========================

  setUp(
    crud.inject(

      // Warm-up
      constantUsersPerSec(20)
        .during(2.minutes),

      // 50
      rampUsersPerSec(20)
        .to(50)
        .during(1.minute),

      constantUsersPerSec(50)
        .during(3.minutes),

      // 100
      rampUsersPerSec(50)
        .to(100)
        .during(1.minute),

      constantUsersPerSec(100)
        .during(3.minutes),

      // 150
      rampUsersPerSec(100)
        .to(150)
        .during(1.minute),

      constantUsersPerSec(150)
        .during(3.minutes),

      // 200
      rampUsersPerSec(150)
        .to(200)
        .during(1.minute),

      constantUsersPerSec(200)
        .during(5.minutes),

      // 300
      rampUsersPerSec(200)
        .to(300)
        .during(1.minute),

      constantUsersPerSec(300)
        .during(5.minutes),

      // Cool down
      rampUsersPerSec(300)
        .to(50)
        .during(2.minutes),

      constantUsersPerSec(50)
        .during(3.minutes)
    )
  ).protocols(httpProtocol)
}
