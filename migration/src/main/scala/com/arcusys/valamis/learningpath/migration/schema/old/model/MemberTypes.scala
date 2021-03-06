package com.arcusys.valamis.learningpath.migration.schema.old.model

private[migration] object MemberTypes extends Enumeration {
  val User = Value(0, "user")
  val UserGroup = Value(1, "userGroup")
  val Organization = Value(2, "organization")
  val Role = Value(3, "role")
}

private[migration] case class Member(id: Long, name: String)
