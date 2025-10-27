package com.zyacodes.edunotifyproj.Interfaces;

import com.zyacodes.edunotifyproj.Models.Event;

public interface EventActionListener {
    void onEditEvent(Event event);
    void onDeleteEvent(Event event);
    void onViewAttendance(Event event);
    void onCloseEvent(Event event);
}
