package teammates.testing.concurrent;

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import teammates.testing.BaseTest2;
import teammates.testing.lib.BrowserInstance;
import teammates.testing.lib.BrowserInstancePool;
import teammates.testing.lib.TMAPI;
import teammates.testing.object.Scenario;
import teammates.testing.object.Student;

public class StudentEvaluationResultsTest2 extends BaseTest2 {
	
	static Scenario scn = setupScenarioInstance("scenario");
	static BrowserInstance bi;
	
	@BeforeClass
	public static void classSetup() {
		bi = BrowserInstancePool.request();
		TMAPI.cleanupCourse(scn.course.courseId);
		
		TMAPI.createCourse(scn.course);
		TMAPI.createEvaluation(scn.evaluation);
		TMAPI.enrollStudents(scn.course.courseId, scn.students);
		TMAPI.createEvaluation(scn.evaluation);
		TMAPI.studentsJoinCourse(scn.students, scn.course.courseId);
		TMAPI.openEvaluation(scn.course.courseId, scn.evaluation.name);
		TMAPI.studentsSubmitFeedbacks(scn.students, scn.course.courseId, scn.evaluation.name);
		TMAPI.closeEvaluation(scn.course.courseId, scn.evaluation.name);
		TMAPI.publishEvaluation(scn.course.courseId, scn.evaluation.name);
	}
	
	@AfterClass
	public static void classTearDown() {
		TMAPI.cleanupCourse(scn.course.courseId);
		
		BrowserInstancePool.release(bi);
	}
	
	@Test
	public void testStudentViewEvaluationResultsSuccessful() throws Exception {
		for (Student student : scn.students) {
			studentViewEvaluationResults(student);
		}
	}
	
	public void studentViewEvaluationResults(Student student) {
		
		bi.studentLogin(student.email, student.password);

		bi.clickEvaluationTab();
		bi.justWait();
		
		bi.studentClickEvaluationViewResults(scn.course.courseId, scn.evaluation.name);
		bi.justWait();
		
		//comments order is random
		for(int i = 0; i < scn.students.size(); i++) {
			Student teammate = scn.students.get(i);
			if(teammate.teamName.equals(student.teamName) && !teammate.name.equals(student.name)){
				assertTrue(bi.studentGetFeedbackFromOthers(teammate.email, student.email));
			}
		}
		
		bi.logout();
	}
}