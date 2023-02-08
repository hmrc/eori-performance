/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.perftests.example

import io.gatling.core.Predef._
import io.gatling.http.HeaderNames.Location
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder
import uk.gov.hmrc.performance.conf.ServicesConfiguration
import uk.gov.hmrc.perftests.example.utils.RequestUtils._

object EORIRequests extends ServicesConfiguration {

  val baseUrl: String = baseUrlFor("internal-movements")
  val strideAuthLogin : String = baseUrlFor("stride-stub")
  val strideAuthResponse : String = baseUrlFor("stride-auth")

  def redirectWithoutStrideSession: HttpRequestBuilder = {
    http("Navigate to internal service and redirect to Stride Login")
      // .get("https://admin.staging.tax.service.gov.uk/customs-exports-internal/choice")
          .get("https://admin.staging.tax.service.gov.uk/customs-update-eori-admin-frontend")
      .check(status.is(303))
      .check(header(Location).saveAs("strideLoginRedirect"))
  }

  def getStrideLoginRedirect: HttpRequestBuilder = {
    http("get stride login redirect")
      .get(s"https://admin.staging.tax.service.gov.uk$${strideLoginRedirect}")
      .check(status.is(303))
      .check(header(Location).saveAs("strideStubRedirect"))

  }

  def getStrideIdpStubPage: HttpRequestBuilder = {
    http("get stride IDP page")
      .get("${strideStubRedirect}")
      .check(status.is(200))
      .check(saveRelayState)
  }

  def postStrideLogin: HttpRequestBuilder = {
    http("post Stride login stub")
      .post(s"${strideAuthLogin}"+"/stride-idp-stub/sign-in")
      .formParam("RelayState", "${strideRelayState}")
      .formParam("pid", "${PID}")
      .formParam("usersGivenName", "")
      .formParam("usersSurname", "")
      .formParam("emailAddress", "")
      .formParam("status", "true")
      .formParam("signature", "valid")
      //.formParam("roles", "write:customs-inventory-linking-exports")
      .formParam("roles", "write:update-enrolment-eori")
      .check(status.is(303))
      .check(header(Location).saveAs("authResponse"))
  }

  def getStrideAuthResponseRedirect: HttpRequestBuilder = {
    http("get stride auth response redirect")
      .get(s"${strideAuthLogin}"+"${authResponse}")
      .check(status.is(200))
      .check(saveSAMLResponse)
  }

  def postSAMLResponseToStrideLogin: HttpRequestBuilder = {
    http("Post SAMLResponse to Stride Login and redirect to internal service")
      .post(s"$strideAuthResponse/stride/auth-response")
      .formParam("SAMLResponse", "${samlResponse}")
      .formParam("RelayState", "${strideRelayState}")
      .check(status.is(303))
      .check(header(Location).is("/customs-exports-internal/choice"))
  }
}
