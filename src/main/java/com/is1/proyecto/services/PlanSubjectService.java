package com.is1.proyecto.services;

import com.is1.proyecto.exceptions.ValidationException;
import com.is1.proyecto.models.PlanSubject;
import com.is1.proyecto.models.Prerequisite;

public class PlanSubjectService {

    public void createPlanSubject(Integer programId, Integer subjectId, Integer year, Integer hours, boolean isElective,
            String[] curseReqs) {

        if (programId == null || subjectId == null || year == null || hours == null) {
            throw new ValidationException("Faltan campos obligatorios");
        }

        PlanSubject ps = new PlanSubject();
        ps.set("programOfStudy_id", programId);
        ps.set("subject_id", subjectId);
        ps.set("year", year);
        ps.set("hours", hours);
        ps.set("is_elective", isElective ? 1 : 0);
        ps.saveIt();

        if (curseReqs != null) {
            for (String subId : curseReqs) {
                Prerequisite p = new Prerequisite();
                p.set("plan_subject_id", ps.getId());
                p.set("required_subject_id", Integer.parseInt(subId));
                p.set("req_type", "COURSE");
                p.saveIt();
            }
        }
        // 3. Si curseReqs no es nulo, iterar y guardar cada Prerequisite
    }

}
