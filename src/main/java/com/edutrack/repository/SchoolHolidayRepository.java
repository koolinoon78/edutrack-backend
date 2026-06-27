package com.edutrack.repository;

import com.edutrack.model.SchoolHoliday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolHolidayRepository extends JpaRepository<SchoolHoliday, Long> {

    Optional<SchoolHoliday> findByHolidayDate(LocalDate date);

    boolean existsByHolidayDate(LocalDate date);

    /** All holidays in a given month — used by the calendar view on the frontend */
    @Query("SELECT h FROM SchoolHoliday h WHERE YEAR(h.holidayDate) = :year AND MONTH(h.holidayDate) = :month ORDER BY h.holidayDate")
    List<SchoolHoliday> findByYearAndMonth(@Param("year") int year, @Param("month") int month);
}
