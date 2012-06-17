package teammates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import teammates.api.Common;
import teammates.api.EntityAlreadyExistsException;
import teammates.api.EntityDoesNotExistException;
import teammates.api.InvalidParametersException;
import teammates.api.Logic;
import teammates.datatransfer.CoordData;
import teammates.datatransfer.CourseData;
import teammates.datatransfer.DataBundle;
import teammates.datatransfer.EvaluationData;
import teammates.datatransfer.StudentActionData;
import teammates.datatransfer.StudentData;
import teammates.datatransfer.SubmissionData;
import teammates.datatransfer.TeamProfileData;
import teammates.datatransfer.TfsData;
import teammates.manager.TeamForming;

public class BackDoorLogic extends Logic{
	
	Logger log = Common.getLogger();
	
	/**
	 * Persists given data in the datastore Works ONLY if the data is correct
	 * and new (i.e. these entities do not already exist in the datastore). The
	 * behavior is undefined if incorrect or not new.
	 * 
	 * @param dataBundleJsonString
	 * @return status of the request in the form 'status meassage'+'additional
	 *         info (if any)' e.g., "[BACKEND_STATUS_SUCCESS]" e.g.,
	 *         "[BACKEND_STATUS_FAILURE]NullPointerException at ..."
	 * @throws EntityAlreadyExistsException
	 * @throws InvalidParametersException
	 * @throws Exception
	 */

	public String persistNewDataBundle(DataBundle dataBundle)
			throws InvalidParametersException, EntityAlreadyExistsException {

		if (dataBundle == null) {
			throw new InvalidParametersException(
					Common.ERRORCODE_NULL_PARAMETER, "Null data bundle");
		}

		HashMap<String, CoordData> coords = dataBundle.coords;
		for (CoordData coord : coords.values()) {
			log.fine("API Servlet adding coord :" + coord.id);
			super.createCoord(coord.id, coord.name, coord.email);
		}

		HashMap<String, CourseData> courses = dataBundle.courses;
		for (CourseData course : courses.values()) {
			log.fine("API Servlet adding course :" + course.id);
			createCourse(course.coord, course.id, course.name);
		}

		HashMap<String, StudentData> students = dataBundle.students;
		for (StudentData student : students.values()) {
			log.fine("API Servlet adding student :" + student.email
					+ " to course " + student.course);
			createStudent(student);
		}

		HashMap<String, EvaluationData> evaluations = dataBundle.evaluations;
		for (EvaluationData evaluation : evaluations.values()) {
			log.fine("API Servlet adding evaluation :" + evaluation.name
					+ " to course " + evaluation.course);
			createEvaluation(evaluation);
		}

		// processing is slightly different for submissions because we are
		// adding all submissions in one go
		HashMap<String, SubmissionData> submissionsMap = dataBundle.submissions;
		List<SubmissionData> submissionsList = new ArrayList<SubmissionData>();
		for (SubmissionData submission : submissionsMap.values()) {
			log.fine("API Servlet adding submission for "
					+ submission.evaluation + " from " + submission.reviewer
					+ " to " + submission.reviewee);
			submissionsList.add(submission);
		}
		createSubmissions(submissionsList);
		log.fine("API Servlet added " + submissionsList.size() + " submissions");

		HashMap<String, TfsData> tfsMap = dataBundle.teamFormingSessions;
		for (TfsData tfs : tfsMap.values()) {
			log.fine("API Servlet adding TeamFormingSession to course "
					+ tfs.course);
			createTfs(tfs);
		}

		HashMap<String, TeamProfileData> teamProfiles = dataBundle.teamProfiles;
		for (TeamProfileData teamProfile : teamProfiles.values()) {
			log.fine("API Servlet adding TeamProfile of " + teamProfile.team
					+ " in course " + teamProfile.course);
			createTeamProfile(teamProfile);
		}

		HashMap<String, StudentActionData> studentActions = dataBundle.studentActions;
		for (StudentActionData studentAction : studentActions.values()) {
			log.fine("API Servlet adding StudentActionData in course "
					+ studentAction.course + " : "
					+ studentAction.action.getValue());
			createStudentAction(studentAction);
		}

		return Common.BACKEND_STATUS_SUCCESS;
	}
	
	public String getCoordAsJson(String coordID) {
		CoordData coordData = getCoord(coordID);
		return Common.getTeammatesGson().toJson(coordData);
	}

	public String getCourseAsJson(String courseId) {
		CourseData course = getCourse(courseId);
		return Common.getTeammatesGson().toJson(course);
	}

	public String getStudentAsJson(String courseId, String email) {
		StudentData student = getStudent(courseId, email);
		return Common.getTeammatesGson().toJson(student);
	}

	public String getEvaluationAsJson(String courseId, String evaluationName) {
		EvaluationData evaluation = getEvaluation(courseId, evaluationName);
		return Common.getTeammatesGson().toJson(evaluation);
	}

	public String getSubmissionAsJson(String courseId, String evaluationName,
			String reviewerEmail, String revieweeEmail) {
		SubmissionData target = getSubmission(courseId, evaluationName,
				reviewerEmail, revieweeEmail);
		return Common.getTeammatesGson().toJson(target);
	}

	public String getTfsAsJson(String courseId) {
		TfsData tfs = getTfs(courseId);
		return Common.getTeammatesGson().toJson(tfs);
	}

	public String getTeamProfileAsJson(String courseId, String teamName) {
		TeamProfileData teamProfile = getTeamProfile(courseId, teamName);
		return Common.getTeammatesGson().toJson(teamProfile);
	}

	public String getTeamFormingLogAsJson(String courseId)
			throws EntityDoesNotExistException {
		List<StudentActionData> teamFormingLogList = getStudentActions(courseId);
		return Common.getTeammatesGson().toJson(teamFormingLogList);
	}
	
	public void editStudentAsJson(String originalEmail, String newValues)
			throws InvalidParametersException, EntityDoesNotExistException {
		StudentData student = Common.getTeammatesGson().fromJson(newValues,
				StudentData.class);
		editStudent(originalEmail, student);
	}

	public void editEvaluationAsJson(String evaluationJson)
			throws InvalidParametersException, EntityDoesNotExistException {
		EvaluationData evaluation = Common.getTeammatesGson().fromJson(
				evaluationJson, EvaluationData.class);
		editEvaluation(evaluation);
	}

	public void editSubmissionAsJson(String submissionJson) throws InvalidParametersException, EntityDoesNotExistException {
		SubmissionData submission = Common.getTeammatesGson().fromJson(
				submissionJson, SubmissionData.class);
		ArrayList<SubmissionData> submissionList = new ArrayList<SubmissionData>();
		submissionList.add(submission);
		editSubmissions(submissionList);
	}

	public void editTfsAsJson(String tfsJson)
			throws EntityDoesNotExistException {
		TfsData tfs = Common.getTeammatesGson()
				.fromJson(tfsJson, TfsData.class);
		TeamForming.inst().editTeamFormingSession(tfs.course, tfs.startTime,
				tfs.endTime, tfs.gracePeriod, tfs.instructions,
				tfs.profileTemplate, tfs.activated, tfs.timeZone);
	}

	public void editTeamProfileAsJson(String originalTeamName,
			String teamProfileJson) throws EntityDoesNotExistException {
		TeamProfileData teamProfile = Common.getTeammatesGson().fromJson(
				teamProfileJson, TeamProfileData.class);
		TeamForming.inst().editTeamProfile(teamProfile.course, "",
				originalTeamName, teamProfile.team, teamProfile.profile);
	}


}