package teammates.test.cases.ui.browsertests;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;


import org.openqa.selenium.By;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import teammates.common.datatransfer.CourseAttributes;
import teammates.common.datatransfer.DataBundle;
import teammates.common.datatransfer.EvaluationAttributes;
import teammates.common.datatransfer.EvaluationAttributes.EvalStatus;
import teammates.common.datatransfer.InstructorAttributes;
import teammates.common.datatransfer.StudentAttributes;
import teammates.common.util.Const;
import teammates.common.util.TimeHelper;
import teammates.common.util.Url;
import teammates.test.driver.AssertHelper;
import teammates.test.driver.BackDoor;
import teammates.test.driver.TestProperties;
import teammates.test.pageobjects.AppPage;
import teammates.test.pageobjects.Browser;
import teammates.test.pageobjects.BrowserPool;
import teammates.test.pageobjects.DevServerLoginPage;
import teammates.test.pageobjects.GoogleLoginPage;
import teammates.test.pageobjects.HomePage;
import teammates.test.pageobjects.LoginPage;
import teammates.test.pageobjects.NotFoundPage;

/**
 * We do not test all access control at UI level. This class contains a few
 * representative tests only. Access control is tested fully at 'Action' level.
 */
public class AllAccessControlUiTests extends BaseUiTestCase {
    
    private static String unregUsername = TestProperties.inst().TEST_UNREG_ACCOUNT;
    private static String unregPassword = TestProperties.inst().TEST_UNREG_PASSWORD;

    private static String studentUsername = TestProperties.inst().TEST_STUDENT1_ACCOUNT;
    private static String studentPassword = TestProperties.inst().TEST_STUDENT1_PASSWORD;
    
    private static String instructorUsername = TestProperties.inst().TEST_INSTRUCTOR_ACCOUNT;
    private static String instructorPassword = TestProperties.inst().TEST_INSTRUCTOR_PASSWORD;

    static String adminUsername = TestProperties.inst().TEST_ADMIN_ACCOUNT;

    private static Browser browser;
    private static DataBundle testData;
    private static String backDoorOperationStatus;
    private static AppPage currentPage;
    private static String link;

    private static InstructorAttributes otherInstructor;

    // both TEST_INSTRUCTOR and TEST_STUDENT are from this course
    private static CourseAttributes ownCourse;

    private static EvaluationAttributes ownEvaluation;
    
    @BeforeClass
    public static void classSetup() {

        printTestClassHeader();

        testData = getTypicalDataBundle();
        
        otherInstructor = testData.instructors.get("instructor1OfCourse2");
        ownCourse = testData.courses.get("typicalCourse1");
        ownEvaluation = testData.evaluations.get("evaluation1InCourse1");

        browser = BrowserPool.getBrowser();
        
        currentPage = HomePage.getNewInstance(browser);
        
    }
    
    @Test
    public void testUserNotLoggedIn() throws Exception {
        
        restoreTypicalTestData();
        
        currentPage.logout().verifyHtml("/login.html");

        ______TS("student pages");

        verifyRedirectToLogin(Const.ActionURIs.STUDENT_HOME_PAGE);
        

        ______TS("instructor pages");

        verifyRedirectToLogin(Const.ActionURIs.INSTRUCTOR_HOME_PAGE);
        

        ______TS("admin pages");

        verifyRedirectToLogin(Const.ActionURIs.ADMIN_HOME_PAGE);
        
        
    }

    @Test
    public void testUserNotRegistered() throws Exception {
        
        restoreTypicalTestData();

        ______TS("student pages");

        loginStudent(unregUsername, unregPassword);

        verifyRedirectToWelcomeStrangerPage(Const.ActionURIs.STUDENT_HOME_PAGE, unregUsername);


        ______TS("instructor pages");

        loginInstructorUnsuccessfully(unregUsername, unregPassword);

        Url url = createUrl(Const.ActionURIs.INSTRUCTOR_HOME_PAGE);
        verifyRedirectToNotAuthorized(url);
        verifyCannotMasquerade(url, otherInstructor.googleId);


        ______TS("admin pages");
        
        //cannot access admin while logged in as student
        verifyCannotAccessAdminPages();
        
        ______TS("incorrect URL");
        
        Url nonExistentActionUrl = new Url(TestProperties.inst().TEAMMATES_URL + "/page/nonExistentAction");
        @SuppressWarnings("unused") //We simply ensures it is the right page type
        NotFoundPage notFoundPage = AppPage.getNewPageInstance(browser, nonExistentActionUrl, NotFoundPage.class);

    }

