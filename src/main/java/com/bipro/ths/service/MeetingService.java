package com.bipro.ths.service;


import com.bipro.ths.model.Meeting;

import java.util.List;

public interface MeetingService {

//    Meeting findAllById(Integer meetingId);
    Meeting save(Meeting meeting);
    List<Meeting> findByPatientId(long patientId);
    List<Meeting> findByDoctorId(long patientId);
    Meeting findMeetingByEventId (String eventId);
    void delete(Meeting meeting);

//    Meeting findByAllByOrderByEventId (String patientId);
//    List<Meeting> findByAllEventIdOrderByDoctorId(String doctorId);
//    List<Meeting> findByAllEventIdOrderByPatientId(long patientId);
//    void deleteAllById(Integer meetingId);

}