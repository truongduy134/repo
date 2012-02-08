package teammates.testing.concurrent;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import teammates.testing.BaseTest2;
import teammates.testing.config.Config;
import teammates.testing.lib.BrowserInstance;
import teammates.testing.lib.BrowserInstancePool;
import teammates.testing.lib.SharedLib;
import teammates.testing.lib.TMAPI;
import teammates.testing.object.Scenario;
import teammates.testing.object.Student;

public class CoordCourseViewTest extends BaseTest2 {

	static BrowserInstance bi;
	static Scenario scn = setupScenarioInstance("scenario");

	static Student FIRST_STUDENT = scn.students.get(0);

	@BeforeClass
	public static void classSetup() throws Exception {
		System.out.println("========== CoordCourseViewTest");
		bi = BrowserInstancePool.request();

		TMAPI.cleanupCourse(scn.course.courseId);

		TMAPI.createCourse(scn.course);
		TMAPI.enrollStudents(scn.course.courseId, scn.students);
		TMAPI.studentsJoinCourse(scn.students, scn.course.courseId);

		System.out.println("Clean inbox for " + FIRST_STUDENT.name);
		try {
			SharedLib.markAllEmailsSeen(FIRST_STUDENT.email, Config.inst().TEAMMATES_APP_PASSWD);
			SharedLib.markAllEmailsSeen(Config.inst().INDIVIDUAL_ACCOUNT, Config.inst().TEAMMATES_APP_PASSWD);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		bi.coordinatorLogin(scn.coordinator.username, scn.coordinator.password);

	}

	@AfterClass
	public static void classTearDown() throws Exception {
		if(bi.isElementPresent(bi.logoutTab)) {
			bi.logout();
		}
		TMAPI.cleanupCourse(scn.course.courseId);
		BrowserInstancePool.release(bi);
		System.out.println("CoordCourseViewTest ==========//");
	}

	/**
	 * Test: coordinator send invitation to individual student
	 * 
	 * Remind all students to join course is defined in StudentCourseJoinTest.java
	 * */
	@Test
	public void testCoordRemindIndividualStudentSuccessful() {
		System.out.println("testCoordRemindIndividualStudentSuccessful");
		String newStudent = Config.inst().INDiVIDUAL_NAME;
		String newEmail = Config.inst().INDIVIDUAL_ACCOUNT;


		bi.clickCourseTab();
		bi.clickCourseEnrol(scn.course.courseId);
		bi.verifyEnrollPage();

		bi.wdFillString(bi.enrolInfo, String.format("%s|%s|%s|", FIRST_STUDENT.teamName, newStudent, newEmail));
		bi.waitAndClick(bi.enrolButton);
		bi.justWait();

		bi.clickCourseTab();
		bi.clickCourseView(scn.course.courseId);
		bi.clickCourseDetailInvite(newStudent);

		//Collect key for the new student
		bi.clickCourseDetailView(newStudent);
		bi.waitForElementPresent(bi.studentDetailKey);
		String key = bi.getElementText(bi.studentDetailKey);
		bi.wdClick(bi.courseViewBackButton);
		
		System.out.println("Key for new student: " + key);
		
		// Assert that student gets a notification email
		bi.justWait();
		assertEquals(key, SharedLib.getRegistrationKeyFromGmail(newEmail, Config.inst().TEAMMATES_APP_PASSWD, scn.course.courseId));

		// Assert that rest students don't get spamed
		bi.justWait();
		assertEquals("", SharedLib.getRegistrationKeyFromGmail(FIRST_STUDENT.email, Config.inst().TEAMMATES_APP_PASSWD, scn.course.courseId));

		bi.logout();
	}

	/**
	 * TODO:
	 * Student list under a course
	 * testCoordViewCourseSortByStudentNameSuccessful
	 * testCoordViewCourseSortByTeamSuccessful
	 * testCoordViewCourseSortByStatusSuccessful
	 * */
	@Test
	public void testCoordViewCourseSortStudents() {

	}
}