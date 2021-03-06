package com.arcusys.valamis.learningpath.impl

import javax.servlet.http.HttpServletRequest

import com.arcusys.valamis.learningpath.Configuration
import com.arcusys.valamis.learningpath.web.servlets.base.PermissionChecker
import com.liferay.portal.kernel.portlet.LiferayPortletSession
import com.liferay.portal.security.permission.PermissionThreadLocal
import com.liferay.portal.service.LayoutLocalServiceUtil


class PermissionCheckerImpl extends PermissionChecker {

  override def hasPermission(permissionId: String)
                            (implicit request: HttpServletRequest): Boolean = {
    val portletId = Configuration.LearningPathPortletId

    val checker = PermissionThreadLocal.getPermissionChecker

    val layoutId = Option(request.getHeader("layoutId"))
      .orElse(Option(request.getParameter("layoutId")))
      .map(_.toLong)

    layoutId
      .flatMap { id =>
        Option(LayoutLocalServiceUtil.fetchLayout(id))
      }
      .exists { layout =>
        val primaryKey = layout.getPlid + LiferayPortletSession.LAYOUT_SEPARATOR + portletId

        checker.hasPermission(layout.getGroupId, portletId, primaryKey, permissionId)
      }
  }
}
