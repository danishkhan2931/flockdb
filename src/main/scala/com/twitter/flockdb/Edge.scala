/*
 * Copyright 2010 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twitter.flockdb

import com.twitter.util.Time
import com.twitter.flockdb.jobs.single._

case class Edge(sourceId: Long, destinationId: Long, position: Long, updatedAt: Time, count: Int,
                state: State) extends Ordered[Edge] {
  def toJob(tableId: Int, forwardingManager: ForwardingManager, uuidGenerator: UuidGenerator) = {
    val job = state match {
      case State.Normal => Add
      case State.Removed => Remove
      case State.Archived => Archive
      case State.Negative => Negate
    }
    job(sourceId, tableId, destinationId, position, updatedAt, forwardingManager, uuidGenerator)
  }

  def compare(other: Edge) = {
    val out = updatedAt.compare(other.updatedAt)
    if (out == 0) {
      state.compare(other.state)
    } else {
      out
    }
  }

  def max(other: Edge) = if (this > other) this else other
}