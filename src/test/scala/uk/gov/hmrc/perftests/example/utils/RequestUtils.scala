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

package uk.gov.hmrc.perftests.example.utils

import java.time.{LocalDate, LocalTime}
import io.gatling.core.Predef._
import io.gatling.core.check.CheckBuilder
import io.gatling.core.check.regex.RegexCheckType
import io.gatling.http.Predef._
import io.gatling.http.check.header.HttpHeaderRegexCheckType

object RequestUtils {

  val csrfPattern = """name="csrfToken" value="([^"]+)""""

  val itemIdRegex = s"""customs-declare-exports/declaration/items/([^"]+)/"""

  val mrnPattern: String = s"""id="submission-tab-other-row0-mrn"
                      |>([^"]+)</td>""".stripMargin

  val declarationIdPatterm = """href="/customs-declare-exports/submissions/([^"]+)/information""""

  def saveCsrfToken: CheckBuilder[RegexCheckType, String, String] = regex(_ => csrfPattern).saveAs("csrfToken")

  def saveItemId: CheckBuilder[HttpHeaderRegexCheckType, Response, String] =
    headerRegex("Location", itemIdRegex).saveAs("itemId")

  def saveMrn: CheckBuilder[RegexCheckType, String, String] = regex(_ => mrnPattern).saveAs("mrn")

  def saveDecID: CheckBuilder[RegexCheckType, String, String] = regex(_ => declarationIdPatterm).saveAs("decID")

  //this will remain the same for every session, but will generate a valid time to be used for every test run
  def validDate: LocalDate = LocalDate.now.minusMonths(1)
  def validYear: String    = validDate.getYear.toString
  def validMonth: String   = validDate.getMonthValue.toString
  def validDay: String     = validDate.getDayOfMonth.toString

  def validTime: LocalTime = LocalTime.now().minusHours(1)
  def validHour: String    = validTime.getHour.toString
  def validMinutes: String = validTime.getMinute.toString
  def validSeconds: String = validTime.getSecond.toString

  //Stride Auth
  val RelayStatePattern                                            = """name="RelayState" value="([^"]+)"""
  def saveRelayState: CheckBuilder[RegexCheckType, String, String] =
    regex(_ => RelayStatePattern).saveAs("strideRelayState")

  val SAMLResponsePattern                                            = """name="SAMLResponse" value="([^"]+)"""
  def saveSAMLResponse: CheckBuilder[RegexCheckType, String, String] =
    regex(_ => SAMLResponsePattern).saveAs("samlResponse")

}
