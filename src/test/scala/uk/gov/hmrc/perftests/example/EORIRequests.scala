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

  val baseUrl: String = baseUrlFor("eori-service")
  val strideAuthLogin : String = baseUrlFor("stride-stub")
  val strideAuthResponse : String = baseUrlFor("stride-auth")

  def redirectWithoutStrideSession: HttpRequestBuilder = {
    http("Navigate to Stride Login")
      .get("https://admin.staging.tax.service.gov.uk/customs-update-eori-admin-frontend")
      .check(status.is(303))
      .check(header(Location).saveAs("strideLoginRedirect"))
  }

  def getStrideLoginRedirect: HttpRequestBuilder = {
    http("get Stride login redirect")
      .get(s"https://admin.staging.tax.service.gov.uk$${strideLoginRedirect}")
      .check(status.is(303))
      .check(header(Location).saveAs("strideStubRedirect"))

  }

  def getStrideIdpStubPage: HttpRequestBuilder = {
    http("get Stride IDP page")
      .get("${strideStubRedirect}")
      .check(status.is(200))
      .check(saveRelayState)
  }

  def postStrideLogin: HttpRequestBuilder = {
    http("post Stride login stub")
     .post(s"${strideAuthLogin}"+"/stride-idp-stub/sign-in")
      .formParam("RelayState", "${strideRelayState}")
      .formParam("pid", "${EORI}")
      .formParam("usersGivenName", "")
      .formParam("usersSurname", "")
      .formParam("emailAddress", "")
      .formParam("status", "true")
      .formParam("signature", "valid")
      .formParam("roles", "${ROLES}")
      .check(status.is(303))
      .check(header(Location).saveAs("authResponse"))
  }

  def getStrideAuthResponseRedirect: HttpRequestBuilder = {
    http("get Stride auth response")
      .get(s"${strideAuthLogin}"+"${authResponse}")
      .check(status.is(200))
      .check(saveSAMLResponse)
  }

  def postSAMLResponseToStrideLogin: HttpRequestBuilder = {
    http("Post SAMLResponse to eori service")
      .post(s"$strideAuthResponse/stride/auth-response")
      .formParam("SAMLResponse", "${samlResponse}")
      .formParam("RelayState", "${strideRelayState}")
      .check(status.is(303))
      .check((header(Location)).is("/customs-update-eori-admin-frontend"))
  }

//Update Journey

  def getSelectUpdateOption: HttpRequestBuilder = {
    http("get stride auth response redirect")
      .get(s"$baseUrl/customs-update-eori-admin-frontend/update")
      .check(status.is(200))
      .check(saveCsrfToken)
      .check(regex("Replace an existing EORI number").exists)
  }

  def postSelectUpdateOption: HttpRequestBuilder = {
    http("post Choose journey type as Update")
      .post(s"$baseUrl/customs-update-eori-admin-frontend")
      .formParam("csrfToken", "${csrfToken}")
      .formParam("update-or-cancel-eori", "Update-Eori")
      .check(status.is(303))
      .check(header(Location).is("/customs-update-eori-admin-frontend/update"))
  }
  def getEnterUpdatDetails: HttpRequestBuilder = {
    http("get Update details response")
      .get(s"$baseUrl/customs-update-eori-admin-frontend/update")
      .check(status.is(200))
      .check(saveCsrfToken)
      .check(regex("Replace an existing EORI number").exists)
  }

  def postEnterUpdatDetails: HttpRequestBuilder = {
    http("post Enter EORI Details for Update")
      .post(s"$baseUrl/customs-update-eori-admin-frontend/update")
       .formParam("csrfToken", "${csrfToken}")
      .formParam("existing-eori","${EORI}")
      .formParam("date-of-establishment.day","${EDAY}")
        .formParam("date-of-establishment.month","${EMONTH}")
        .formParam("date-of-establishment.year","${EYEAR}")
        .formParam("new-eori","${NEWEORI}")
      .check(status.is(303))
      .check(header(Location).is("/customs-update-eori-admin-frontend/confirm-update?oldEoriNumber=${EORI}&establishmentDate=${EDAY}%2F${EMONTH}%2F${EYEAR}&newEoriNumber=${NEWEORI}"))
  }
  def getConfirmUpdate: HttpRequestBuilder = {
    http("get Confirm for update")
      .get(s"$baseUrl/customs-update-eori-admin-frontend/confirm-update?oldEoriNumber=$${EORI}&establishmentDate=$${EDAY}%2F$${EMONTH}%2F$${EYEAR}&newEoriNumber=$${NEWEORI}")
      .check(status.is(200))
      .check(saveCsrfToken)
      .check(regex("Replacing EORI number ${EORI}").exists)
  }

  def postConfirmUpdate: HttpRequestBuilder = {
    http("post Confirm for update")
      .post(s"$baseUrl/customs-update-eori-admin-frontend/confirm-update?oldEoriNumber=$${EORI}&establishmentDate=$${EDAY}%2F$${EMONTH}%2F$${EYEAR}&newEoriNumber=$${NEWEORI}")
      .formParam("csrfToken", "${csrfToken}")
      .formParam("confirm","true")
      .check(status.is(303))
      .check(header(Location).is("/customs-update-eori-admin-frontend/update"))
  }