    @Test
    public void testStudentAccessToAdminPages() throws Exception {
        restoreTypicalTestData();
        loginStudent(studentUsername, studentPassword);
        verifyCannotAccessAdminPages();
    }

    @Test
    public void testStudentHome() {
        
        restoreTypicalTestData();
        loginStudent(studentUsername, studentPassword);
        
        ______TS("cannot view other homepage");
        
        link = Const.ActionURIs.STUDENT_HOME_PAGE;
        verifyCannotMasquerade(link, otherInstructor.googleId);
    }



    @Test
    public void testStudentEvalSubmission() {
        
        restoreSpecialTestData();
        
        loginStudent(studentUsername, studentPassword);
        
        link = Const.ActionURIs.STUDENT_EVAL_SUBMISSION_EDIT_PAGE;
        link = Url.addParamToUrl(link, Const.ParamsNames.COURSE_ID, ownCourse.id);
        EvaluationAttributes ownEvaluation = testData.evaluations.get("evaluation1InCourse1");
        link = Url.addParamToUrl(link, Const.ParamsNames.EVALUATION_NAME,    ownEvaluation.name);
        
        ______TS("student cannot submit evaluation in AWAITING state");
    
        ownEvaluation.startTime = TimeHelper.getDateOffsetToCurrentTime(1);
        ownEvaluation.endTime = TimeHelper.getDateOffsetToCurrentTime(2);
        ownEvaluation.activated = false;
        assertEquals(EvalStatus.AWAITING, ownEvaluation.getStatus());
        backDoorOperationStatus = BackDoor.editEvaluation(ownEvaluation);
        assertEquals(Const.StatusCodes.BACKDOOR_STATUS_SUCCESS, backDoorOperationStatus);
        currentPage.navigateTo(createUrl(link))
            .verifyStatus(Const.StatusMessages.EVALUATION_NOT_OPEN);
        assertEquals("true", currentPage.getElementAttribute(By.id(Const.ParamsNames.POINTS + "0"), "disabled"));
        assertEquals("true", currentPage.getElementAttribute(By.id(Const.ParamsNames.JUSTIFICATION + "0"), "disabled"));
        assertEquals("true", currentPage.getElementAttribute(By.id(Const.ParamsNames.COMMENTS + "0"), "disabled"));
        assertEquals("true", currentPage.getElementAttribute(By.id("button_submit"), "disabled"));
        
        ______TS("student can view own evaluation submission page");
    
        link = Const.ActionURIs.STUDENT_EVAL_SUBMISSION_EDIT_PAGE;
        link = Url.addParamToUrl(link, Const.ParamsNames.COURSE_ID, ownCourse.id);
        link = Url.addParamToUrl(link, Const.ParamsNames.EVALUATION_NAME,
                ownEvaluation.name);
        verifyPageContains(link, studentUsername
                + "{*}Evaluation Submission{*}" + ownCourse.id + "{*}"
                + ownEvaluation.name);
    
        ______TS("student cannot submit evaluation in CLOSED state");
        
        ownEvaluation.startTime = TimeHelper.getDateOffsetToCurrentTime(-2);
        ownEvaluation.endTime = TimeHelper.getDateOffsetToCurrentTime(-1);
        backDoorOperationStatus = BackDoor.editEvaluation(ownEvaluation);
        assertEquals(Const.StatusCodes.BACKDOOR_STATUS_SUCCESS, backDoorOperationStatus);
        currentPage.navigateTo(createUrl(link))
            .verifyStatus(Const.StatusMessages.EVALUATION_NOT_OPEN);
        assertEquals("true", currentPage.getElementAttribute(By.id(Const.ParamsNames.POINTS + "0"), "disabled"));
        assertEquals("true", currentPage.getElementAttribute(By.id(Const.ParamsNames.JUSTIFICATION + "0"), "disabled"));
        assertEquals("true", currentPage.getElementAttribute(By.id(Const.ParamsNames.COMMENTS + "0"), "disabled"));
        assertEquals("true", currentPage.getElementAttribute(By.id("button_submit"), "disabled"));
        
        ______TS("student cannot submit evaluation in CLOSED state (evaluation with different timezone)");
        //Set the end time to the next hour, but push the timezone ahead 2 hours, so the evaluation has expired by 1 hour
        //Then we verify that the evaluation is disabled
        ownEvaluation.endTime = TimeHelper.getNextHour();
        ownEvaluation.timeZone = 10.0;   //put user's timezone ahead by 10hrs
        //TODO: this test case needs tweaking. It fails on some computers when
        //  the above is set to +2. Furthermore, we need a test case to ensure
        //  editing is enabled when the user timezone is behind. This test 
        //  case only checks if editing is disabled when timezone is ahead.
        backDoorOperationStatus = BackDoor.editEvaluation(ownEvaluation);
        assertEquals(Const.StatusCodes.BACKDOOR_STATUS_SUCCESS, backDoorOperationStatus);
        currentPage.navigateTo(createUrl(link))
            .verifyStatus(Const.StatusMessages.EVALUATION_NOT_OPEN);
        assertEquals("true", currentPage.getElementAttribute(By.id(Const.ParamsNames.POINTS + "0"), "disabled"));
        assertEquals("true", currentPage.getElementAttribute(By.id(Const.ParamsNames.JUSTIFICATION + "0"), "disabled"));
        assertEquals("true", currentPage.getElementAttribute(By.id(Const.ParamsNames.COMMENTS + "0"), "disabled"));
        assertEquals("true", currentPage.getElementAttribute(By.id("button_submit"), "disabled"));
        
        ______TS("student cannot submit evaluation in PUBLISHED state");
    
        ownEvaluation.endTime = TimeHelper.getDateOffsetToCurrentTime(-1);
        ownEvaluation.timeZone = 0.0;
        ownEvaluation.published = true;
        assertEquals(EvalStatus.PUBLISHED, ownEvaluation.getStatus());
        backDoorOperationStatus = BackDoor.editEvaluation(ownEvaluation);
        assertEquals(Const.StatusCodes.BACKDOOR_STATUS_SUCCESS, backDoorOperationStatus);
        currentPage.navigateTo(createUrl(link))
            .verifyStatus(Const.StatusMessages.EVALUATION_NOT_OPEN);
        assertEquals("true", currentPage.getElementAttribute(By.id(Const.ParamsNames.POINTS + "0"), "disabled"));
        assertEquals("true", currentPage.getElementAttribute(By.id(Const.ParamsNames.JUSTIFICATION + "0"), "disabled"));
        assertEquals("true", currentPage.getElementAttribute(By.id(Const.ParamsNames.COMMENTS + "0"), "disabled"));
        assertEquals("true", currentPage.getElementAttribute(By.id("button_submit"), "disabled"));
        
        deleteSpecialTestData();
        
    }

