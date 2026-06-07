package com.is1.proyecto.services;

import com.is1.proyecto.models.Schedule;
import com.is1.proyecto.models.ScheduleCareers;
import com.is1.proyecto.models.ScheduleProfessors;

import org.javalite.activejdbc.LazyList;

import org.javalite.activejdbc.Base;

public class ScheduleService {
    
    public void createSchedule(String id_materia, int anio, String[] carreras, String[] profesores){
        
        if(id_materia == null || profesores == null || carreras == null){
            throw new IllegalArgumentException("Faltan datos obligatorios.");
        }

        Base.openTransaction();

        Schedule cronograma = new Schedule();
        cronograma.set("current_year", anio);
        cronograma.set("subject_id", id_materia);
        cronograma.saveIt();

        Object cronogramaId = cronograma.getId();

        for(String profesorId : profesores){
            ScheduleProfessors prof = new ScheduleProfessors();
            prof.set("schedule_id", cronogramaId);
            prof.set("professor_id", profesorId);
            prof.saveIt();
        }

        for(String carreraId : carreras){
            ScheduleCareers carr = new ScheduleCareers();
            carr.set("schedule_id", cronograma.getId());
            carr.set("career_id", carreraId);
            carr.saveIt();
        }

        Base.commitTransaction();
    }

    public void deleteSchedule(String cronograma_id){
        if(cronograma_id == null){
            throw new IllegalArgumentException("Debes seleccionar un cronograma.");
        }
        Schedule cronoABorrar = Schedule.findById(cronograma_id);
        LazyList<ScheduleProfessors> profesores = ScheduleProfessors.find("schedule_id = ?", cronograma_id);
        LazyList<ScheduleCareers> carreras = ScheduleCareers.find("schedule_id = ?", cronograma_id);
        if(cronoABorrar != null){
            cronoABorrar.delete();
            for(ScheduleProfessors profe : profesores){
                profe.delete();
            }
            for(ScheduleCareers carrera : carreras){
                carrera.delete();
            }
        }
    }

}
