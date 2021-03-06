package teammates.test.cases.ui.browsertests;

import static org.testng.AssertJUnit.assertEquals;
import static org.junit.Assert.assertTrue;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import teammates.common.datatransfer.DataBundle;
import teammates.common.datatransfer.StudentAttributes;
import teammates.common.util.Const;
import teammates.common.util.Url;
import teammates.test.driver.BackDoor;
import teammates.test.pageobjects.Browser;
import teammates.test.pageobjects.BrowserPool;
import teammates.test.pageobjects.InstructorCourseDetailsPage;
import teammates.test.pageobjects.InstructorCourseStudentDetailsEditPage;

/**
 * Covers the 'edit student details' functionality for instructors.
 * SUT: {@link InstructorCourseStudentDetailsEditPage}.
 */
public class InstructorCourseStudentDetailsEditPageUiTest extends BaseUiTestCase {
    private static Browser browser;
    private static InstructorCourseStudentDetailsEditPage editPage;
    private static DataBundle testData;
    

    @BeforeClass
    public static void classSetup() throws Exception {
        printTestClassHeader();
        testData = loadDataBundle("/InstructorCourseStudentDetailsEditPageUiTest.json");
        restoreTestDataOnServer(testData);
        browser = BrowserPool.getBrowser();
    }
    
    
    @Test
    public void testAll() throws Exception{
        testContent();
        testInputValidation();
//        no links to check
        testEditAction();
    }
    
    public void testContent() throws Exception{
        
        String instructorId = testData.instructors.get("CCSDEditUiT.instr").googleId;
        String courseId = testData.courses.get("CCSDEditUiT.CS2104").id;
        
        ______TS("content: unregistered student");
        
        Url editPageUrl = createUrl(Const.ActionURIs.INSTRUCTOR_COURSE_STUDENT_DETAILS_EDIT)
        .withUserId(instructorId)
        .withCourseId(courseId)
        .withStudentEmail(testData.students.get("unregisteredStudent").email);
        
        editPage = loginAdminToPage(browser, editPageUrl, InstructorCourseStudentDetailsEditPage.class);
        editPage.verifyHtml("/InstructorCourseStudentEditUnregisteredPage.html");
        
        ______TS("content: registered student");
        
        editPageUrl = createUrl(Const.ActionURIs.INSTRUCTOR_COURSE_STUDENT_DETAILS_EDIT)
            .withUserId(instructorId)
            .withCourseId(courseId)
            .withStudentEmail(testData.students.get("registeredStudent").email);
        
        editPage = loginAdminToPage(browser, editPageUrl, InstructorCourseStudentDetailsEditPage.class);
        editPage.verifyHtml("/InstructorCourseStudentEditPage.html");
    }
    
    public void testInputValidation() {
        
        ______TS("input validation");
        
        editPage.submitUnsuccessfully(null, "", null, null)
            .verifyStatus("Please fill in all the relevant fields.");
        
        editPage.submitUnsuccessfully("invalidstudentnamewithmorethan40characters", "New teamname", null, null)
            .verifyStatus("Name should only consist of alphanumerics or hyphens, apostrophes, fullstops, commas, slashes, round brackets\nand not more than 40 characters.");
        
        editPage.submitUnsuccessfully("New guy", "invalidteamnamewithmorethan60characterslooooooooooooooooooong", null, null)
            .verifyStatus("Team name should contain less than 60 characters.");
        
        editPage.submitUnsuccessfully("New guy", "new team", "invalidemail", null)
            .verifyStatus("The e-mail address is invalid.");
    }


    public void testEditAction() throws Exception{
        
        ______TS("Error case, invalid email parameter (email already taken by others)");

        StudentAttributes anotherStudent = testData.students.get("unregisteredStudent");
        
        
        editPage  = editPage.submitUnsuccessfully("New name2", "New team2", anotherStudent.email, "New comments2");
        editPage.verifyStatus(Const.StatusMessages.STUDENT_EMAIL_CONFLIT+anotherStudent.name+"/"+anotherStudent.email); //??
        editPage.verifyIsCorrectPage("CCSDEditUiT.jose.tmms@gmail.com");
            
        // Verify data
        StudentAttributes student  = BackDoor.getStudent(testData.courses.get("CCSDEditUiT.CS2104").id, "CCSDEditUiT.jose.tmms@gmail.com");
        assertEquals("José Gómez",student.name);
        assertEquals("Team 1",student.team);
        assertEquals(testData.students.get("registeredStudent").googleId,student.googleId);
        assertEquals("CCSDEditUiT.jose.tmms@gmail.com",student.email);
        assertEquals("This student's name is José Gómez",student.comments);
        
        
        ______TS("edit action");
        
        InstructorCourseDetailsPage detailsPage = editPage.submitSuccessfully("New name", "New team", "newemail@gmail.com", "New comments");
        detailsPage.verifyStatus(Const.StatusMessages.STUDENT_EDITED);
        detailsPage.verifyIsCorrectPage(testData.courses.get("CCSDEditUiT.CS2104").id);
            
        // Verify data
        student  = BackDoor.getStudent(testData.courses.get("CCSDEditUiT.CS2104").id, "newemail@gmail.com");
        assertEquals("New name",student.name);
        assertEquals("New team",student.team);
        assertEquals(testData.students.get("registeredStudent").googleId,student.googleId);
        assertEquals("newemail@gmail.com",student.email);
        assertEquals("New comments",student.comments);
    }


    @AfterClass
    public static void classTearDown() throws Exception {
        BrowserPool.release(browser);
    }
}