    @Test
    public void testStudentEvalResult() {
        restoreSpecialTestData();
        loginStudent(studentUsername, studentPassword);
        ______TS("student cannot view own evaluation result before publishing");
        
        ownEvaluation.published = false;
        assertTrue(EvalStatus.PUBLISHED != ownEvaluation.getStatus());
        backDoorOperationStatus = BackDoor.editEvaluation(ownEvaluation);
        assertEquals(Const.StatusCodes.BACKDOOR_STATUS_SUCCESS, backDoorOperationStatus);
    
        link = Const.ActionURIs.STUDENT_EVAL_RESULTS_PAGE;
        link = Url.addParamToUrl(link, Const.ParamsNames.COURSE_ID, ownCourse.id);
        link = Url.addParamToUrl(link, Const.ParamsNames.EVALUATION_NAME,
                ownEvaluation.name);
        verifyRedirectToNotAuthorized(link); //TODO: this error should be handled better.
    
        ______TS("student can view own evaluation result after publishing");
    
        ownEvaluation.startTime = TimeHelper.getDateOffsetToCurrentTime(-2);
        ownEvaluation.endTime = TimeHelper.getDateOffsetToCurrentTime(-1);
        ownEvaluation.timeZone = 0.0;
        ownEvaluation.published = true;
        assertEquals(EvalStatus.PUBLISHED, ownEvaluation.getStatus());
        backDoorOperationStatus = BackDoor.editEvaluation(ownEvaluation);
        assertEquals(Const.StatusCodes.BACKDOOR_STATUS_SUCCESS, backDoorOperationStatus);
        verifyPageContains(link, studentUsername + "{*}Evaluation Results{*}"
                + ownEvaluation.name + "{*}" + ownCourse.id);
        deleteSpecialTestData();
    }

