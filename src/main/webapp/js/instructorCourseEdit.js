//Add course status codes
var COURSE_STATUS_SERVERERROR = -1;
var COURSE_STATUS_VALID_INPUT = 0;
var COURSE_STATUS_EMPTY = 3;
var COURSE_STATUS_LONG_ID = 4;
var COURSE_STATUS_LONG_NAME = 5;
var COURSE_STATUS_INVALID_ID = 6;
var COURSE_STATUS_INSTRUCTOR_LIST_EMPTY = 7;
var COURSE_STATUS_INSTRUCTOR_LIST_FIELDS_MISSING = 8;
var COURSE_STATUS_INSTRUCTOR_LIST_FIELDS_EXTRA = 9;
var COURSE_STATUS_INSTRUCTOR_LIST_NAME_INVALID = 10;
var COURSE_STATUS_INSTRUCTOR_LIST_EMAIL_INVALID = 11;

function verifyInstructorList() {
	var instructorList = $("#" + COURSE_INSTRUCTOR_LIST).val();
	if(instructorList.trim() == ""){
		setStatusMessage(DISPLAY_COURSE_INSTRUCTOR_LIST_EMPTY, true);
		return false;
	}
	
	// client-side validation
	var confirmation = true;
	if (!checkInstructorWithinInstructorList($("#" + COURSE_INSTRUCTOR_ID).val(), instructorList)){
		confirmation = confirm(MESSAGE_INSTRUCTOR_NOT_WHTHIN_INSTRUCTOR_LIST);
	}
	
	if (confirmation){		
		var statusCode = isCourseInstructorListValid(instructorList);
		if(statusCode != COURSE_STATUS_VALID_INPUT) {
			setStatusMessage(courseStatusToMessage(statusCode),true);
			return false;
		} else {
			return true;
		}
	} else {
		return false;
	}
}

/**
 * Checks if current logged in person appears in the instructor list
 */
function checkInstructorWithinInstructorList(instructorID, instructorList){
	var entries = instructorList.split("\n");
	for (var i = 0; i < entries.length; i++){
		if(entries[i].search(instructorID) == 0){
			return true;
		}
	}
	return false;
}


function isCourseInstructorListValid(input) {
	input = input.replace(/\t/g,"|");
	var entries = input.split("\n");
	var fields;

	var entriesLength = entries.length;
	for ( var x = 0; x < entriesLength; x++) {
		if (entries[x] != "") {
			// Separate the fields
			fields = entries[x].split("|");
			var fieldsLength = fields.length;
			console.log(fieldsLength);
			// Make sure that all fields are present and valid
			if (fieldsLength<3) {
				return COURSE_STATUS_INSTRUCTOR_LIST_FIELDS_MISSING;
			} else if(fieldsLength>3){
				return COURSE_STATUS_INSTRUCTOR_LIST_FIELDS_EXTRA;
			} else if (!isNameValid(fields[1].trim())) {
				return COURSE_STATUS_INSTRUCTOR_LIST_NAME_INVALID;
			} else if (!isEmailValid(fields[2].trim())) {
				return COURSE_STATUS_INSTRUCTOR_LIST_EMAIL_INVALID;
			}
		}
	}
	return COURSE_STATUS_VALID_INPUT;
}


/**
 * Converts error codes into displayable message
 * @param statusCode
 * @returns
 */
function courseStatusToMessage(statusCode) {
	switch (statusCode) {
	case COURSE_STATUS_EMPTY:
		return DISPLAY_COURSE_MISSING_FIELD;
	case COURSE_STATUS_LONG_ID:
		return DISPLAY_COURSE_LONG_ID;
	case COURSE_STATUS_LONG_NAME:
		return DISPLAY_COURSE_LONG_NAME;
	case COURSE_STATUS_INVALID_ID:
		return DISPLAY_COURSE_INVALID_ID;
	case COURSE_STATUS_INSTRUCTOR_LIST_EMPTY:
		return DISPLAY_COURSE_INSTRUCTOR_LIST_EMPTY;
	case COURSE_STATUS_INSTRUCTOR_LIST_FIELDS_MISSING:
		return DISPLAY_INPUT_FIELDS_MISSING;
	case COURSE_STATUS_INSTRUCTOR_LIST_FIELDS_EXTRA:
		return DISPLAY_INPUT_FIELDS_EXTRA;
	case COURSE_STATUS_INSTRUCTOR_LIST_NAME_INVALID:
		return DISPLAY_NAME_INVALID;
	case COURSE_STATUS_INSTRUCTOR_LIST_EMAIL_INVALID:
		return DISPLAY_EMAIL_INVALID;
	default:
		return DISPLAY_INVALID_INPUT;
	}
}