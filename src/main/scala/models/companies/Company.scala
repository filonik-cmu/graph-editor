package editor.models.companies

case class Department(
  val id: String,
  var companyId: String,
  var name: String,
)

case class Company(
  val id: String,
  var name: String
)

case class Database(
  var companies: Seq[Company] = Seq.empty,
  var departments: Seq[Department] = Seq.empty
)
