package com.edutrack.service;

import com.edutrack.model.SchoolHoliday;
import com.edutrack.repository.SchoolHolidayRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CalendarService {

    private final SchoolHolidayRepository holidayRepo;

    public CalendarService(SchoolHolidayRepository holidayRepo) {
        this.holidayRepo = holidayRepo;
    }

    /**
     * Returns true only if the date is:
     *   - Not a Saturday or Sunday
     *   - Not in the school_holidays table
     */
    public boolean isSchoolDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return false;
        }
        return !holidayRepo.existsByHolidayDate(date);
    }

    /**
     * Counts actual school days between fromDate and toDate inclusive,
     * excluding weekends and any special holidays in that range.
     * Used for accurate attendance % calculations.
     */
    public long countSchoolDays(LocalDate fromDate, LocalDate toDate) {
        // Fetch all holidays in the range (may span multiple months)
        Set<LocalDate> holidays = holidayRepo.findAll().stream()
                .map(SchoolHoliday::getHolidayDate)
                .filter(d -> !d.isBefore(fromDate) && !d.isAfter(toDate))
                .collect(Collectors.toSet());

        return fromDate.datesUntil(toDate.plusDays(1))
                .filter(d -> {
                    DayOfWeek dow = d.getDayOfWeek();
                    return dow != DayOfWeek.SATURDAY
                        && dow != DayOfWeek.SUNDAY
                        && !holidays.contains(d);
                })
                .count();
    }

    /**
     * Returns every day in the given month that is a school day.
     * Used by the frontend calendar to highlight active school days.
     */
    public List<LocalDate> getSchoolDaysInMonth(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        List<SchoolHoliday> holidays = holidayRepo.findByYearAndMonth(year, month);

        Set<LocalDate> holidayDates = holidays.stream()
                .map(SchoolHoliday::getHolidayDate)
                .collect(Collectors.toSet());

        return ym.atDay(1)
                .datesUntil(ym.atEndOfMonth().plusDays(1))
                .filter(d -> {
                    DayOfWeek dow = d.getDayOfWeek();
                    return dow != DayOfWeek.SATURDAY
                        && dow != DayOfWeek.SUNDAY
                        && !holidayDates.contains(d);
                })
                .collect(Collectors.toList());
    }

    /** All special holidays in a month with their reasons — for the admin calendar view */
    public List<SchoolHoliday> getHolidaysInMonth(int year, int month) {
        return holidayRepo.findByYearAndMonth(year, month);
    }
}
