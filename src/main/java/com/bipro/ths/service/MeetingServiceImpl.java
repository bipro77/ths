package com.bipro.ths.service;


import com.bipro.ths.model.Meeting;
import com.bipro.ths.repository.MeetingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MeetingServiceImpl implements MeetingService {

    @Autowired
    private MeetingRepository meetingRepository;

    @Override
    public Meeting save(Meeting meeting) {
        return meetingRepository.save(meeting);
    }

    @Override
    public List<Meeting> findByPatientId(long patientId) {
        return meetingRepository.findByByPatientId(patientId);
    }

    @Override
    public List<Meeting> findByDoctorId(long patientId) {
        return meetingRepository.findByByDoctortId(patientId);
    }

    @Override
    public Meeting findMeetingByEventId(String eventId) {
        return meetingRepository.findMeetingByEventId(eventId);
    }

    @Override
    public void delete(Meeting meeting) {
         meetingRepository.delete(meeting);
    }


}