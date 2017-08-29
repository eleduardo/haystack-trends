/*
 *  Copyright 2017 Expedia, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.expedia.www.haystack.metricpoints.transformer

import com.expedia.open.tracing.Span
import com.expedia.www.haystack.metricpoints.entities.{MetricPoint, MetricType, TagKeys}

import scala.collection.JavaConverters._

trait StatusCountMetricPointTransformer extends MetricPointTransformer {

  val SUCCESS_METRIC_NAME = "success-spans"
  val FAILURE_METRIC_NAME = "failure-spans"

  override def mapSpan(span: Span): List[MetricPoint] = {
    getErrorField(span) match {
      case Some(errorValue) =>
        val tags = Map(TagKeys.OPERATION_NAME_KEY -> span.getOperationName,
          TagKeys.SERVICE_NAME_KEY -> span.getProcess.getServiceName)
        if (errorValue) {
          MetricPoint(FAILURE_METRIC_NAME, MetricType.Gauge,tags, 1, getDataPointTimestamp(span)) :: super.mapSpan(span)
        } else {
          MetricPoint(SUCCESS_METRIC_NAME, MetricType.Gauge, tags, 1, getDataPointTimestamp(span)) :: super.mapSpan(span)
        }

      case None => super.mapSpan(span)
    }

  }

  protected def getErrorField(span: Span): Option[Boolean] = {
    span.getTagsList.asScala.find(tag => tag.getKey.equalsIgnoreCase(ERROR_KEY)).map(_.getVBool)
  }
}
