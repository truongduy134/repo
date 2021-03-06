package teammates.test.cases.ui;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import teammates.common.datatransfer.DataBundle;
import teammates.common.datatransfer.FeedbackSessionAttributes;
import teammates.common.datatransfer.InstructorAttributes;
import teammates.common.util.Const;
import teammates.logic.core.FeedbackSessionsLogic;
import teammates.storage.api.FeedbackSessionsDb;
import teammates.ui.controller.Action;
import teammates.ui.controller.RedirectResult;

public class InstructorFeedbackDeleteActionTest extends BaseActionTest {

    DataBundle dataBundle;
    
    @BeforeClass
    public static void classSetUp() throws Exception {
        printTestClassHeader();
        uri = Const.ActionURIs.INSTRUCTOR_FEEDBACK_DELETE;
    }

    @BeforeMethod
    public void caseSetUp() throws Exception {
        dataBundle = getTypicalDataBundle();
        restoreTypicalDataInDatastore();
    }
    
    @Test
    public void testAccessControl() throws Exception{
        FeedbackSessionAttributes fs = dataBundle.feedbackSessions.get("session1InCourse1");
        
        String[] submissionParams = new String[]{
                Const.ParamsNames.COURSE_ID, fs.courseId,
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fs.feedbackSessionName,
        };
        
        verifyUnaccessibleWithoutLogin(submissionParams);
        verifyUnaccessibleForUnregisteredUsers(submissionParams);
        verifyUnaccessibleForStudents(submissionParams);
        verifyUnaccessibleForInstructorsOfOtherCourses(submissionParams);
        verifyAccessibleForInstructorsOfTheSameCourse(submissionParams);
        
        //recreate the entity
        FeedbackSessionsLogic.inst().createFeedbackSession(fs);
        verifyAccessibleForAdminToMasqueradeAsInstructor(submissionParams);        
    }
    
    @Test
    public void testExecuteAndPostProcess() throws Exception{
        FeedbackSessionsDb fsDb = new FeedbackSessionsDb();
        FeedbackSessionAttributes fs = dataBundle.feedbackSessions.get("session1InCourse1");
        
        String[] submissionParams = new String[]{
                Const.ParamsNames.COURSE_ID, fs.courseId,
                Const.ParamsNames.FEEDBACK_SESSION_NAME, fs.feedbackSessionName,
        };
        
        InstructorAttributes instructor = dataBundle.instructors.get("instructor1OfCourse1");
        gaeSimulation.loginAsInstructor(instructor.googleId);
        
        assertNotNull(fsDb.getFeedbackSession(fs.courseId, fs.feedbackSessionName));
        
        Action a = gaeSimulation.getActionObject(uri, submissionParams);
        RedirectResult r = (RedirectResult) a.executeAndPostProcess();
        
        assertNull(fsDb.getFeedbackSession(fs.courseId, fs.feedbackSessionName));
        assertEquals(Const.ActionURIs.INSTRUCTOR_FEEDBACKS_PAGE
                        + "?message=The+feedback+session+has+been+deleted."
                        + "&error=false&user=idOfInstructor1OfCourse1", 
                        r.getDestinationWithParams());
        assertEquals(Const.StatusMessages.FEEDBACK_SESSION_DELETED, r.getStatusMessage());
        assertEquals(false, r.isError);
    }    
}
