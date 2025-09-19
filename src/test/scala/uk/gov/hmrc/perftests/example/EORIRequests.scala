/*
 * Copyright 2024 HM Revenue & Customs
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

  val baseUrl: String            = baseUrlFor("eori-service")
  val strideAuthLogin: String    = baseUrlFor("stride-stub")
  val strideAuthResponse: String = baseUrlFor("stride-auth")

  def redirectWithoutStrideSession: HttpRequestBuilder =
    http("Navigate to Stride Login")
      .get(s"$baseUrl/manage-eori-number")
      .check(status.is(303))
      .check(header(Location).saveAs("strideLoginRedirect"))

  def getStrideLoginRedirect: HttpRequestBuilder =
    http("get Stride login redirect")
      .get("#{strideLoginRedirect}")
      .check(status.is(303))
      .check(header(Location).saveAs("strideStubRedirect"))

  def getSignInRedirect: HttpRequestBuilder =
    http("get Stride IDP page1")
      .get(s"$baseUrl#{strideStubRedirect}")
      .check(status.is(303))
      .check(header(Location).saveAs("strideSignInRedirect"))

  def getStrideIdpStubPage: HttpRequestBuilder =
    http("get Stride IDP page2")
      .get("#{strideSignInRedirect}")
      .check(status.is(200))
      .check(saveRelayState)

  def postStrideLogin: HttpRequestBuilder =
    http("post Stride login stub")
      .post(s"$strideAuthLogin/stride-idp-stub/sign-in")
      .formParam("RelayState", "#{strideRelayState}")
      .formParam("pid", "#{EORI}")
      .formParam("usersGivenName", "")
      .formParam("usersSurname", "")
      .formParam("emailAddress", "")
      .formParam("status", "true")
      .formParam("signature", "valid")
      .formParam("roles", "#{ROLES}")
      .check(status.is(303))
      .check(header(Location).saveAs("authResponse"))

  def getStrideAuthResponseRedirect: HttpRequestBuilder =
    http("get Stride auth response")
      .get(s"$strideAuthLogin#{authResponse}")
      .check(status.is(200))
      .check(saveSAMLResponse)

  def postSAMLResponseToStrideLogin: HttpRequestBuilder =
    http("Post SAMLResponse to eori service")
      .post(s"$strideAuthResponse/stride/auth-response")
      .formParam("SAMLResponse", "#{samlResponse}")
      .formParam("RelayState", "#{strideRelayState}")
      .check(status.is(303))
      .check(
        header(Location).is(
          "/stride-demo-frontend/stride/test/roles/all?successURL=%2Fmanage-eori-number&origin=customs-update-eori-admin-frontend"
        )
      )

  def getSelectUpdateOption: HttpRequestBuilder =
    http("get stride auth response redirect")
      .get(s"$baseUrl/manage-eori-number")
      .check(status.is(200))
      .check(saveCsrfToken)
      .check(regex("Do you want to replace an existing EORI number or cancel subscriptions to HMRC services?").exists)

  def postSelectUpdateOption: HttpRequestBuilder =
    http("post Choose journey type as Update")
      .post(s"$baseUrl/manage-eori-number")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("update-or-cancel-eori", "Update-Eori")
      .check(status.is(303))
      .check(header(Location).is("/manage-eori-number/update"))

  def getEnterUpdatDetails: HttpRequestBuilder =
    http("get Update details response")
      .get(s"$baseUrl/manage-eori-number/update")
      .check(status.is(200))
      .check(saveCsrfToken)
      .check(regex("Replace an existing EORI number").exists)

  def postEnterUpdatDetails: HttpRequestBuilder =
    http("post Enter EORI Details for Update")
      .post(s"$baseUrl/manage-eori-number/update")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("existing-eori", "#{EORI}")
      .formParam("date-of-establishment.day", "#{EDAY}")
      .formParam("date-of-establishment.month", "#{EMONTH}")
      .formParam("date-of-establishment.year", "#{EYEAR}")
      .formParam("new-eori", "#{NEWEORI}")
      .check(status.is(200))

  def getConfirmUpdate: HttpRequestBuilder =
    http("get Confirm for update")
      .get(s"$baseUrl/manage-eori-number/update")
      .check(status.is(200))
      .check(saveCsrfToken)

  def postConfirmUpdate: HttpRequestBuilder =
    http("post Confirm for update")
      .post(s"$baseUrl/manage-eori-number/confirm-update")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("existing-eori", "#{EORI}")
      .formParam("date-of-establishment", "#{EDAY}/#{EMONTH}/#{EYEAR}")
      .formParam("new-eori", "#{NEWEORI}")
      .formParam("enrolment-list", "HMRC-ATAR-ORG,HMRC-GVMS-ORG")
      .check(status.is(303))

  def getUpdateConfirmValidation: HttpRequestBuilder =
    http("get Confirm for Cancel")
      .get(
        s"#baseUrl/manage-eori-number/success?cancelOrUpdate=Update-Eori&oldEoriNumber=#{EORI}&newEoriNumber=#{NEWEORI}&subscribedEnrolments=HMRC-ATAR-ORG%2CHMRC-GVMS-ORG&notUpdatableEnrolments="
      )
      .check(status.is(200))
      .check(saveCsrfToken)
      .check(regex("EORI number #{EORI} has been replaced with #{NEWEORI}").exists)

  def getSelectCancelOption: HttpRequestBuilder =
    http("get Choose Journey type as Cancel")
      .get(s"$baseUrl/manage-eori-number")
      .check(status.is(200))
      .check(saveCsrfToken)
      .check(regex("Do you want to replace an existing EORI number or cancel subscriptions to HMRC services?").exists)

  def postSelectCancelOption: HttpRequestBuilder =
    http("post Choose journey type as Cancel")
      .post(s"$baseUrl/manage-eori-number")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("update-or-cancel-eori", "Cancel-Eori")
      .check(status.is(303))
      .check(header(Location).is("/manage-eori-number/cancel"))

  def getEnterCancelDetails: HttpRequestBuilder =
    http("get Enter details for Cancel")
      .get(s"$baseUrl/manage-eori-number/cancel")
      .check(status.is(200))
      .check(saveCsrfToken)
      .check(regex("Cancel a trader’s subscriptions to HMRC services").exists)

  def postEnterCancelDetails: HttpRequestBuilder =
    http("post Enter details for Cancel")
      .post(s"$baseUrl/manage-eori-number/cancel")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("existing-eori", "#{EORI}")
      .formParam("date-of-establishment.day", "#{EDAY}")
      .formParam("date-of-establishment.month", "#{EMONTH}")
      .formParam("date-of-establishment.year", "#{EYEAR}")
      .check(status.is(200))

  def getConfirmCancel: HttpRequestBuilder =
    http("get Confirm for Cancel")
      .get(
        s"$baseUrl/manage-eori-number/success?cancelOrUpdate=Cancel-Eori&oldEoriNumber=#{EORI}&cancelledEnrolments=HMRC-ATAR-ORG%2CHMRC-GVMS-ORG&nonCancelableEnrolments="
      )
      .check(status.is(200))
      .check(saveCsrfToken)

  def postConfirmCancel: HttpRequestBuilder =
    http("post Confirm for Cancel")
      .post(s"$baseUrl/manage-eori-number/confirm-cancel")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("existing-eori", "#{EORI}")
      .formParam("date-of-establishment", "#{EDAY}/#{EMONTH}/#{EYEAR}")
      .formParam("enrolment-list", "HMRC-ATAR-ORG,HMRC-GVMS-ORG")
      .formParam("not-cancellable-enrolment-list", "")
      .check(status.is(200))

  def getCancelConfirmValidation: HttpRequestBuilder =
    http("get Confirm for Cancel")
      .get(
        s"$baseUrl/manage-eori-number/success?cancelOrUpdate=Cancel-Eori&oldEoriNumber=#{EORI}&cancelledEnrolments=HMRC-ATAR-ORG%2CHMRC-GVMS-ORG&nonCancelableEnrolments="
      )
      .check(status.is(200))
      .check(saveCsrfToken)
      .check(regex("Subscriptions cancelled for #{EORI}").exists)
}
