package com.bipro.ths.controller;

import java.io.IOException;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.bipro.ths.model.Meeting;
import com.bipro.ths.model.User;
import com.bipro.ths.service.MeetingService;
import com.bipro.ths.service.RoleService;
import com.bipro.ths.service.UserService;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets.Details;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.*;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar.Events;
import com.google.api.services.calendar.CalendarScopes;


@Controller
@RequestMapping(value = "/googleCal")
public class GoogleCalController {

	@Autowired
	private UserService userService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private MeetingService meetingService;

	private final static Log logger = LogFactory.getLog(GoogleCalController.class);
	private static final String APPLICATION_NAME = "ths";
	private static HttpTransport httpTransport;
	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	private static com.google.api.services.calendar.Calendar client;

	GoogleClientSecrets clientSecrets;
	GoogleAuthorizationCodeFlow flow;
	Credential credential;

	@Value("${google.client.client-id}")
	private String clientId;
	@Value("${google.client.client-secret}")
	private String clientSecret;
	@Value("${google.client.redirectUri}")
	private String redirectURI;

	private Set<Event> events = new HashSet<>();

	final DateTime date1 = new DateTime("2017-05-05T16:30:00.000+05:30");
	final DateTime date2 = new DateTime(new Date());

//	public void setEvents(Set<Event> events) {
//		this.events = events;
//	}

	String redirectControllerName = "";

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public RedirectView googleConnectionStatus(HttpServletRequest request) throws Exception {
		return new RedirectView(authorize());
	}

