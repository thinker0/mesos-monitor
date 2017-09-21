package com.github.thinker0.mesos.models


import com.fasterxml.jackson.annotation.JsonProperty


case class GetState(
  `type`: String,
  @JsonProperty("get_state") state : State
)

case class State(
                get_agents: GetAgents,
                get_executors: GetExecutors,
                get_frameworks: GetFrameworks,
                get_tasks: GetTasks
                )

case class GetAgents( agents: Seq[Agent])

case class Agent(
                active: Boolean,
                agentInfo: AgentInfo,
                capabilities: Seq[Role],
                pid: String,
                registered_time: AnyVal,
                reregistered_time: AnyVal,
                total_resources: Seq[Resource],
                version: String
                )

case class AgentInfo(
                    attributes: Seq[Attribute],
                    hostname: String,
                    id : Value,
                    port : Long,
                    resources: Seq[Resource]
                    )

case class Resource(
                   allocation_info: Option[AllocationInfo],
                   name: String,
                   role: Option[String],
                   scalar: Option[Value],
                   `type`:String,
                   ranges : Option[Range]
                   )
case class AllocationInfo(role: String)

case class Range(range: Seq[RangeValues])
case class RangeValues(begin: Long, end:Long)
case class Attribute(
                    name: String,
                    `type`: String,
                    test: Value
                    )

case class Value(value: AnyVal)

case class Role(`type`: String)

case class GetExecutors()
case class GetFrameworks()
case class GetTasks(
                     completed_tasks: Seq[AnyVal],
                     tasks: Seq[Task]
                   )

case class Task(
               agent_id : Value,
               executor_id: Value,
               framework_id: Value,
               labels: Seq[Label],
               name: String,
               resources: Seq[Resource],
               stat: String,
               status_update_state: String,
               status_update_uuid: String,
               statuses: Seq[Statuses],
               task_id: Value
               )
case class Statuses(
                   agent_id: Value,
                   container_status: String, //.... TODO
                   executor_id: Value,
                   message: String,
                   source: String,
                   state: String,
                   tasl_id: Value,
                   timestamp: Long,
                   uuid: String
                   )
case class Label(key:String, value: String)