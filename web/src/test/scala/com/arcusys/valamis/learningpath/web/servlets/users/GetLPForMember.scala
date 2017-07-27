package com.arcusys.valamis.learningpath.web.servlets.users

import com.arcusys.valamis.learningpath.web.servlets.LPServletTestBase
import com.arcusys.valamis.learningpath.{LiferayHelperTestImpl, ServletImpl}
import com.arcusys.valamis.members.picker.model.MemberTypes
import com.arcusys.valamis.members.picker.model.impl.ForcedUserInfo
import com.arcusys.valamis.members.picker.service.LiferayHelper

/**
  * Created by mminin on 15/03/2017.
  */
class GetLPForMember  extends LPServletTestBase {

  val testUserId = 101
  val user2Id = 102
  val user3Id = 103

  val userAuthHeaders = Map(("userId", testUserId.toString))

  override def servlet = new ServletImpl(dbInfo) {
    override def liferayHelper: LiferayHelper = new LiferayHelperTestImpl(
      companyId,
      users = Seq(
        ForcedUserInfo(testUserId, "user 1", "/logo/u1", Nil, Nil, Nil, Nil),
        ForcedUserInfo(user2Id, "user 2", "/logo/u2", Nil, Nil, Nil, Nil),
        ForcedUserInfo(user3Id, "user 3", "/logo/u2", Nil, Nil, Nil, Nil)
      ))
  }


  test("should return learning path info with users and members counts") {

    val lpId = createLearningPath("test 1")

    addMember(lpId, testUserId, MemberTypes.User)
    addMember(lpId, user2Id, MemberTypes.User)
    addMember(lpId, user3Id, MemberTypes.User)

    createActivityGoal(lpId)
    createGoalGroup(lpId, "group 1")

    get("/users/current/learning-paths") {
      status should beOk

      body should haveJson(
        s"""{
          |  "total": 1,
          |  "items": [{
          |      "id": $lpId,
          |      "published":false,
          |      "title":"test 1",
          |      "openBadgesEnabled":false,
          |      "createdDate":"2017-03-14T14:17:55Z",
          |      "modifiedDate":"2017-03-14T14:17:55Z",
          |
          |      "userMembersCount": 3,
          |      "goalsCount": 2,
          |
          |      "hasDraft": true
          |  }]
          |}""".stripMargin,
        ignoreValues = Seq("createdDate", "modifiedDate")
      )
    }
  }

  test("should support filter and pagination") {

    for( i <- 1 to 100) createLearningPath("test " + i)

    get("/users/current/learning-paths",
      ("skip", "3"), ("take", "7"), ("title", "test 3"), ("sort", "-title")
    ) {
      status should beOk

      body should haveJson(
        s"""{
           |  "total": 11,
           |  "items": [
           |    { "title": "test 36" },
           |    { "title": "test 35" },
           |    { "title": "test 34" },
           |    { "title": "test 33" },
           |    { "title": "test 32" },
           |    { "title": "test 31" },
           |    { "title": "test 30" }
           |  ]
           |}""".stripMargin)
    }
  }


  test("should contains current user progress") {

    val lpId = createLearningPath("test 1")

    createActivityGoal(lpId)
    createGoalGroup(lpId, "group 1")

    addMember(lpId, testUserId, MemberTypes.User)

    publish(lpId)

    get("/users/current/learning-paths") {
      status should beOk

      body should haveJson(
        s"""{
           |  "total": 1,
           |  "items": [{
           |      "id": $lpId,
           |      "published":true,
           |      "title":"test 1",
           |      "userMembersCount": 1,
           |      "goalsCount": 2,
           |      "hasDraft": false
           |  }]
           |}""".stripMargin)
    }
  }

}
