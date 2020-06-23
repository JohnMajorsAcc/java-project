package edu.javacourse.studentorder.domain;

public enum StudentOrderStatus {
    START,
    CHECKED;

    public static StudentOrderStatus fromValue(int value){
        for(StudentOrderStatus studentOrderStatus:StudentOrderStatus.values()){
            if(studentOrderStatus.ordinal() == value){
                return studentOrderStatus;
            }
        }
        throw new RuntimeException(("Unknown status value " + value));
    }
}
