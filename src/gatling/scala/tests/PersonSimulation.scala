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
        constantUsersPerSec(10)
          .during(1.minutes),

      // 50
      rampUsersPerSec(10)
        .to(25)
        .during(30.seconds),

      constantUsersPerSec(25)
        .during(90.seconds),

      // 100
//      rampUsersPerSec(25)
//        .to(50)
//        .during(30.seconds),
//
//      constantUsersPerSec(50)
//        .during(90.seconds),
//
//      // 150
//      rampUsersPerSec(50)
//        .to(75)
//        .during(30.seconds),
//
//      constantUsersPerSec(75)
//        .during(90.seconds),
//
//      // 200
//      rampUsersPerSec(75)
//        .to(100)
//        .during(30.seconds),
//
//      constantUsersPerSec(100)
//        .during(150.seconds),
//
//      // 300
//      rampUsersPerSec(100)
//        .to(150)
//        .during(30.seconds),
//
//      constantUsersPerSec(150)
//        .during(150.seconds),
//
//      // Cool down
//      rampUsersPerSec(1500)
//        .to(25)
//        .during(120.seconds),
//
//      constantUsersPerSec(25)
//        .during(90.seconds)
    )
  ).protocols(httpProtocol)
}
