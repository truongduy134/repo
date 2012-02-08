package teammates.jdo;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.gson.annotations.SerializedName;

/**
 * TeamForming is a persistent data class that holds information pertaining to an
 * TeamForming Session on Teammates.
 * 
 * @author Kalpit Jain
 * 
 */
@PersistenceCapable
public class TeamForming {
	@SuppressWarnings("unused")
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;

	@Persistent
	@SerializedName("course_id")
	private String courseID;

	@Persistent
	@SerializedName("timezone")
	private double timeZone;
	
	@Persistent
	@SerializedName("start_time")
	private Date startTime;
	
	@Persistent
	@SerializedName("end_time")
	private Date endTime;

	@Persistent
	@SerializedName("grace")
	private int gracePeriod;
	
	@Persistent
	@SerializedName("instr")
	private String instructions;
	
	@Persistent
	@SerializedName("profile_template")
	private String profileTemplate;

	@Persistent
	private boolean activated;

	/**
	 * Constructs an Evaluation object.
	 * 
	 * @param courseID
	 * @param timeZone	
	 * @param start
	 * @param deadline
	 * @param gracePeriod
	 * @param instructions
	 * @param profileTemplate
	 */
	public TeamForming(String courseID, double timeZone, Date start, Date deadline, int gracePeriod,
			String instructions, String profileTemplate) {
		this.setCourseID(courseID);
		this.setTimeZone(timeZone);
		this.setStart(start);
		this.setDeadline(deadline);
		this.setGracePeriod(gracePeriod);
		this.setInstructions(instructions);
		this.setProfileTemplate(profileTemplate);		
	}

	public void setCourseID(String courseID) {
		this.courseID = courseID;
	}

	public String getCourseID() {
		return courseID;
	}
	
	public void setTimeZone(double timeZone2) {
		this.timeZone = timeZone2;
	}

	public double getTimeZone() {
		return timeZone;
	}
	
	public void setStart(Date start) {
		this.startTime = start;
	}

	public Date getStart() {
		return startTime;
	}

	public void setDeadline(Date deadline) {
		this.endTime = deadline;
	}

	public Date getDeadline() {
		return endTime;
	}

	public void setGracePeriod(int gracePeriod) {
		this.gracePeriod = gracePeriod;
	}

	public int getGracePeriod() {
		return gracePeriod;
	}

	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}

	public String getInstructions() {
		return instructions;
	}
	
	public void setProfileTemplate(String profileTemplate) {
		this.profileTemplate = profileTemplate;
	}

	public String getProfileTemplate() {
		return profileTemplate;
	}

	public void setActivated(boolean activated) {
		this.activated = activated;
	}

	public boolean isActivated() {
		return activated;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("courseID: " + courseID);
		sb.append("\nstarttime: " + startTime);
		sb.append("\nendtime: " + endTime);
		sb.append("\ninstruction: " + instructions);
		sb.append("\nprofiletemplate:" + profileTemplate);
		return sb.toString();
	}
}