package com.arcusys.valamis.learningpath.strutsactions

import javax.portlet.{PortletMode, PortletRequest, WindowState}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.arcusys.valamis.learningpath.Configuration
import com.liferay.portal.kernel.portlet.LiferayPortletURL
import com.liferay.portal.kernel.struts.{BaseStrutsAction, StrutsAction}
import com.liferay.portal.kernel.util._
import com.liferay.portal.security.auth.AuthTokenUtil
import com.liferay.portal.theme.ThemeDisplay
import com.liferay.portal.util.PortalUtil
import com.liferay.portlet.PortletURLFactoryUtil

object LearningPathOpenAction {
  val IdKey = "lpId"
  val PlidKey = "plid"

  def getURL(plid: Long, learningPathId: Long, host: String, maximized: Boolean): String = {
    val path = PortalUtil.getPathMain
    val url = s"${host}${path}/portal/learning-path/open?$PlidKey=$plid&$IdKey=$learningPathId"

    if (maximized) url + "&maximized=true" else url
  }
}

class LearningPathOpenAction extends BaseStrutsAction {

  val portletId = Configuration.LearningPathPortletId

  override def execute(originalStrutsAction: StrutsAction, request: HttpServletRequest,
                       response: HttpServletResponse): String = {
    val plId =
      Option(ParamUtil.getLong(request, LearningPathOpenAction.PlidKey)) filter (_ > 0) getOrElse {
        val themeDisplay = request.getAttribute(WebKeys.THEME_DISPLAY).asInstanceOf[ThemeDisplay]
        themeDisplay.getPlid
      }

    val learningPathId = Option(ParamUtil.getLong(request, LearningPathOpenAction.IdKey)) filter (_ > 0)

    val portletUrl =
      getDynamicPortletURL(plId, request, portletId).toString

    val params = learningPathId map (id => "#learning-path/" + id) getOrElse ""

    response.sendRedirect(portletUrl + params)
    ""
  }

  protected def getDynamicPortletURL(plid: Long, request: HttpServletRequest,
                                     portletId: String): LiferayPortletURL = {
    val portletURL = PortletURLFactoryUtil.create(request, portletId, plid, PortletRequest.RENDER_PHASE)
    if (addPortletToken) {
      val token: String = AuthTokenUtil.getToken(request, plid, portletId)
      portletURL.setParameter("p_p_auth", token)
    }
    portletURL.setPortletMode(PortletMode.VIEW)
    portletURL.setWindowState(WindowState.MAXIMIZED)
    portletURL
  }

  private final val addPortletToken: Boolean =
    GetterUtil.getBoolean(PropsUtil.get(PropsKeys.PORTLET_ADD_DEFAULT_RESOURCE_CHECK_ENABLED))
}