	@RequestMapping(value = "/login", method = RequestMethod.GET, params = "code")
	public String oauth2Callback(@RequestParam(value = "code") String code) {
		com.google.api.services.calendar.model.Events eventList;
		String message;
		try {
			TokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectURI).execute();
			credential = flow.createAndStoreCredential(response, "userID");
			client = new com.google.api.services.calendar.Calendar.Builder(httpTransport, JSON_FACTORY, credential)
					.setApplicationName(APPLICATION_NAME).build();
//			Events events = client.events();
////////			eventList = events.list("primary").setTimeMin(date1).setTimeMax(date2).execute();
////////			message = eventList.getItems().toString();
//			System.out.println("My:" + eventList.getItems());
		} catch (Exception e) {
			logger.warn("Exception while handling OAuth2 callback (" + e.getMessage() + ")."
					+ " Redirecting to google connection status page.");
			message = "Exception while handling OAuth2 callback (" + e.getMessage() + ")."
					+ " Redirecting to google connection status page.";
		}

//		System.out.println("cal message:" + message);
		return redirectControllerName;
//		return new ResponseEntity<>(message, HttpStatus.OK);
	}

	public Set<Event> getEvents() throws IOException {
		return this.events;
	}

	private String authorize() throws Exception {
		AuthorizationCodeRequestUrl authorizationUrl;
		if (flow == null) {
			Details web = new Details();
			web.setClientId(clientId);
			web.setClientSecret(clientSecret);
			clientSecrets = new GoogleClientSecrets().setWeb(web);
			httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets,
					Collections.singleton(CalendarScopes.CALENDAR)).build();
		}
		authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(redirectURI);
		System.out.println("cal authorizationUrl->" + authorizationUrl);
		return authorizationUrl.build();
	}

	@RequestMapping(value = "/addEventsLoginLoader", method = RequestMethod.GET)
	public String addEventsLoginLoader(){
		redirectControllerName = "redirect:/googleCal/doctorsList";
		return "redirect:/googleCal/login";
	}

	@RequestMapping(value = "/doctorsList", method = RequestMethod.GET)
	public String addEventsDoctorsList(ModelMap model, HttpServletRequest request, HttpSession session, Principal principal){
		List<User> userList = userService.findAllByRoles(roleService.findAllByName("DOCTOR"));
		model.addAttribute("userList", userList);
		return "googleCal/doctorsList";
	}



	@RequestMapping(value = "/viewEventsLoginLoader", method = RequestMethod.GET)
	public String viewEventsLoginLoader(){
		redirectControllerName = "redirect:/googleCal/viewEvents";
		return "redirect:/googleCal/login";
}

	@RequestMapping(value = "/viewEvents", method = RequestMethod.GET)
	public String viewEvents(ModelMap model, HttpSession session) throws Exception {

		String patientId = String.valueOf(session.getAttribute("currentUserId"));
		List<Meeting> meetingList = meetingService.findByPatientId(Long.parseLong( patientId ));
		if (meetingList.isEmpty()){
			return "googleCal/viewEvents";
		}
		List<String> eventIdList = new ArrayList<>();
		for ( Meeting meeting:	meetingList	 ) {
//			System.out.println("meeting.getEventId " + meeting.getEventId());
			eventIdList.add( meeting.getEventId() );
		}
//		eventIdList.add("9pn3euhdrotdkjqbf9o9os66cs");
//		eventIdList.add("9pn3euhdrotdkjqbf9o9os66cs");
		com.google.api.services.calendar.model.Events eventList;
		client = new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
				.setApplicationName(APPLICATION_NAME).build();
		// Retrieve the calendar
//		com.google.api.services.calendar.model.Calendar calendar =
//				client.calendars().get("primary").execute();

		Events events = client.events();
		List<Event> eventsList = new ArrayList<>();
		for ( String eventId:	eventIdList	 ) {
			Event event = client.events().get("primary", eventId).execute();
//			System.out.println("Event Creator 555 "+ event.getCreator().getEmail());
			System.out.println("Event E Id 555 "+ event.getId());
			// Move an event to another calendar

//			event =	client.events().move("primary", event.getId(), "bipro5500@gmail.com").setSendNotifications(true).execute();
//			Calendar service = new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
//					.setApplicationName("applicationName").build();
// Retrieve the event from the API
//			Event event00 = service.events().get("primary", eventId).execute();
// Move an event to another calendar
//			Event updatedEvent =
//					service.events().move("primary", eventId, "bipro5500@gmail.com").execute();
//event.setOrganizer(new Event.Organizer().setEmail("bipro5500@gmail.com"));
// Update the event
//            Event updatedEvent = service.events().update("primary", event.getId(), event).execute();
//            System.out.println("Organizer 999 "+updatedEvent.getOrganizer().getEmail());
//			System.out.println(updatedEvent.getUpdated());
//			System.out.println("55555a " + event.getStart().getDate());
//			System.out.println("55555b " + event.getStart().getTimeZone());
//			System.out.println("55555c " + event.getStart().getDateTime().toStringRfc3339());
//			System.out.println("55555c1 " + event.getStart().getDateTime());
//			System.out.println("55555d " + event.getStart().getDateTime().toString());
//			System.out.println("55555e " + event.getStart().getDateTime().getValue());
//			event.getStart().setDate();
//			System.out.println("55555 " +  event.getStart().getDateTime());
			eventsList.add(event);
		}
		model.addAttribute("eventsList", eventsList);
//			eventList = events.list("primary").setTimeMin(date1).setTimeMax(date2).execute();

		// Retrieve an event
//		Event event = client.events().get("primary", "eventId").execute();
//            model.addAttribute("usernameList", usernameList);
//		Event event = client.events().get("primary", "9pn3euhdrotdkjqbf9o9os66cs").execute();
//		System.out.println(event.getSummary());
//		Event updatedEvent =

		return "googleCal/viewEvents";
	}

	@RequestMapping(value = "/viewEventsAll", method = RequestMethod.GET)
	public String viewEventsAll() throws Exception {
		com.google.api.services.calendar.model.Events eventList;
		authorize();
		client = new com.google.api.services.calendar.Calendar.Builder(httpTransport, JSON_FACTORY, credential)
				.setApplicationName(APPLICATION_NAME).build();
		Events events = client.events();
		eventList = events.list("primary").setTimeMin(date1).setTimeMax(date2).execute();
			System.out.println("My:" + eventList.getItems());
		return "googleCal/viewEvents";
	}


		@RequestMapping(value = "/addEvents/{doctorId}/{emailDoctor}", method = RequestMethod.GET)
	public String addEventsForm(ModelMap model, HttpServletRequest request, HttpSession session, Principal principal,
								@PathVariable("doctorId") String doctorId, @PathVariable("emailDoctor") String emailDoctor	){
			model.addAttribute("emailDoctor", emailDoctor);
			session.setAttribute("doctorId",doctorId);
		return "googleCal/addEvents";
	}

	@RequestMapping(value = "/addEventsSubmit", method = RequestMethod.POST)
	public String addEventsSubmit( ModelMap model, HttpSession session, @RequestParam("name") String name, @RequestParam("date") String date,
								   @RequestParam("time") String time, @RequestParam("description") String description,
								 @RequestParam("emailDoctor") String emailDoctor) throws ParseException, IOException {
		String day = date.substring(0,2);
		String month = date.substring(3,5);
		String year = date.substring(6,10);
		String hour = time.substring(0,1);
		String minute = time.substring(2,4);
		String dateTimeStart = year+"-"+month+"-"+day+"T0"+hour+":"+minute+":00+06:00";

		SimpleDateFormat df = new SimpleDateFormat("HH:mm");
		Date d = df.parse(hour+":"+minute);
		java.util.Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(d);
		cal.add(cal.MINUTE, 30);
		String newTime = df.format(cal.getTime());
		String dateTimeEnd = year+"-"+month+"-"+day+"T"+newTime+":00+06:00";

		String emailPatient = (String) session.getAttribute("currentUserEmail");
//		System.out.println("emailDoctor "+emailDoctor);
//		System.out.println("emailPatient "+emailPatient);
		setEvents(session, name, dateTimeStart, dateTimeEnd, description, emailDoctor, emailPatient);
//		model.addAttribute("eventsList", eventsList);
////		return "googleCal/viewEvents";
		return "redirect:/googleCal/viewEvents";
	}

	@RequestMapping(value = "/setEvents", method = RequestMethod.GET)
	public void setEvents(  HttpSession session, String summary,  String dateTimeStart, String dateTimeEnd,
							String description,  String emailDoctor,  String emailPatient) throws IOException {
		client = new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
				.setApplicationName(APPLICATION_NAME).build();
		Event event = new Event()
				.setSummary("Doctor's Appointment Confirmation - Telemedicine and Healthcare Service")
				.setLocation("Suhrawardi Udyan Rd, Dhaka 1200")
				.setDescription(description)
				.setStatus("confirmed");

		//  Set Event Creator
//		Event.Creator creator = new Event.Creator();
//		creator.setEmail("bipro5500@gmail.com");
//		event.setCreator(creator);
//		event.setAnyoneCanAddSelf(true);

//		DateTime startDateTime = new DateTime("2015-05-28T09:00:00-07:00");
		DateTime startDateTime = new DateTime(dateTimeStart);
		EventDateTime start = new EventDateTime()
				.setDateTime(startDateTime)
				.setTimeZone("Asia/Dhaka");
		event.setStart(start);

		DateTime endDateTime = new DateTime(dateTimeEnd);
		EventDateTime end = new EventDateTime()
				.setDateTime(endDateTime)
				.setTimeZone("Asia/Dhaka");
		event.setEnd(end);

		String[] recurrence = new String[] {"RRULE:FREQ=DAILY;COUNT=1"};
		event.setRecurrence(Arrays.asList(recurrence));

		EventAttendee[] attendees = new EventAttendee[] {
				new EventAttendee().setEmail(emailDoctor),
				new EventAttendee().setEmail(emailPatient),
		};
		event.setAttendees(Arrays.asList(attendees));
		Event.Organizer organizer = new Event.Organizer();
		organizer.setEmail(emailDoctor).setDisplayName("Doctor");
		organizer.setSelf(false);
		organizer.setId(emailDoctor);
		event.setOrganizer(organizer);

//		// Update the event
//		Event updatedEvent = client.events().update("primary", event.getId(), event).execute();

//		System.out.println(updatedEvent.getUpdated());

		EventReminder[] reminderOverrides = new EventReminder[] {
				new EventReminder().setMethod("email").setMinutes(24 * 60),
				new EventReminder().setMethod("popup").setMinutes(10),
		};
		Event.Reminders reminders = new Event.Reminders()
				.setUseDefault(false)
				.setOverrides(Arrays.asList(reminderOverrides));
		event.setReminders(reminders);

		ConferenceSolutionKey conferenceSKey = new ConferenceSolutionKey();
		conferenceSKey.setType("hangoutsMeet"); // Non-G suite user
		CreateConferenceRequest createConferenceReq = new CreateConferenceRequest();
		createConferenceReq.setRequestId("bipro77777"); // Random ID generated by you
		createConferenceReq.setConferenceSolutionKey(conferenceSKey);
		ConferenceData conferenceData = new ConferenceData();
		conferenceData.setCreateRequest(createConferenceReq);
		event.setConferenceData(conferenceData);
		//.setAnyoneCanAddSelf(true).setGuestsCanModify(true).setGuestsCanSeeOtherGuests(true).setOrganizer(organizer);
		String calendarId = "primary";
//			event = client.events().insert(calendarId, event).execute();
		event = client.events().insert(calendarId, event).setConferenceDataVersion(1).setSendNotifications(true).execute();
//		event =	client.events().move("primary", event.getId(), emailDoctor).setSendNotifications(true).execute();
		String meetingId = event.getHangoutLink();
		System.out.println("What is HangoutLink ? = "+meetingId);
		System.out.printf("Event created: %s\n", event.getHtmlLink());
		System.out.printf("Event ID: %s\n", event.getId());


		Meeting meeting = new Meeting();
		meeting.setEventId(event.getId());
//		System.out.println("77777 "+ (String)session.getAttribute("doctorId"));
//		System.out.println("77777 "+ String.valueOf(session.getAttribute("currentUserId")));
		String doctorId = (String) session.getAttribute("doctorId");
		String patientId = String.valueOf(session.getAttribute("currentUserId"));
		meeting.setDoctorId( Long.parseLong( doctorId ));
		meeting.setPatientId( Long.parseLong( patientId ));
//		System.out.println("55555  "+meeting.getEventId() + "  " + meeting.getDoctorId()+ "  " + meeting.getPatientId());
		meetingService.save(meeting);



//		// Create access rule with associated scope
//		AclRule rule = new AclRule();
//		AclRule.Scope scope = new AclRule.Scope();
//		scope.setType("scopeType").setValue("default");
//		rule.setScope(scope).setRole("owner");
//
//// Insert new access rule
//		AclRule createdRule = client.acl().insert("primary", rule).execute();
//		System.out.println(createdRule.getId());
//		event =	client.events().move("primary", event.getId(), emailDoctor).setSendNotifications(true).execute();
	}

	@RequestMapping(value = "/delEvents/{eventId}", method = RequestMethod.GET)
	public String delEvents(@PathVariable("eventId") String eventId) throws Exception {
		// Initialize Calendar service with valid OAuth credentials
		client = new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
				.setApplicationName(APPLICATION_NAME).build();
		Meeting meeting =  meetingService.findMeetingByEventId(eventId);
//		System.out.println("meetingId " + meeting.getId());
		meetingService.delete(meeting);
		// Delete an event  .setSendNotifications(true)
		client.events().delete("primary", eventId).setSendUpdates("all").setSendNotifications(true).execute();
		return "redirect:/googleCal/viewEvents";
	}

	@RequestMapping(value = "/move", method = RequestMethod.GET)
	public void viewEvents() throws Exception {
//		client = new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
//				.setApplicationName(APPLICATION_NAME).build();
//		Event updatedEvent =
//				client.events().move("primary", "9e2pbu6so70h46lnp1ie8q1ack", "bipro5500@gmail.com").execute();

		// Initialize Calendar service with valid OAuth credentials
		Calendar service = new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
				.setApplicationName("applicationName").build();

// Create and initialize a new event (could also retrieve an existing event)
//		Event event = new Event();
//		event.setICalUID("originalUID");
//
//		Event.Organizer organizer = new Event.Organizer();
//		organizer.setEmail("organizerEmail");
//		organizer.setDisplayName("organizerDisplayName");
//		event.setOrganizer(organizer);
//
//		ArrayList<EventAttendee> attendees = new ArrayList<EventAttendee>();
//		attendees.add(new EventAttendee().setEmail("attendeeEmail"));
//// ...
//		event.setAttendees(attendees);
//
//		Date startDate = new Date();
//		Date endDate = new Date(startDate.getTime() + 3600000);
//		DateTime start = new DateTime(startDate, TimeZone.getTimeZone("UTC"));
//		event.setStart(new EventDateTime().setDateTime(start));
//		DateTime end = new DateTime(endDate, TimeZone.getTimeZone("UTC"));
//		event.setEnd(new EventDateTime().setDateTime(end));
		Event event = client.events().get("bipro77@gmail.com", "6bopskbgsl18ao91vi5dklc4ro").execute();
// Import the event into a calendar
		Event importedEvent = service.events().calendarImport("primary", event).execute();

		System.out.println(importedEvent.getId());
	}
}