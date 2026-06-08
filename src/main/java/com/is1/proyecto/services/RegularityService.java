package com.is1.proyecto.services;

import com.is1.proyecto.models.*;
import java.util.*;

public class RegularityService {

    public List<Map<String, Object>> getProfessorSubjects(int professorId) {
        List<Map<String, Object>> result = new ArrayList<>();

        List<ScheduleProfessors> schedules = ScheduleProfessors.where("professor_id = ?", professorId);

        for (ScheduleProfessors sp : schedules) {
            Schedule schedule = Schedule.findById(sp.getInteger("schedule_id"));
            if (schedule == null)
                continue;

            Subject subject = Subject.findById(schedule.get("subject_id"));
            if (subject == null)
                continue;

            Map<String, Object> map = new HashMap<>();
            map.put("id", subject.getId());
            map.put("name", subject.getString("name"));
            result.add(map);
        }

        return result;
    }

    public List<Map<String, Object>> getStudentsBySubject(int subjectId) {
        List<Map<String, Object>> result = new ArrayList<>();

        List<PlanSubject> planSubjects = PlanSubject.where("subject_id = ?", subjectId);

        for (PlanSubject ps : planSubjects) {
            List<Enrollment> enrollments = Enrollment.where(
                    "plan_subject_id = ? AND status = 'CURSANDO'", ps.getId());

            for (Enrollment enrollment : enrollments) {
                Student student = Student.findById(enrollment.getStudentId());
                if (student == null)
                    continue;

                Map<String, Object> map = new HashMap<>();
                map.put("enrollmentId", enrollment.getId());
                map.put("studentName", student.getString("name"));
                map.put("studentSurname", student.getString("surname"));
                map.put("studentDni", student.getString("dni"));
                map.put("status", enrollment.getStatus());
                map.put("subjectId", subjectId);
                result.add(map);
            }
        }

        return result;
    }

    public Map<String, Object> markAsRegular(int enrollmentId, int professorId) {
        return updateStatus(enrollmentId, professorId, "REGULAR");
    }

    public Map<String, Object> markAsLibre(int enrollmentId, int professorId) {
        return updateStatus(enrollmentId, professorId, "LIBRE");
    }

    private Map<String, Object> updateStatus(int enrollmentId, int professorId, String newStatus) {
        Map<String, Object> result = new HashMap<>();

        Enrollment enrollment = Enrollment.findById(enrollmentId);
        if (enrollment == null) {
            result.put("ok", false);
            result.put("error", "Inscripción no encontrada.");
            return result;
        }

        if (!enrollment.isCursando()) {
            result.put("ok", false);
            result.put("error", "El alumno no está cursando esta materia.");
            return result;
        }

        enrollment.set("status", newStatus).saveIt();
        result.put("ok", true);
        return result;
    }
}