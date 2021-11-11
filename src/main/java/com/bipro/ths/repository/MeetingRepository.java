package com.bipro.ths.repository;

import com.bipro.ths.model.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

//    Meeting findAllById(Integer meetingId);
    Meeting save(Meeting meeting);
        ////    @Query( "select u from User u inner join u.roles r where r.role in :roles" )
//    @Query(value = "SELECT * FROM meetings WHERE patient_id = 64", nativeQuery = true)

    @Query("SELECT m FROM Meeting m WHERE m.patientId = :patientId")
          List<Meeting> findByByPatientId(@Param("patientId") long patientId);

    @Query("SELECT m FROM Meeting m WHERE m.doctorId = :doctorId")
    List<Meeting> findByByDoctortId(@Param("doctorId") long doctorId);

    @Query("SELECT m FROM Meeting m WHERE m.eventId = :eventId")
      Meeting findMeetingByEventId (@Param("eventId") String eventId);

    void delete(Meeting meeting);

//    List<Meeting> findByAllEventIdOrderByDoctorId(String doctorId);
//    List<Meeting> findByAllEventIdOrderByPatientId(long patientId);
//    void deleteAllById(Integer meetingId);
//    User findByEmail(String email);
//    User findByUsername(String name);
//    User findAllById(Integer id);//
//    @Query("SELECT username FROM User")
//    List<String> findAllUsernames();
//
//    @Query("SELECT email FROM User")
//    List<String> findAllEmail();
//
////    @Query( "select u from User u inner join u.roles r where r.role in :roles" )
////    List<User> findBySpecificRoles(@Param("roles") List<Role> roles);
//
////    @Query("SELECT u FROM User u WHERE u.roles = 1")
//    List<User> findAllByRoles(Role role);


}