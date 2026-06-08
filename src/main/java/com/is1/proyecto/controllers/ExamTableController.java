package com.is1.proyecto.controllers;

import com.is1.proyecto.models.*;
import com.is1.proyecto.services.ExamTableService;
import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static spark.Spark.get;
import static spark.Spark.post;

public class ExamTableController {

    public static void init() {

        get("/professor/exam-tables", (req, res) -> {
            int professorId = (int) req.session().attribute("userId");
            List<ExamTable> mesas = ExamTable.where("professor_id = ?", professorId);

            Map<String, Object> model = new HashMap<>();
            model.put("examTables", mesas.stream().map(m -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", m.getId());
                map.put("examDate", m.getString("exam_date"));
                map.put("location", m.getString("location"));
                map.put("isOpen", m.isOpen());
                map.put("isClosed", m.isClosed());
                map.put("isCancelled", m.isCancelled());
                Subject subject = Subject.findById(m.getInteger("subject_id"));
                Career career = Career.findById(m.getInteger("career_id"));
                map.put("subjectName", subject != null ? subject.getString("name") : "");
                map.put("careerName", career != null ? career.getString("name") : "");
                return map;
            }).collect(Collectors.toList()));

            String success = req.queryParams("message");
            String error = req.queryParams("error");
            if (success != null && !success.isEmpty())
                model.put("successMessage", success);
            if (error != null && !error.isEmpty())
                model.put("errorMessage", error);

            return new ModelAndView(model, "exam-tables.mustache");
        }, new MustacheTemplateEngine());

        get("/professor/exam-tables/new", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("careers", Career.findAll().toMaps());

            String error = req.queryParams("error");
            if (error != null && !error.isEmpty())
                model.put("errorMessage", error);

            return new ModelAndView(model, "exam-tables-new.mustache");
        }, new MustacheTemplateEngine());

        get("/professor/exam-tables/subjects/:careerId", (req, res) -> {
            int careerId = Integer.parseInt(req.params("careerId"));
            List<Map<String, Object>> subjects = Subject.where("career_id = ?", careerId).toMaps();
            res.type("application/json");

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < subjects.size(); i++) {
                Map s = subjects.get(i);
                json.append("{\"id\":").append(s.get("id"))
                        .append(",\"name\":\"").append(s.get("name")).append("\"}");
                if (i < subjects.size() - 1)
                    json.append(",");
            }
            json.append("]");

            return json.toString();
        });

        get("/professor/exam-tables/:id/enrollments", (req, res) -> {
            int professorId = (int) req.session().attribute("userId");
            int examTableId = Integer.parseInt(req.params("id"));

            ExamTable mesa = ExamTable.findById(examTableId);
            if (mesa == null || mesa.getInteger("professor_id") != professorId) {
                res.redirect("/professor/exam-tables?error=Mesa no encontrada.");
                return null;
            }

            Subject subject = Subject.findById(mesa.getInteger("subject_id"));
            Career career = Career.findById(mesa.getInteger("career_id"));
            List<ExamEnrollment> enrollments = ExamEnrollment.where("exam_table_id = ?", examTableId);

            Map<String, Object> model = new HashMap<>();
            model.put("examTableId", examTableId);
            model.put("subjectName", subject != null ? subject.getString("name") : "");
            model.put("careerName", career != null ? career.getString("name") : "");
            model.put("examDate", mesa.getString("exam_date"));
            model.put("location", mesa.getString("location"));
            model.put("enrollments", enrollments.stream().map(e -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", e.getId());
                map.put("examTableId", examTableId);
                map.put("calification", e.get("calification"));
                map.put("isEnrolled", "Enrolled".equals(e.getString("condition")));
                map.put("isApproved", "Approved".equals(e.getString("condition")));
                map.put("isFailed", "Failed".equals(e.getString("condition")));
                map.put("isAbsent", "Absent".equals(e.getString("condition")));
                Student student = Student.findById(e.getInteger("student_id"));
                if (student != null) {
                    map.put("studentName", student.getString("name"));
                    map.put("studentSurname", student.getString("surname"));
                    map.put("studentDni", student.getString("dni"));
                }
                return map;
            }).collect(Collectors.toList()));

            String success = req.queryParams("message");
            String error = req.queryParams("error");
            if (success != null && !success.isEmpty())
                model.put("successMessage", success);
            if (error != null && !error.isEmpty())
                model.put("errorMessage", error);

            return new ModelAndView(model, "exam-table-enrollments.mustache");
        }, new MustacheTemplateEngine());

        post("/professor/exam-tables/create", (req, res) -> {
            int professorId = (int) req.session().attribute("userId");
            int subjectId = Integer.parseInt(req.queryParams("subject_id"));
            int careerId = Integer.parseInt(req.queryParams("career_id"));
            String date = req.queryParams("exam_date");
            String location = req.queryParams("location");

            ExamTableService service = new ExamTableService();
            var result = service.createExamTable(professorId, subjectId, careerId, date, location);

            if ((boolean) result.get("ok")) {
                res.redirect("/professor/exam-tables?message=Mesa creada correctamente.");
            } else {
                res.redirect("/professor/exam-tables/new?error=" + result.get("error"));
            }
            return null;
        });

        post("/professor/exam-tables/:id/grade/:enrollmentId", (req, res) -> {
            int professorId = (int) req.session().attribute("userId");
            int examTableId = Integer.parseInt(req.params("id"));
            int enrollmentId = Integer.parseInt(req.params("enrollmentId"));
            int calification = Integer.parseInt(req.queryParams("calification"));

            ExamTableService service = new ExamTableService();
            var result = service.gradeStudent(enrollmentId, professorId, calification);

            if ((boolean) result.get("ok")) {
                res.redirect(
                        "/professor/exam-tables/" + examTableId + "/enrollments?message=Nota cargada correctamente.");
            } else {
                res.redirect("/professor/exam-tables/" + examTableId + "/enrollments?error=" + result.get("error"));
            }
            return null;
        });

        post("/professor/exam-tables/:id/close", (req, res) -> {
            int professorId = (int) req.session().attribute("userId");
            int examTableId = Integer.parseInt(req.params("id"));

            ExamTableService service = new ExamTableService();
            var result = service.closeExamTable(examTableId, professorId);

            if ((boolean) result.get("ok")) {
                res.redirect("/professor/exam-tables?message=Mesa cerrada correctamente.");
            } else {
                res.redirect("/professor/exam-tables?error=" + result.get("error"));
            }
            return null;
        });

        post("/professor/exam-tables/:id/cancel", (req, res) -> {
            int professorId = (int) req.session().attribute("userId");
            int examTableId = Integer.parseInt(req.params("id"));

            ExamTableService service = new ExamTableService();
            var result = service.cancelExamTable(examTableId, professorId);

            if ((boolean) result.get("ok")) {
                res.redirect("/professor/exam-tables?message=Mesa cancelada.");
            } else {
                res.redirect("/professor/exam-tables?error=" + result.get("error"));
            }
            return null;
        });
    }
}