package net.fwbrasil.zoot.core.endpoint

import scala.concurrent.Future
import scala.reflect.runtime.universe
import net.fwbrasil.smirror.sClassOf
import net.fwbrasil.zoot.core.Api
import net.fwbrasil.zoot.core.request.RequestMethod
import net.fwbrasil.zoot.core.util.RichIterable.RichIterable
import net.fwbrasil.zoot.core.Spec

class EndpointSpec extends Spec {

    implicit val mirror = scala.reflect.runtime.currentMirror

    "Endpoint" - {
        "listFor" - {
            "happy day" - {
                "with the commom case" - {
                    "should parse the endpoint annotations to templates" in {
                        Endpoint.listFor[TestApi1].map(_.template) shouldBe
                            List(EndpointTemplate(RequestMethod.GET, "/endpoint1"),
                                EndpointTemplate(RequestMethod.POST, "/endpoint2"),
                                EndpointTemplate(RequestMethod.DELETE, "/endpoint3"))
                    }
                    "should relect endpoints methods" in {
                        Endpoint.listFor[TestApi1].map(_.sMethod) shouldBe
                            List("endpoint1", "endpoint2", "endpoint3")
                            .map(name => sClassOf(classOf[TestApi1]).methods.find(_.name == name).get)
                    }
                }
                "with default param" - {

                    "should ignore the synthetic default param method" in {
                        val sMethod = sClassOf(classOf[TestApi2]).methods.filter(_.name == "endpoint").onlyOne
                        val endpoint = Endpoint.listFor[TestApi2].onlyOne
                        endpoint shouldBe
                            Endpoint(EndpointTemplate(RequestMethod.GET, "/endpoint"), sMethod)
                    }
                }
                "with reference to the outer instance" - {
                    "should ignore the synthetic $outer method" in {
                        val sMethod = sClassOf(classOf[TestApi3]).methods.filter(_.name == "endpoint").onlyOne
                        Endpoint.listFor[TestApi3].onlyOne shouldBe
                            Endpoint(EndpointTemplate(RequestMethod.GET, "/endpoint"), sMethod)
                    }
                }
            }
            "sorry day" - {
                "non future return" in {
                    val exception =
                        intercept[IllegalArgumentException] {
                            Endpoint.listFor[TestApi5]
                        }
                    exception.getMessage.contains("'endpoint' should return scala.concurrent.Future.") shouldBe true
                }
                "non abstract endpoint method" in {
                    intercept[IllegalArgumentException] {
                        Endpoint.listFor[TestApi6]
                    }
                }
                "abstract non-endpoint method" in {
                    intercept[IllegalArgumentException] {
                        Endpoint.listFor[TestApi7]
                    }
                }
            }
        }

    }

    trait TestApi1 extends Api {
        @endpoint(method = RequestMethod.GET, path = "/endpoint1")
        def endpoint1: Future[Unit]

        @endpoint(method = RequestMethod.POST, path = "/endpoint2")
        def endpoint2(string: String): Future[String]

        @endpoint(method = RequestMethod.DELETE, path = "/endpoint3")
        def endpoint3(string: String, int: Int = 12): Future[(String, Int)]
    }

    trait TestApi2 extends Api {
        @endpoint(method = RequestMethod.GET, path = "/endpoint")
        def endpoint(a: String = "a"): Future[String]
    }

    trait TestApi3 extends Api {
        @endpoint(method = RequestMethod.GET, path = "/endpoint")
        def endpoint: Future[String]
    }

    trait TestApi4 extends Api {
        @endpoint(method = RequestMethod.GET, path = "/endpoint")
        def endpoint(a: String): Future[String]
        def evil = "evil"
    }

    trait TestApi5 extends Api {
        @endpoint(method = RequestMethod.GET, path = "/endpoint")
        def endpoint(a: String = "a"): String
    }

    trait TestApi6 extends Api {
        @endpoint(method = RequestMethod.GET, path = "/endpoint")
        def endpoint(a: String = "a") = Future.successful(a)
    }
    
    trait TestApi7 extends Api {
        @endpoint(method = RequestMethod.GET, path = "/endpoint")
        def endpoint(a: String = "a"): Future[String]
        
        def wrong: Boolean
    }

}