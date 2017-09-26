package com.github.thinker0.mesos

package object models {


}


case class GetState(
  `type`: String,
  get_state: State
)

case class State(
  get_agents: GetAgents,
  get_executors: GetExecutors,
  get_frameworks: GetFrameworks,
  get_tasks: GetTasks
)

case class GetAgents(agents: Seq[Agent])

case class Agent(
  active: Boolean,
  agent_info: AgentInfo,
  capabilities: Seq[Role],
  offered_resources: Seq[Resource],
  allocated_resources: Seq[Resource],
  pid: String,
  registered_time: AnyVal,
  reregistered_time: AnyVal,
  total_resources: Seq[Resource],
  version: String
)

case class AgentInfo(
  attributes: Seq[Attribute],
  hostname: String,
  id: Value,
  port: Long,
  resources: Seq[Resource]
)

case class Resource(
  allocation_info: Option[AllocationInfo],
  name: String,
  role: Option[String],
  scalar: Option[Value],
  revocable: Revocable,
  `type`: String,
  ranges: Option[Range]
)

case class Revocable()

case class AllocationInfo(role: String)

case class Range(range: Seq[RangeValues])

case class RangeValues(begin: Long, end: Long)

case class Attribute(
  name: String,
  `type`: String,
  text: Value
)

case class Value(value: AnyVal)

case class Role(`type`: String)

case class GetExecutors(
  executors: Seq[Executor]
)

case class Executor(
  agent_id: Value,
  executor_info: ExecutorInfo
)

case class ExecutorInfo(
  command: Command,
  executor_id: Value,
  framework_id: Value,
  labels: Labels,
  name: String,
  resources: Seq[Resource],
  source: String
)

case class Command(
  shell: Boolean,
  uris: Seq[Executable],
  value: String
)

case class Executable(
  executable: Boolean,
  extract: Boolean,
  value: String
)

case class GetFrameworks(
  frameworks: Seq[Framework]
)

case class Framework(
  active: Boolean,
  connected: Boolean,
  framework_info: FrameworkInfo,
  recovered: Boolean,
  registered_time: Nanoseconds,
  reregistered_time: Nanoseconds,
  allocated_resources: Seq[Resource],
  capabilities: Seq[Role],
  offered_resources: Seq[Resource],
  offers: Seq[OfferAgentInfo],
  pid: String,
  total_resources: Seq[Resource],
  version: String
)

case class OfferAgentInfo(
  agent_id: Value,
  allocation_info: Role,
  attribute: Seq[Attribute],
  executor_ids: Seq[Value],
  framework_id: Value,
  hostname: String,
  id: Value,
  Resources: Seq[Resource],
  url: AgentURL
)

case class AgentURL(
  address : Address,
  path: String,
  scheme: String
)

case class Address(
  name: String,
  ip: String,
  port: Long
)

case class FrameworkInfo(
  capabilities: Seq[Role],
  checkpoint: Boolean,
  id: Value,
  failover_timeout: Long,
  hostname: String,
  name: String,
  role: String,
  user: String,
  webui_url: String
)

case class Nanoseconds(nanoseconds: Long)

case class GetTasks(
  completed_tasks: Seq[AnyVal],
  tasks: Seq[Task]
)

case class Task(
  agent_id: Value,
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
  container_status: ContainerStatus,
  executor_id: Value,
  message: String,
  source: String,
  state: String,
  tasl_id: Value,
  timestamp: Long,
  uuid: String
)

case class Labels(labels: Seq[Label])

case class Label(key: String, value: String)

case class ContainerStatus(
  container_id: Value,
  executor_id: Option[Long],
  network_infos: Option[Seq[NetworkInfo]]
)

case class NetworkInfo(
  ip_addresses: Seq[IPAddress]
)

case class IPAddress(ip_address: String)


/**
  * mesos-slave
  */

case class GetContainers(
  containers: Seq[Container],
  `type`: String
)

case class Container(
  container_id: Value,
  container_status: ContainerStatus,
  executor_id: Value,
  executor_name: String,
  framework_id: Value,
  resource_statistics: ResourceStatistics
)

case class ResourceStatistics(
  cpus_limit: Double,
  cpus_system_time_secs: Double,
  cpus_user_time_secs: Double,
  mem_anon_bytes: Long,
  mem_cache_bytes: Long,
  mem_file_bytes: Long,
  mem_limit_bytes: Long,
  mem_mapped_file_bytes: Long,
  mem_rss_bytes: Long,
  mem_swap_bytes: Long,
  mem_total_bytes: Long,
  mem_unevictable_bytes: Long,
  timestamp: Double
)