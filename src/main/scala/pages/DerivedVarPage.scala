package editor.pages

import scala.scalajs.js
import scala.scalajs.js.JSON

import com.raquo.laminar.api.L.{*, given}

import editor.models.{*,given}
import editor.models.companies.*
import editor.utilities.*

object DerivedVarPage:
  private val companyIdGen = new IntGenerator()
  private val departmentIdGen = new IntGenerator()

  private val databaseVar = Var(Database(
    companies = Seq(
      Company(companyIdGen().toString(), "HelloWorld Inc"),
      Company(companyIdGen().toString(), "Wayland Corp"),
    ),
  ))

  private val companiesVar = databaseVar.zoom(
    _.companies
  )(
    (database, companies) => database.copy(companies=companies)
  )(unsafeWindowOwner)

  private val departmentsVar = databaseVar.zoom(
    _.departments
  )(
    (database, departments) => database.copy(departments=departments)
  )(unsafeWindowOwner)

  object TextInput:
    type Model = String
    def apply(modelVar: Var[Model]): HtmlElement = 
      input(
        `type` := "text",
        controlled(
          value <-- modelVar.signal,
          onInput.mapToValue --> modelVar.writer
        )
      )
  
  object DepartmentItem:
    type Model = Department
    def apply(modelVar: Var[Model])(mods: Modifier[HtmlElement]*): HtmlElement = 
      li(
        onMountInsert { ctx => 
          val nameVar = modelVar.zoom(
            _.name
          )(
            (department, name) => department.copy(name=name)
          )(ctx.owner)
          div(
            TextInput(nameVar),
            mods
          )
        }
      )
  
  object DepartmentList:
    type Model = Seq[Department]
    def apply(modelSignal: Signal[Model]): HtmlElement = 
      ul(
        onMountInsert { ctx => 
          children <-- modelSignal.split(_.id){(id, _, departmentSignal) =>
            val departmentVar = departmentsVar.zoom(
              _.find(_.id == id).get
            )(
              (departments, department) => departments.update(_.id == id, (_) => department)
            )(ctx.owner)
            DepartmentItem(departmentVar)(
              button(
                cls := "btn-sm btn-red",
                onClick --> (_ => departmentsVar.update(
                  _.delete(_.id == id, (_) => {})
                )),
                "Delete"
              )
            )
          }
        }
      )
  
  object CompanyItem:
    type Model = Company
    def apply(modelVar: Var[Model])(mods: Modifier[HtmlElement]*):  HtmlElement = 
      li(
        onMountInsert { ctx => 
          val nameVar = modelVar.zoom(
            _.name
          )(
            (department, name) => department.copy(name=name)
          )(ctx.owner)
          div(
            TextInput(nameVar),
            mods
          )
        }
      )
  
  object CompanyList:
    type Model = Seq[Company]
    def apply(modelSignal: Signal[Model]): HtmlElement = 
      ul(
        onMountInsert { ctx => 
          children <-- modelSignal.split(_.id){(id, _, companySignal) =>
            val companyVar = companiesVar.zoom(
              _.find(_.id == id).get
            )(
              (companies, company) => companies.update(_.id == id, (_) => company)
            )(ctx.owner)
            CompanyItem(companyVar)(
              button(
                cls := "btn-sm btn-green",
                onClick --> (_ => departmentsVar.update(
                  _.create(() => Department(departmentIdGen().toString(), id, "Unnamed"))
                )),
                "Create"
              ),
              button(
                cls := "btn-sm btn-red",
                onClick --> (_ => companiesVar.update(
                  _.delete(_.id == id, (_) => {})
                )),
                "Delete"
              )
            ).amend(
              DepartmentList(departmentsVar.signal.map(
                _.filter(_.companyId == id)
              ))
            )
          }
        }
      )
  
  object DatabaseItem:
    def apply(mods: Modifier[HtmlElement]*): HtmlElement = 
      div(
        div(
          mods
        ),
        CompanyList(companiesVar.signal),
      )

  def apply: HtmlElement = 
    div(
      cls := "grid grid-cols-2 overflow-hidden",
      onMountInsert { ctx => 
        DatabaseItem(
          button(
            cls := "btn-sm btn-green",
            onClick --> (_ => companiesVar.update(
              _.create(() => Company(companyIdGen().toString(), "Unnamed"))
            )),
            "Create"
          ),
        )
      },
      pre(
        cls := "text-xs overflow-auto",
        child.text <-- databaseVar.signal.map(
          (database) => JSON.stringify(database.asInstanceOf[js.Any], space=2)
        )
      ),
    )