    @Test
    public void testInstructorHome() {
    
        restoreSpecialTestData();
        
        loginInstructor(instructorUsername, instructorPassword);
    
        ______TS("cannot view other homepage");
    
        link = Const.ActionURIs.INSTRUCTOR_HOME_PAGE;
        verifyCannotMasquerade(link, otherInstructor.googleId);
        
        deleteSpecialTestData();
    }
    
    @Test
    public void testPubliclyAccessiblePages() {
        
        ______TS("log out page");
        // has been covered in testUserNotLoggedIn method
        
        ______TS("unauthorized page");
        Url url = createUrl(Const.ViewURIs.UNAUTHORIZED);
        currentPage.navigateTo(url);
        verifyRedirectToNotAuthorized();
        
        ______TS("error page");
        url = createUrl(Const.ViewURIs.ERROR_PAGE);
        currentPage.navigateTo(url);
        currentPage.verifyHtml("/errorPage.html");
        
        ______TS("deadline exceeded error page");
        url = createUrl(Const.ViewURIs.DEADLINE_EXCEEDED_ERROR_PAGE);
        currentPage.navigateTo(url);
        currentPage.verifyHtml("/deadlineExceededErrorPage.html");
        
        ______TS("entity not found page");
        url = createUrl(Const.ViewURIs.ENTITY_NOT_FOUND_PAGE);
        currentPage.navigateTo(url);
        currentPage.verifyHtml("/entityNotFoundPage.html");
        
        ______TS("action not found page");
        url = createUrl(Const.ViewURIs.ACTION_NOT_FOUND_PAGE);
        currentPage.navigateTo(url);
        currentPage.verifyHtml("/pageNotFound.html");
        
        ______TS("show message page");
        url = createUrl(Const.ViewURIs.SHOW_MESSAGE);
        currentPage.navigateTo(url);
        currentPage.verifyHtml("/showMessage.html");
        
        ______TS("maintenance page");
        url = createUrl(Const.ViewURIs.MAINTENANCE_PAGE);
        currentPage.navigateTo(url);
        currentPage.verifyHtml("/maintenance.html");
    }
    

    private void loginStudent(String userName, String password) {
        currentPage.logout();
        LoginPage loginPage = HomePage.getNewInstance(browser).clickStudentLogin();
        currentPage = loginPage.loginAsStudent(userName, password);
    }
    
    private void loginInstructorUnsuccessfully(String userName, String password) {
        currentPage.logout();
        LoginPage loginPage = HomePage.getNewInstance(browser).clickInstructorLogin();
        currentPage = loginPage.loginAsInstructorUnsuccessfully(userName, password);
    }
    
    private void loginInstructor(String userName, String password) {
        currentPage.logout();
        LoginPage loginPage = HomePage.getNewInstance(browser).clickInstructorLogin();
        currentPage = loginPage.loginAsInstructor(userName, password);
    }

    private static void restoreTypicalTestData() {
        
        testData = getTypicalDataBundle();
        
        // This test suite requires some real accounts; Here, we inject them to the test data.
        testData.students.get("student1InCourse1").googleId = TestProperties.inst().TEST_STUDENT1_ACCOUNT;
        testData.instructors.get("instructor1OfCourse1").googleId = TestProperties.inst().TEST_INSTRUCTOR_ACCOUNT;
        
        String backDoorOperationStatus = BackDoor.restoreDataBundle(testData); 
        assertEquals(Const.StatusCodes.BACKDOOR_STATUS_SUCCESS, backDoorOperationStatus);
    }
    