//Cancel Journey

  def getSelectCancelOption: HttpRequestBuilder = {
    http("get Choose Journey type as Cancel")
      .get(s"$baseUrl/customs-update-eori-admin-frontend")
      .check(status.is(200))
      .check(saveCsrfToken)
      .check(regex("Replace an existing EORI number").exists)
  }

  def postSelectCancelOption: HttpRequestBuilder = {
    http("post Choose journey type as Cancel")
      .post(s"$baseUrl/customs-update-eori-admin-frontend")
      .formParam("csrfToken", "${csrfToken}")
      .formParam("update-or-cancel-eori", "Cancel-Eori")
      .check(status.is(303))
      .check(header(Location).is("/customs-update-eori-admin-frontend/cancel"))
  }

  def getEnterCancelDetails: HttpRequestBuilder = {
    http("get Enter details for Cancel")
      .get(s"$baseUrl/customs-update-eori-admin-frontend/cancel")
      .check(status.is(200))
      .check(saveCsrfToken)
      .check(regex("Cancel a trader’s subscriptions to HMRC services").exists)
  }

  def postEnterCancelDetails: HttpRequestBuilder = {
    http("post Enter details for Cancel")
      .post(s"$baseUrl/customs-update-eori-admin-frontend/cancel")
      .formParam("csrfToken", "${csrfToken}")
    .formParam("existing-eori", "${EORI}")
      .formParam("date-of-establishment.day","${EDAY}")
    .formParam("date-of-establishment.month","${EMONTH}")
    .formParam("date-of-establishment.year","${EYEAR}")
      .check(status.is(303))
      .check(header(Location).is("/customs-update-eori-admin-frontend/confirm-cancel?existingEori=${EORI}&establishmentDate=${EDAY}%2F${EMONTH}%2F${EYEAR}"))
  }
  def getConfirmCancel: HttpRequestBuilder = {
    http("get Confirm for Cancel")
      .get(s"$baseUrl/customs-update-eori-admin-frontend/confirm-cancel?existingEori=$${EORI}&establishmentDate=$${EDAY}%2F$${EMONTH}%2F$${EYEAR}")
      .check(status.is(200))
      .check(saveCsrfToken)
      .check(regex("Cancel subscriptions for ${EORI}").exists)
  }

  def postConfirmCancel: HttpRequestBuilder = {
    http("post Confirm for Cancel")
      .post(s"$baseUrl/customs-update-eori-admin-frontend/confirm-cancel?existingEori=$${EORI}&establishmentDate=$${EDAY}%2F$${EMONTH}%2F$${EYEAR}")
      .formParam("csrfToken", "${csrfToken}")
      .formParam("confirm","true")
      .check(status.is(303))
      .check(header(Location).is("/customs-update-eori-admin-frontend/cancel"))
  }
  def getCancelConfirmValidation: HttpRequestBuilder = {
    http("get Confirm for Cancel")
      .get(s"$baseUrl/customs-update-eori-admin-frontend/success?cancelOrUpdate=Cancel-Eori&oldEoriNumber=$${EORI}&cancelledEnrolments=HMRC-ATAR-ORG%2CHMRC-GVMS-ORG")
      .check(status.is(200))
      .check(saveCsrfToken)
      .check(regex("Subscriptions cancelled for ${EORI}").exists)
  }
}
