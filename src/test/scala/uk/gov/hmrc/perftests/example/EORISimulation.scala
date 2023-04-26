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

import uk.gov.hmrc.performance.simulation.PerformanceTestRunner
import uk.gov.hmrc.perftests.example.EORIRequests._

class EORISimulation extends PerformanceTestRunner {
  setup("Stride Auth Login", "Stride auth login steps") withActions(
    redirectWithoutStrideSession,
    getStrideLoginRedirect,
    getStrideIdpStubPage,
    postStrideLogin,
    getStrideAuthResponseRedirect,
    postSAMLResponseToStrideLogin
  )
  setup("Update Journey", "Update EORI Journey steps") withActions(

    getSelectUpdateOption,
    postSelectUpdateOption,
    getEnterUpdatDetails,
    postEnterUpdatDetails,
    getConfirmUpdate,
    postConfirmUpdate,
    getUpdateConfirmValidation
  )
  setup("Cancel Journey", "Cancel EORI Journey steps") withActions(

    getSelectCancelOption,
    postSelectCancelOption,
    getEnterCancelDetails,
    postEnterCancelDetails,
    getConfirmCancel,
    postConfirmCancel,
    getCancelConfirmValidation
  )
  runSimulation()
}
