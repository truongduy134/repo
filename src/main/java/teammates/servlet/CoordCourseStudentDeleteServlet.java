package teammates.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import teammates.api.Common;
import teammates.jsp.Helper;

@SuppressWarnings("serial")
/**
 * Servlet to handle Delete Course action
 * @author Aldrian Obaja
 *
 */
public class CoordCourseStudentDeleteServlet extends ActionServlet<Helper> {
	
	private static final String DISPLAY_URL = Common.JSP_COORD_COURSE_DETAILS;

	@Override
	protected Helper instantiateHelper() {
		return new Helper();
	}

	@Override
	protected boolean doAuthenticateUser(HttpServletRequest req,
			HttpServletResponse resp, Helper helper) throws IOException {
		if(!helper.user.isCoord && !helper.user.isAdmin){
			resp.sendRedirect(Common.JSP_UNAUTHORIZED);
			return false;
		}
		return true;
	}

	@Override
	protected void doAction(HttpServletRequest req, Helper helper) {
		// Get parameters
		String courseID = req.getParameter(Common.PARAM_COURSE_ID);
		String studentEmail = req.getParameter(Common.PARAM_STUDENT_EMAIL);
		
		// Process action
		helper.server.deleteStudent(courseID, studentEmail);
		System.out.println(studentEmail);
		helper.statusMessage = Common.MESSAGE_STUDENT_DELETED;
	}

	@Override
	protected void doCreateResponse(HttpServletRequest req,
			HttpServletResponse resp, Helper helper) throws ServletException,
			IOException {
		if(helper.nextUrl==null) helper.nextUrl = DISPLAY_URL;
		helper.nextUrl = Helper.addParam(helper.nextUrl, Common.PARAM_COURSE_ID, req.getParameter(Common.PARAM_COURSE_ID));
		helper.nextUrl = Helper.addParam(helper.nextUrl, Common.PARAM_USER_ID, helper.requestedUser);
		helper.nextUrl = Helper.addParam(helper.nextUrl, Common.PARAM_STATUS_MESSAGE, helper.statusMessage);
		
		resp.sendRedirect(helper.nextUrl);
	}
}