    private static void restoreSpecialTestData() {
        
        testData = getTypicalDataBundle();
        
        // This test suite requires some real accounts; Here, we inject them to the test data.
        testData.students.get("student1InCourse1").googleId = TestProperties.inst().TEST_STUDENT1_ACCOUNT;
        testData.instructors.get("instructor1OfCourse1").googleId = TestProperties.inst().TEST_INSTRUCTOR_ACCOUNT;
        
        String backDoorOperationStatus = BackDoor.restoreDataBundle(testData); 
        assertEquals(Const.StatusCodes.BACKDOOR_STATUS_SUCCESS, backDoorOperationStatus);
    }

    private void verifyCannotAccessAdminPages() {
        //cannot access directly
        Url url = createUrl(Const.ActionURIs.ADMIN_HOME_PAGE);
        verifyRedirectToNotAuthorized(url);
        //cannot access by masquerading either
        url = url.withUserId(adminUsername);
        verifyRedirectToNotAuthorized(url);
    }

    private void verifyCannotMasquerade(String link, String otherInstructorId) {
        link = Url.addParamToUrl(link, Const.ParamsNames.USER_ID, otherInstructorId);
        verifyRedirectToNotAuthorized(link);
    }
    
    private void verifyCannotMasquerade(Url url, String otherInstructorId) {
        verifyRedirectToNotAuthorized(url.withUserId(otherInstructorId));
    }

    private void verifyRedirectToWelcomeStrangerPage(String path, String unregUsername) {
        printUrl(appUrl + path);
        currentPage.navigateTo(createUrl(path));
        // A simple regex check is enough because we do full HTML tests
        // elsewhere
        AssertHelper.assertContainsRegex("{*}" + unregUsername + "{*}Welcome stranger{*}",
                currentPage.getPageSource());
    }

    private void verifyRedirectToNotAuthorized() {
        String pageSource = currentPage.getPageSource();
        //TODO: Distinguish between these two types of access denial
        assertTrue(pageSource.contains("You are not authorized to view this page.")||
                pageSource.contains("Your client does not have permission"));
    }

    private void verifyRedirectToNotAuthorized(String path) {
        printUrl(appUrl + path);
        currentPage.navigateTo(createUrl(path));
        verifyRedirectToNotAuthorized();
    }
    
    private void verifyRedirectToNotAuthorized(Url url) {
        printUrl(url.toString());
        currentPage.navigateTo(url);
        verifyRedirectToNotAuthorized();
    }

    private void verifyPageContains(String path, String targetText) {
        printUrl(appUrl + path);
        currentPage.navigateTo(createUrl(path));
        AssertHelper.assertContainsRegex(targetText, currentPage.getPageSource());
    }

    private void verifyRedirectToLogin(String path) {
        printUrl(appUrl + path);
        currentPage.navigateTo(createUrl(path));
        assertTrue(isLoginPage(currentPage));
    }

    private boolean isLoginPage(AppPage currentPage) {
        return GoogleLoginPage.containsExpectedPageContents(currentPage.getPageSource())
                || DevServerLoginPage.containsExpectedPageContents(currentPage.getPageSource());
    }

    private void printUrl(String url) {
        print("   " + url);
    }

    @AfterClass
    public static void classTearDown() throws Exception {
        
        //delete any data related to real accounts used in testing (to prevent state leakage to other tests)
        deleteSpecialTestData();
        BrowserPool.release(browser);
    }

    private static void deleteSpecialTestData() {
        StudentAttributes student = testData.students.get("student1InCourse1");
        BackDoor.deleteStudent(student.course, student.email);
        InstructorAttributes instructor = testData.instructors.get("instructor1OfCourse1");
        BackDoor.deleteInstructor(instructor.courseId, instructor.email);
        BackDoor.deleteCourse(student.course);
    }

}
